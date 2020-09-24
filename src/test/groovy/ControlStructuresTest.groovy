import org.junit.jupiter.api.Test

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
}
