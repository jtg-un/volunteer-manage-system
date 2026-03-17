package com.vms.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vms.common.dto.EvaluationDTO;
import com.vms.common.exception.BusinessException;
import com.vms.common.vo.EvaluationVO;
import com.vms.activity.service.EvaluationService;
import com.vms.repository.entity.*;
import com.vms.repository.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评价服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationServiceImpl implements EvaluationService {

    private final EvaluationMapper evaluationMapper;
    private final RegistrationMapper registrationMapper;
    private final ActivityMapper activityMapper;
    private final SysUserMapper userMapper;
    private final OrganizationMapper organizationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long evaluate(Long orgId, EvaluationDTO dto) {
        // 验证报名记录
        Registration registration = registrationMapper.selectById(dto.getRegId());
        if (registration == null) {
            throw new BusinessException(404, "报名记录不存在");
        }

        // 验证活动归属
        Activity activity = activityMapper.selectById(registration.getActivityId());
        if (activity == null || !activity.getOrgId().equals(orgId)) {
            throw new BusinessException(403, "只能评价自己活动的志愿者");
        }

        // 验证报名状态
        if (registration.getRegStatus() != 1) {
            throw new BusinessException(400, "只能评价已通过的报名");
        }

        // 检查是否已评价
        LambdaQueryWrapper<Evaluation> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(Evaluation::getRegId, dto.getRegId());
        if (evaluationMapper.selectCount(existWrapper) > 0) {
            throw new BusinessException(400, "该报名已评价，请勿重复评价");
        }

        // 创建评价记录
        Evaluation evaluation = new Evaluation();
        evaluation.setRegId(dto.getRegId());
        evaluation.setScoreTraining(dto.getScoreTraining());
        evaluation.setScoreCooperation(dto.getScoreCooperation());
        evaluation.setScoreExecution(dto.getScoreExecution());
        evaluation.setComment(dto.getComment());
        evaluationMapper.insert(evaluation);

        log.info("评价完成: regId={}, avgScore={}", dto.getRegId(),
                calculateAvgScore(dto.getScoreTraining(), dto.getScoreCooperation(), dto.getScoreExecution()));

        return evaluation.getEvalId();
    }

    @Override
    public EvaluationVO getByRegId(Long regId) {
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getRegId, regId);
        Evaluation evaluation = evaluationMapper.selectOne(wrapper);

        if (evaluation == null) {
            return null;
        }

        return convertToVO(evaluation);
    }

    @Override
    public Page<EvaluationVO> listMyEvaluations(Long userId, int page, int size) {
        // 查询用户的所有报名ID
        LambdaQueryWrapper<Registration> regWrapper = new LambdaQueryWrapper<>();
        regWrapper.eq(Registration::getUserId, userId)
                .eq(Registration::getRegStatus, 1); // 只查询已通过的报名
        List<Registration> registrations = registrationMapper.selectList(regWrapper);

        if (registrations.isEmpty()) {
            return new Page<>(page, size, 0);
        }

        List<Long> regIds = registrations.stream()
                .map(Registration::getRegId)
                .collect(Collectors.toList());

        // 查询评价记录
        Page<Evaluation> evalPage = new Page<>(page, size);
        LambdaQueryWrapper<Evaluation> evalWrapper = new LambdaQueryWrapper<>();
        evalWrapper.in(Evaluation::getRegId, regIds)
                .orderByDesc(Evaluation::getCreateTime);
        Page<Evaluation> result = evaluationMapper.selectPage(evalPage, evalWrapper);

        return convertToVOPage(result, registrations);
    }

    @Override
    public boolean isEvaluated(Long regId) {
        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getRegId, regId);
        return evaluationMapper.selectCount(wrapper) > 0;
    }

    /**
     * 计算综合评分
     */
    private BigDecimal calculateAvgScore(BigDecimal training, BigDecimal cooperation, BigDecimal execution) {
        BigDecimal sum = training.add(cooperation).add(execution);
        return sum.divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP);
    }

    /**
     * 转换为VO
     */
    private EvaluationVO convertToVO(Evaluation evaluation) {
        EvaluationVO vo = new EvaluationVO();
        vo.setEvalId(evaluation.getEvalId());
        vo.setRegId(evaluation.getRegId());
        vo.setScoreTraining(evaluation.getScoreTraining());
        vo.setScoreCooperation(evaluation.getScoreCooperation());
        vo.setScoreExecution(evaluation.getScoreExecution());
        vo.setAvgScore(calculateAvgScore(evaluation.getScoreTraining(),
                evaluation.getScoreCooperation(), evaluation.getScoreExecution()));
        vo.setComment(evaluation.getComment());
        vo.setCreateTime(evaluation.getCreateTime());

        // 查询报名信息
        Registration registration = registrationMapper.selectById(evaluation.getRegId());
        if (registration != null) {
            // 查询活动信息
            Activity activity = activityMapper.selectById(registration.getActivityId());
            if (activity != null) {
                vo.setActivityTitle(activity.getTitle());

                // 查询组织信息
                Organization org = organizationMapper.selectById(activity.getOrgId());
                if (org != null) {
                    vo.setOrgName(org.getOrgName());
                }
            }

            // 查询志愿者信息
            SysUser user = userMapper.selectById(registration.getUserId());
            if (user != null) {
                vo.setVolunteerName(user.getRealName());
                vo.setVolunteerPhone(user.getPhone());
            }
        }

        return vo;
    }

    /**
     * 转换为VO分页结果
     */
    private Page<EvaluationVO> convertToVOPage(Page<Evaluation> result, List<Registration> registrations) {
        Page<EvaluationVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());

        if (result.getRecords().isEmpty()) {
            return voPage;
        }

        // 构建报名ID到报名记录的映射
        Map<Long, Registration> regMap = registrations.stream()
                .collect(Collectors.toMap(Registration::getRegId, r -> r));

        // 批量查询活动信息
        List<Long> activityIds = registrations.stream()
                .map(Registration::getActivityId)
                .distinct()
                .collect(Collectors.toList());

        LambdaQueryWrapper<Activity> actWrapper = new LambdaQueryWrapper<>();
        actWrapper.in(Activity::getActivityId, activityIds);
        List<Activity> activities = activityMapper.selectList(actWrapper);
        Map<Long, Activity> activityMap = activities.stream()
                .collect(Collectors.toMap(Activity::getActivityId, a -> a));

        // 批量查询组织信息
        List<Long> orgIds = activities.stream()
                .map(Activity::getOrgId)
                .distinct()
                .collect(Collectors.toList());

        LambdaQueryWrapper<Organization> orgWrapper = new LambdaQueryWrapper<>();
        orgWrapper.in(Organization::getOrgId, orgIds);
        List<Organization> organizations = organizationMapper.selectList(orgWrapper);
        Map<Long, Organization> orgMap = organizations.stream()
                .collect(Collectors.toMap(Organization::getOrgId, o -> o));

        // 转换
        List<EvaluationVO> voList = result.getRecords().stream()
                .map(eval -> {
                    EvaluationVO vo = new EvaluationVO();
                    vo.setEvalId(eval.getEvalId());
                    vo.setRegId(eval.getRegId());
                    vo.setScoreTraining(eval.getScoreTraining());
                    vo.setScoreCooperation(eval.getScoreCooperation());
                    vo.setScoreExecution(eval.getScoreExecution());
                    vo.setAvgScore(calculateAvgScore(eval.getScoreTraining(),
                            eval.getScoreCooperation(), eval.getScoreExecution()));
                    vo.setComment(eval.getComment());
                    vo.setCreateTime(eval.getCreateTime());

                    Registration reg = regMap.get(eval.getRegId());
                    if (reg != null) {
                        Activity activity = activityMap.get(reg.getActivityId());
                        if (activity != null) {
                            vo.setActivityTitle(activity.getTitle());

                            Organization org = orgMap.get(activity.getOrgId());
                            if (org != null) {
                                vo.setOrgName(org.getOrgName());
                            }
                        }
                    }

                    return vo;
                })
                .collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }
}