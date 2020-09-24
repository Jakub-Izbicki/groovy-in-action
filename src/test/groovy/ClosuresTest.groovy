import org.junit.jupiter.api.Test

import java.awt.font.ShapeGraphicAttribute
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

    @Test
    void "closures implement isCase"() {
        def odd = { it % 2 == 1 }

        assert 3 in odd
        assert [1, 2, 3, 4].grep(odd) == [1, 3]

        switch (3) {
            case odd:
                assert true
                break
            default:
                assert false
        }
    }

    @Test
    void "scope in closures, birthday context"() {
        def x = 0

        // local variable x is available to closure for read and write at declaration time
        // (in java 8 lambdas no write, only read).
        // birthday context - closure keeps reference to x when declared,
        // so at the time of execution, it can write to it
        def incrementX = { x++ }

        10.times(incrementX)
        assert x == 10
    }

    @Test
    void "closures scope in action"() {
        def julia = new Mother()

        // closure declaration time, refs to local variables are kept,
        // refs to free variables are kept according to resolve strategy (default is OWNER_FIRST)
        def closure = julia.birth('param')

        // closure execution time, refs are resolved
        def context = closure()

        // when resolving refs, closure first checks its local scope, then this, then owner, then delegate:
        // this - is always enclosing class
        // owner - same as this, unless closure is nested in another closure, then outer closure is the owner
        // delegate - same as owner, but can be changed programmatically (e.g. with method .with())
        assert closure.thisObject == julia
        assert closure.owner == julia
        assert closure.delegate == julia

        // when resolving refs, owner vs delegate conflicts may appear,
        // but it can be controlled with a strategy:
        assert closure.resolveStrategy == Closure.OWNER_FIRST

        assert context == [
                'prop', 'method', // resolved local variables
                'param', 'local'  // resolved free variables
        ]
    }

    class Mother {

        def prop = 'prop'

        def method() { 'method' }

        Closure birth(param) {
            def local = 'local'
            return { [prop, method(), param, local] }
        }
    }

    @Test
    void "dynamically changing of delegate in closure"() {
        def closure = { myString }

        // myString ref is resolved against delegates' scope
        // (first is looked for in local, then owner, only then in delegate is found)
        assert new MyClass1().with(closure) == "1"
        assert new MyClass2().with(closure) == "2"

        // will throw MissingPropertyException: No such property: myString2 for class: ClosuresTest
        // because closure's this, owner, and delegate is ClosuresTest class, and there is no myString
        assertThrows(MissingPropertyException, () -> closure())
    }

    class MyClass1 {
        String myString = "1"
    }

    class MyClass2 {
        String myString = "2"
    }

    @Test
    void "closure accumulator"() {
        def accumulator = acc(0)

        assert accumulator(0) == 0
        assert accumulator(1) == 1
        assert accumulator(1) == 2
        assert accumulator(2) == 4
    }

    // returned closure has a reference to local variable n,
    // and so consecutive calls to closure uce this variable to store accumulated number
    // during the lifetime of closure
    def acc(n) {
        return { n += it }
    }

    @Test
    void "returning from closure"() {
        // end return - last expression's result is returned, return keyword is optional
        assert [1, 2, 3].collect { it * 2 } == [1, 4, 6]
        assert [1, 2, 3].collect { return it * 2 } == [1, 4, 6]
    }

    @Test
    void "visitor pattern with closures"() {
        def drawing = new Drawing(shapes: [new Square(width: 1), new Circle(radius: 1)])

        def total = 0
        drawing.accept { total += it.area() }

        assert total == 4.141592653589793
    }

    class Drawing {

        List shapes

        def accept(Closure yield) {
            shapes.each { it.accept(yield) }
        }
    }

    class Shape {
        def accept(Closure yield) {
            yield(this)
        }
    }

    class Square extends Shape {
        def width

        def area() {
            width**2
        }
    }

    class Circle extends Shape {
        def radius

        def area() {
            Math.PI * radius**2
        }
    }
}
