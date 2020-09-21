package trampoline;

import java.util.stream.Stream;

public interface Trampoline<T> {

  T get();

  default Trampoline<T> jump() {
    return this;
  }

  default T result() {
    return get();
  }

  default boolean complete() {
    return true;
  }

  static <U> Trampoline<U> done(final U result) {
    return () -> result;
  }

  static <W> Trampoline<W> more(Trampoline<Trampoline<W>> trampoline) {
    return new Trampoline<W>() {

      @Override
      public W get() {
        return trampoline(this);
      }

      @Override
      public Trampoline<W> jump() {
        return trampoline.result();
      }

      @Override
      public W result() {
        return get();
      }

      @Override
      public boolean complete() {
        return false;
      }

      private W trampoline(final Trampoline<W> trampoline) {
        return Stream.iterate(trampoline, Trampoline::jump)
            .filter(Trampoline::complete)
            .findFirst()
            .map(Trampoline::result)
            .orElseThrow();
      }
    };
  }
}
