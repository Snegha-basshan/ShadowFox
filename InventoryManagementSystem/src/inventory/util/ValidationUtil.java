package inventory.util;

public class ValidationUtil {

    public static boolean isNumeric(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}


