package br.edu.ifrs.genki.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class MigrationsTest {

    @Test
    fun everyVersionStepHasMigration() {
        val steps = MIGRATIONS.map { it.startVersion to it.endVersion }
        assertEquals((1 until DB_VERSION).map { it to it + 1 }, steps)
    }

    @Test
    fun currentSchemaIsExported() {
        val schema = File("schemas/br.edu.ifrs.genki.data.GenkiDatabase/$DB_VERSION.json")
        assertTrue("Esquema da versão $DB_VERSION não exportado em app/schemas", schema.exists())
    }
}
