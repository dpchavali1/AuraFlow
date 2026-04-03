package com.auraflow.garden.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

actual fun createDatabaseBuilder(): RoomDatabase.Builder<AuraFlowDatabase> {
    val dbPath = "${documentsDirectory()}/auraflow.db"
    return Room.databaseBuilder<AuraFlowDatabase>(name = dbPath)
        .setDriver(BundledSQLiteDriver())
}

private fun documentsDirectory(): String {
    val paths = platform.Foundation.NSSearchPathForDirectoriesInDomains(
        platform.Foundation.NSDocumentDirectory,
        platform.Foundation.NSUserDomainMask,
        true,
    )
    return paths.first() as String
}
