import java.util.UUID;

public class User {
    private UUID id;
    private String username;

    public User(String username) {
        this.id = UUID.randomUUID();
        this.username = username;
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
}