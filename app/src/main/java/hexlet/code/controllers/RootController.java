package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.javalin.http.Handler;

import java.net.URL;

public class RootController {
    public static Handler welcome = ctx -> {
        ctx.render("index.html");
    };

}
