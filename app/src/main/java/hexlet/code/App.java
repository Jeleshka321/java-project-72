package hexlet.code;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hexlet.code.controller.UrlCheckController;
import hexlet.code.controller.UrlController;
import hexlet.code.repository.BaseRepository;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinJte;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.sql.SQLException;

@Slf4j
public class App {
    public static Javalin getApp() throws SQLException, IOException {
        var hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(Utils.getJdbcUrl());
        var dataSource = new HikariDataSource(hikariConfig);
        var sql = Utils.readResourceFile("schema.sql");
        log.info(sql);
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute(sql);
        }
        BaseRepository.dataSource = dataSource;
        var app = Javalin.create(config -> {
            config.plugins.enableDevLogging();
        });
        JavalinJte.init(Utils.createTemplateEngine());
        app.get(NamedRoutes.mainPath(), UrlController::showMainPage);
        app.get(NamedRoutes.urlsPath(), UrlController::showUrls);
        app.get(NamedRoutes.urlPath("{id}"), UrlController::showUrl);
        app.post(NamedRoutes.urlsPath(), UrlController::createUrl);
        app.post(NamedRoutes.urlChecksPath("{id}"), UrlCheckController::makeCheck);
        return app;
    }
    public static void main(String[] args) throws SQLException, IOException {
        var app = getApp();
        app.start(Utils.getPort());
    }
}