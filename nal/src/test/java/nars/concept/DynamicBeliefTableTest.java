package nars.concept;

import nars.$;
import nars.NAR;
import nars.Narsese;
import nars.Task;
import nars.concept.dynamic.DynTruth;
import nars.concept.dynamic.DynamicBeliefTable;
import nars.concept.dynamic.DynamicConcept;
import nars.nar.Default;
import nars.term.Compound;
import nars.truth.Truth;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import static nars.$.$;
import static nars.Op.QUESTION;
import static org.junit.Assert.*;

/**
 * Created by me on 10/27/16.
 */
public class DynamicBeliefTableTest {

    @Test
    public void testDynamicConjunction2() throws Narsese.NarseseException {
        NAR n = new Default();
        n.believe("a:x", 1f, 0.9f);
        n.believe("a:y", 1f, 0.9f);
        n.believe("b:x", 0f, 0.9f);
        n.run(1);
        long now = n.time();
        float dur = n.time.dur();
        assertEquals($.t(1f,0.81f), n.concept($("(a:x && a:y)"), true).belief(now, dur));
        assertEquals($.t(0f,0.81f), n.concept($("(b:x && a:y)"), true).belief(now, dur));
        assertEquals($.t(0f,0.81f), n.concept($("(a:x && (--,a:y))"), true).belief(now, dur));
        assertEquals($.t(1f,0.81f), n.concept($("((--,b:x) && a:y)"), true).belief(now, dur));
        assertEquals($.t(0f,0.81f), n.concept($("((--,b:x) && (--,a:y))"), true).belief(now, dur));
    }
    @Test
    public void testDynamicIntersection() throws Narsese.NarseseException {
        NAR n = new Default();
        n.believe("a:x", 1f, 0.9f);
        n.believe("a:y", 1f, 0.9f);
        n.believe("a:z", 0f, 0.9f);
        n.believe("x:b", 1f, 0.9f);
        n.believe("y:b", 1f, 0.9f);
        n.believe("z:b", 0f, 0.9f);
        n.run(1);
        long now = n.time();
        float dur = n.time.dur();
        assertTrue(n.concept($("((x|y)-->a)"), true) instanceof DynamicConcept);
        assertEquals($.t(1f,0.81f), n.concept($("((x|y)-->a)"), true).belief(now, dur));
        assertEquals($.t(0f,0.81f), n.concept($("((x|z)-->a)"), true).belief(now, dur));
        assertEquals($.t(1f,0.81f), n.concept($("((x&z)-->a)"), true).belief(now, dur));
        assertEquals($.t(1f,0.81f), n.concept($("(b --> (x|y))"), true).belief(now, dur));
        assertEquals($.t(1f,0.81f), n.concept($("(b --> (x|z))"), true).belief(now, dur));
        assertEquals($.t(0f,0.81f), n.concept($("(b --> (x&z))"), true).belief(now, dur));

        assertTrue(n.concept($("((x|(--,y))-->a)"), true) instanceof DynamicConcept);
        assertEquals($.t(0f,0.81f), n.concept($("((x|(--,y))-->a)"), true).belief(now, dur));
        assertEquals($.t(1f,0.81f), n.concept($("((x|(--,z))-->a)"), true).belief(now, dur));
    }

    @Test
    public void testDynamicConjunction3() throws Narsese.NarseseException {
        NAR n = new Default();
        n.believe("a:x", 1f, 0.9f);
        n.believe("a:y", 1f, 0.9f);
        n.believe("a:z", 1f, 0.9f);
        n.run(1);

        Concept cc = n.concept($("(&&, a:x, a:y, a:z)"), true);
        Truth now = cc.belief(n.time(), n.time.dur());
        assertTrue($.t(1f, 0.73f).equals(now, 0.01f));
        //the truth values were provided despite the belief tables being empty:
        assertTrue(cc.beliefs().isEmpty());

        //test unknown:
        {
            Concept ccn = n.concept($("(&&, a:x, a:w)"), true);
            Truth nown = ccn.belief(n.time(), n.time.dur());
            assertNull(nown);
        }

        //test negation:
        {
            Concept ccn = n.concept($("(&&, a:x, (--, a:y), a:z)"), true);
            Truth nown = ccn.belief(n.time(), n.time.dur());
            assertTrue($.t(0f, 0.73f).equals(nown, 0.01f));
        }

        //test change after a component's revision:
        n.believe("a:y", 0, 0.95f);
        n.run(1);
        Concept ccn = n.concept("(&&, a:x, a:y, a:z)");
        Truth now2 = ccn.belief(n.time(), n.time.dur());
        assertTrue(now2.freq() < 0.4f);

    }

