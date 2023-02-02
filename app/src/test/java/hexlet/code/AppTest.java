package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.DB;
import io.ebean.Database;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AppTest {

    private static Javalin app;
    private static String baseUrl;
    private static Database database;
    private static Url url;

    @BeforeAll
    public static void beforeAll() {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
        database = DB.getDefault();
        url = new Url("https://www.example.com");
    }

    @AfterAll
    public static void afterAll() {
        app.stop();
    }

    @BeforeEach
    public final void beforeEach() {
        database.truncate("url");
        url.save();
    }

    @Test
    void testRootIndex() {
        HttpResponse<String> response = Unirest.get(baseUrl).asString();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains("Бесплатно проверяйте сайты на SEO пригодность");
    }

    @Test
    void testAddValidUrl() {
        String inputUrl = "https://ya.ru:8080/example/path";
        //В базу должен добавляться только домен с протоколом и порт, если он был указан.
        //Ожидаем что в БД будет сокращенный вариант url-а.
        String expectedUrl = "https://ya.ru:8080";

        HttpResponse<String> response = Unirest.post(baseUrl + "/urls")
                .field("url", inputUrl)
                .asString();

        assertThat(response.getStatus()).isEqualTo(302);

        Url urlFromDb = new QUrl()
                .name.equalTo(expectedUrl)
                .findOne();

        assertThat(urlFromDb).isNotNull();
        assertThat(urlFromDb.getName()).isEqualTo(expectedUrl);
    }

    @Test
    void testAddDuplicatedUrl() {
        String inputUrl = "https://www.duplicate-it.com";
        int expectedAmountOfCopies = 1;

        //Добавляем один url два раза. Ожидаем 422 и отсутствие дублей в БД.
        Unirest.post(baseUrl + "/urls")
                .field("url", inputUrl)
                .asEmpty();

        HttpResponse<String> response = Unirest.post(baseUrl + "/urls")
                .field("url", inputUrl)
                .asString();

        assertThat(response.getStatus()).isEqualTo(422);

        int amount = new QUrl()
                .name.equalTo(inputUrl)
                .findCount();

        assertThat(amount).isEqualTo(expectedAmountOfCopies);
    }

    @Test
    void testAddInvalidUrl() {
        String inputUrl = "ya.ru:8080/example/path";
        //В базу должен добавляться только домен без протокола должна быть ошибка.
        String expectedUrl = "ya.ru:8080";
        HttpResponse<String> response = Unirest.post(baseUrl + "/urls")
                .field("url", inputUrl)
                .asString();

        assertThat(response.getStatus()).isEqualTo(422);

//        Код состояния ответа HTTP 422 указывает, что сервер понимает тип содержимого в теле запроса
//        и синтаксис запроса является правильным, но серверу не удалось обработать инструкции содержимого.

        Url urlFromDb = new QUrl()
                .name.equalTo(expectedUrl)
                .findOne();

        assertThat(urlFromDb).isNull();
    }

    @Test
    void testShowInputtedUrls() {
        //Url https://www.example.com добавляется при создании БД, проверим, что он будет в списке.
        HttpResponse<String> response = Unirest.get(baseUrl + "/urls").asString();
        Assertions.assertThat(response.getBody()).contains("https://www.example.com");

        Url urlFromDb = new QUrl()
                .name.equalTo("https://www.example.com")
                .findOne();

        assertThat(urlFromDb.getName()).isEqualTo("https://www.example.com");
    }

    @Test
    void testShowUrl() {
        String inputUrl = "https://www.to-check-id.com";

        //Добавляем запись и узнаем ее id, проверяем будет она по ссылке /urls/id.
        Unirest.post(baseUrl + "/urls")
                .field("url", inputUrl)
                .asEmpty();

        Url url = new QUrl()
                .name.equalTo(inputUrl)
                .findOne();

        assert url != null;
        Long id = url.getId();

        HttpResponse<String> response = Unirest.get(baseUrl + "/urls/" + id).asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains(inputUrl);
    }
}
