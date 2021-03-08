package at.ac.uibk.mcsconnect.functional.validation;

import at.ac.uibk.mcsconnect.functional.common.Result;

import java.util.function.Function;

public final class Assertion {

  private Assertion() {
  }

  public static <T> Result<T> assertCondition(T value, Function<T, Boolean> f) {
    return assertCondition(value, f, "Assertion error: condition should evaluate to true");
  }

  public static <T> Result<T> assertCondition(T value, Function<T, Boolean> f, String errMsg) {
    return f.apply(value)
        ? Result.success(value)
        : Result.failure(errMsg, new IllegalStateException(errMsg));
  }

  public static Result<Boolean> assertTrue(boolean condition) {
    return assertTrue(condition, "Assertion error: condition should be true");
  }

  public static Result<Boolean> assertTrue(boolean condition, String errMsg) {
    return assertCondition(condition, x -> x, errMsg);
  }

  public static Result<Boolean> assertFalse(boolean condition) {
    return assertFalse(condition, "Assertion error: condition should be false");
  }

  public static Result<Boolean> assertFalse(boolean condition, String errMsg) {
    return assertCondition(condition, x -> !x, errMsg);
  }

  public static <T> Result<T> assertNotNull(T t) {
    return assertNotNull(t, "Assertion error: object should not be null");
  }

  public static <T> Result<T> assertNotNull(T t, String errMsg) {
    return assertCondition(t, x -> x != null, errMsg);
  }

  public static Result<Integer> assertPositive(int value) {
    return assertPositive(value, String.format("Assertion error: value %s must be positive", value));
  }

  public static Result<Integer> assertPositive(int value, String errMsg) {
    return assertCondition(value, x -> x > 0, errMsg);
  }

  public static Result<Integer> assertInRange(int value, int min, int max) {
    return assertCondition(value, x -> x >= min && x < max, String.format("Assertion error: value %s should be between %s and %s (exclusive)", value, min, max));
  }

  public static Result<Integer> assertPositiveOrZero(int value) {
    return assertPositiveOrZero(value, String.format("Assertion error: value %s must not be negative", 0));
  }

  public static Result<Integer> assertPositiveOrZero(int value, String errMsg) {
    return assertCondition(value, x -> x >= 0, errMsg);
  }

  public static <A> void assertType(A element, Class<?> clazz) {
    assertType(element, clazz, String.format("Wrong type: %s, expected: %s", element.getClass().getName(), clazz.getName()));
  }

  public static <A> Result<A> assertType(A element, Class<?> clazz, String errMsg) {
    return assertCondition(element, e -> e.getClass().equals(clazz), errMsg);
  }
}
