package pl.edu.mimuw.tuto.common;

/**
 * Custom Optional class - Java 8's version requires Android API level 24.
 */
public class Optional<T> {
  private T value;
  private boolean hasValue;

  private Optional() {
    this.hasValue = false;
    this.value = null;
  }

  private Optional(T value) {
    this.hasValue = true;
    this.value = value;
  }

  public static <E> Optional<E> absent() {
    return new Optional<>();
  }

  public static <E> Optional<E> of(E value) {
    return new Optional<>(value);
  }

  public boolean isPresent() {
    return hasValue;
  }

  public boolean isAbsent() {
    return !hasValue;
  }

  public void set(T value) {
    this.value = value;
    this.hasValue = (value != null);
  }

  public T get() {
    if (!hasValue) {
      return null;
    }
    return this.value;
  }
}