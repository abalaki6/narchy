package nars.term;

import nars.$;
import nars.Narsese;
import nars.The;
import nars.term.atom.Atomic;
import nars.term.sub.Subterms;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by me on 11/12/15.
 */
public class TermVectorTest {

    @Test
    public void testSubtermsEquality() throws Narsese.NarseseException {

        Term a = $.$("(a-->b)");
        //return Atom.the(Utf8.toUtf8(name));

        //        int olen = name.length();
//        switch (olen) {
//            case 0:
//                throw new RuntimeException("empty atom name: " + name);
//
////            //re-use short term names
////            case 1:
////            case 2:
////                return theCached(name);
//
//            default:
//                if (olen > Short.MAX_VALUE/2)
//                    throw new RuntimeException("atom name too long");

        //  }
        //return Atom.the(Utf8.toUtf8(name));

        //        int olen = name.length();
//        switch (olen) {
//            case 0:
//                throw new RuntimeException("empty atom name: " + name);
//
////            //re-use short term names
////            case 1:
////            case 2:
////                return theCached(name);
//
//            default:
//                if (olen > Short.MAX_VALUE/2)
//                    throw new RuntimeException("atom name too long");

        //  }
        Compound b = $.impl(Atomic.the("a"), Atomic.the("b"));

        assertEquals(a.subterms(), b.subterms());
        assertEquals(a.subterms().hashCode(), b.subterms().hashCode());

        assertNotEquals(a, b);
        assertNotEquals(a.hashCode(), b.hashCode());

        assertEquals(0, Subterms.compare(a.subterms(), b.subterms()));
        assertEquals(0, Subterms.compare(b.subterms(), a.subterms()));

        assertNotEquals(0, a.compareTo(b));
        assertNotEquals(0, b.compareTo(a));

        /*assertTrue("after equality test, subterms vector determined shareable",
                a.subterms() == b.subterms());*/


    }

    @Test public void testSortedTermContainer() throws Narsese.NarseseException {
        Subterms a = The.subterms($.$("a"), $.$("b"));
        assertTrue(a.isSorted());
        Subterms b = The.subterms($.$("b"), $.$("a"));
        assertFalse(b.isSorted());
        Subterms s = The.subterms(Terms.sorted(b.arrayClone()));
        assertTrue(s.isSorted());
        assertEquals(a, s);
        assertNotEquals(b, s);
    }



}
