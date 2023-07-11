package com.example.leak

import android.content.Context
import android.util.Log
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.cash.sqldelight.logs.LogSqliteDriver
import com.example.leak.db.LeakDb

object DatabaseProvider {

    private var db: LeakDb? = null

    fun instance(context: Context): LeakDb {
        if (db == null) {
            synchronized(DatabaseProvider) {
                if (db == null) {
                    db = LeakDb(
                        LogSqliteDriver(
                            AndroidSqliteDriver(LeakDb.Schema, context.applicationContext),
                        ) { Log.d("Database", it) },
                    )
                }
            }
        }
        return db!!
    }
}