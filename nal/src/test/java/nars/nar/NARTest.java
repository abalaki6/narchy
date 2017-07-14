package nars.nar;

import nars.NAR;
import nars.Narsese;
import nars.concept.Concept;
import nars.term.Termed;
import org.junit.Ignore;
import org.junit.Test;

import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Created by me on 8/7/15.
 */
public class NARTest {


//    @Test
//    public void testEmptyMemoryToJSON() throws IOException, InterruptedException, ClassNotFoundException {
//        Memory m = new Terminal().memory;
//        String j = JSON.omDeep.writeValueAsString(m);
//        assertTrue(j.length() > 16);
//
//        String pretty = JSON.omDeep.writerWithDefaultPrettyPrinter().writeValueAsString(m);
//        System.out.println(pretty);
//
//        assertTrue(pretty.length() > j.length() );
//    }
//
//    @Test
//    public void testEmptyMemorySerialization() throws IOException, InterruptedException, ClassNotFoundException {
//        /** empty memory, and serialize it */
//        Memory m = new Default2(1024,1,1,1).memory;
//        byte[] bm = m.toBytes();
//        assertTrue(bm.length > 64);
//
//        assertEquals(m, new JBossMarshaller().objectFromByteBuffer(bm) );
//
//    }

    @Test @Ignore
    public void testMemoryTransplant() throws Narsese.NarseseException {

        //this.activeTasks = activeTasks;
        NAR nar = new NARS().get();
        //DefaultAlann nar = new DefaultAlann(m, 32);

        //TextOutput.out(nar);

        nar.input("<a-->b>.", "<b-->c>.").run(25);

        nar.input("<a-->b>.", "<b-->c>.");
        nar.stop();

        assertTrue(nar.terms.size() > 5);

        int nc;
        assertTrue((nc = nar.terms.size()) > 0);


        //a new nar with the same memory is allowed to
        //take control of it after the first stops
        //this.activeTasks = activeTasks;
        NAR nar2 = new NARS().get();

        assertTrue(nar.time() > 1);

        //it should have existing concepts
        assertEquals(nc, nar2.terms.size());


    }


    @Test
    public void testFluentBasics() throws Exception {
        int frames = 32;
        AtomicInteger cycCount = new AtomicInteger(0);
        StringWriter sw = new StringWriter( );

        new NARS().get()
                .input("<a --> b>.", "<b --> c>.")
                .stopIf( () -> false )
                .eachCycle(n -> cycCount.incrementAndGet() )
                .trace(sw).run(frames);

        new NARS().get()
                .input("<a --> b>.", "<b --> c>.")
                .stopIf(() -> false)
                .eachCycle(n -> cycCount.incrementAndGet())
                .trace(sw)
                .forEachConceptTask(true, true, true, true, false, 1, System.out::println );

        //System.out.println(sw.getBuffer());
        assertTrue(sw.toString().length() > 16);
        assertEquals(frames, cycCount.get());


    }



    @Test public void testBeforeNextFrameOnlyOnce() {
        AtomicInteger b = new AtomicInteger(0);
        NAR n = new Terminal(64);

        n.runLater(b::incrementAndGet);
        n.run(4);
        assertEquals(1, b.get());

    }

//
//    @Test public void testFork() throws Exception {
//        NAR a = new Default();
//        a.input("b:a.");
//        a.input("c:b.");
//        a.frame(8);
//
//        NAR b = a.fork();
//
//        assertEquals(a, b);
//
//
//    }

    @Test
    public void testConceptInstancing() throws Narsese.NarseseException {
        NAR n = new NARS().get();

        String statement1 = "<a --> b>.";

        Termed a = n.term("a");
        assertTrue(a != null);
        Termed a1 = n.term("a");
        assertTrue(a.equals(a1));

        n.input(statement1);
        n.run(4);

        n.input(" <a  --> b>.  ");
        n.run(1);
        n.input(" <a--> b>.  ");
        n.run(1);

        String statement2 = "<a --> c>.";
        n.input(statement2);
        n.run(4);

        Termed a2 = n.term("a");
        assertNotNull(a2);

        Concept ca = n.concept(a2);
        assertTrue(ca != null);


    }
}