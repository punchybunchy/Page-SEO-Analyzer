package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import java.time.Instant;

@Entity
public class UrlCheck extends Model {
    @Id
    private Long id;
    @WhenCreated
    private Instant createdAt;
    private int statusCode;
    private String title;
    private String h1;
    @Lob
    private String description;
    @ManyToOne
    private Url url;

    public UrlCheck(int statusCode, String title, String h1, String description, Url url) {
        this.statusCode = statusCode;
        this.title = title;
        this.h1 = h1;
        this.description = description;
        this.url = url;
    }

    public final long getId() {
        return id;
    }

    public final Instant getCreatedAt() {
        return createdAt;
    }

    public final int getStatusCode() {
        return statusCode;
    }

    public final String getTitle() {
        return title;
    }

    public final String getH1() {
        return h1;
    }

    public final String getDescription() {
        return description;
    }

    public final Url getUrl() {
        return url;
    }
}
