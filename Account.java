import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
public class Account implements Serializable {
    private static final long serialVersionUID = 1L;
    private String accountId;
    private String email;
    private String passwordHash;
    private LocalDateTime createdAt;
    public Account() {}
    public Account(String email, String password) {
        this.accountId = UUID.randomUUID().toString();
        this.email = email;
        this.passwordHash = password;
        this.createdAt = LocalDateTime.now();
    }
    public boolean verifyPassword(String password) {
        return password != null && passwordHash != null && passwordHash.equals(password);
    }
    public String getAccountId() { return accountId; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setEmail(String email) { this.email = email; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
}
