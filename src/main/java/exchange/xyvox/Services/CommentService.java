package exchange.xyvox.Services;

import exchange.xyvox.Models.AppUser;
import exchange.xyvox.Models.Comment;
import exchange.xyvox.Models.Post;
import exchange.xyvox.Repositories.CommentRepository;
import exchange.xyvox.Repositories.PostRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepo;
    private final PostRepository postRepo;
    private final AppUserService appUserService;

    @Autowired
    public CommentService(CommentRepository commentRepo, PostRepository postRepo, AppUserService appUserService) {
        this.commentRepo = commentRepo;
        this.postRepo = postRepo;
        this.appUserService = appUserService;
    }

    @Transactional
    public void createComment(Long postId, String content) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        AppUser user = appUserService.getUserByUsername(getCurrentUsername());
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setAuthor(user);
        comment.setPost(post);
        commentRepo.save(comment);
    }

    @Transactional
    public void updateComment(Long commentId, String newContent) {
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        String currentUser = getCurrentUsername();
        boolean isAuthor = comment.getAuthor().getUsername().equals(currentUser);
        boolean isModerator = appUserService.hasRole(currentUser, "MODERATOR") || appUserService.hasRole(currentUser, "ADMINISTRATOR");
        if (!isAuthor && !isModerator) {
            throw new SecurityException("No rights to edit this comment.");
        }
        comment.setContent(newContent);
        commentRepo.save(comment);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        String currentUser = getCurrentUsername();
        boolean isAuthor = comment.getAuthor().getUsername().equals(currentUser);
        boolean isModerator = appUserService.hasRole(currentUser, "MODERATOR") || appUserService.hasRole(currentUser, "ADMINISTRATOR");
        if (!isAuthor && !isModerator) {
            throw new SecurityException("No rights to delete this comment.");
        }
        commentRepo.delete(comment);
    }

    public List<Comment> findCommentsByPostId(Long postId) {
        return commentRepo.findByPostIdOrderByCreatedAtAsc(postId);
    }

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String email = ((UserDetails)principal).getUsername();
            return appUserService.getUserByEmail(email).getUsername();
        }
        AppUser user = appUserService.getUserByEmail(principal.toString());
        return user.getUsername();
    }
}