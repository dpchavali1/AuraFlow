package com.auraflow.garden.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import com.auraflow.garden.platform.PlatformContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private object AndroidDatabaseProvider : KoinComponent {
    val platformContext: PlatformContext by inject()
}

actual fun createDatabaseBuilder(): RoomDatabase.Builder<AuraFlowDatabase> {
    val context = AndroidDatabaseProvider.platformContext.context
    return Room.databaseBuilder(
        context = context,
        klass = AuraFlowDatabase::class.java,
        name = context.getDatabasePath("auraflow.db").absolutePath,
    )
}
