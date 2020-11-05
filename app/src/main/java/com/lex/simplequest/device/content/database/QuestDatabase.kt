package com.lex.simplequest.device.content.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.lex.simplequest.device.content.provider.QuestContract

class QuestDatabase(
    ctx: Context,
    name: String,
    cursorFactory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(ctx, name, cursorFactory, version) {

    companion object {
        private const val DATABASE_NAME = "quest.db"
        private const val DATABASE_VERSION = 1

        private const val SQL_CREATE_TRACKS_TABLE =
            "CREATE TABLE ${QuestContract.Tracks.TABLE_NAME} (" +
                    "${QuestContract.Tracks.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "${QuestContract.Tracks.COLUMN_NAME} TEXT NOT NULL, " +
                    "${QuestContract.Tracks.COLUMN_START_TIME} REAL NOT NULL, " +
                    "${QuestContract.Tracks.COLUMN_END_TIME} REAL);"

        private const val SQL_CREATE_POINTS_TABLE =
            "CREATE TABLE ${QuestContract.Points.TABLE_NAME} (" +
                    "${QuestContract.Points.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "${QuestContract.Points.COLUMN_TRACK_ID} INTEGER NOT NULL, " +
                    "${QuestContract.Points.COLUMN_LATITUDE} REAL NOT NULL DEFAULT 0.0, " +
                    "${QuestContract.Points.COLUMN_LONGITUDE} REAL NOT NULL DEFAULT 0.0, " +
                    "${QuestContract.Points.COLUMN_ALTITUDE} REAL DEFAULT NULL, " +
                    "${QuestContract.Points.COLUMN_TIMESTAMP} REAL NOT NULL," +
                    "FOREIGN KEY (${QuestContract.Points.COLUMN_TRACK_ID}) REFERENCES ${QuestContract.Tracks.TABLE_NAME} (${QuestContract.Tracks.COLUMN_ID}) ON DELETE CASCADE ON UPDATE CASCADE);"
    }

    constructor(ctx: Context) : this(ctx, DATABASE_NAME, null, DATABASE_VERSION)

    override fun onCreate(db: SQLiteDatabase) {
        Log.i("qaz", "DB onCreate")
        db.execSQL(SQL_CREATE_TRACKS_TABLE)
        db.execSQL(SQL_CREATE_POINTS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // do nothing yet
    }

    override fun onOpen(db: SQLiteDatabase?) {
        super.onOpen(db)
        Log.d("qaz", "DB onOpen")
    }
}