package wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * Класс для запуска и конфигурации WireMock сервера,
 * который эмулирует Banking Web Application.
 */
public class BankAppMock {

    private static final int PORT = 8080;
    private static final String HOST = "localhost";
    private static WireMockServer wireMockServer;

    public static void start() {
        if (wireMockServer == null || !wireMockServer.isRunning()) {
            WireMockConfiguration config = wireMockConfig()
                    .port(PORT)
                    .usingFilesUnderClasspath("wiremock/mappings") // Указываем путь к мокам
                    .withRootDirectory("src/test/resources/wiremock"); // Корневая директория

            wireMockServer = new WireMockServer(config);
            wireMockServer.start();

            // Здесь можно программно создать моки, если не хочешь использовать JSON-файлы
            setupStubs();

            System.out.println("WireMock server started on http://" + HOST + ":" + PORT);
        }
    }

    public static void stop() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
            System.out.println("WireMock server stopped");
        }
    }

    public static String getBaseUrl() {
        return "http://" + HOST + ":" + PORT;
    }

    private static void setupStubs() {
        // Программное создание моков (альтернатива JSON-файлам)
        // Пример мока для успешного логина через API
        /*
        wireMockServer.stubFor(post(urlEqualTo("/api/login"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\": \"success\", \"token\": \"fake-jwt-token\"}")));
        */
    }
}