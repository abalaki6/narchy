package jcog.bloom;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by jeff on 14/05/16.
 */
public class LeakySetTest {

    private LeakySet<String> filter;

    @BeforeEach
    public void before() {
        this.filter = BloomFilterBuilder.get().buildFilter();
    }

    @Test
    public void whenAskedIfContainsAddedObject_returnsTrue() {
        String string = "somestr";

        filter.add(string);
        boolean isContained = filter.contains(string);

        assertTrue(isContained);
    }

    // This test is not valid for arbitrary values since Bloom filters can yield false positives.
    // For this special case it does work though.
    @Test
    public void whenAskedIfContainsNotAddedObject_returnsFalse() {
        String string1 = "somestr";
        String string2 = "someotherstr";
        assertNotEquals(string1, string2);

        filter.add(string1);
        boolean isStr2Contained = filter.contains(string2);

        assertFalse(isStr2Contained);
    }

}
