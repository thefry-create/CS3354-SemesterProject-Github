public class LoginGUI {
    private final AuthController authController;
    public LoginGUI(AuthController authController) { this.authController = authController; }
    public void submitLogin() {
        // Hook real UI form values into authController.login(...)
    }
    public void clickCreateAccount() { displayRegistrationForm(); }
    public void displayRegistrationForm() {}
    public void submitRegistration(String name, String email, String password) {
        User user = authController.register(name, email, password);
        if (user == null) {
            displayValidationErrors(new Msg("This email is already registered with an existing account."));
        } else {
            showDashboard();
        }
    }
    public void displayValidationErrors(Msg msg) { System.out.println(msg.getMsg()); }
    public void showDashboard() { System.out.println("Dashboard loaded."); }
}