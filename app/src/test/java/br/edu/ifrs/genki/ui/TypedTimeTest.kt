package br.edu.ifrs.genki.ui

import br.edu.ifrs.genki.ui.components.typedTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalTime

class TypedTimeTest {

    @Test
    fun anyMinuteTyped() {
        assertEquals(LocalTime.of(8, 7), typedTime("8", "7"))
        assertEquals(LocalTime.of(23, 59), typedTime("23", "59"))
        assertEquals(LocalTime.of(0, 0), typedTime("00", "00"))
    }

    @Test
    fun outOfRangeOrEmptyIsNull() {
        assertNull(typedTime("24", "00"))
        assertNull(typedTime("12", "60"))
        assertNull(typedTime("", "30"))
        assertNull(typedTime("7", ""))
    }

    @Test
    fun digitOnFullFieldReplaces() {
        // RV-047-02: com "30" e o cursor em qualquer lugar, digitar 7 deixa "7".
        assertEquals("7", br.edu.ifrs.genki.ui.components.twoDigits("30", "307"))
        assertEquals("7", br.edu.ifrs.genki.ui.components.twoDigits("30", "370"))
        assertEquals("7", br.edu.ifrs.genki.ui.components.twoDigits("30", "730"))
        assertEquals("15", br.edu.ifrs.genki.ui.components.twoDigits("1", "15"))
        assertEquals("", br.edu.ifrs.genki.ui.components.twoDigits("3", ""))
        assertNull(br.edu.ifrs.genki.ui.components.twoDigits("3", "3a"))
    }
}
