package local.mertcan.jarvis;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ContactMatcherTest {
    @Test public void resolvesTurkishPossessiveAndAccusativeForms() {
        assertTrue(ContactMatcher.matchesForTest("annemi", "Annem"));
        assertTrue(ContactMatcher.matchesForTest("annemi", "Anne"));
        assertTrue(ContactMatcher.matchesForTest("annem", "Anne"));
        assertTrue(ContactMatcher.matchesForTest("babamı", "Babam"));
    }

    @Test public void keepsUnrelatedNamesSeparate() {
        assertFalse(ContactMatcher.matchesForTest("annemi", "Ahmet"));
        assertFalse(ContactMatcher.matchesForTest("babamı", "Murat"));
    }
}
