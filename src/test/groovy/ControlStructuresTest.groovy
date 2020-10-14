import org.junit.jupiter.api.Test

import java.awt.Point
import java.util.regex.Matcher

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

    @Test
    void "for loop"() {
        // foreach loop uses `in` keyword,
        // can use implicit type for loop variable
        for (i in 0..3) {
            assert i <= 3
        }

        // an iterable can be an object
//        def file = new File("some/path")
//        for (line in file) {
//
//        }

        // an iterable can be an object
        Matcher numbersMatcher = "fmad2834n2" =~ /\d/
        def matched = []
        for (String match in numbersMatcher) {
            matched << match
        }
        assert matched.join() == '28342'

        // will not iterate if iterable is null
        for (i in null) {
            assert false
        }

        // if an iterable cannot be iterated over (does not implement proper methods),
        // for loop will run with an wrapper iterable containing only one object
        def count = 0
        for (i in new Object()) {
            assert count == 0
            count++
        }
        assert count == 1
    }

    @Test
    void "closures having last expression evaluating to void will return null"() {
        assert {}() == null
    }
}
