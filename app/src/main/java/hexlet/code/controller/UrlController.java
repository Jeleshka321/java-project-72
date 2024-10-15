package hexlet.code.controller;

import hexlet.code.dto.BasePage;
import hexlet.code.dto.urls.UrlPage;
import hexlet.code.dto.urls.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.NamedRoutes;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import io.javalin.http.NotFoundResponse;

import io.javalin.http.Context;
import java.net.URL;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;

public class UrlController {
    public static void showMainPage(Context ctx) {
        var page = new BasePage();
        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setColor(ctx.consumeSessionAttribute("color"));
        ctx.render("main.jte", Collections.singletonMap("page", page));
    }
    public  static void createUrl(Context ctx) throws SQLException {
        var inputName = ctx.formParamAsClass("url", String.class).getOrDefault(null);
        URL inputUrl = null;
        try {
            inputUrl = new URL(inputName);
        } catch (MalformedURLException e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("color", "danger");
            ctx.redirect(NamedRoutes.mainPath());
        }
        if (inputUrl != null) {
            String normalizedUrl = String
                    .format(
                            "%s://%s%s",
                            inputUrl.getProtocol(),
                            inputUrl.getHost(),
                            inputUrl.getPort() == -1 ? "" : ":" + inputUrl.getPort()
                    )
                    .toLowerCase();

            var url = UrlRepository.findByName(normalizedUrl).orElse(null);
            if (url != null) {
                ctx.sessionAttribute("flash", "Страница уже существует");
                ctx.sessionAttribute("flash-type", "info");
                ctx.redirect(NamedRoutes.urlsPath());
            } else {
                var newUrl = new Url(normalizedUrl);
                UrlRepository.save(newUrl);
                ctx.sessionAttribute("flash", "Страница усрешно добавлена");
                ctx.sessionAttribute("color", "success");
                ctx.redirect(NamedRoutes.urlsPath());
            }
        }
    }
    public static void showUrls(Context ctx) throws SQLException {
        var urlsWithLastChecks = new HashMap<Url, UrlCheck>();
        var urls = UrlRepository.getUrls();
        for (var url : urls) {
            UrlCheckRepository.getLastCheck(url.getId()).ifPresentOrElse(
                    (check) -> urlsWithLastChecks.put(url, check),
                    () -> urlsWithLastChecks.put(url, null));
        }
        var page = new UrlsPage(urlsWithLastChecks);
        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setColor(ctx.consumeSessionAttribute("color"));
        ctx.render("urls/index.jte", Collections.singletonMap("page", page));
    }
    public static void showUrl(Context ctx) throws SQLException {
        var id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);
        var url = UrlRepository.findById(id).orElseThrow(() ->
                new NotFoundResponse(String.format("Url with id = %s not found", id)));
        var urlChecks = UrlCheckRepository.getUrlChecks(id);
        var page = new UrlPage(url, urlChecks);
        page.setFlash(ctx.consumeSessionAttribute("flash"));
        page.setColor(ctx.consumeSessionAttribute("color"));
        ctx.render("urls/show.jte", Collections.singletonMap("page", page));
    }
}