import java.time.LocalDateTime;
import java.util.UUID;

public class ShortLink {
    private String id;
    private String originalUrl;
    private String shortCode;
    private UUID userId;
    private int clickLimit;
    private int clickCount;
    private LocalDateTime creationDate;
    private LocalDateTime expirationDate;
    private boolean isActive;

    public ShortLink(String originalUrl, String shortCode, UUID userId, int clickLimit, LocalDateTime expirationDate) {
        this.id = UUID.randomUUID().toString();
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.userId = userId;
        this.clickLimit = clickLimit;
        this.clickCount = 0;
        this.creationDate = LocalDateTime.now();
        this.expirationDate = expirationDate;
        this.isActive = true;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getOriginalUrl() { return originalUrl; }
    public String getShortCode() { return shortCode; }
    public UUID getUserId() { return userId; }
    public int getClickLimit() { return clickLimit; }
    public int getClickCount() { return clickCount; }
    public LocalDateTime getCreationDate() { return creationDate; }
    public LocalDateTime getExpirationDate() { return expirationDate; }
    public boolean isActive() { return isActive; }

    public void incrementClickCount() { this.clickCount++; }
    public void setActive(boolean active) { isActive = active; }
    public boolean isExpired() { return LocalDateTime.now().isAfter(expirationDate); }
    public boolean isClickLimitExceeded() { return clickCount >= clickLimit; }
}