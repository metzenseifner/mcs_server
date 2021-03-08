package at.ac.uibk.mcsconnect.functional.common;

public interface Effect<T> {
  void apply(T t);
}