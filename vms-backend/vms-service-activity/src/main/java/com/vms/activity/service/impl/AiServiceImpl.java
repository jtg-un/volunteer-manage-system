package com.vms.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vms.activity.service.AiService;
import com.vms.activity.service.support.ActivitySupport;
import com.vms.common.dto.AiRequestDTO;
import com.vms.common.vo.AiRecommendVO;
import com.vms.common.vo.VolunteerStatsVO;
import com.vms.repository.entity.Activity;
import com.vms.repository.entity.Registration;
import com.vms.repository.entity.SysDict;
import com.vms.repository.mapper.ActivityMapper;
import com.vms.repository.mapper.RegistrationMapper;
import com.vms.repository.mapper.SysDictMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI推荐服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final ActivityMapper activityMapper;
    private final RegistrationMapper registrationMapper;
    private final SysDictMapper dictMapper;
    private final ActivitySupport activitySupport;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${deepseek.api-key}")
    private String apiKey;

    @Value("${deepseek.base-url}")
    private String baseUrl;

    @Value("${deepseek.model}")
    private String model;

    @Override
    public AiRecommendVO recommend(Long userId, AiRequestDTO dto) {
        // 1. 获取志愿者历史数据
        VolunteerStatsVO stats = getVolunteerStats(userId);
        Map<String, Integer> categoryStats = getCategoryStats(userId);

        // 2. 获取当前可报名活动（运行中状态）
        List<Activity> availableActivities = getRunningActivities();

        // 3. 构建Prompt
        String prompt = buildPrompt(stats, categoryStats, availableActivities, dto.getQuestion());

        // 4. 调用DeepSeek API
        String aiResponse = callDeepSeek(prompt);

        // 5. 根据AI回复筛选推荐活动（简单匹配策略）
        List<AiRecommendVO.ActivitySimpleVO> recommendations = matchActivities(aiResponse, availableActivities);

        // 6. 如果没有匹配到，默认推荐前3个热门活动
        if (recommendations.isEmpty() && !availableActivities.isEmpty()) {
            recommendations = availableActivities.stream()
                    .sorted((a, b) -> {
                        int countA = activitySupport.calculateTotalCurrentCount(a.getActivityId());
                        int countB = activitySupport.calculateTotalCurrentCount(b.getActivityId());
                        return countB - countA;
                    })
                    .limit(3)
                    .map(this::convertToSimpleVO)
                    .collect(Collectors.toList());
        }

        // 7. 返回结果
        AiRecommendVO result = new AiRecommendVO();
        result.setAnswer(aiResponse);
        result.setActivities(recommendations);
        return result;
    }

    /**
     * 获取志愿者统计数据
     */
    private VolunteerStatsVO getVolunteerStats(Long userId) {
        VolunteerStatsVO stats = new VolunteerStatsVO();
        stats.setTotalHours(java.math.BigDecimal.ZERO);
        stats.setTotalPoints(0);
        stats.setActivityCount(0);

        // 统计参与活动次数
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Registration::getUserId, userId)
               .in(Registration::getRegStatus, 1, 2); // 已通过或已拒绝
        Long count = registrationMapper.selectCount(wrapper);
        stats.setActivityCount(count.intValue());

        return stats;
    }

    /**
     * 获取类别偏好统计
     */
    private Map<String, Integer> getCategoryStats(Long userId) {
        Map<String, Integer> categoryStats = new HashMap<>();

        // 获取用户参与的活动中类
        LambdaQueryWrapper<Registration> regWrapper = new LambdaQueryWrapper<>();
        regWrapper.eq(Registration::getUserId, userId)
                   .eq(Registration::getRegStatus, 1); // 已通过

        List<Registration> registrations = registrationMapper.selectList(regWrapper);
        for (Registration reg : registrations) {
            Activity activity = activityMapper.selectById(reg.getActivityId());
            if (activity != null && activity.getCategoryId() != null) {
                String categoryName = activitySupport.getCategoryName(activity.getCategoryId());
                categoryStats.merge(categoryName, 1, Integer::sum);
            }
        }

        return categoryStats;
    }

    /**
     * 获取运行中的活动列表
     */
    private List<Activity> getRunningActivities() {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Activity::getStatus, 1) // 运行中
               .orderByDesc(Activity::getCreateTime)
               .last("LIMIT 20");
        return activityMapper.selectList(wrapper);
    }

    /**
     * 构建AI Prompt
     */
    private String buildPrompt(VolunteerStatsVO stats, Map<String, Integer> categoryStats,
                               List<Activity> activities, String userQuestion) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("以下是当前可报名的活动列表，请为志愿者推荐合适的活动。\n\n");

        // 志愿者信息
        if (stats.getActivityCount() > 0) {
            prompt.append("志愿者已参与").append(stats.getActivityCount()).append("次活动");
            if (!categoryStats.isEmpty()) {
                prompt.append("，偏好: ");
                categoryStats.forEach((cat, count) -> prompt.append(cat).append("、"));
            }
            prompt.append("\n\n");
        }

        // 可推荐活动列表（只列出标题和关键信息）
        prompt.append("可报名活动:\n");
        for (Activity act : activities) {
            String categoryName = activitySupport.getCategoryName(act.getCategoryId());
            String regionName = activitySupport.getRegionName(act.getRegionCode());
            prompt.append("- ").append(act.getTitle())
                  .append("（").append(categoryName).append("、").append(regionName != null ? regionName : "线上").append("）\n");
        }

        // 用户问题
        prompt.append("\n用户问题: ");
        if (userQuestion != null && !userQuestion.isBlank()) {
            prompt.append(userQuestion);
        } else {
            prompt.append("推荐活动");
        }

        return prompt.toString();
    }

    /**
     * 调用DeepSeek API
     */
    private String callDeepSeek(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", "你是志愿服务活动推荐助手。如果用户问非活动相关问题，回复：'我只能为您推荐志愿活动，请问您想参加哪种类型的活动？'。推荐时直接说出活动标题，简洁不超过80字。"),
                    Map.of("role", "user", "content", prompt)
            ));
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 150);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/chat/completions",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                return choices.get(0).path("message").path("content").asText();
            }

            return "抱歉，暂时无法获取推荐，请稍后再试。";

        } catch (Exception e) {
            log.error("调用DeepSeek API失败: {}", e.getMessage());
            return "抱歉，AI服务暂时不可用，您可以浏览活动列表自行选择感兴趣的活动。";
        }
    }

    /**
     * 根据AI回复匹配活动（通过活动标题匹配）
     */
    private List<AiRecommendVO.ActivitySimpleVO> matchActivities(String aiResponse, List<Activity> activities) {
        List<AiRecommendVO.ActivitySimpleVO> result = new ArrayList<>();

        // 通过活动标题匹配
        for (Activity activity : activities) {
            if (aiResponse.contains(activity.getTitle())) {
                result.add(convertToSimpleVO(activity));
            }
        }

        // 最多返回3个推荐
        if (result.size() > 3) {
            result = result.subList(0, 3);
        }

        return result;
    }

    /**
     * 转换为简要VO
     */
    private AiRecommendVO.ActivitySimpleVO convertToSimpleVO(Activity activity) {
        AiRecommendVO.ActivitySimpleVO vo = new AiRecommendVO.ActivitySimpleVO();
        vo.setActivityId(activity.getActivityId());
        vo.setTitle(activity.getTitle());
        vo.setCategoryName(activitySupport.getCategoryName(activity.getCategoryId()));
        vo.setRegionName(activitySupport.getRegionName(activity.getRegionCode()));
        vo.setOrgName(activitySupport.getOrgName(activity.getOrgId()));
        vo.setCoverImageUrl(activitySupport.getCoverImageUrl(activity.getActivityId()));
        vo.setCurrentCount(activitySupport.calculateTotalCurrentCount(activity.getActivityId()));
        vo.setPlanCount(activitySupport.calculateTotalPlanCount(activity.getActivityId()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
        if (activity.getStartTime() != null) {
            vo.setStartTime(activity.getStartTime().format(formatter));
        }
        if (activity.getEndTime() != null) {
            vo.setEndTime(activity.getEndTime().format(formatter));
        }

        return vo;
    }
}