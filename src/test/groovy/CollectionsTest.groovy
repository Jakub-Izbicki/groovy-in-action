import org.junit.jupiter.api.Test

import java.util.stream.Collectors

import static org.junit.Assert.assertArrayEquals
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
        assert (-2..1) == [-2, -1, 0, 1]
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

    // to be able to be used in ranges, datatype must implement next(), previous()
    // and implement Comparable (compareTo())
    static class Numeral implements Comparable {

        private final int num

        Numeral(int num) {
            this.num = num
        }

        Numeral next() { // ++ operator
            return new Numeral(num + 1)
        }

        Numeral previous() { // -- operator
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
        list[-1..-1] = [] // remove element in range from list, NOT the same as `list[-1] = []`, duh
        assert list == ['x1', 'x2', 'x3']
        list[1..1] = ['y2'] // switch one, the same as `list[1] = 'y2'`
        assert list == ['x1', 'y2', 'x3']

        // when using reversed ranges with subscript operator on list
        // resulting list is also reversed!

        // reversed range
        assert [0, 1, 2, 3, 4, 5][3..1] == [3, 2, 1]
        // vs normal range
        assert [0, 1, 2, 3, 4, 5][1..3] == [1, 2, 3]

        // mixing positive with negative numbers shows that ranges in subscript dont behave as normal ranges
        // they are not typical ranges, but they are just indices of elements in list.
        // e.g [1..-2] will get elements of indices [1, 2, 3, 4], instead of [1, 0, -1, -2] here:
        assert [0, 1, 2, 3, 4, 5][1..-2] == [1, 2, 3, 4]
        // or [-2..1] will get indices [4 (-2), 3, 2, 1], instead of [-2, -1, 0, 1]
        assert [0, 1, 2, 3, 4, 5][-2..1] == [4, 3, 2, 1]

        // but this behaviour can be overridden by forcing range to list:
        assert [0, 1, 2, 3, 4, 5][(1..-2).toList()] == [1, 0, 5, 4]
    }

    @Test
    void "removing and adding in lists"() {
        assert [1, [2], [3, 4]].flatten() == [1, 2, 3, 4]
        // gets elements present in both (intersection)
        assert [1, 2, 3].intersect([2, 3, 4]) == [2, 3]
        // checks if intersection is empty
        assert [1, 2, 3].disjoint([4, 5, 6])

        assert [1, 2, 3].pop() == 1 // pops 1st element (since groovy 2.5)}

        assert [1, 2].reverse() == [2, 1]

        assert [2, 3, 1].sort() == [1, 2, 3]
        def list = [[1], [0, 2, 8], [4, 2]]
        assert list.sort { a, b -> a[0] <=> b[0] } == [[0, 2, 8], [1], [4, 2]]
        assert list.sort { el -> el.size() } == [[1], [4, 2], [0, 2, 8]]

        list = ['a', 'b', 'c']
        list.remove(2)
        list.remove('b')
        assert list == ['a']

        list << 'd' // leftShift() operator
        assert list == ['a', 'd']

        assert [1, 2, 3].collect { item -> item * 2 } == [2, 4, 6] // java's map()
        assert [1, 2, 3].findAll { item -> item % 2 != 0 } == [1, 3] // java's filter()

        //sets and removing nulls
        assert new HashSet<>([1, 2, 2]).toList() == [1, 2]
        assert [1, 2, 2].unique() == [1, 2]
        assert [1, 2, 2].unique() instanceof ArrayList

        assert [1, null, 2].findAll { it != null } == [1, 2]
        assert [1, null, 2].grep { it } == [1, 2] // groovy truth
    }

    @Test
    void "query, iteration and reduce"() {
        def list = [1, 2, 4, 3, 4]
        assert list.first() == list.head()
        assert list.last() != list.tail()
        assert list.tail() == [2, 4, 3, 4]
        assert list.count(4) == 2
        assert list.max() == 4
        assert list.join('-') == "1-2-4-3-4"

        assert list.find { item -> item % 2 == 0 } == 2 // finds first
        assert list.every { item -> item < 5 }
        assert list.any { item -> item == 3 }

        assert list.each { item -> assert item in list }
        assert list.reverseEach { item -> assert item in list }
        assert list.eachWithIndex { item, i -> assert i instanceof Integer }

        // java's reduce()
        // in this case uses list's head as init acc
        assert list.inject { acc, item -> acc + item } == 1 + 2 + 4 + 3 + 4

        assert !([1, 2].asSynchronized() instanceof ArrayList)
        assertThrows(UnsupportedOperationException.class, () -> [1, 2].asImmutable() << 3)
    }

    @Test
    void "lists in action - quickSort implementation"() {
        assert quickSort([]) == []
        assert quickSort([1]) == [1]
        assert quickSort([1, 1]) == [1, 1]
        assert quickSort([1, 2]) == [1, 2]
        assert quickSort([3, 2, 1]) == [1, 2, 3]
        assert quickSort([3, 1, 1, 3, 2]) == [1, 1, 2, 3, 3]
        // works because string implements methods size(), getAt() and findAll()
        assert quickSort('edcba') == 'abcde'.toList()
        // misc items work, because they work with <, > and == operators
        assert quickSort([1.0f, 'a', 10, null]) == [null, 1.0f, 10, 'a']
    }

    def quickSort(list) {
        if (list.size() < 2) {
            return list
        }

        def pivot = list[list.size().intdiv(2)]

        def smaller = list.findAll { it < pivot }
        def same = list.findAll { it == pivot }
        def bigger = list.findAll { it > pivot }

        return quickSort(smaller) + same + quickSort(bigger)
    }

    @Test
    void "using groovy's lists like java's streams"() {
        def urls = [
                new URL('http', 'myshop.com', 80, 'index.html'),
                new URL('https', 'myshop.com', 443, 'buynow.html'),
                new URL('ftp', 'myshop.com', 21, 'downloads')
        ]

        // groovy's list methods
        assert urls
                .findAll { it.port < 99 }
                .collect { it.file.toUpperCase() }
                .sort()
                .join(', ') == 'DOWNLOADS, INDEX.HTML'

        // java's stream with groovy's sugar syntax
        assert urls.stream()
                .filter { it.port < 99 }
                .map { it.file.toUpperCase() }
                .sorted()
                .collect(Collectors.joining(', ')) == 'DOWNLOADS, INDEX.HTML'
    }

    @Test
    void "maps in groovy"() {
        def map = [a: 1, b: 2, 'c': 3]
        assert map['a'] == 1
        assert map['c'] == 3
        assert map.size() == 3
        assert map instanceof LinkedHashMap

        assert [a: 1] == ['a': 1] // for strings (no special chars) as keys, quotes can be omitted
        def x = 'a'
        assert [a: 1] == [(x): 1] // use parenthesis to force use of a variable, instead of string

        assert [:] instanceof LinkedHashMap
        assert [:].size() == 0

        def treeMap = new TreeMap()
        treeMap.putAll(map)
        assert treeMap['a'] == 1

        def spreaded = [*: map, d: 4] // spread operator!
        assert spreaded == [a: 1, b: 2, c: 3, d: 4]
    }

    @Test
    void "subscript operator with maps"() {
        def map = [a: 1, b: 2, 'c': 3]
        assert map['a'] == 1 // getAt() operator
        assert map.a == 1
        assert map.get('a') == 1
        assert map.get('a', 0) == 1

        assert map['d'] == null
        assert map.d == null
        assert map.get('d') == null
        // when using get() with default, if value not found under key, default is put under that key
        assert map.get('d', 0) == 0
        assert map.get('d') == 0

        map['d'] = 4 // putAt() operator
        assert map.d == 4
        map.d = 5
        assert map.d == 5
        map.'e' = 6
        assert map.'e' == 6
    }

    @Test
    void "query maps"() {
        def map = [a: 1, b: 2, c: 3]
        def otherMap = [b: 2, c: 3, a: 1] // different ordering but same entries
        assert map == otherMap

        assert !map.isEmpty()
        assert map.size() == 3
        assert map.containsKey('a')
        assert map.containsValue(1)
        assert map.entrySet() instanceof Set

        assert map.every { entry -> entry.value < 4 }
        assert map.any { entry -> entry.key == 'a' }

        // list needs to be converted to Set to use Set's equals
        assert map.keySet() == ['a', 'b', 'c'] as Set
        // values need to be converted to list to use List's equals
        assert map.values().asList() == [1, 2, 3]
    }
}
