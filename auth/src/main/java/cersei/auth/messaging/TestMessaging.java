package cersei.auth.messaging;

public interface TestMessaging {
    void successLogin(String successMessage);
    void failureLogin(String failMessage);
}
