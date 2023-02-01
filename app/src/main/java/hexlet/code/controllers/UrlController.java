package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.javalin.http.Handler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class UrlController {

    public static Handler addUrl = ctx -> {
        String imputedUrl = ctx.formParamAsClass("url", String.class).getOrDefault(null);

        URL enteredUrl = null;
        try {
            enteredUrl = new URL(imputedUrl);

        } catch (MalformedURLException e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.render("index.html");
            return;
        }

        String normalizedUrl = enteredUrl.getProtocol() + "://" + enteredUrl.getAuthority();
        boolean urlExists =
                new QUrl()
                        .name.equalTo(normalizedUrl)
                        .exists();

        if (urlExists) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "warning");
            ctx.render("index.html");
            return;
        }

        Url url = new Url(normalizedUrl);
        url.save();

        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.sessionAttribute("flash-type", "success");
        ctx.redirect("/urls");
    };

    public static Handler listUrls = ctx -> {
        List<Url> urls = new QUrl()
                .orderBy()
                .id.asc()
                .findList();

        ctx.attribute("urls", urls);
        ctx.render("urls/index.html");
    };

    public static Handler showUrl = ctx -> {
        Long urlId = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(urlId)
                .findOne();

        ctx.attribute("url", url);
        ctx.render("/urls/show.html");
    };

    public static Handler checkUrl = ctx -> {};
}
