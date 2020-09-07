import groovy.transform.Immutable
import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.junit.jupiter.api.Test

import java.awt.Point
import java.util.regex.Matcher

import static org.junit.Assert.assertThrows

class DatatypesTest {

    @Test
    void "calling methods on seemingly primitive types (but everything is an object)"() {
        // 1
        assert (1 + 1 * 2).toString() == "3"

        // 2
        int integer = 1
        assert integer.toString() == "1"

        // 3
        assert "aabbcc" - "a" == "abbcc"
        assert "aabbcc" - "a" - "a" == "bbcc"
        assert "abab" - "a" == "bab"
    }

    @Test
    void "numeric literals"() {
        assert 1
                instanceof Integer
        assert 1.2
                instanceof BigDecimal
        assert 1.2g
                instanceof BigDecimal
        assert 1.2e2
                instanceof BigDecimal
        assert 1L
                instanceof Long
        assert 1f
                instanceof Float
        assert 1.2f
                instanceof Float
        assert 1d
                instanceof Double
        assert 1.2d
                instanceof Double
        assert 1g
                instanceof BigInteger
    }

    @Test
    void "autoboxing when calling java methods"() {
        int integer = 67
        assert integer instanceof Integer

        //method accepts and returns int primitive in java, in groovy it's autoboxed
        assert "ABCDE".indexOf(integer) == 2
        assert "ABCDE".indexOf(integer) instanceof Integer
    }

    @Test
    void "variable declarations using optional typing"() {
        def a = 1
        int b = 1
        Integer c = 1

        assert a instanceof Integer && b instanceof Integer && c instanceof Integer
    }

    @Test
    void "groovy enforces java's types system during runtime, not compile time"() {

        assertThrows(GroovyCastException.class, () -> {
            // test will compile and start, but will throw at below line
            int integer = new Object()
        })
    }

    @Test
    void "in groovy, strict types are not dynamic, they never change"() {
        int integer = 1
        assert integer instanceof Integer

        assertThrows(GroovyCastException.class, () -> {
            integer = new Object()
        })
    }

    @Test
    void "unless a type is specified using 'def'"() {
        def dynamic = 1
        assert dynamic instanceof Integer

        dynamic = new String("Hi")
        assert dynamic instanceof String
    }

    @Test
    void "groovy auto casts when def is used"() {
        def i = getInt()
        assert i instanceof Object
        assert i instanceof Integer
        assert i + 1 == 2
    }

    Integer getInt() {
        return 1;
    }

    @Test
    void "auto casting lists and maps to arbitrary classes when type is specified"() {
        def point = new Point(0, 0)
        assert point instanceof Point

        def noPoint = [0, 0]
        assert !(noPoint instanceof Point)
        assert noPoint instanceof List

        // list or map of constructor arguments
        Point pointFromList = [0, 0]
        assert pointFromList instanceof Point
        Point pointFromMap = [x: 0, y: 0]
        assert pointFromMap instanceof Point
    }

    @Test
    void "+ and == operator implementing in custom class"() {
        def moneyUsd1 = new Money(1, 'USD')
        def moneyUsd2 = new Money(1, 'USD')
        def moneyPln = new Money(1, 'PLN')

        assert moneyUsd1 == moneyUsd1
        assert moneyUsd1 == moneyUsd2
        assert moneyUsd1 != moneyPln
        assert moneyUsd1 + moneyUsd2 == new Money(2, 'USD')
        assert moneyUsd1 + 1 == new Money(2, 'USD')
    }

    @Immutable
    static class Money {

        int amount
        String currency

        Money plus(Money other) {
            if (currency != other.currency) {
                throw IllegalArgumentException(" Cannot add different currencies")
            }

            return new Money(amount + other.amount, currency);
        }

        Money plus(Integer num) {
            return new Money(amount + num, currency);
        }
    }

    @Test
    void "coercion - promoting arguments to general or specific type"() {
        assert 1 + 1.0 instanceof BigDecimal
        assert 1.0 + 1 instanceof BigDecimal
        assert 1 + 1.0f instanceof Double
        assert 1.0f + 1 instanceof Double
    }

