package trampoline;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class TrampolineTest {

    public static final Map<Integer, String> FIBONACCI_TESTS = Map.of(
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
    );

    @Test
    public void trampolineShouldPreventStackOverflow() {
        FIBONACCI_TESTS.forEach((n, expected) -> {
            assert fibonacci(n).equals(new BigInteger(expected));
        });
    }

    @Test
    public void useIterationToPreventStackOverflow() {
        FIBONACCI_TESTS.forEach((n, expected) -> {
            assert fibonacciIterative(n).equals(new BigInteger(expected));
        });
    }

    private static BigInteger fibonacci(int n) {
        return fibonacciTrampolined(n, new BigInteger("1"), new BigInteger("0")).result();
    }

    // fibonacci implemented as a tail-recursive method, so it can be used with a trampoline
    private static Trampoline<BigInteger> fibonacciTrampolined(int n, BigInteger current,
                                                               BigInteger previous) {
        if (n == 0) {
            return Trampoline.done(previous);
        }
        if (n == 1) {
            return Trampoline.done(current);
        }

        return Trampoline.more(() -> fibonacciTrampolined(n - 1, current.add(previous), current));
    }

    // implement iterative fibonacci just for comparison
    private static BigInteger fibonacciIterative(int nth) {
        return Stream
                .iterate(new BigInteger[]{new BigInteger("1"), new BigInteger("0")},
                        computed -> new BigInteger[]{computed[0].add(computed[1]), computed[0]})
                .limit(nth + 1)
                .map(a -> a[1])
                .max(Comparator.naturalOrder())
                .orElseThrow();
    }
}
