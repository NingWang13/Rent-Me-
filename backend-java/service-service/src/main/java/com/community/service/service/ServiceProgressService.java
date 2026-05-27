package com.community.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.exception.BusinessException;
import com.community.service.entity.ServiceProgress;
import com.community.service.mapper.ServiceProgressMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ServiceProgressService extends ServiceImpl<ServiceProgressMapper, ServiceProgress> {

    public static final int STATUS_PENDING = 1;
    public static final int STATUS_IN_PROGRESS = 2;
    public static final int STATUS_COMPLETED = 3;

    @Transactional(rollbackFor = Exception.class)
    public ServiceProgress createProgress(Long orderId, Long helperId, String stepName, String description) {
        ServiceProgress progress = ServiceProgress.builder()
                .orderId(orderId)
                .helperId(helperId)
                .step(1)
                .stepName(stepName)
                .description(description)
                .status(STATUS_IN_PROGRESS)
                .startTime(LocalDateTime.now())
                .build();

        save(progress);
        return progress;
    }

    public List<ServiceProgress> getProgressByOrderId(Long orderId, Long currentUserId) {
        List<ServiceProgress> progressList = list(new LambdaQueryWrapper<ServiceProgress>()
                .eq(ServiceProgress::getOrderId, orderId)
                .orderByAsc(ServiceProgress::getStep)
        );

        if (progressList.isEmpty()) {
            return progressList;
        }

        boolean isHelper = progressList.stream()
                .anyMatch(p -> p.getHelperId() != null && p.getHelperId().equals(currentUserId));

        if (!isHelper) {
            throw new BusinessException(403, "无权访问该订单进度");
        }

        return progressList;
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceProgress updateProgress(Long progressId, Long currentUserId, String description, String location, String photoUrl) {
        ServiceProgress progress = getById(progressId);
        if (progress == null) {
            throw new BusinessException(404, "进度记录不存在");
        }

        if (!progress.getHelperId().equals(currentUserId)) {
            throw new BusinessException(403, "无权更新该进度");
        }

        if (description != null) {
            progress.setDescription(description);
        }
        if (location != null) {
            progress.setLocation(location);
        }
        if (photoUrl != null) {
            progress.setPhotoUrl(photoUrl);
        }

        updateById(progress);
        return progress;
    }

    @Transactional(rollbackFor = Exception.class)
    public ServiceProgress completeProgress(Long progressId, Long currentUserId) {
        ServiceProgress progress = getById(progressId);
        if (progress == null) {
            throw new BusinessException(404, "进度记录不存在");
        }

        if (!progress.getHelperId().equals(currentUserId)) {
            throw new BusinessException(403, "无权完成该进度");
        }

        progress.setStatus(STATUS_COMPLETED);
        progress.setCompleteTime(LocalDateTime.now());

        updateById(progress);
        return progress;
    }

    public List<ServiceProgress> getInProgressServices(Long helperId) {
        return list(new LambdaQueryWrapper<ServiceProgress>()
                .eq(ServiceProgress::getHelperId, helperId)
                .eq(ServiceProgress::getStatus, STATUS_IN_PROGRESS)
                .orderByDesc(ServiceProgress::getStartTime)
        );
    }
}
