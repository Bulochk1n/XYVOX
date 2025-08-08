package exchange.xyvox.Controllers;

import exchange.xyvox.Models.AppUser;
import exchange.xyvox.Models.CommentView;
import exchange.xyvox.Models.Post;
import exchange.xyvox.Models.PostView;
import exchange.xyvox.Services.AppUserService;
import exchange.xyvox.Services.CommentService;
import exchange.xyvox.Services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/xyvox/api/v1/posts")
public class PostsController {

    private final PostService postService;
    private final CommentService commentService;
    private final AppUserService appUserService;

    @Autowired
    public PostsController(PostService postService,
                           CommentService commentService,
                           AppUserService appUserService) {
        this.postService = postService;
        this.commentService = commentService;
        this.appUserService = appUserService;
    }


    @GetMapping
    public String viewAllPosts(Model model) {
        List<Post> posts = postService.findAllPosts();

        String currentUsername = getCurrentUsername();
        List<PostView> postViews = posts.stream().map(post -> {
            boolean likedByCurrent = false;
            if (currentUsername != null) {
                likedByCurrent = post.getLikedBy().stream()
                        .anyMatch(u -> u.getUsername().equals(currentUsername));
            }
            List<CommentView> commentViews = commentService.findCommentsByPostId(post.getId())
                    .stream()
                    .map(c -> new CommentView(
                            c.getId(),
                            c.getAuthor().getUsername(),
                            c.getAuthor().getEmail(),
                            c.getContent(),
                            c.getCreatedAt()
                    ))
                    .collect(Collectors.toList());

            return new PostView(
                    post.getId(),
                    post.getAuthor().getUsername(),
                    post.getAuthor().getEmail(),
                    post.getContent(),
                    post.getCreatedAt(),
                    post.getLikedBy().size(),
                    likedByCurrent,
                    post.getComments().size(),
                    commentViews
            );
        }).collect(Collectors.toList());

        model.addAttribute("posts", postViews);
        return "posts";
    }

    @PostMapping("/create")
    public String createPost(@RequestParam("content") String content) {
        postService.createPost(content);
        return "redirect:/xyvox/api/v1/posts";
    }


    @PostMapping("/edit/{id}")
    public String editPost(@PathVariable("id") Long id,
                           @RequestParam("content") String content) {
        postService.updatePost(id, content);
        return "redirect:/xyvox/api/v1/posts";
    }


    @PostMapping("/delete/{id}")
    public String deletePost(@PathVariable("id") Long id) {
        postService.deletePost(id);
        return "redirect:/xyvox/api/v1/posts";
    }


    @PostMapping("/like/{id}")
    public String likePost(@PathVariable("id") Long id) {
        postService.likePost(id);
        return "redirect:/xyvox/api/v1/posts";
    }

    @PostMapping("/unlike/{id}")
    public String unlikePost(@PathVariable("id") Long id) {
        postService.unlikePost(id);
        return "redirect:/xyvox/api/v1/posts";
    }

    @PostMapping("/comments/create")
    public String createComment(
            Authentication authentication,
            @RequestParam("postId") Long postId,
            @RequestParam("content") String content) {

        commentService.createComment(postId, content);
        return "redirect:/xyvox/api/v1/posts";
    }

    @PostMapping("/comments/edit/{id}")
    public String editComment(@PathVariable("id") Long id,
                              @RequestParam("content") String content) {
        commentService.updateComment(id, content);
        return "redirect:/xyvox/api/v1/posts";
    }

    @PostMapping("/comments/delete/{id}")
    public String deleteComment(@PathVariable("id") Long id) {
        commentService.deleteComment(id);
        return "redirect:/xyvox/api/v1/posts";
    }

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            AppUser user = appUserService.getUserByEmail(((UserDetails) principal).getUsername());
            return user.getUsername();
        }
        return null;
    }
}