import org.junit.jupiter.api.Test

import java.util.stream.Collectors

import static org.junit.Assert.assertThrows

class ClosuresTest {

    @Test
    void "closures"() {
        (0..3).each { num -> assert num instanceof Integer }

        // when closure accepting single argument, 'it' ca be used
        (0..3).each { assert it instanceof Integer }

        // when closure is declared as a last parameter of the method, it can be
        // written without parenthesis, instead of (but still valid):
        (0..3).each({ assert it instanceof Integer })

        def printer = { line -> println line } // == new Closure() {...}
        assert printer instanceof Closure

        // closure as a return object from method
        // vs java 8 lambdas, were only functional interface instances can be returned
        assert getPrinter() instanceof Closure
    }

    def Closure getPrinter() {
        return { println it }
    }

    @Test
    void "method closures"() {
        def limiter6 = new StringLimiter(limit: 6) // GroovyBean constructor

        def words = ['long string', 'medium', 'short', 'tiny']

        // .& method closure operator
        // used to extract instance method as a closure
        Closure upTo6 = limiter6.&isUpTo

        assert 'medium' == words.find(upTo6) // note parenthesis instead of curly braces
        assert 'short' == words.find(new StringLimiter(limit: 5).&isUpTo)
    }

    class StringLimiter {
        Integer limit

        boolean isUpTo(String val) {
            return val.size() <= limit
        }
    }

    @Test
    void "multimethods teaser"() {
        // only one method closure extracted
        def getSize = new MultiMethodSample().&count

        // runtime overload resolution
        // different implementations are used for different arguments
        assert 5 == getSize('acbde')
        assert 3 == getSize(['a', 'b', 'c'])
        assert 3 == getSize(1, 2)
    }

    class MultiMethodSample {

        int count(String val) {
            return val.size()
        }

        int count(List list) {
            return list.size()
        }

        int count(int a, int b) {
            return a + b
        }
    }

    @Test
    void "calling closures"() {
        def slow = benchmark(1000) { (int) it / 2 }
        def fast = benchmark(1000) { it.intdiv(2) }
        assert fast * 2 < slow
    }

    def benchmark(int repeat, operation) { // argument types are optional
        def start = System.nanoTime()

        repeat.times { operation(it) }

        return System.nanoTime() - start
    }

    @Test
    void "default args in closure"() {
        def add = { a, b = 5 -> return a + b }
        assert add(1, 2) == 3
        assert add(1) == 6
    }

    @Test
    void "closure params count"() {
        // technique used e.g. in Map.each to pass Entity or key and value to closure
        assert paramsCount { one -> } == 1
        assert paramsCount { one, two -> } == 2

        assert params { String s -> } == [String]
        assert params { String s, Integer i -> } == [String, Integer]
    }

    int paramsCount(Closure c) {
        return c.getMaximumNumberOfParameters()
    }

    List<Class> params(Closure c) {
        return c.getParameterTypes()
    }

    @Test
    void "currying a closure"() {
        // a two arguments closure
        def mult = { a, b -> a * b }
        // a one argument closure
        def twoTimes = mult.curry(2) // replacing one of the params with fixed value

        assert twoTimes(3) == 6
    }

    @Test
    void "currying used in functional programming"() {
        // a closure accepting other closures as parameters
        def configurator = { format, filter, line ->
            filter(line) ? format(line) : null
        }

        // a closure accepting other closures as parameters
        def appender = { config, append, line ->
            def out = config(line)
            if (out) append(out)
        }

        // defining closures which will be supplied to above closures
        def dateFormatter = { line -> "${new Date()}: $line" }
        def debugFilter = { line -> line.contains('debug') }
        def trueFalseAppender = { line -> true } // for the sake of assertion

        // currying closures with other closures
        def logConfig = configurator.curry(dateFormatter, debugFilter)
        def log = appender.curry(logConfig, trueFalseAppender)

        assert log("some debug message")
        assert !log("not logged message")
    }

    @Test
    void "closure composition"() {
        def plusTwo = { it + 2 }
        def square = { it * 2 }
        def half = { it / 2 }

        // closure composition with the use of left or rightShift operators
        // return value of the first in chain will be passed to second and so on
        assert (plusTwo >> square)(2) == square(plusTwo(2))

        assert (plusTwo >> square >> half)(2) == half(square(plusTwo(2)))
    }

    @Test
    void "memoize a closure"() {
        def fib
        // closure can call itself recursively
        fib = { it < 2 ? 1 : fib(it - 1) + fib(it - 2) }

        // creates a variant of a closure with a caching mechanism
        // return values for all different call arguments will be cached, and for subsequent
        // call with these arguments, cached values will be returned
        fib = fib.memoize()
        assert fib(40) == 165_580_141

        // other variands of memoize

        // will keep at least n cache entries, when exceeding n, entries are rotating
        // using LRU strategy and these out of n, are eligible for gc
        fib.memoizeAtLeast(1)

        // will keep at most n cache entries, when reaching max, entries are rotating
        // using LRU strategy
        fib.memoizeAtMost(1)

        fib.memoizeBetween(1, 2) // combination of above
    }

    @Test
    void "using trampoline in closure to avoid stack overflow (tail recursion workaround in jvm)"() {
        def last
        // trampolined closure will not call recursively, instead it will return another closure
        // which trampoline will call in another iteration
        // until a non-closure value is returned from trampoline
        // effectively converts recursion into iteration
        last = { it.size() == 1 ? it.head() : last.trampoline(it.tail()) }.trampoline()

        assert last(0..10_000) == 10_000
    }
}
