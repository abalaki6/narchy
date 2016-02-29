package nars.nal.nal8;

import nars.$;
import nars.nar.Default;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Test for command tasks, specifically 'echo'
 */
public class echoTest {

    @Test
    public void testEcho1() {
        Default d = new Default();
        assertTrue(d.exe.containsKey($.operator("echo")));
        d.input("echo(\"abc\")!");
        d.input("echo(\"abc\");");
        d.run(10);
        //tODO verify output
    }
}
