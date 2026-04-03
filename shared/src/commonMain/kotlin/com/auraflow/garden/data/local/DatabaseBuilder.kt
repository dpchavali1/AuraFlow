package com.auraflow.garden.data.local

import androidx.room.RoomDatabase

expect fun createDatabaseBuilder(): RoomDatabase.Builder<AuraFlowDatabase>
