package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.time.Instant;
import java.util.List;

// Аннотация @Entity обозначает, что класс Url является моделью
@Entity
// Класс модели наследуется от класса io.ebean.Model
// Благодаря этому у модели появляется метод save() для добавления сущности в БД
public class Url extends Model {

    // Аннотация @Id обозначает, что поле класса является автогенерируемым первичным ключом
    @Id
    private long id;
    private String name;

    // Аннотация @WhenCreated обозначает, что поле фиксирует временную метку создания Entity
    @WhenCreated
    private Instant createdAt;

    @OneToMany
    private List<UrlCheck> urlChecks;

    public Url(String name) {
        this.name = name;
    }

    public final long getId() {
        return id;
    }

    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public final List<UrlCheck> getUrlChecks() {
        return urlChecks;
    }

    public final Instant getCreatedAt() {
        return createdAt;
    }

}
