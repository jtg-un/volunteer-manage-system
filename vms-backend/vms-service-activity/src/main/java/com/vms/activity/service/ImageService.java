package com.vms.activity.service;

import com.vms.common.dto.ImageSortDTO;
import com.vms.common.vo.ImageVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 图片服务接口
 */
public interface ImageService {

    /**
     * 上传图片
     * @param bizType 业务类型 (activity_image/org_gallery)
     * @param bizId 业务ID
     * @param file 图片文件
     * @param isCover 是否封面
     * @return 图片VO
     */
    ImageVO uploadImage(String bizType, Long bizId, MultipartFile file, Integer isCover);

    /**
     * 获取图片列表
     * @param bizType 业务类型
     * @param bizId 业务ID
     * @return 图片列表
     */
    List<ImageVO> listImages(String bizType, Long bizId);

    /**
     * 删除图片
     * @param fileId 文件ID
     */
    void deleteImage(Long fileId);

    /**
     * 设置封面
     * @param fileId 文件ID
     */
    void setCover(Long fileId);

    /**
     * 排序图片
     * @param dto 排序DTO
     */
    void sortImages(ImageSortDTO dto);

    /**
     * 获取图片数量
     * @param bizType 业务类型
     * @param bizId 业务ID
     * @return 图片数量
     */
    int countImages(String bizType, Long bizId);

    /**
     * 获取图片信息
     * @param fileId 文件ID
     * @return 文件实体
     */
    com.vms.repository.entity.SysFile getFileInfo(Long fileId);
}