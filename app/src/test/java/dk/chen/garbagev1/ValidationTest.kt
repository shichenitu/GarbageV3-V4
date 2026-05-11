package dk.chen.garbagev1

import org.junit.Test
import org.junit.Assert.*

class ValidationTest {
    private val regex = Regex("^[\\p{L} ]*$")

    @Test
    fun testDanishCharactersAllowed() {
        assertTrue("Æble".matches(regex))
        assertTrue("Øl".matches(regex))
        assertTrue("Århus".matches(regex))
        assertTrue("æøåÆØÅ".matches(regex))
        assertTrue("Plastic Waste".matches(regex))
    }

    @Test
    fun testNumbersAndSymbolsNotAllowed() {
        assertFalse("Waste123".matches(regex))
        assertFalse("Waste!".matches(regex))
        assertFalse("Waste@".matches(regex))
    }
}
