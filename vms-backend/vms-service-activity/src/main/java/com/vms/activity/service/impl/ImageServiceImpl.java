package com.vms.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.vms.activity.service.ImageService;
import com.vms.common.dto.ImageSortDTO;
import com.vms.common.exception.BusinessException;
import com.vms.common.vo.ImageVO;
import com.vms.repository.entity.SysFile;
import com.vms.repository.mapper.SysFileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 图片服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final SysFileMapper sysFileMapper;

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.access.url-prefix}")
    private String urlPrefix;

    // 允许的图片类型
    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    // 最大文件大小：5MB
    private static final long MAX_SIZE = 5 * 1024 * 1024;

    // 组织风采最大数量
    private static final int MAX_ORG_GALLERY = 8;

    @Override
    @Transactional
    public ImageVO uploadImage(String bizType, Long bizId, MultipartFile file, Integer isCover) {
        // 验证文件
        validateFile(file);

        // 组织风采数量限制检查
        if ("org_gallery".equals(bizType)) {
            int count = countImages(bizType, bizId);
            if (count >= MAX_ORG_GALLERY) {
                throw new BusinessException(400, "组织风采最多只能上传" + MAX_ORG_GALLERY + "张图片");
            }
        }

        // 如果设置封面，先取消原有封面
        if (isCover != null && isCover == 1) {
            clearCover(bizType, bizId);
        }

        // 生成文件名和路径
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String bizPath = bizType.replace("_image", "").replace("_gallery", "");
        String newFilename = bizId + "_" + System.currentTimeMillis() + extension;

        // 创建目录
        String dirPath = uploadPath + bizPath + "/" + datePath + "/";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 保存文件
        String filePath = dirPath + newFilename;
        try {
            file.transferTo(new File(filePath));
            log.info("图片上传成功: {}", filePath);
        } catch (IOException e) {
            log.error("图片保存失败", e);
            throw new BusinessException(500, "文件保存失败");
        }

        // 构建URL
        String fileUrl = urlPrefix + bizPath + "/" + datePath + "/" + newFilename;

        // 计算排序序号（取当前最大+1）
        int sortOrder = getMaxSortOrder(bizType, bizId) + 1;

        // 保存数据库记录
        SysFile sysFile = new SysFile();
        sysFile.setBizType(bizType);
        sysFile.setBizId(bizId);
        sysFile.setFileUrl(fileUrl);
        sysFile.setOriginalName(originalFilename);
        sysFile.setFileType(extension.replace(".", ""));
        sysFile.setFileSize(file.getSize());
        sysFile.setSortOrder(sortOrder);
        sysFile.setIsCover(isCover != null ? isCover : 0);
        sysFileMapper.insert(sysFile);

        return toVO(sysFile);
    }

    @Override
    public List<ImageVO> listImages(String bizType, Long bizId) {
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysFile::getBizType, bizType)
               .eq(SysFile::getBizId, bizId)
               .orderByAsc(SysFile::getSortOrder);
        List<SysFile> files = sysFileMapper.selectList(wrapper);
        return files.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteImage(Long fileId) {
        SysFile file = sysFileMapper.selectById(fileId);
        if (file == null) {
            throw new BusinessException(404, "图片不存在");
        }

        // 删除物理文件
        String filePath = uploadPath + file.getFileUrl().replace(urlPrefix, "");
        File physicalFile = new File(filePath);
        if (physicalFile.exists()) {
            physicalFile.delete();
            log.info("物理文件已删除: {}", filePath);
        }

        // 删除数据库记录
        sysFileMapper.deleteById(fileId);
    }

    @Override
    @Transactional
    public void setCover(Long fileId) {
        SysFile file = sysFileMapper.selectById(fileId);
        if (file == null) {
            throw new BusinessException(404, "图片不存在");
        }

        // 先取消原有封面
        clearCover(file.getBizType(), file.getBizId());

        // 设置新封面
        LambdaUpdateWrapper<SysFile> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysFile::getFileId, fileId)
               .set(SysFile::getIsCover, 1);
        sysFileMapper.update(null, wrapper);
    }

    @Override
    @Transactional
    public void sortImages(ImageSortDTO dto) {
        for (ImageSortDTO.SortItem item : dto.getItems()) {
            LambdaUpdateWrapper<SysFile> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(SysFile::getFileId, item.getFileId())
                   .set(SysFile::getSortOrder, item.getSortOrder());
            if (item.getIsCover() != null) {
                wrapper.set(SysFile::getIsCover, item.getIsCover());
            }
            sysFileMapper.update(null, wrapper);
        }
    }

    @Override
    public int countImages(String bizType, Long bizId) {
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysFile::getBizType, bizType)
               .eq(SysFile::getBizId, bizId);
        return Math.toIntExact(sysFileMapper.selectCount(wrapper));
    }

    @Override
    public SysFile getFileInfo(Long fileId) {
        return sysFileMapper.selectById(fileId);
    }

    // ========== 私有方法 ==========

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请选择要上传的文件");
        }

        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(400, "文件大小不能超过5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException(400, "只支持 JPG、PNG、GIF、WEBP 格式的图片");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

    private void clearCover(String bizType, Long bizId) {
        LambdaUpdateWrapper<SysFile> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SysFile::getBizType, bizType)
               .eq(SysFile::getBizId, bizId)
               .eq(SysFile::getIsCover, 1)
               .set(SysFile::getIsCover, 0);
        sysFileMapper.update(null, wrapper);
    }

    private int getMaxSortOrder(String bizType, Long bizId) {
        LambdaQueryWrapper<SysFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysFile::getBizType, bizType)
               .eq(SysFile::getBizId, bizId)
               .orderByDesc(SysFile::getSortOrder)
               .last("LIMIT 1");
        SysFile lastFile = sysFileMapper.selectOne(wrapper);
        return lastFile != null ? lastFile.getSortOrder() : 0;
    }

    private ImageVO toVO(SysFile file) {
        ImageVO vo = new ImageVO();
        vo.setFileId(file.getFileId());
        vo.setFileUrl(file.getFileUrl());
        vo.setOriginalName(file.getOriginalName());
        vo.setSortOrder(file.getSortOrder());
        vo.setIsCover(file.getIsCover());
        vo.setCreateTime(file.getCreateTime());
        return vo;
    }
}