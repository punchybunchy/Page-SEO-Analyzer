package hexlet.code;

import hexlet.code.controllers.RootController;
import hexlet.code.controllers.UrlController;
import io.javalin.Javalin;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import io.javalin.plugin.rendering.template.JavalinThymeleaf;

import static io.javalin.apibuilder.ApiBuilder.*;

public class App {

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "4985");
        return Integer.valueOf(port);
    }

    private static String getMode() {
        return System.getenv().getOrDefault("APP_ENV", "development");
    }

    private static boolean isProduction() {
        return getMode().equals("production");
    }

    // Получаем инстанс движка шаблонизатора Thymeleaf
    private static TemplateEngine getTemplateEngine() {
        // Создаём инстанс движка шаблонизатора
        TemplateEngine templateEngine = new TemplateEngine();
        // Добавляем к нему диалекты
        templateEngine.addDialect(new LayoutDialect());
        templateEngine.addDialect(new Java8TimeDialect());
        // Настраиваем преобразователь шаблонов, так, чтобы обрабатывались
        // файлы в директории с шаблонами /templates/
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/");
        // Добавляем преобразователь шаблонов к движку шаблонизатора
        templateEngine.addTemplateResolver(templateResolver);

        return templateEngine;
    }

    private static void addRoutes (Javalin app) {
        app.get("/", RootController.welcome);
//        app.post("/", RootController.addUrl);

        app.routes(() -> {
            path("urls", () -> {
                get(UrlController.listUrls);
                post(UrlController.addUrl);
                path("{id}", () -> {
                    get(UrlController.showUrl);
                    post(UrlController.checkUrl);
                });
            });
        });
    }

    public static Javalin getApp() {
        // Создаём Javalin инстанс приложения, в аргумент передаем конфигурацию
        Javalin app = Javalin.create(config -> {
            if (!isProduction()) {
                // Включаем логирование на локальной проверке
                config.enableDevLogging();
            }

            // Подключаем настроенный шаблонизатор Thymeleaf к фреймворку Javalin
            JavalinThymeleaf.configure(getTemplateEngine());

            // Подключаем фреймворк со стилями Bootstrap из библиотеки webjars
            config.enableWebjars();
        });

        // Добавляем в приложение маршруты
        addRoutes(app);

        // Обработчик before запускается перед каждым запросом.
        // Устанавливаем атрибут ctx для запросов.
        app.before(ctx -> {
            ctx.attribute("ctx", ctx);
        });

        return app;
    }

    public static void main(String[] args) {
        Javalin app = getApp();
        app.start(getPort());
    }
}
