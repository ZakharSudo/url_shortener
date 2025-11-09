import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UrlShortenerService {
    private final Map<String, ShortLink> shortLinks = new ConcurrentHashMap<>();
    private final Map<String, String> urlToShortCode = new ConcurrentHashMap<>();
    private final Map<UUID, List<String>> userLinks = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 6;

    public String createShortLink(String originalUrl, User user) {
        // Генерация уникального кода для каждого пользователя
        String userSpecificKey = originalUrl + ":" + user.getId();
        
        if (urlToShortCode.containsKey(userSpecificKey)) {
            return urlToShortCode.get(userSpecificKey);
        }

        String shortCode;
        do {
            shortCode = generateRandomCode();
        } while (shortLinks.containsKey(shortCode));

        // Лимит переходов: 10 раз
        int clickLimit = 10;
        // Время жизни: 24 часа
        LocalDateTime expirationDate = LocalDateTime.now().plusHours(24);

        ShortLink shortLink = new ShortLink(originalUrl, shortCode, user.getId(), clickLimit, expirationDate);
        
        shortLinks.put(shortCode, shortLink);
        urlToShortCode.put(userSpecificKey, shortCode);
        userLinks.computeIfAbsent(user.getId(), k -> new ArrayList<>()).add(shortCode);

        return shortCode;
    }

    public String redirect(String shortCode) {
        ShortLink link = shortLinks.get(shortCode);
        
        if (link == null) {
            throw new IllegalArgumentException("Ссылка не найдена");
        }
        
        if (!link.isActive()) {
            throw new LinkExpiredException("Ссылка недоступна");
        }
        
        if (link.isExpired()) {
            link.setActive(false);
            throw new LinkExpiredException("Срок действия ссылки истек");
        }
        
        if (link.isClickLimitExceeded()) {
            link.setActive(false);
            throw new LinkExpiredException("Лимит переходов по ссылке исчерпан");
        }
        
        link.incrementClickCount();
        return link.getOriginalUrl();
    }

    public boolean deleteLink(String shortCode, UUID userId) {
        ShortLink link = shortLinks.get(shortCode);
        if (link != null && link.getUserId().equals(userId)) {
            shortLinks.remove(shortCode);
            userLinks.get(userId).remove(shortCode);
            return true;
        }
        return false;
    }

    public List<ShortLink> getUserLinks(UUID userId) {
        List<String> userLinkCodes = userLinks.get(userId);
        if (userLinkCodes == null) return new ArrayList<>();
        
        List<ShortLink> links = new ArrayList<>();
        for (String code : userLinkCodes) {
            ShortLink link = shortLinks.get(code);
            if (link != null) {
                links.add(link);
            }
        }
        return links;
    }

    public ShortLink getLinkInfo(String shortCode) {
        return shortLinks.get(shortCode);
    }

    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    public Map<String, ShortLink> getAllLinks() {
        return new HashMap<>(shortLinks);
    }
}