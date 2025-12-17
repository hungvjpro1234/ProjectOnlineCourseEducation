package com.example.projectonlinecourseeducation.data.lessoncomment;

import com.example.projectonlinecourseeducation.core.model.lesson.LessonComment;
import com.example.projectonlinecourseeducation.data.lessoncomment.remote.*;
import com.example.projectonlinecourseeducation.data.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

/**
 * Remote implementation of LessonCommentApi
 *
 * - All API calls are synchronous (execute)
 * - MUST be wrapped by AsyncApiHelper at UI layer
 * - Listener methods are NO-OP (backend không có realtime push)
 */
public class LessonCommentRemoteApiService implements LessonCommentApi {

    private final LessonCommentRetrofitService api =
            RetrofitClient.getInstance()
                    .getRetrofit()
                    .create(LessonCommentRetrofitService.class);

    // =====================================================
    // Mapping helpers
    // =====================================================

    private LessonComment mapDto(LessonCommentDto d) {
        if (d == null) return null;

        return new LessonComment(
                d.id,
                d.lessonId,
                d.userId,
                d.userName,
                d.content,
                d.createdAt,
                d.isDeleted,
                d.teacherReplyContent,
                d.teacherReplyBy,
                d.teacherReplyAt
        );
    }

    private List<LessonComment> mapList(List<LessonCommentDto> dtos) {
        List<LessonComment> list = new ArrayList<>();
        if (dtos == null) return list;
        for (LessonCommentDto d : dtos) {
            list.add(mapDto(d));
        }
        return list;
    }

    // =====================================================
    // API methods (MATCH LessonCommentApi)
    // =====================================================

    @Override
    public List<LessonComment> getCommentsForLesson(String lessonId) {
        try {
            Response<LessonCommentListResponse> res =
                    api.getCommentsForLesson(lessonId).execute();

            return (res.isSuccessful()
                    && res.body() != null
                    && res.body().success)
                    ? mapList(res.body().data)
                    : new ArrayList<>();

        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public LessonComment addComment(String lessonId,
                                    String userId,
                                    String userName,
                                    String content) {
        try {
            AddCommentRequest req = new AddCommentRequest();
            req.lessonId = lessonId;
            req.content = content;

            Response<LessonCommentDetailResponse> res =
                    api.addComment(req).execute();

            return (res.isSuccessful()
                    && res.body() != null
                    && res.body().success)
                    ? mapDto(res.body().data)
                    : null;

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean deleteComment(String commentId, String userId) {
        try {
            // userId dùng cho backend authorization (JWT), không cần gửi body
            return api.deleteComment(commentId).execute().isSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public LessonComment markCommentAsDeleted(String commentId) {
        try {
            Response<LessonCommentDetailResponse> res =
                    api.markCommentAsDeleted(commentId).execute();

            return (res.isSuccessful()
                    && res.body() != null
                    && res.body().success)
                    ? mapDto(res.body().data)
                    : null;

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public LessonComment addReply(String commentId,
                                  String teacherName,
                                  String replyContent) {
        try {
            AddReplyRequest req = new AddReplyRequest();
            req.replyContent = replyContent;

            Response<LessonCommentDetailResponse> res =
                    api.addReply(commentId, req).execute();

            return (res.isSuccessful()
                    && res.body() != null
                    && res.body().success)
                    ? mapDto(res.body().data)
                    : null;

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public LessonComment deleteReply(String commentId) {
        try {
            Response<LessonCommentDetailResponse> res =
                    api.deleteReply(commentId).execute();

            return (res.isSuccessful()
                    && res.body() != null
                    && res.body().success)
                    ? mapDto(res.body().data)
                    : null;

        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public int getCommentCount(String lessonId) {
        try {
            Response<Integer> res =
                    api.getCommentCount(lessonId).execute();

            return (res.isSuccessful() && res.body() != null)
                    ? res.body()
                    : 0;

        } catch (Exception e) {
            return 0;
        }
    }

    // =====================================================
    // Listener support (NO-OP for Remote)
    // =====================================================

    @Override
    public void addLessonCommentUpdateListener(LessonCommentUpdateListener listener) {
        // NO-OP: backend không có push realtime
    }

    @Override
    public void removeLessonCommentUpdateListener(LessonCommentUpdateListener listener) {
        // NO-OP
    }
}
