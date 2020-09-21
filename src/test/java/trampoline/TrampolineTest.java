package trampoline;

import java.math.BigInteger;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TrampolineTest {

  @Test
  public void trampolineShouldPreventStackOverflow() {
    Map.of(
        0, "0",
        1, "1",
        2, "1",
        3, "2",
        4, "3",
        5, "5",
        10, "55",
        56, "225851433717",
        156, "178890334785183168257455287891792",
        195, "25299086886458645685589389182743678652930"
    ).forEach((n, expected) -> {
      assert fibonacci(n).equals(new BigInteger(expected));
    });
  }

  private static BigInteger fibonacci(int n) {
    return fibonacciTrampolined(n, new BigInteger("1"), new BigInteger("0")).result();
  }

  private static Trampoline<BigInteger> fibonacciTrampolined(int n, BigInteger acc1,
      BigInteger acc2) {
    if (n == 0) {
      return Trampoline.done(acc2);
    }
    if (n == 1) {
      return Trampoline.done(acc1);
    }

    return Trampoline.more(() -> fibonacciTrampolined(n - 1, acc1.add(acc2), acc1));
  }
}
