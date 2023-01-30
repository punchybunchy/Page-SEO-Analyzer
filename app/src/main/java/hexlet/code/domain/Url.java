package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;

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



}
