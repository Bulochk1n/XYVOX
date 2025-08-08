package exchange.xyvox.Models;

import java.sql.Timestamp;

public class CommentView {
    private Long id;
    private String authorUsername;
    private String authorEmail;
    private String content;
    private Timestamp createdAt;

    public CommentView() { }

    public CommentView(Long id, String authorUsername, String authorEmail, String content, Timestamp createdAt) {
        this.id = id;
        this.authorUsername = authorUsername;
        this.authorEmail = authorEmail;
        this.content = content;
        this.createdAt = createdAt;
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
}