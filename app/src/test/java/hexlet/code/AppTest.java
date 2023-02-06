package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.DB;
import io.ebean.Database;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AppTest {

    private static Javalin app;
    private static String baseUrl;
    private static Database database;
    private static Url url;
    private static MockWebServer mockWebServer;

    @BeforeAll
    public static void beforeAll() throws IOException {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
        database = DB.getDefault();

        mockWebServer = new MockWebServer();
        MockResponse mockResponse = new MockResponse().setBody(Files.readString(
                Paths.get("./src/test/resources/test.html"), StandardCharsets.UTF_8));
        mockWebServer.enqueue(mockResponse);
        mockWebServer.start();

    }

    @AfterAll
    public static void afterAll() throws IOException {
        app.stop();
        mockWebServer.shutdown();
    }

    @BeforeEach
    public final void beforeEach() {
        database.script().run("/truncate.sql");
        database.script().run("/seed-test-app-db.sql");
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
        assert urlFromDb != null;
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

//        assertThat(response.getStatus()).isEqualTo(422);

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

//        assertThat(response.getStatus()).isEqualTo(422);

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
        long id = url.getId();

        HttpResponse<String> response = Unirest.get(baseUrl + "/urls/" + id).asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains(inputUrl);
    }

    @Test
    void testUrlCheck() {
        final String title = "Title of the test";
        final String description = "Description of the test page";
        final String h1 = "H1 test text";

        //Получаем от MockWebServer сервера адрес тестовой страницы и нормализуем используя метод getNormalizedUrl
        String inputUrl = getNormalizedUrl(mockWebServer.url("/").toString());

        //Добавляем тестовую страницу на проверку
        HttpResponse<String> postUrlResponse = Unirest.post(baseUrl + "/urls")
                .field("url", inputUrl)
                .asString();

        //Проверяем статус
        assertThat(postUrlResponse.getStatus()).isEqualTo(302);

        //Проверяем, что адрес отображается в списке
        HttpResponse<String> getUrlsResponse = Unirest.get(baseUrl + "/urls").asString();
        assertThat(getUrlsResponse.getBody()).contains(inputUrl);

        //Находим ID присвоенный в БД
        Url testUrl = new QUrl()
                .name.equalTo(inputUrl)
                .findOne();

        long id = testUrl.getId();

        //Отправляем запрос на проверку URL
        HttpResponse responseCheck = Unirest
                .post(baseUrl + "/urls/" + id + "/checks")
                .asEmpty();

        assertThat(responseCheck.getStatus()).isEqualTo(302);

        HttpResponse<String> responseShow = Unirest
                .get(baseUrl + "/urls/" + id)
                .asString();

        assertThat(responseShow.getStatus()).isEqualTo(200);
        assertThat(responseShow.getBody()).contains(title);
        assertThat(responseShow.getBody()).contains(description);
        assertThat(responseShow.getBody()).contains(h1);
    }

    private static String getNormalizedUrl(String inputUrl) {
        String normalizedUrl = "";
        try {
            URL enteredUrl = new URL(inputUrl);
            normalizedUrl = enteredUrl.getProtocol() + "://" + enteredUrl.getAuthority();

        } catch (MalformedURLException e) {
            System.out.println("Добавлен некорректный URL");
        }
        return  normalizedUrl;
    }

}
