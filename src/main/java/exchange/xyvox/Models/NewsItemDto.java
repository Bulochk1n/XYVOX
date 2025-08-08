package exchange.xyvox.Models;

import java.time.Instant;
import java.time.LocalDateTime;

public class NewsItemDto {

    private Long id;
    private String title;
    private String link;
    private String source;
    private LocalDateTime pubDate;
    private String summary;

    public NewsItemDto() {
    }

    public NewsItemDto(Long id, String title, String link, String source, LocalDateTime pubDate, String summary) {
        this.id = id;
        this.title = title;
        this.link = link;
        this.source = source;
        this.pubDate = pubDate;
        this.summary = summary;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getSource() {
        return source;
    }
    public void setSource(String source) {
        this.source = source;
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