package exchange.xyvox.Models;

import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "news_items",
        indexes = {
                @Index(name = "idx_news_pubdate", columnList = "pub_date DESC"),
                @Index(name = "idx_news_externalid", columnList = "external_id", unique = true)
        })
public class NewsItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, length = 128)
    private String source;

    @Column(nullable = false, length = 512)
    private String title;


    @Column(nullable = false, length = 1024)
    private String link;


    @Column(name = "pub_date", nullable = false)
    private LocalDateTime pubDate;


    @Column(columnDefinition = "TEXT")
    private String summary;



    public NewsItem() {
    }

    public NewsItem( String source, String title, String link, LocalDateTime pubDate, String summary) {
        this.source = source;
        this.title = title;
        this.link = link;
        this.pubDate = pubDate;
        this.summary = summary;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }


    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
    }


    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }


    public String getLink() {
        return link;
    }
    public void setLink(String link) {
        this.link = link;
    }


    public LocalDateTime getPubDate() {
        return pubDate;
    }
    public void setPubDate(LocalDateTime pubDate) {
        this.pubDate = pubDate;
    }


    public String getSummary() {
        return summary;
    }
    public void setSummary(String summary) {
        this.summary = summary;
    }
}