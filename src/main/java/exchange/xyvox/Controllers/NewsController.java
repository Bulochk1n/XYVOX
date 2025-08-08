package exchange.xyvox.Controllers;

import exchange.xyvox.Models.NewsItem;
import exchange.xyvox.Models.NewsItemDto;
import exchange.xyvox.Repositories.NewsRepository;
import exchange.xyvox.Services.CryptoNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/xyvox/api/v1/news")
public class NewsController {

    private final CryptoNewsService newsService;

    @Autowired
    public NewsController(CryptoNewsService newsSvc) {
        this.newsService = newsSvc;
    }

    @GetMapping
    public String listNews(Model model) {
        model.addAttribute("newsList", newsService.findAll());
        return "news";
    }

    @Secured({"ROLE_MODERATOR","ROLE_ADMIN"})
    @GetMapping("/add-news")
    public String createForm(Model model) {
        model.addAttribute("newsItem", new NewsItem());
        return "add-news";
    }

    @Secured({"ROLE_MODERATOR","ROLE_ADMIN"})
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("newsItem", newsService.findById(id));
        return "add-news";
    }


    @Secured({"ROLE_MODERATOR","ROLE_ADMIN"})
    @PostMapping("/add-news/save")
    public String save(@ModelAttribute NewsItem newsItem) {
        newsItem.setPubDate(LocalDateTime.now());
        newsService.save(newsItem);
        return "redirect:/xyvox/api/v1/news";
    }

    @Secured({"ROLE_MODERATOR","ROLE_ADMIN"})
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        newsService.delete(id);
        return"redirect:/xyvox/api/v1/news";
    }

}