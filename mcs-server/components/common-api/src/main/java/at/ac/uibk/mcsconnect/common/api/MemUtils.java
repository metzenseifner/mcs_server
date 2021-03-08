package at.ac.uibk.mcsconnect.common.api;

public class MemUtils {
    public static <T> T nullCheck(T object) throws RuntimeException {
        if (object == null) throw new RuntimeException(String.format("%s may not be null", object.getClass().getSimpleName()));
        return object;
    }
}