    @Test
    void "string literals"() {
        assert 'foo' // java-like

        def name = 'foo'
        assert "$name bar" == 'foo bar'

        assert """multiline $name""" == '''multiline foo'''
        assert "foo\nfoo" != /foo\nfoo/

        assert "'Hello there'" != '"Hello there"'
    }

    @Test
    void "character literals"() {
        assert 'x' instanceof String
        assert 'x'.toCharacter() instanceof Character

        assert "x" instanceof String
        def foo = "foo"
        assert "x$foo" instanceof GString
    }

    @Test
    void "gstrings"() {
        def names = [me: 'Tarzan', you: 'Jane']
        def quote = "Me: $names.me, you: $names.you"
        assert quote == "Me: Tarzan, you: Jane"

        assert quote.strings[0] == "Me: "
        assert quote.strings[1] == ", you: "
        assert quote.values == ['Tarzan', 'Jane']
        assert quote.valueCount == 2

        assert "Me: ${names.me.toLowerCase()}" == "Me: tarzan" // braces denote closure

        assert "got 1\$" == 'got 1$'
    }

    @Test
    void "groovy misc string operations"() {
        def greeting = "Hello Groovy"

        assert greeting[0] == 'H'
        assert greeting[-1] == 'y'
        assert greeting[6..11] == 'Groovy'

        assert 'x'.center(3) == ' x '

        def padded = ['1', '10', '100', '1000'].collect { it.padLeft(5) }
        assert padded == ['    1', '   10', '  100', ' 1000']
    }

    @Test
    void "using left shift and assign operator with strings"() {
        def greet = "Hello"
        assert greet instanceof String
        greet <<= " Groovy" // greet def variable changes type to StringBuilder
        assert greet instanceof StringBuffer

        String greet2 = "Hi"
        assert greet2 instanceof String
        greet2 <<= " Groovy" // greet has specified String type, so it does not change dynamically
        assert greet2 instanceof String
    }

    @Test
    void "and StringBuffer"() {
        // no changing a string in place, because strings are immutable in Java and Groovy,
        // but we can do it with a use of StringBuffer (thread safe) or StringBuilder
        def greet = "Hello"
        greet <<= " Groovy"
        greet << "!"
        assert greet.toString() == "Hello Groovy!"

        greet[1..4] = "i"
        assert greet.toString() == "Hi Groovy!"

        assert greet.toString() - "Hi " == "Groovy!"
    }

    @Test
    void "escaping slashes (\\) in slashy strings"() {
        assert "\\d" == /\d/
    }

    @Test
    void "regex operators in groovy"() {
        def quote = "Me Tarzan - you Jane."

        // find operator
        def matcher = quote =~ /\w - \w/ // find occurrences of dash surrounded by words and spaces

        assert matcher
        assert matcher instanceof Matcher
        assert matcher.size() == 1

        // match operator
        def match = quote ==~ /^Me(.*)Jane\.$/
        assert match
        assert match instanceof Boolean

        // slashy gstrings
        def WORD = /\w+/
        assert quote ==~ /^($WORD( -)? $WORD)*\.$/

        assert quote.split(/ /).size() == 5 // using slashy string in split method
    }

    @Test
    void "use find operator with closures"() {
        def quote = "Me Tarzan - you Jane."

        def found = ''
        (quote =~ /a/).each { found += it }
        assert found.size() == 3

        assert quote.replaceAll(/ /) { '_' } == "Me_Tarzan_-_you_Jane."
    }

    @Test
    void "matcher contains list of matched groups"() {
        def matcher = 'a b c' =~ /\S/ // non-whitespace chars
        assert matcher[1] == 'b'
        assert matcher[1..2] == ['b', 'c']
    }

    @Test
    void "parallel assignment"() {
        def (one, two, three) = 'a b c' =~ /\S/ // non-whitespace chars
        assert one == 'a'
        assert two == 'b'
        assert three == 'c'
    }
}