    @Test
    public void testDynamicConjunction2Temporal() throws Narsese.NarseseException {
        NAR n = new Default();
        n.believe($("(x)"), (long)0, 1f, 0.9f);
        n.believe($("(y)"), (long)4, 1f, 0.9f);
        n.run(2);
        CompoundConcept cc = (CompoundConcept) n.concept($("((x) && (y))"), true);
//        cc.print();
//
//
//        System.out.println("Instantaneous");
//        for (int i = -4; i <= n.time()+4; i++) {
//            System.out.println( i + ": " + cc.belief(i) );
//        }

        DynamicBeliefTable xtable = (DynamicBeliefTable) ((cc).beliefs());
        {
            Compound template = $("((x) &&+4 (y))");
            System.out.println(template);

            DynTruth xt = xtable.truth(0, template, true);
            assertNotNull(xt);
            assertEquals($.t(1f, 0.81f), xt.truth());

            for (int i = -4; i <= n.time() + 4; i++) {
                System.out.println(i + ": " + xtable.truth(i, template.dt(), true) + " " + xtable.generate(template, i));
            }
        }

        long when = 0;
        for (int i = 0; i <= 8; i++) {
            Compound template = $("((x) &&+"+ i + " (y))");
            System.out.println( xtable.truth(when, template.dt(), true) + " " + xtable.generate(template, when));
        }

        assertEquals(0.81f, xtable.generate($("((x) &&+4 (y))"), 0).conf(), 0.01f); //best match to the input
        assertEquals(0.58f, xtable.generate($("((x) &&+2 (y))"), 0).conf(), 0.01f);
        assertEquals(0.23f, xtable.generate($("((x) &&+0 (y))"), 0).conf(), 0.01f);


    }

    @Test public void testDynamicProductImageExtensional() throws Narsese.NarseseException {
        NAR n = new Default();

        n.believe($("f(x,y)"), (long)0, 1f, 0.9f);

        CompoundConcept prod = (CompoundConcept) n.concept($("f(x, y)"), false);
        float dur = n.time.dur();

        Truth t = prod.belief(n.time(), dur);
        System.out.println(t);

        CompoundConcept imgX = (CompoundConcept) n.concept($("(x --> (/,f,_,y))"), true);
        assertEquals(t, imgX.belief(n.time(), dur));


        CompoundConcept imgY = (CompoundConcept) n.concept($("(y --> (/,f,x,_))"), true);
        assertEquals(t, imgY.belief(n.time(), dur));



        n.run(16); //by now, structural decomposition should have also had the opportunity to derive the image

        Truth t2 = prod.belief(n.time(), dur);

        assertEquals(t2, imgX.belief(n.time(), dur));
        assertEquals(t2, imgY.belief(n.time(), dur));

    }

    @Test public void testDynamicProductImageIntensional() throws Narsese.NarseseException {
        NAR n = new Default();
        float dur = n.time.dur();

        n.believe($("(f-->(x,y))"), (long)0, 1f, 0.9f);

        CompoundConcept prod = (CompoundConcept) n.concept($("(f-->(x, y))"), false);
        Truth t = prod.belief(0, dur);

        CompoundConcept imgX = (CompoundConcept) n.concept($("((\\,f,_,y)-->x)"), true);
        assertEquals(t, imgX.belief(0, dur));

        CompoundConcept imgY = (CompoundConcept) n.concept($("((\\,f,x,_)-->y)"), true);
        assertEquals(t, imgY.belief(0, dur));



        n.run(16); //by now, structural decomposition should have also had the opportunity to derive the image

        Truth t2 = prod.belief(0, dur);

        assertEquals(t2, imgX.belief(0, dur));
        assertNotEquals(t2, imgX.belief(n.time(), dur));
        assertEquals(t2, imgY.belief(0, dur));
        assertNotEquals(t2, imgY.belief(n.time(), dur));

    }

    @Test public void testAnswerTemplateWithVar() throws Narsese.NarseseException {
        NAR n = new Default();
        String c = "(tetris-->(((0,(1,(1))),(0,(0,(1,(0)))))&((1,(0,(1))),(0,(0,(1,(0)))))))";
        n.believe(c);
        n.run(1);
        @Nullable Task a = n.concept(c).beliefs().match(0, 0, 1, $.task($("(tetris-->#1)"), QUESTION, null).apply(n), false);
        //System.out.println(a);
        assertEquals("$.50;.90$ (tetris-->(((0,(1,(1))),(0,(0,(1,(0)))))&((1,(0,(1))),(0,(0,(1,(0))))))). %1.0;.90%", a.toString());
//        @Nullable Task b = n.concept(c).beliefs().match(10, 0, 1, $.task($("(tetris-->#1)"), QUESTION, null).apply(n), false);
//        System.out.println(b);
    }
}