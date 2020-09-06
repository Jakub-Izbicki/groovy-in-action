import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.junit.jupiter.api.Test

import java.awt.Point

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
    void "groovy types are not dynamic, they never change"() {
        int integer = 1
        integer = 2

        assertThrows(GroovyCastException.class, () -> {
            integer = new Object()
        })
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
}
