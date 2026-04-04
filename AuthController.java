public class AuthController {
    private final UserRepository userRepository;
    private User currentUser;
    public AuthController(UserRepository userRepository) { this.userRepository = userRepository; }
    public User login(String email, String password) {
        User user = userRepository.getUserByEmail(email);
        if (user == null || user.getAccount() == null) { return null; }
        if (!user.getAccount().verifyPassword(password)) { return null; }
        currentUser = user;
        return user;
    }
    public User register(String name, String email, String password) {
        Msg result = userRepository.createAccount(name, email, password);
        if (!"Account created successfully.".equals(result.getMsg())) { return null; }
        return userRepository.getUserByEmail(email);
    }
    public void logout() { currentUser = null; }
    public User getCurrentUser() { return currentUser; }
}