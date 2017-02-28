import org.junit.jupiter.api.Test;
import org.junit.platform.runner.IncludeEngines;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.runner.SelectClasses;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
//@SelectPackages("")
@IncludeEngines("junit-jupiter")
@SelectClasses({
    BasicSecurityHeaderTests.class,
    CallbackPageTest.class,
    ControllerTest.class
})

/*
@RunWith(Suite.class)
@Suite.SuiteClasses({
        BasicSecurityHeaderTests.class,
        CallbackPageTest.class
})
*/

public class TestSuite {

}
