package Solution;
import org.junit.ComparisonFailure;
import java.lang.reflect.*;

import static org.junit.Assert.assertEquals;

public class main {
    public static void main(String[] args) throws Exception {
        /*
        // get the class of Integer's nested class IntegerCache
        Class<?> intCache = Class.forName("java.lang.Integer$IntegerCache");
        // get the Field describing 'cache', which is a private and final field
        Field cache = intCache.getDeclaredField("cache");
        // disable the private and final modifiers of 'cache'
        disableFinalPrivate(cache);
        // replace the current cache with a new one.
        // 'cache' is static, so no instance is required.
        cache.set(null, getUltimateAnswer());

        // now see what you get for 1 + 1...
        // note that (1 + 1) is an int, and is boxed in an Integer only
        // when passed to format().
        System.out.format("1 + 1 = %d", 1 + 1);


        // node: the hack only changed the particular Field pointed by cache.
        // subsequent calls to intCache.getDeclaredField("cache") will have
        // to be hacked again in order to use them to modify 'cache' again.


        boolean isNull = Object.class.getSuperclass() == null;

        int[] x = {1};

        callChange(x);

        System.out.println(x[0]);
        */

        A a = new A();

        B b = new B();

        B.C c = b.new C();

        boolean isSame;
        Class<?> supeClassC = c.getClass().getSuperclass();
        Class<?> supeClassB = B.class.getSuperclass();

        System.out.println(supeClassC == supeClassB);

        //B.C.INNNERINNER e = b.new INNNERINNER();

        //System.out.println(e.getClass());

    }

    private static void callThrowComparisonFailure() throws ComparisonFailure {
        assertEquals("1","2");
    }

    private static void callChange(int[] x) {
        x[0] = 2;
    }

    public static void disableFinalPrivate(Field field) throws Exception {
        // first, disable the 'private' modifier (this is not a hack - it's
        // what setAccessible is designed to do)
        field.setAccessible(true);
        // all modifiers are stored in one int, acting as a bitmap
        int fieldModifiers = field.getModifiers();
        if (Modifier.isFinal(fieldModifiers)) {
            // the field is final, as its 'modifiers' member indicates.
            // we'd like to change that member, but it's private.
            Field fieldMod = Field.class.getDeclaredField("modifiers");
            // so now we disable the private limitation
            fieldMod.setAccessible(true);
            // and turn the FINAL bit off. Note that the object whose
            // 'modifiers' member is being modified is the original
            // field, which describes Integer$IntegerCache.cache
            fieldMod.setInt(field, fieldModifiers & ~Modifier.FINAL);
            // So now we've used reflection to work around its own limitations,
            // and the final modifier has been disabled...
            // this is sufficient, because when modifying a final field
            // using reflection, the exception is thrown by the reflection
            // code and not by some lower-level JVM stuff.
        }
    }

    static Integer[] getUltimateAnswer() {
        // return an array of 256 integers (as is the original cache),
        // all pointing on Integer(42)
        Integer[] ret = new Integer[256];
        java.util.Arrays.fill(ret, 42);
        return ret;
    }

}
