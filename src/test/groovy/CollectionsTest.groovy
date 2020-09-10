import org.junit.jupiter.api.Test

import static org.junit.Assert.assertThrows

class CollectionsTest {

    @Test
    void "ranges in groovy"() {
        assert (0..10).size() == 11
        assert (0..10).contains(10)
        assert (0..<10).size() == 10
        assert !(0..<10).contains(10)

        assert (0..3) == [0, 1, 2, 3]
        assert (3..0) == [3, 2, 1, 0]
        assert (0..3).containsWithinBounds(1)
        assert 0..1 instanceof Range
        assert new IntRange(0, 3) instanceof Range

        // reversed ranges
        assert (-2..0) == [-2, -1, 0]
        assert (1..-2) == [1, 0, -1, -2]

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

    @Test
    void "use custom datatype in range"() {
        def one = new Numeral(1)
        def ten = new Numeral(10)
        assert (one..ten).size() == 10

        assert new Numeral(5) in (one..ten)
    }

    // to be able to be used in ranges, datatype must implement next(), previous() and compareTo()
    // and implement Comparable
    static class Numeral implements Comparable {

        private final int num

        Numeral(int num) {
            this.num = num
        }

        Numeral next() {
            return new Numeral(num + 1)
        }

        Numeral previous() {
            return new Numeral(num - 1)
        }

        int compareTo(Object other) {
            return num <=> other.num
        }
    }

    @Test
    void "spaceship operator used to compare objects"() {
        assert 0 <=> 1 == -1
        assert 1 <=> 0 == 1
        assert 0 <=> 0 == 0

        assert 0 <=> null == 1
        assert null <=> 0 == -1
        assert null <=> null == 0

        // tricky when used with different types (second type is cast to first if possible)
        assert 53 <=> "5" == 0

        // can throw if cast not possible
        assertThrows(IllegalArgumentException.class,
                () -> 1 <=> "foo")
    }


    @Test
    void "lists in groovy"() {
        // by default groovy lists are of type ArrayList
        assert [] instanceof ArrayList
        assert [].size() == 0

        assert (1..3) instanceof Range
        assert (1..3).toList() instanceof ArrayList

        def list = [1, 2, 3]
        // array-like subscript operator (method is getAt())
        assert list[1] == 2
        // (method is putAt())
        list[1] = 100
        assert list[1] == 100

        // list implementation can be forced
        def linkedList = new LinkedList<String>()
        linkedList.add("foo")
        assert linkedList.size() == 1
        assert linkedList[0] == "foo"

        assert list.toList() == [1, 100, 3]
        assert "foo".toList() == ['f', 'o', 'o']
    }

    @Test
    void "subscript operator with ranges"() {
        def list = ['a', 'b', 'c', 'e', 'f', 'g']
        assert list[0..2] == ['a', 'b', 'c']
        assert list[1, 2] == ['b', 'c']

        list[0..2] = ['x1', 'x2', 'x3'] // switch elements in range with provided elements
        assert list == ['x1', 'x2', 'x3', 'e', 'f', 'g']
        list[3..5] = ['x4'] // list shrinks
        assert list == ['x1', 'x2', 'x3', 'x4']
        list[-1..-1] = [] // remove element in range from list, NOT the same as `list[-1] = []`
        assert list == ['x1', 'x2', 'x3']
        list[1..1] = ['y2'] // switch one, the same as `list[1] = 'y2'`
        assert list == ['x1', 'y2', 'x3']

        // when using reversed ranges with subscript operator on list
        // resulting list is also reversed!

        // reversed range
        assert [0, 1, 2, 3, 4, 5][3..1] == [3, 2, 1]
        // vs normal range
        assert [0, 1, 2, 3, 4, 5][1..3] == [1, 2, 3]

        // when using positive on the left, and negative on the right, ranges in subscript no longer behave as ranges
        // they are not reversed ranges, instead they are just indices of elements in list:
        // e.g [1..-2] will get elements from 2nd to 2nd but least:
        assert [0, 1, 2, 3, 4, 5][1..-2] == [1, 2, 3, 4] // tricky case !!!
        assert [0, 1, 2, 3, 4, 5][-2..1] == [1, 2, 3, 4]
    }
}
