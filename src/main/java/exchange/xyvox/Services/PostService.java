package exchange.xyvox.Services;

import exchange.xyvox.Models.AppUser;
import exchange.xyvox.Models.Post;
import exchange.xyvox.Repositories.PostRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepo;
    private final AppUserService appUserService;

    @Autowired
    public PostService(PostRepository postRepo, AppUserService appUserService) {
        this.postRepo = postRepo;
        this.appUserService = appUserService;
    }

    public List<Post> findAllPosts() {
        return postRepo.findAll(org.springframework.data.domain.Sort.by("createdAt").descending());
    }


    @Transactional
    public Post createPost(String content) {
        System.out.println("Create Post " + content);
        String username = getCurrentUsername();
        System.out.println("Username " + username);
        AppUser user = appUserService.getUserByUsername(username);
        System.out.println("useer id " + user.getUserID());
        Post post = new Post();
        post.setContent(content);
        post.setAuthor(user);
        return postRepo.save(post);
    }

    public Optional<Post> findById(Long id) {
        return postRepo.findById(id);
    }

    @Transactional
    public void updatePost(Long postId, String newContent) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        String currentUser = getCurrentUsername();
        boolean isAuthor = post.getAuthor().getUsername().equals(currentUser);
        boolean isModerator = appUserService.hasRole(currentUser, "MODERATOR") || appUserService.hasRole(currentUser, "ADMINISTRATOR");
        if (!isAuthor && !isModerator) {
            throw new SecurityException("No rights for editing post.");
        }
        post.setContent(newContent);
        postRepo.save(post);
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        String currentUser = getCurrentUsername();
        boolean isAuthor = post.getAuthor().getUsername().equals(currentUser);
        boolean isModerator = appUserService.hasRole(currentUser, "MODERATOR") || appUserService.hasRole(currentUser, "ADMINISTRATOR");
        if (!isAuthor && !isModerator) {
            throw new SecurityException("No rights for editing post.");
        }
        postRepo.delete(post);
    }

    @Transactional
    public void likePost(Long postId) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        AppUser user = appUserService.getUserByUsername(getCurrentUsername());
        post.getLikedBy().add(user);
        postRepo.save(post);
    }

    @Transactional
    public void unlikePost(Long postId) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        AppUser user = appUserService.getUserByUsername(getCurrentUsername());
        post.getLikedBy().remove(user);
        postRepo.save(post);
    }

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();
            return appUserService.getUserByEmail(email).getUsername();
        }
        AppUser user = appUserService.getUserByEmail(principal.toString());
        return user.getUsername();
    }
}