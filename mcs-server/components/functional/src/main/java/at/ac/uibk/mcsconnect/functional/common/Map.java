package at.ac.uibk.mcsconnect.functional.common;

import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static at.ac.uibk.mcsconnect.functional.common.List.list;

public class Map<T, U> {

  private static String NULL_KEY = "Map key can't be null";
  private static String NULL_VALUE = "Map value can't be null";

  /** Delegate */
  private final ConcurrentMap<T, U> map = new ConcurrentHashMap<>();

  public static <T, U> Map<T, U> empty() {
    return new Map<>();
  }

  public static <T, U> Map<T, U> add(Map<T, U> m, T t, U u) {
    Objects.requireNonNull(t, NULL_KEY);
    Objects.requireNonNull(u, NULL_VALUE);
    m.map.put(t, u);
    return m;
  }

  public Result<U> get(final T t) {
    return t == null
        ? Result.failure(NULL_KEY, new IllegalArgumentException(NULL_KEY))
        : Result.of(() -> this.map.get(t), String.format("Key %s not found in map", t));
  }

  public boolean containsKey(final T t) {
    return this.map.containsKey(t);
  }

  public Map<T, U> put(Tuple<T, U> e) {
    return put(e._1, e._2);
  }

  public Map<T, U> put(T t, U u) {
    return add(this, t, u);
  }

  public Map<T, U> replace(Tuple<T, U> e) {
    return put(e);
  }

  public Map<T, U> removeKey(T t) {
    this.map.remove(t);
    return this;
  }

  public List<T> keys() {
    return List.fromCollection(this.map.keySet());
  }

  public List<U> values() {
    return List.fromCollection(this.map.values());
  }

  public List<Tuple<T, U>> entries() {
    return List.fromCollection(this.map.entrySet().stream().map(e -> new Tuple<>(e.getKey(), e.getValue())).collect(Collectors.toCollection(LinkedList::new)));
  }

  public int size() {
    return this.map.size();
  }

  public void foreach(Consumer<Tuple<T, U>> c) {
    this.map.entrySet().forEach(e -> c.accept(new Tuple<>(e.getKey(), e.getValue())));
  }

  //public abstract <B> B foldLeft(B identity, Function<B, Function<A, B>> f);
  //public abstract <B> Tuple<B, List<A>> foldLeft(B identity, B acc, Function<B, Function<A, B>> f);

  public <B> B fold(B identity, Function<B, Function<Tuple<T, U>, B>> f) {
    return entries().foldLeft(identity, f);
  }

  public static <T, U> Map<T, U> fromJavaMap(java.util.Map<T, U> javaMap) {
    Map<T, U> output = new Map<T, U>();
    for (java.util.Map.Entry<T, U> entry: javaMap.entrySet()) {
      output.put(entry.getKey(), entry.getValue());
    }
    return output;
  }

}
