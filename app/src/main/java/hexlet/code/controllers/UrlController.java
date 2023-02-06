package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UrlController {

    public static final int UNPROCESSABLE_ENTITY_STATUS_CODE = 422;

    public static Handler addUrl = ctx -> {
        String inputUrl = ctx.formParamAsClass("url", String.class).getOrDefault(null);

        URL enteredUrl;
        try {
            enteredUrl = new URL(inputUrl);

        } catch (MalformedURLException e) {
            ctx.status(UNPROCESSABLE_ENTITY_STATUS_CODE);
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
            ctx.status(UNPROCESSABLE_ENTITY_STATUS_CODE);
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
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1) - 1;
        int rowsPerPage = 10;

        PagedList<Url> pagedUrls = new QUrl()
                .setFirstRow(page * rowsPerPage)
                .setMaxRows(rowsPerPage)
                .orderBy()
                .id.asc()
                .findPagedList();

        List<Url> urls = pagedUrls.getList();

        int lastPage = pagedUrls.getTotalPageCount() + 1;
        int currentPage = pagedUrls.getPageIndex() + 1;
        List<Integer> pages = IntStream
                .range(1, lastPage)
                .boxed()
                .collect(Collectors.toList());


        ctx.attribute("urls", urls);
        ctx.attribute("pages", pages);
        ctx.attribute("currentPage", currentPage);
        ctx.render("urls/index.html");
    };

    public static Handler showUrl = ctx -> {
        Long urlId = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(urlId)
                .findOne();

        List<UrlCheck> urlChecks = new QUrlCheck()
                .url.equalTo(url)
                .orderBy()
                .id.asc()
                .findList();

        ctx.attribute("urlChecks", urlChecks);
        ctx.attribute("url", url);
        ctx.render("/urls/show.html");
    };

    public static Handler urlCheck = ctx -> {

        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url urlItem = new QUrl()
                .id.equalTo(id)
                .findOne();

        try {
            UrlCheck urlCheckItem = getUrlCheckItem(urlItem);
            urlCheckItem.save();

            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flash-type", "success");

        } catch (UnirestException e) {
            ctx.sessionAttribute("flash", "Некорректный адрес");
            ctx.sessionAttribute("flash-type", "danger");
        } catch (Exception e) {
            ctx.sessionAttribute("flash", e.getMessage());
            ctx.sessionAttribute("flash-type", "danger");
        }

        ctx.redirect("/urls/" + id);
    };

    private static UrlCheck getUrlCheckItem(Url url) {

        HttpResponse<String> response = Unirest.get(url.getName()).asString();

        int statusCode = response.getStatus();
        String html = response.getBody();
        Document doc = Jsoup.parse(html);

        String title = doc.title();

        Element h1tag = doc.selectFirst("h1");
        String h1 = h1tag != null ? h1tag.text() : "";

        Element descrAttribute = doc.getElementsByAttributeValueContaining("name", "description").first();
        String description = descrAttribute != null ? descrAttribute.attr("content") : "";

        return new UrlCheck(statusCode, title, h1, description, url);
    }

}
