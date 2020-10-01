import org.junit.jupiter.api.Test

import java.awt.*
import java.util.regex.Matcher

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


}
