import org.junit.jupiter.api.Test

import java.awt.Point

class ControlStructuresTest {

    @Test
    void "groovy truth"() {
        assert true
        assert !false

        // when matcher has a match
        assert ('abc' =~ /[a-z]/)
        assert !('abc' =~ /[1-9]/)

        // when list is non-empty
        assert [1]
        assert ![]

        // when map is non-empty
        assert [a: 1]
        assert ![:]

        // when iterator has next element
        def iterator = [1].iterator()
        assert iterator
        iterator.next()
        assert !iterator

        // when strings are non-empty
        assert 'a'
        assert !''

        // when numbers are non-zero
        assert 1
        assert 1.1
        assert 1G
        assert !0

        // when object refs are non-null
        assert new Object()
        assert !null

        // can be overwritten with asBoolean()
        assert !new Falsy()
    }

    class Falsy {
        def asBoolean() {
            false
        }
    }

    @Test
    void "caveats with assignment operator"() {
        def x = 1

        // will return assignment value
        if (x = 1) {
            assert true
        }

        // will return assignment value
        if (x = 0) {
            assert false
        }

        // infinite loop
        while (x = 1) {
            assert true
            break
        }
    }

    @Test
    void "elvis operator"() {
        def someVal = "abc"
        def defaultVal = "default"

        // note: elvis operator allows to evaluate argument only once
        assert (someVal ?: defaultVal) == "abc"
        assert (null ?: defaultVal) == "default"
    }

    @Test
    void "switch statement with the use of classifiers"() {
        // matching cases work because objects implement isCase():
        switch (10) {
            case 0:
                assert false
                break
            case 0..9: // range's isCase() is a.contains(b)
                assert false
                break
            case [8, 9, 11]: // list's isCase() is a.contains(b)
                assert false
                break
            case Float: // class's isCase() is a.isInstance(b)
                assert false
                break
            case { it % 2 != 0 }: // closure's isCase() is a.call(b)
                assert false
                break
            case "abc": // string's isCase() is (a == null && b == null) || a.equals(b)
                assert false
                break
            case new Point(1, 2): // object's default isCase() is a.equals(b)
                assert false
                break
            case ~/../: // pattern's isCase() is a.matcher(b.toString()).matches()
                assert true
                break
            default:
                assert false
        }

        // isCase is also user in collection's grep() method, with a classifier as its argument
        // and with `in` operator
    }
}
