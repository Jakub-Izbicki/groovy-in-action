import org.junit.jupiter.api.Test

class CollectionsTest {

    @Test
    void "ranges in groovy"() {
        assert (0..10).size() == 11
        assert (0..10).contains(10)
        assert !(0..<10).contains(10)
        assert (0..<10).size() == 10

        assert (0..3) == [0, 1, 2, 3]
        assert (3..0) == [3, 2, 1, 0]
        assert (0..3).containsWithinBounds(1)
        assert 0..1 instanceof Range
        assert new IntRange(0, 3) instanceof Range

        assert (0.0..1.0).size() == 2
        assert (0.0..1.0).contains(1.0)
        assert (0.0..1.0).containsWithinBounds(0.2)
        assert ('a'..'c').contains('b')

        assert !(0.1..10.4).contains(10.4)
        assert (0.1..10.4).contains(10.1)

        def els = []
        for (el in 1..3) {
            els << el
        }
        assert els.size() == 3

        def els2 = []
        (1..3).each { els2 << it }
        assert els2.size() == 3
    }

    @Test
    void "ranges implement isCase method"() {
        assert 5 in 0..10
        assert (0..10).isCase(5)

        def age = 35
        switch (age) {
            case 0..20:
                assert false
                break
            case 21..40:
                assert true
                break
            case 41..100:
                assert false
                break
        }

        [10, 23, 34, 44, 51, 76].grep(30..50) == [34, 44]
    }
}
