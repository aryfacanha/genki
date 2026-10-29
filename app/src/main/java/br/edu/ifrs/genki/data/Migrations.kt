package br.edu.ifrs.genki.data

import androidx.room.migration.Migration

/** Versão atual do banco. Subir só junto com uma migração nova em [MIGRATIONS]. */
const val DB_VERSION = 1

/**
 * Migrações de uma versão para a seguinte (1→2, 2→3...), para que atualizar o APK não apague os medicamentos.
 * Ao mudar entidades: subir [DB_VERSION], acrescentar aqui um `Migration(n, n + 1)` com o SQL da mudança e
 * comparar com o esquema exportado em `app/schemas`. `MigrationsTest` falha se faltar algum passo.
 */
val MIGRATIONS: Array<Migration> = arrayOf()
