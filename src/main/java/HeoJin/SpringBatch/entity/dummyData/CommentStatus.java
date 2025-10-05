package HeoJin.SpringBatch.entity.dummyData;

public enum CommentStatus {
    ACTIVE,        // 활성
    DELETED,       // 사용자 삭제 ("삭제된 댓글입니다" 표시)
    ADMIN_DELETED  // 관리자 삭제 (완전히 숨김)
}