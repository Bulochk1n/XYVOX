package exchange.xyvox.Repositories;

import exchange.xyvox.Models.NewsItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface NewsRepository extends JpaRepository<NewsItem, Long> {
    default Page<NewsItem> findAllByOrderByPubDateDesc(Pageable pageable) {
        return null;
    }
    Page<NewsItem> findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(
            String titleKeyword,
            String summaryKeyword,
            Pageable pageable
    );
}