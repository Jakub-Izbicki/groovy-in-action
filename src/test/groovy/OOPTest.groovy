import org.junit.jupiter.api.Test

import java.awt.Point
import java.awt.Rectangle as SuperShapeRectangle
import java.util.stream.Stream

import static org.junit.jupiter.api.Assertions.assertThrows

class OOPTest {

    @Test
    void "declaring variables in class"() {
        assert new DeclaringVariables().someMethod()
    }

    class DeclaringVariables {

        public publicVar

        private privateVar

        protected one, two, three

        // if no visibility modifier is specified, a variable becomes a `property` (see GroovyBeans)
        String defaultStringVar

        // if no visibility or type specified, need to use a `def` keyword as a placeholder
        def defaultVar

        static staticField

        // default visibility of methods is public
        def someMethod() {
            def localUntypedVar = 1
        }
    }

    @Test
    void "access properties with subscript"() {
        def counter = new SubscriptAccess(count: 1)

        counter.count = 1
        assert counter.count == 1

        counter["count"] = 2
        assert counter["count"] == 2
    }

    class SubscriptAccess {
        def count
    }

    @Test
    void "override property accessor and assignment operators"() {
        def getSetOverride = new GetSetOverride()

        // for an existing member get and set methods seem not to work
        getSetOverride.count = 1
        assert getSetOverride.count == 1

        getSetOverride.nonExisting = 1
        assert getSetOverride.count == 101

        assert getSetOverride.nonExisting == 999
    }

    class GetSetOverride {

        int count = 0

        void set(String name, Object value) {
            count += 100
        }

        Object get(String name) {
            return 999
        }
    }

    @Test
    void "imitating named arguments with map formal argument"() {
        assert acceptsNamedArgumentsWithMap(a: 1, b: 2, c: 3) == [1, 2, 3]
    }

    def acceptsNamedArgumentsWithMap(Map args) {
        return args.values().toList()
    }

    @Test
    void "call methods by string name"() {
        assert new Point(1, 2).getY() == 2
        assert new Point(1, 2).'getY'() == 2
    }

    @Test
    void "safe dereferencing"() {
        Point point = new Point(1, 1)
        assert point.x == 1
        point = null

        // ?. operator checks if reference is not null, and if not then executes expression on reference,
        // otherwise null is returned
        assert point?.x == null
    }

    @Test
    void "positional parameters in constructors"() {
        def point1 = new Point(1, 2) // classic

        // when there is a list coerced to some other type,
        // groovy calls type's constructor with list's elements as arguments, preserving list's order
        def point2 = [1, 2] as Point // explicit coercion
        Point point3 = [1, 2] // implicit coercion
    }

    @Test
    void "named parameters in default constructor"() {
        // when no-args constructor present, below expresion will forst call it,
        // and then call setters for all the arguments passes in Map (as named arguments are put into Map object)
        def a = new NamedParametersConstructor(one: 1, two: 2)
        assert a.getOne() == 1


        // will throw because class has no no-args constructor
        // or a constructor with Map as an argument (so calling named parameters constructor is unavailable)
        assertThrows(GroovyRuntimeException.class,
                { new NoNoArgsConstructor(one: 1, two: 2) })
    }

    // no constructor so no-args constructor generated
    // named parameters constructor can be used if there is no-args constructor present
    // or constructor with a Map as a first argument
    class NamedParametersConstructor {
        int one, two
    }

    class NoNoArgsConstructor {
        int one, two

        NoNoArgsConstructor(int one, int two) {
            this.one = one
            this.two = two
        }
    }

    @Test
    void "using import aliases"() {
        assert new SuperShapeRectangle()
    }

    @Test
    void "classpath resolve sources in groovy"() {
        // JDK/JRE      -> %JAVA_HOME%/lib
        //                 %JAVA_HOME%/lib/ext
        // OS + CMD     -> CLASSPATH variable
        // Java         -> -cp
        // Groovy       -> %GROOVY_HOME%/lib
        //              -> -cp
        //              -> . (current dir)
        assert true
    }

    @Test
    void "interfaces can be implemented with closures"() {
        MyInt myInt1 = new MyImpl()
        assert myInt1.doSth('a') == 'a'
        // closure coerced to a type implementing a functional interface
        MyInt myInt2 = { it }
        assert myInt2.doSth('a') == 'a'
    }

    interface MyInt {
        def doSth(string)
    }

    class MyImpl implements MyInt {

        @Override
        def doSth(string) {
            return string
        }
    }

    @Test
    void "multimethods - dispatching methods based on their dynamic argument types"() {
        assert doSth(new Point()) == 'object'
        assert doSth("foo") == 'string'

        assert new ClassWithEquals() == new ClassWithEquals()
        assert new ClassWithEquals() != "foo"
    }

    def doSth(Object arg) {
        return 'object'
    }

    def doSth(String arg) {
        return 'string'
    }

    class ClassWithEquals {

        // overridden and invoked only for arguments of type ClassWithEquals
        // the rest fallbacks to Object.equals()
        boolean equals(ClassWithEquals obj) {
            return true
        }
    }

    @Test
    void "traits in groovy as a form of mixin"() {
        def book = new Book(isbn: "foo", title: "bar")
        book.save()

        assert book.getUuid() != null
        assert book.getVersion() == 1
    }

    trait Identifiable {
        String uuid
    }

    trait Versioned {
        int version
    }

    trait Persistent implements Identifiable {
        def save() {
            uuid = new Random().nextInt().toString()
        }
    }

    class Entity implements Identifiable, Versioned, Persistent {
        String title

        def save() {
            version++
            Persistent.super.save() // way to access props from traits
        }
    }

    class Book extends Entity {
        String isbn
    }
}
