import groovy.transform.Immutable
import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.junit.jupiter.api.Test

import java.awt.Point
import java.util.regex.Matcher
import java.util.regex.Pattern

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
        assert 1g
                instanceof BigInteger
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
    // adds its own implementations of equals and hashCode
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
    void "equality and comparing objects"() {
        def point1 = new Point(1, 1)
        def point2 = new Point(1, 1)

        // Point does not implement comparable, so Points' equals() is called
        assert point1 == point2
        // will throw cuz Point doesnt implement Comparable
        assertThrows(IllegalArgumentException.class, () -> point1 > point2)

        assert point1 === point1 // java's identity operator == (since groovy 3.0)
        assert point1.is(point1)
        assert point1 !== point2 // java's identity operator == (since groovy 3.0)
        assert !point1.is(point2)

        // will call Boolean.compareTo instead of equals(), cuz Boolean implements Comparable
        assert true != false

        // won't attempt to call any method on null (equals nor compareTo), because actually,
        // before comparing, groovy always calls other method first:
        // ScriptBytecodeAdapter.compareEqual(), which checks for nulls, and only then calls:
        // DefaultTypeTransformation.compareTo(), which check if types are compatible
        // (otherwise returns -1), and only then calls equals(), or compareTo()
        assert null != 1
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
        greet <<= " Groovy" // greet def variable changes type to StringBuffer
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

        // full match operator
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

    @Test
    void "if pattern contains parenthesis, matchers' matches are lists of groups, instead of strings"() {
        def matcher = 'a:1 b:2 c:3' =~ /([a-z]):([0-9])/
        assert matcher.size() == 3
        assert matcher[0].size() == 3
        assert matcher[0] == ['a:1', 'a', '1']
        assert matcher[0][1] == 'a'

        // if closure defined with multiple params, they are populated with matcher's groups
        // (amount of groups must match params count, or else will throw)
        matcher.each { full, firstGroup, secondGroup ->
            assert full.size() == 3
            assert firstGroup.size() == 1
            assert secondGroup.size() == 1
        }
    }

    @Test
    void "pattern operator"() {
        // perform costly finite-state machine creation only once with ~ operator
        def fourLetterWord = ~/\b\w{4}\b/
        assert fourLetterWord instanceof Pattern

        def matcher = fourLetterWord.matcher("Me Tarzan - you Jane") // equivalent of =~ operator
        assert matcher.size() == 1

        assert fourLetterWord.isCase("Jane") // equivalent of ==~ operator
        assert "Jane" in fourLetterWord

        switch ("Jane") {
            case fourLetterWord:
                assert true
                break
            default:
                assert false
        }
    }

    @Test
    void "coercion with numeric operators"() {
        // if any is Float or Double -> Double
        assert 1d + 1f instanceof Double
        assert 1f + 1f instanceof Double

        // otherwise, if any is BigDecimal -> BigDecimal
        assert 1.0G + 1 instanceof BigDecimal

        // otherwise, if any is BigInteger -> BigInteger
        assert 1G + 1 instanceof BigInteger

        // otherwise, if any is Long -> Long
        assert 1L + 1 instanceof Long

        // otherwise, result is Integer
        assert (Byte) 1 + (Byte) 1 instanceof Integer
    }

    @Test
    void "numeric operators caveats"() {
        // for division, if any is Float or Double, result is Double
        assert 1f / 2 instanceof Double
        // otherwise, result is BigDecimal
        assert 1 / 2 instanceof BigDecimal
        // Integer as a division result only by casting
        assert (Integer) (1 / 2) instanceof Integer
        // or with method
        assert 1.intdiv(2) instanceof Integer

        //non-power operators do not promote result type (will overflow)
        assert Integer.MAX_VALUE + 1 instanceof Integer
        assert Integer.MAX_VALUE + 1 == Integer.MIN_VALUE

        // power operator promotes if necessary
        assert 2**30 instanceof Integer
        assert 2**31 instanceof BigInteger
        assert 2**31 == Integer.MAX_VALUE + 1G
        assert 2**3.5 instanceof Double

        // equals operator coerces to a more general type, e.g. Float to BigDecimal:
        assert 1.5G == 1.5F
        // 1.1 cannot be properly represented as float - after conversion to BigDecimal, is equals 1.100000023841858G
        assert 1.1G != 1.1F
    }

    @Test
    void "GDK methods for numbers"() {
        assert 2 == 2.5.toInteger() // conversion method
        assert 2 == 2.5 as Integer // coercion
        assert 2 == (int) 2.5
        assert 4 == 4.5.trunc()

        assert '5.34315'.isNumber()
        assert 5 == '5'.toInteger()
        assert 5 == '5' as Integer
        assert 53 == (int) '5' // casts string of size 1 to its unicode value !!
        assert '5 times' == 5 + " times"

        def times = []
        5.times {
            times << "x"
        }
        assert times.size() == 5

        def upTo = []
        1.upto(3) {
            upTo << it
        }
        assert upTo == [1, 2, 3]

        def steps = []
        1.step(3, 0.5) {
            steps << it
        }
        assert steps == [1, 1.5, 2.0, 2.5]
    }
}
