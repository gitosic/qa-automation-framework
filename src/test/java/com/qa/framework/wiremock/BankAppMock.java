package com.qa.framework.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
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
            String rootPath = System.getProperty("user.dir") + "/src/test/resources/wiremock";

            WireMockConfiguration config = wireMockConfig()
                    .port(PORT)
                    .usingFilesUnderDirectory(rootPath)
                    .withRootDirectory(rootPath);

            wireMockServer = new WireMockServer(config);
            wireMockServer.start();

            System.out.println("✅ WireMock server started on " + getBaseUrl());
            System.out.println("📁 Using absolute path: " + rootPath);

            setupStubs();
        }
    }

    public static void stop() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
            System.out.println("✅ WireMock server stopped");
        }
    }

    public static String getBaseUrl() {
        return "http://" + HOST + ":" + PORT;
    }

    private static void setupStubs() {
        // Программно создаем мок для страницы логина
        wireMockServer.stubFor(get(urlEqualTo("/login"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/html")
                        .withHeader("Access-Control-Allow-Origin", "*")
                        .withBody("<!DOCTYPE html><html><head><title>Bank App - Login</title><style>.error { color: red; display: none; margin-top: 10px; } .success { color: green; display: none; margin-top: 10px; }</style></head><body><h1>Welcome to Bank App</h1><form id=\"loginForm\"><input type=\"text\" id=\"username\" name=\"username\" placeholder=\"Username\" required><input type=\"password\" id=\"password\" name=\"password\" placeholder=\"Password\" required><button type=\"submit\">Login</button></form><div id=\"errorMessage\" class=\"error\"></div><div id=\"successMessage\" class=\"success\"></div><script>document.getElementById('loginForm').addEventListener('submit', async (e) => { e.preventDefault(); const username = document.getElementById('username').value; const password = document.getElementById('password').value; document.getElementById('errorMessage').style.display = 'none'; document.getElementById('successMessage').style.display = 'none'; try { const response = await fetch('/api/login', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ username, password }) }); const result = await response.json(); if (result.status === 'success') { document.getElementById('successMessage').style.display = 'block'; document.getElementById('successMessage').textContent = result.message || 'Login successful! Redirecting...'; setTimeout(() => { window.location.href = '/dashboard'; }, 1000); } else { document.getElementById('errorMessage').style.display = 'block'; document.getElementById('errorMessage').textContent = result.message || 'Invalid credentials'; } } catch (error) { document.getElementById('errorMessage').style.display = 'block'; document.getElementById('errorMessage').textContent = 'Network error'; } });</script></body></html>")));

        // Мок для успешного логина
        wireMockServer.stubFor(post(urlEqualTo("/api/login"))
                .withRequestBody(containing("admin"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Access-Control-Allow-Origin", "*")
                        .withHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                        .withHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With")
                        .withBody("{\"status\": \"success\", \"token\": \"fake-jwt-token-12345\", \"message\": \"Login successful\"}")));

        // Мок для неуспешного логина
        wireMockServer.stubFor(post(urlEqualTo("/api/login"))
                .withRequestBody(containing("wrong"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Access-Control-Allow-Origin", "*")
                        .withHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                        .withHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With")
                        .withBody("{\"status\": \"error\", \"message\": \"Invalid credentials\"}")));

        // Мок для CORS preflight
        wireMockServer.stubFor(options(urlEqualTo("/api/login"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Access-Control-Allow-Origin", "*")
                        .withHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                        .withHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With")
                        .withHeader("Access-Control-Max-Age", "3600")));

        // Мок для dashboard
        wireMockServer.stubFor(get(urlEqualTo("/dashboard"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/html")
                        .withHeader("Access-Control-Allow-Origin", "*")
                        .withBody("<h1>Dashboard</h1><p>Welcome to your banking dashboard!</p>")));

        System.out.println("✅ Programmatic stubs setup completed");
    }
}