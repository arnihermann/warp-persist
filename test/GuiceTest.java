import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.testng.annotations.Test;

/**
 * Created with IntelliJ IDEA.
 * On: May 14, 2007 9:40:09 AM
 *
 * @author Dhanji R. Prasanna <a href="mailto:dhanji@gmail.com">email</a>
 */
public class GuiceTest {
    @Inject private GuiceTest test;
    private String value;

    public GuiceTest() {}

    //just some scratch code
    @Test(expectedExceptions = RuntimeException.class) 
    public final void testProvider() {
        assert null != Guice.createInjector(new AbstractModule() {
            protected void configure() {
                bind(GuiceTest.class).toProvider(GuiceTestProvider.class);
            }
        }).getInstance(GuiceTest.class).value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static class GuiceTestProvider implements Provider<GuiceTest> {
        @Inject GuiceTest test;
        public GuiceTest get() {
            test.setValue("other");
            return test;
        }
    }

    public static void main(String...args) {
        Guice.createInjector().getInstance(GuiceTest.class);
    }

    private static void print(GuiceTest instance) {
        if (instance.test != null) {
            System.out.println(instance.test);
            print(instance.test);
        }

        throw new AssertionError();
    }
}
