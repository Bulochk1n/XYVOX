package exchange.xyvox.Models;

import java.sql.Timestamp;
import java.util.List;

public class PostView {
    private Long id;
    private String authorUsername;
    private String authorEmail;
    private String content;
    private Timestamp createdAt;

    private int likeCount;
    private boolean likedByCurrentUser;

    private int commentCount;
    private List<CommentView> comments;

    public PostView() { }

    public PostView(Long id,
                    String authorUsername,
                    String authorEmail,
                    String content,
                    Timestamp createdAt,
                    int likeCount,
                    boolean likedByCurrentUser,
                    int commentCount,
                    List<CommentView> comments) {
        this.id = id;
        this.authorUsername = authorUsername;
        this.authorEmail = authorEmail;
        this.content = content;
        this.createdAt = createdAt;
        this.likeCount = likeCount;
        this.likedByCurrentUser = likedByCurrentUser;
        this.commentCount = commentCount;
        this.comments = comments;
    }


    public Long getId() {
        return id;
    }
    public String getAuthorUsername() {
        return authorUsername;
    }
    public String getAuthorEmail() { return authorEmail; }
    public String getContent() {
        return content;
    }
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    public int getLikeCount() {
        return likeCount;
    }
    public boolean isLikedByCurrentUser() {
        return likedByCurrentUser;
    }
    public int getCommentCount() {
        return commentCount;
    }
    public List<CommentView> getComments() {
        return comments;
    }
}