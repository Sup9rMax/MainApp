package org.example;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

interface IHttpService {
    void get(int id);
    void getAll();
    void post(String json);
    void put(int id, String json);
    void delete(int id);
}

class HttpService implements IHttpService {
    private final String BASE_URL = "https://jsonplaceholder.typicode.com/todos/";
    private final HttpClient client = HttpClient.newHttpClient();

    @Override
    public void get(int id) {
        sendRequest("GET", BASE_URL + id, null);
    }

    @Override
    public void getAll() {
        sendRequest("GET", BASE_URL, null);
    }

    @Override
    public void post(String json) {
        sendRequest("POST", BASE_URL, json);
    }

    @Override
    public void put(int id, String json) {
        sendRequest("PUT", BASE_URL + id, json);
    }

    @Override
    public void delete(int id) {
        sendRequest("DELETE", BASE_URL + id, null);
    }

    private void sendRequest(String method, String url, String body) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(url));

            switch (method) {
                case "GET", "DELETE" -> builder.method(method, HttpRequest.BodyPublishers.noBody());
                case "POST", "PUT" -> builder.method(method, HttpRequest.BodyPublishers.ofString(body))
                        .header("Content-Type", "application/json");
            }

            HttpRequest request = builder.build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Response (" + response.statusCode() + "):");
            System.out.println(response.body());
        } catch (IOException | InterruptedException e) {
            System.err.println("Ошибка при выполнении запроса: " + e.getMessage());
        }
    }
}

class CommandHandler {
    private final IHttpService service;

    public CommandHandler(IHttpService service) {
        this.service = service;
    }

    public boolean handle(String input) {
        String[] parts = input.trim().split("\\s+", 2);
        String command = parts[0].toUpperCase();

        try {
            return switch (command) {
                case "GET" -> {
                    if (parts.length == 2 && !parts[1].equalsIgnoreCase("ALL"))
                        service.get(Integer.parseInt(parts[1]));
                    else
                        service.getAll();
                    yield true;
                }
                case "POST" -> {
                    service.post("{\"title\": \"New Todo\", \"completed\": false}");
                    yield true;
                }
                case "PUT" -> {
                    service.put(1, "{\"title\": \"Updated Todo\", \"completed\": true}");
                    yield true;
                }
                case "DELETE" -> {
                    service.delete(1);
                    yield true;
                }
                case "EXIT" -> false;
                default -> {
                    System.out.println("Неизвестная команда. Доступные: GET [id|ALL], POST, PUT, DELETE, EXIT");
                    yield true;
                }
            };
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: ожидается числовой ID");
            return true;
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        IHttpService service = new HttpService();
        CommandHandler handler = new CommandHandler(service);

        System.out.println("Введите команду (GET [id|ALL], POST, PUT, DELETE, EXIT):");

        boolean running = true;
        while (running) {
            System.out.print("> ");
            String input = scanner.nextLine();
            running = handler.handle(input);
        }

        System.out.println("Завершение программы.");
        scanner.close();
    }
}
