package hexlet.code;

import io.javalin.Javalin;

public class App {

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "4985");
        return Integer.valueOf(port);
    }

//    private static String getMode() {
//        return System.getenv().getOrDefault("APP_ENV", "development");
//    }
//
//    private static boolean isProduction() {
//        return getMode().equals("production");
//    }

    public static Javalin getApp() {
        Javalin app = Javalin.create(
                // Включаем логирование.
                config -> {
                    config.plugins.enableDevLogging();
                })
                .get("/", ctx -> ctx.result("Hello World"));

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
