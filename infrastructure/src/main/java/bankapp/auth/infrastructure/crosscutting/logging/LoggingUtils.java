package bankapp.auth.infrastructure.crosscutting.logging;

public class LoggingUtils {


    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        int atIndex = email.indexOf("@");
        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        if (localPart.length() <= 2) {
            return "***" + domain;
        }
        return localPart.substring(0, 2) + "***" + domain;
    }
}
