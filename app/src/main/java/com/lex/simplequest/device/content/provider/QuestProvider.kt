package com.lex.simplequest.device.content.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import com.lex.simplequest.device.content.database.QuestDatabase
import java.lang.IllegalArgumentException
import java.util.*

class QuestProvider() : ContentProvider() {
    companion object {
        private const val TAG = "QuestProvider"
        private const val TRACKS_CODE = 1
        private const val POINTS_CODE = 2
        private const val CHECK_POINTS_CODE = 3

        private val URI_MATCHER = UriMatcher(UriMatcher.NO_MATCH)
    }

    private lateinit var database: QuestDatabase

    init {
        URI_MATCHER.apply {
            addURI(QuestContract.AUTHORITY, QuestContract.Tracks.TABLE_NAME, TRACKS_CODE)
            addURI(QuestContract.AUTHORITY, QuestContract.Points.TABLE_NAME, POINTS_CODE)
            addURI(QuestContract.AUTHORITY, QuestContract.CheckPoints.TABLE_NAME, CHECK_POINTS_CODE)
        }
    }

    override fun onCreate(): Boolean {
        database = QuestDatabase(context!!)
        return true
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val db = database.writableDatabase
        val rowId = try {
            db.beginTransaction()
            when (URI_MATCHER.match(uri)) {
                TRACKS_CODE -> {
                    insertOrUpdateUniqueRow(
                        db,
                        QuestContract.Tracks.TABLE_NAME,
                        QuestContract.Tracks.COLUMN_ID,
                        Arrays.asList(QuestContract.Tracks.COLUMN_ID),
                        values
                    )
                }
                POINTS_CODE -> {
                    insertOrUpdateUniqueRow(
                        db,
                        QuestContract.Points.TABLE_NAME,
                        QuestContract.Points.COLUMN_ID,
                        Arrays.asList(QuestContract.Points.COLUMN_ID),
                        values
                    )
                }
                CHECK_POINTS_CODE -> {
                    insertOrUpdateUniqueRow(
                        db,
                        QuestContract.CheckPoints.TABLE_NAME,
                        QuestContract.CheckPoints.COLUMN_ID,
                        Arrays.asList(QuestContract.CheckPoints.COLUMN_ID),
                        values
                    )
                }

                else -> throw IllegalArgumentException("Unsupported URI $uri")
            }
        } finally {
            db.endTransaction()
        }

        if (QuestContract.INCORRECT_ROW_ID != rowId) {
            val cr = context!!.contentResolver
            cr.notifyChange(uri, null)
        }
        return ContentUris.withAppendedId(uri, rowId)
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        val queryBuilder = SQLiteQueryBuilder()
        val db = database.readableDatabase

        queryBuilder.tables = when (URI_MATCHER.match(uri)) {
            TRACKS_CODE -> {
                QuestContract.Tracks.TABLE_NAME
            }
            POINTS_CODE -> {
                QuestContract.Points.TABLE_NAME
            }
            CHECK_POINTS_CODE -> {
                QuestContract.CheckPoints.TABLE_NAME
            }
            else -> throw IllegalArgumentException("Unsupported URI $uri")
        }

        val cursor =
            queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder)
        cursor?.setNotificationUri(context!!.contentResolver, uri)
        return cursor
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        val db = database.writableDatabase;
        val updateCount: Int
        try {
            db.beginTransaction()
            when (URI_MATCHER.match(uri)) {
                TRACKS_CODE -> {
                    updateCount =
                        db.update(QuestContract.Tracks.TABLE_NAME, values, selection, selectionArgs)
                    db.setTransactionSuccessful()
                }
                POINTS_CODE -> {
                    updateCount =
                        db.update(QuestContract.Points.TABLE_NAME, values, selection, selectionArgs)
                    db.setTransactionSuccessful()
                }
                CHECK_POINTS_CODE -> {
                    updateCount =
                        db.update(
                            QuestContract.CheckPoints.TABLE_NAME,
                            values,
                            selection,
                            selectionArgs
                        )
                    db.setTransactionSuccessful()
                }
                else -> throw IllegalArgumentException("Unsupported URI $uri")
            }
        } finally {
            db.endTransaction()
        }

        if (updateCount > 0) {
            val cr = context!!.contentResolver
            cr.notifyChange(uri, null)
        }

        return updateCount
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val db = database.writableDatabase;
        val deleteCount: Int
        try {
            db.beginTransaction()
            when (URI_MATCHER.match(uri)) {
                TRACKS_CODE -> {
                    deleteCount =
                        db.delete(QuestContract.Tracks.TABLE_NAME, selection, selectionArgs)
                    db.setTransactionSuccessful()
                }
                POINTS_CODE -> {
                    deleteCount =
                        db.delete(QuestContract.Points.TABLE_NAME, selection, selectionArgs)
                    db.setTransactionSuccessful()
                }
                POINTS_CODE -> {
                    deleteCount =
                        db.delete(QuestContract.CheckPoints.TABLE_NAME, selection, selectionArgs)
                    db.setTransactionSuccessful()
                }
                else -> throw IllegalArgumentException("Unsupported URI $uri")
            }
        } finally {
            db.endTransaction()
        }

        if (deleteCount > 0) {
            val cr = context!!.contentResolver
            cr.notifyChange(uri, null)
        }

        return deleteCount
    }

    override fun getType(uri: Uri): String? =
        null


    private fun insertOrUpdateUniqueRow(
        db: SQLiteDatabase, tableName: String?, idCol: String,
        uniqueColumns: List<String>, values: ContentValues?
    ): Long {
        var rowId = getConflictId(db, values!!, tableName!!, idCol, uniqueColumns)
        if (-1L == rowId) {
            rowId = db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_IGNORE)
        } else {
            db.update(tableName, values, "$idCol = $rowId", null)
        }
        if (-1L != rowId) db.setTransactionSuccessful()
        return rowId
    }

    private fun getConflictId(
        db: SQLiteDatabase, values: ContentValues, tableName: String,
        idCol: String, uniqueColumns: List<String>
    ): Long {
        var cursor: Cursor? = null
        try {
            val selection = StringBuilder()
            for (uniqueColumn in uniqueColumns) {
                if (selection.isNotEmpty()) selection.append(" AND ")
                selection.append(
                    String.format(
                        "%s = '%s'",
                        uniqueColumn,
                        values.getAsString(uniqueColumn)
                    )
                )
            }
            cursor = db.query(
                tableName,
                arrayOf(idCol),
                selection.toString(),
                null,
                null,
                null,
                null
            )
            return if (null != cursor && cursor.moveToFirst()) {
                cursor.getLong(0)
            } else -1
        } catch (ignore: Throwable) {
            ignore.printStackTrace()
        } finally {
            cursor?.close()
        }
        return -1
    }
}