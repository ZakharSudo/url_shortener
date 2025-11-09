import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

public class LinkCleanupService {
    private final UrlShortenerService urlShortenerService;
    private final Timer timer;

    public LinkCleanupService(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
        this.timer = new Timer(true);
    }

    public void startCleanupService() {
        // Проверка каждые 30 минут
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                cleanupExpiredLinks();
            }
        }, 0, 30 * 60 * 1000); // 30 минут
    }

    private void cleanupExpiredLinks() {
        urlShortenerService.getAllLinks().values().stream()
            .filter(link -> link.isExpired() || !link.isActive())
            .forEach(link -> urlShortenerService.deleteLink(link.getShortCode(), link.getUserId()));
    }

    public void stopCleanupService() {
        timer.cancel();
    }
}