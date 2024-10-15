package hexlet.code;

import hexlet.code.model.Url;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public final class TestApp {
    private static Javalin app;
    private static MockWebServer mockServer;
    private final String firstUrlCorrect = "https://ru.hexlet.io/programs/java";
    private final String secondUrlCorrect = "https://github.com/MenzurenkoKirill";
    private final String incorrectUrl = "https//ru.hexlet.io/programs/java";

    @BeforeAll
    public static void beforeAll() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
    }

    @BeforeEach
    public void beforeEach() throws SQLException, IOException {
        app = App.getApp();
    }
    @AfterAll
    public static void afterAll() throws IOException {
        mockServer.shutdown();
    }

    @org.junit.jupiter.api.Test
    public void showMainPageTest() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.mainPath());
            assertThat(response.code()).isEqualTo(200);
        });
    }
    @org.junit.jupiter.api.Test
    public void createUrlTest() {
        JavalinTest.test(app, ((server, client) -> {
            String requestBody = "url=" + firstUrlCorrect;
            var response = client.post(NamedRoutes.urlsPath(), requestBody);
            var urls = UrlRepository.getUrls();
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).contains("https://ru.hexlet.io");
            assertThat(urls.size()).isEqualTo(1);
            assertThat(urls.get(0).getName()).isEqualTo("https://ru.hexlet.io");
        }));
    }
    @org.junit.jupiter.api.Test
    public void showUrlsPageTest() {
        JavalinTest.test(app, ((server, client) -> {
            Url url1 = new Url(firstUrlCorrect);
            Url url2 = new Url(secondUrlCorrect);
            UrlRepository.save(url1);
            UrlRepository.save(url2);
            var response = client.get(NamedRoutes.urlsPath());
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).contains("hexlet").contains("github");
            assertThat(UrlRepository.getUrls().size()).isEqualTo(2);
        }));
    }

    @org.junit.jupiter.api.Test
    public void showUrlPageTest() {
        JavalinTest.test(app, (server, client) -> {
            var url = new Url(secondUrlCorrect);
            UrlRepository.save(url);
            var response = client.get(NamedRoutes.urlPath(url.getId()));
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).contains("github");
        });
    }
    @org.junit.jupiter.api.Test
    void urlNotFoundTest() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.urlPath("9999"));
            assertThat(response.code()).isEqualTo(404);
        });
    }
    @org.junit.jupiter.api.Test
    public void testCreateIncorrectUrl() {
        JavalinTest.test(app, ((server, client) -> {
            String requestBody = "url=" + incorrectUrl;
            var response = client.post(NamedRoutes.urlsPath(), requestBody);
            assertThat(response.code()).isEqualTo(200);
            assertThat(UrlRepository.getUrls().size()).isEqualTo(0);
        }));
    }
    @org.junit.jupiter.api.Test
    public void testMakeCheck() throws SQLException, IOException {
        String page = Files.readString(Paths.get("./src/test/resources/testPage.html"));
        MockResponse mockResponse = new MockResponse().setResponseCode(200).setBody(page);
        mockServer.enqueue(mockResponse);
        String urlString = mockServer.url(NamedRoutes.mainPath()).toString();
        Url testUrl = new Url(urlString);
        UrlRepository.save(testUrl);
        JavalinTest.test(app, (server, client) -> {
            var response = client.post(NamedRoutes.urlChecksPath(testUrl.getId()));
            assertThat(response.code()).isEqualTo(200);
            var lastCheck = UrlCheckRepository.getLastCheck(testUrl.getId()).get();
            assertThat(lastCheck.getStatusCode()).isEqualTo(200);
            assertThat(lastCheck.getTitle()).isEqualTo("Sample title");
            assertThat(lastCheck.getH1()).isEqualTo("Sample header");
            assertThat(lastCheck.getDescription()).contains("Sample description");
        });
    }
}