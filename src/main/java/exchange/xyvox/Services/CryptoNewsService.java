package exchange.xyvox.Services;

import com.rometools.rome.feed.synd.SyndEntry;
import exchange.xyvox.Configs.AppProperties;
import exchange.xyvox.Models.NewsItem;
import exchange.xyvox.Repositories.NewsRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CryptoNewsService {

    private final NewsRepository newsRepo;

    @Autowired
    public CryptoNewsService(NewsRepository newsRepo) {
        this.newsRepo = newsRepo;
    }

    public List<NewsItem> findAll() {
        return newsRepo.findAll(Sort.by("pubDate").descending());
    }

    public NewsItem findById(Long id) {
        return newsRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("News not found"));
    }

    public NewsItem save(NewsItem ni) {
        if (ni.getPubDate() == null) {
            ni.setPubDate(LocalDateTime.now());
        }
        return newsRepo.save(ni);
    }

    public void delete(Long id) {
        newsRepo.deleteById(id);
    }
}