import java.awt.*;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

public class Main {
    private static UrlShortenerService urlService = new UrlShortenerService();
    private static LinkCleanupService cleanupService = new LinkCleanupService(urlService);
    private static Map<UUID, User> users = new HashMap<>();
    private static User currentUser = null;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        cleanupService.startCleanupService();
        System.out.println("=== Сервис сокращения ссылок ===");
        
        try {
            while (true) {
                if (currentUser == null) {
                    showAuthMenu();
                } else {
                    showMainMenu();
                }
            }
        } finally {
            cleanupService.stopCleanupService();
        }
    }

    private static void showAuthMenu() {
        System.out.println("\n--- Авторизация ---");
        System.out.println("1. Создать нового пользователя");
        System.out.println("2. Войти по UUID");
        System.out.println("3. Выход");
        System.out.print("Выберите опцию: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        switch (choice) {
            case 1:
                createNewUser();
                break;
            case 2:
                loginByUUID();
                break;
            case 3:
                System.exit(0);
                break;
            default:
                System.out.println("Неверный выбор");
        }
    }

    private static void showMainMenu() {
        System.out.println("\n--- Главное меню ---");
        System.out.println("Текущий пользователь: " + currentUser.getUsername() + " (" + currentUser.getId() + ")");
        System.out.println("1. Создать короткую ссылку");
        System.out.println("2. Перейти по короткой ссылке");
        System.out.println("3. Мои ссылки");
        System.out.println("4. Удалить ссылку");
        System.out.println("5. Выйти из аккаунта");
        System.out.println("6. Выход");
        System.out.print("Выберите опцию: ");

        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        switch (choice) {
            case 1:
                createShortLink();
                break;
            case 2:
                redirectToLink();
                break;
            case 3:
                showUserLinks();
                break;
            case 4:
                deleteLink();
                break;
            case 5:
                currentUser = null;
                break;
            case 6:
                System.exit(0);
                break;
            default:
                System.out.println("Неверный выбор");
        }
    }

    private static void createNewUser() {
        System.out.print("Введите имя пользователя: ");
        String username = scanner.nextLine();
        
        User user = new User(username);
        users.put(user.getId(), user);
        currentUser = user;
        
        System.out.println("Пользователь создан! Ваш UUID: " + user.getId());
        System.out.println("Сохраните этот UUID для последующего входа!");
    }

    private static void loginByUUID() {
        System.out.print("Введите ваш UUID: ");
        String uuidString = scanner.nextLine();
        
        try {
            UUID uuid = UUID.fromString(uuidString);
            User user = users.get(uuid);
            if (user != null) {
                currentUser = user;
                System.out.println("Успешный вход! Добро пожаловать, " + user.getUsername());
            } else {
            	System.out.println("Пользователь не найден");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Неверный формат UUID");
        }
    }

    private static void createShortLink() {
        System.out.print("Введите длинную ссылку: ");
        String originalUrl = scanner.nextLine();
        
        try {
            String shortCode = urlService.createShortLink(originalUrl, currentUser);
            System.out.println("Короткая ссылка создана: " + shortCode);
            System.out.println("Лимит переходов: 10");
            System.out.println("Время жизни: 24 часа");
        } catch (Exception e) {
            System.out.println("Ошибка при создании ссылки: " + e.getMessage());
        }
    }

    private static void redirectToLink() {
        System.out.print("Введите короткий код: ");
        String shortCode = scanner.nextLine();
        
        try {
            String originalUrl = urlService.redirect(shortCode);
            System.out.println("Перенаправление на: " + originalUrl);
            
            // Автоматическое открытие в браузере
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(originalUrl));
            } else {
                System.out.println("Автоматическое открытие браузера не поддерживается");
            }
        } catch (LinkExpiredException e) {
            System.out.println("Ошибка: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void showUserLinks() {
        java.util.List<ShortLink> links = urlService.getUserLinks(currentUser.getId());
        if (links.isEmpty()) {
            System.out.println("У вас нет созданных ссылок");
            return;
        }
        
        System.out.println("\n--- Ваши ссылки ---");
        for (ShortLink link : links) {
            System.out.println("Короткий код: " + link.getShortCode());
            System.out.println("Оригинальный URL: " + link.getOriginalUrl());
            System.out.println("Переходы: " + link.getClickCount() + "/" + link.getClickLimit());
            System.out.println("Создана: " + link.getCreationDate());
            System.out.println("Истекает: " + link.getExpirationDate());
            System.out.println("Статус: " + (link.isActive() ? "Активна" : "Неактивна"));
            System.out.println("---");
        }
    }

    private static void deleteLink() {
        System.out.print("Введите короткий код для удаления: ");
        String shortCode = scanner.nextLine();
        
        boolean deleted = urlService.deleteLink(shortCode, currentUser.getId());
        if (deleted) {
            System.out.println("Ссылка удалена");
        } else {
            System.out.println("Ссылка не найдена или у вас нет прав для удаления");
        }
    }
}
            