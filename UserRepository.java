import java.util.HashMap;
import java.util.Map;
public class UserRepository {
    private static final String USERS_FILE = "users.ser";
    private final Map<String, User> usersByEmail;
    private final Map<String, User> usersById;
    public UserRepository() {
        this.usersByEmail = StorageUtil.loadObject(USERS_FILE, new HashMap<>());
        this.usersById = new HashMap<>();
        rebuildIdIndex();
    }
    private void rebuildIdIndex() {
        usersById.clear();
        for (User user : usersByEmail.values()) {
            usersById.put(user.getUserId(), user);
        }
    }
    private void persist() {
        StorageUtil.saveObject(USERS_FILE, usersByEmail);
        rebuildIdIndex();
    }
    public User getUserByEmail(String email) {
        if (email == null) { return null; }
        return usersByEmail.get(email.trim().toLowerCase());
    }
    public User getUserById(String userId) { return usersById.get(userId); }
    public void save(User user) {
        if (user == null || user.getAccount() == null || user.getAccount().getEmail() == null) { return; }
        usersByEmail.put(user.getAccount().getEmail().trim().toLowerCase(), user);
        persist();
    }
    public Msg createAccount(String name, String email, String password) {
        if (email == null || email.isBlank()) { return new Msg("Email is required."); }
        String normalizedEmail = email.trim().toLowerCase();
        if (usersByEmail.containsKey(normalizedEmail)) {
            return new Msg("This email is already registered with an existing account.");
        }
        Account account = new Account(normalizedEmail, password);
        User user = new User(name, account);
        usersByEmail.put(normalizedEmail, user);
        persist();
        return new Msg("Account created successfully.");
    }
}