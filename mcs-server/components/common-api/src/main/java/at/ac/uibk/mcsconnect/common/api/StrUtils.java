package at.ac.uibk.mcsconnect.common.api;

import at.ac.uibk.mcsconnect.functional.common.Result;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class StrUtils {

    public static Function<StringBuilder, String> stringBuilderToHexString = (StringBuilder sb) ->
            sb.toString().codePoints().mapToObj(c -> String.format("0x%04x", c)).collect(Collectors.joining(", "));

    public static Function<String, String> stringToHexString = s ->
            s.codePoints().mapToObj(c -> String.format("0x%04x", c)).collect(Collectors.joining(", "));

    public static Function<String, Function<Pattern, Boolean>> stringContainsPattern = s -> p -> p.matcher(s).find();

    private static final Map<Character,String> charMap;
    static {
        charMap = new HashMap<>();
        charMap.put((char) 0x1B, "ESC");
        charMap.put((char) 0x0A, "LF");
        charMap.put((char) 0x0D, "CR");
    }

    private static final String CRLF = "" + (char) 0x0D + (char) 0x0A;
    private static final String CR = "" + (char) 0x0D;
    private static final String ESC = "" + (char) 0x1B;
    private static final String LF = "" + (char) 0x0A;
    private static final String LFLF = "" + (char) 0x0A + (char) 0x0A;
    private static final String A_CHARS = "[^\\n]"; // any char except newline (because newline marks end)


    public static String swapNonPrintable(String string) {

        return string
                .chars()
                .mapToObj(c -> (char) c)
                .map(c -> charMap.getOrDefault(c, c.toString()))
                .collect(Collectors.joining());
    }

    public static Result<Integer> parseAsInteger(String value) {
        Result<String> rString = Result.of(value);
        return rString.flatMap(x -> {
            try {
                return Result.success(Integer.parseInt(x));
            } catch (NumberFormatException e) {
                return Result.failure(String.format("Invalid value wile parsing %s", x));
            }
        });
    }

    public static Result<Long> parseAsLong(String value) {
        Result<String> rString = Result.of(value);
        return rString.flatMap(x -> {
            try {
                return Result.success(Long.parseLong(x));
            } catch (NumberFormatException e) {
                return Result.failure(String.format("Invalid value wile parsing %s", x));
            }
        });
    }

    public static Result<LocalDateTime> parseAsLocalDateTime(String str) {
        Result<String> rStopTime = Result.of(str);
        return rStopTime.flatMap(x -> {
            try {
                return Result.success(LocalDateTime.parse(x));
            } catch (NullPointerException | NumberFormatException n) {
                return Result.failure(String.format("Invalid value while parsing value: %s", str));
            }
        });
    }

    public static <T extends Enum<?>> Result<T> parseAsEnum(final String parameterName,
                                                            final Class<T> enumClass) {

        Function<String, Result<T>> f = t -> {
            try {
                T constant = enumClass.getEnumConstants()[0]; // trick ...
                @SuppressWarnings("unchecked")
                T value = (T) Enum.valueOf(constant.getClass(), t);// ...to allow T here
                return Result.success(value);
            } catch (Exception e) {
                return Result.failure(String.format("Error parsing StrUtil. Could not convert %s to %s", parameterName, enumClass.getSimpleName()));
            }
        };
        return getAsType(f, parameterName);
    }

    public static <T> Result<T> getAsType(final Function<String, Result<T>> function, final String value) {
        Result<String> rString = Result.of(value);
        return rString.flatMap(s -> {
            try {
                return function.apply(s);
            } catch (Exception e) {
                return Result.failure(String.format("Error parsing value %s" ,e));
            }
        });
    }

    public static Function<Integer, Function<String, String>> shortenStringToLength =
            len -> str -> str.length() > len ? str.substring(0, len) : str;

}