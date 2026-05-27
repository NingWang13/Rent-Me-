package com.community.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.community.common.exception.BusinessException;
import com.community.user.entity.Feedback;
import com.community.user.mapper.FeedbackMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class FeedbackService extends ServiceImpl<FeedbackMapper, Feedback> {

    public static final int TYPE_SUGGESTION = 1;
    public static final int TYPE_COMPLAINT = 2;
    public static final int TYPE_BUG = 3;
    public static final int TYPE_OTHER = 4;

    public static final int STATUS_PENDING = 1;
    public static final int STATUS_PROCESSING = 2;
    public static final int STATUS_RESOLVED = 3;
    public static final int STATUS_CLOSED = 4;

    @Transactional(rollbackFor = Exception.class)
    public Feedback submitFeedback(Long userId, Integer type, String content,
                                   String images, Integer rating, String contact) {
        if (content == null || content.trim().length() < 10) {
            throw new BusinessException(400, "反馈内容至少10个字");
        }

        if (type == null || (type != TYPE_SUGGESTION && type != TYPE_COMPLAINT && type != TYPE_BUG && type != TYPE_OTHER)) {
            throw new BusinessException(400, "反馈类型无效");
        }

        if (rating != null && (rating < 1 || rating > 5)) {
            throw new BusinessException(400, "评分必须在1-5之间");
        }

        Feedback feedback = Feedback.builder()
                .userId(userId)
                .type(type)
                .content(content)
                .images(images)
                .status(STATUS_PENDING)
                .rating(rating)
                .contact(contact)
                .build();

        save(feedback);

        return feedback;
    }

    public Page<Feedback> getUserFeedbacks(Long userId, int page, int size) {
        return page(new Page<>(page, size),
                new LambdaQueryWrapper<Feedback>()
                        .eq(Feedback::getUserId, userId)
                        .orderByDesc(Feedback::getCreateTime)
        );
    }

    public List<Feedback> getPendingFeedbacks() {
        return list(new LambdaQueryWrapper<Feedback>()
                .eq(Feedback::getStatus, STATUS_PENDING)
                .orderByAsc(Feedback::getCreateTime)
        );
    }

    public Page<Feedback> getFeedbacksByStatus(Integer status, int page, int size) {
        return page(new Page<>(page, size),
                new LambdaQueryWrapper<Feedback>()
                        .eq(status != null, Feedback::getStatus, status)
                        .orderByDesc(Feedback::getCreateTime)
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public void replyFeedback(Long feedbackId, String replyContent) {
        Feedback feedback = getById(feedbackId);
        if (feedback == null) {
            throw new BusinessException(404, "反馈不存在");
        }

        feedback.setReplyContent(replyContent);
        feedback.setReplyTime(LocalDateTime.now());
        feedback.setStatus(STATUS_RESOLVED);

        updateById(feedback);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateFeedbackStatus(Long feedbackId, Integer status) {
        Feedback feedback = getById(feedbackId);
        if (feedback == null) {
            throw new BusinessException(404, "反馈不存在");
        }

        feedback.setStatus(status);
        updateById(feedback);
    }

    public Map<String, Object> getFeedbackStatistics() {
        long total = count();
        long pending = count(new LambdaQueryWrapper<Feedback>()
                .eq(Feedback::getStatus, STATUS_PENDING));
        long processing = count(new LambdaQueryWrapper<Feedback>()
                .eq(Feedback::getStatus, STATUS_PROCESSING));
        long resolved = count(new LambdaQueryWrapper<Feedback>()
                .eq(Feedback::getStatus, STATUS_RESOLVED));

        double averageRating = 0;
        List<Feedback> ratedFeedbacks = list(new LambdaQueryWrapper<Feedback>()
                .isNotNull(Feedback::getRating)
                .ne(Feedback::getRating, 0));
        if (!ratedFeedbacks.isEmpty()) {
            averageRating = ratedFeedbacks.stream()
                    .mapToInt(Feedback::getRating)
                    .average()
                    .orElse(0);
        }

        return Map.of(
                "total", total,
                "pending", pending,
                "processing", processing,
                "resolved", resolved,
                "averageRating", Math.round(averageRating * 10) / 10.0
        );
    }
}
