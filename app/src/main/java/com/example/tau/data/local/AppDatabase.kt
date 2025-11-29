package com.example.tau.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "tau.db"
        private const val DATABASE_VERSION = 2

        private const val TABLE_USER = "user"
        private const val COLUMN_USER_ID = "id"

        private const val TABLE_DISCIPLINES = "disciplines"
        private const val COLUMN_DISC_ID = "id"
        private const val COLUMN_DISC_REMOTE_ID = "remote_id"
        private const val COLUMN_DISC_USER_ID = "user_id"
        private const val COLUMN_DISC_NAME = "name"
        private const val COLUMN_DISC_TEACHER = "teacher"
        private const val COLUMN_DISC_ROOM = "room"
        private const val COLUMN_DISC_COLOR = "color"
        private const val COLUMN_DISC_SYNCED = "synced"

        private const val TABLE_TASKS = "tasks"
        private const val COLUMN_TASK_ID = "id"
        private const val COLUMN_TASK_REMOTE_ID = "remote_id"
        private const val COLUMN_TASK_USER_ID = "user_id"
        private const val COLUMN_TASK_TITLE = "title"
        private const val COLUMN_TASK_DESCRIPTION = "description"
        private const val COLUMN_TASK_STATUS = "status"
        private const val COLUMN_TASK_DISC_ID = "discipline_id"
        private const val COLUMN_TASK_DATE = "expiration_date"
        private const val COLUMN_TASK_SYNCED = "synced"

        private const val TABLE_SCHEDULES = "schedules"
        private const val COLUMN_SCHED_ID = "id"
        private const val COLUMN_SCHED_REMOTE_ID = "remote_id"
        private const val COLUMN_SCHED_USER_ID = "user_id"
        private const val COLUMN_SCHED_DISC_ID = "discipline_id"
        private const val COLUMN_SCHED_DAY = "day_of_week"
        private const val COLUMN_SCHED_START = "start_time"
        private const val COLUMN_SCHED_END = "end_time"
        private const val COLUMN_SCHED_SYNCED = "synced"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $TABLE_USER ($COLUMN_USER_ID INTEGER PRIMARY KEY)")

        db.execSQL("""
            CREATE TABLE $TABLE_DISCIPLINES (
                $COLUMN_DISC_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_DISC_REMOTE_ID INTEGER,
                $COLUMN_DISC_USER_ID INTEGER NOT NULL,
                $COLUMN_DISC_NAME TEXT NOT NULL,
                $COLUMN_DISC_TEACHER TEXT,
                $COLUMN_DISC_ROOM TEXT,
                $COLUMN_DISC_COLOR TEXT NOT NULL,
                $COLUMN_DISC_SYNCED INTEGER DEFAULT 0
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_TASKS (
                $COLUMN_TASK_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TASK_REMOTE_ID INTEGER,
                $COLUMN_TASK_USER_ID INTEGER NOT NULL,
                $COLUMN_TASK_TITLE TEXT NOT NULL,
                $COLUMN_TASK_DESCRIPTION TEXT,
                $COLUMN_TASK_STATUS INTEGER DEFAULT 0,
                $COLUMN_TASK_DISC_ID INTEGER NOT NULL,
                $COLUMN_TASK_DATE TEXT,
                $COLUMN_TASK_SYNCED INTEGER DEFAULT 0
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_SCHEDULES (
                $COLUMN_SCHED_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_SCHED_REMOTE_ID INTEGER,
                $COLUMN_SCHED_USER_ID INTEGER NOT NULL,
                $COLUMN_SCHED_DISC_ID INTEGER NOT NULL,
                $COLUMN_SCHED_DAY INTEGER NOT NULL,
                $COLUMN_SCHED_START TEXT NOT NULL,
                $COLUMN_SCHED_END TEXT NOT NULL,
                $COLUMN_SCHED_SYNCED INTEGER DEFAULT 0
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_DISCIPLINES")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_TASKS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_SCHEDULES")

            db.execSQL("""
                CREATE TABLE $TABLE_DISCIPLINES (
                    $COLUMN_DISC_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_DISC_REMOTE_ID INTEGER,
                    $COLUMN_DISC_USER_ID INTEGER NOT NULL,
                    $COLUMN_DISC_NAME TEXT NOT NULL,
                    $COLUMN_DISC_TEACHER TEXT,
                    $COLUMN_DISC_ROOM TEXT,
                    $COLUMN_DISC_COLOR TEXT NOT NULL,
                    $COLUMN_DISC_SYNCED INTEGER DEFAULT 0
                )
            """)

            db.execSQL("""
                CREATE TABLE $TABLE_TASKS (
                    $COLUMN_TASK_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_TASK_REMOTE_ID INTEGER,
                    $COLUMN_TASK_USER_ID INTEGER NOT NULL,
                    $COLUMN_TASK_TITLE TEXT NOT NULL,
                    $COLUMN_TASK_DESCRIPTION TEXT,
                    $COLUMN_TASK_STATUS INTEGER DEFAULT 0,
                    $COLUMN_TASK_DISC_ID INTEGER NOT NULL,
                    $COLUMN_TASK_DATE TEXT,
                    $COLUMN_TASK_SYNCED INTEGER DEFAULT 0
                )
            """)

            db.execSQL("""
                CREATE TABLE $TABLE_SCHEDULES (
                    $COLUMN_SCHED_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_SCHED_REMOTE_ID INTEGER,
                    $COLUMN_SCHED_USER_ID INTEGER NOT NULL,
                    $COLUMN_SCHED_DISC_ID INTEGER NOT NULL,
                    $COLUMN_SCHED_DAY INTEGER NOT NULL,
                    $COLUMN_SCHED_START TEXT NOT NULL,
                    $COLUMN_SCHED_END TEXT NOT NULL,
                    $COLUMN_SCHED_SYNCED INTEGER DEFAULT 0
                )
            """)
        }
    }

    fun saveUserId(userId: Int) {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_USER")
        val values = ContentValues().apply {
            put(COLUMN_USER_ID, userId)
        }
        db.insert(TABLE_USER, null, values)
        db.close()
    }

    fun getUserId(): Int? {
        val db = readableDatabase
        val cursor = db.query(TABLE_USER, arrayOf(COLUMN_USER_ID), null, null, null, null, null)
        var userId: Int? = null
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return userId
    }

    fun clearSession() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_USER")
        db.execSQL("DELETE FROM $TABLE_DISCIPLINES")
        db.execSQL("DELETE FROM $TABLE_TASKS")
        db.execSQL("DELETE FROM $TABLE_SCHEDULES")
        db.close()
    }

    fun isLoggedIn(): Boolean {
        return getUserId() != null
    }

    fun insertDiscipline(remoteId: Int?, userId: Int, name: String, teacher: String, room: String, color: String, synced: Boolean = false): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_DISC_REMOTE_ID, remoteId)
            put(COLUMN_DISC_USER_ID, userId)
            put(COLUMN_DISC_NAME, name)
            put(COLUMN_DISC_TEACHER, teacher)
            put(COLUMN_DISC_ROOM, room)
            put(COLUMN_DISC_COLOR, color)
            put(COLUMN_DISC_SYNCED, if (synced) 1 else 0)
        }
        val id = db.insert(TABLE_DISCIPLINES, null, values)
        db.close()
        return id
    }

    fun updateDiscipline(localId: Long, remoteId: Int?, name: String, teacher: String, room: String, color: String, synced: Boolean = false) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_DISC_REMOTE_ID, remoteId)
            put(COLUMN_DISC_NAME, name)
            put(COLUMN_DISC_TEACHER, teacher)
            put(COLUMN_DISC_ROOM, room)
            put(COLUMN_DISC_COLOR, color)
            put(COLUMN_DISC_SYNCED, if (synced) 1 else 0)
        }
        db.update(TABLE_DISCIPLINES, values, "$COLUMN_DISC_ID = ?", arrayOf(localId.toString()))
        db.close()
    }

    fun deleteDiscipline(localId: Long) {
        val db = writableDatabase
        db.delete(TABLE_DISCIPLINES, "$COLUMN_DISC_ID = ?", arrayOf(localId.toString()))
        db.close()
    }

    fun getAllDisciplines(userId: Int): List<LocalDiscipline> {
        val disciplines = mutableListOf<LocalDiscipline>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_DISCIPLINES,
            null,
            "$COLUMN_DISC_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        while (cursor.moveToNext()) {
            disciplines.add(LocalDiscipline(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DISC_ID)),
                remoteId = if (cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_DISC_REMOTE_ID))) null else cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DISC_REMOTE_ID)),
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DISC_USER_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISC_NAME)),
                teacher = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISC_TEACHER)) ?: "",
                room = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISC_ROOM)) ?: "",
                color = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISC_COLOR)),
                synced = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DISC_SYNCED)) == 1
            ))
        }
        cursor.close()
        db.close()
        return disciplines
    }

    fun getAllDisciplinesMap(userId: Int): Map<Long, LocalDiscipline> {
        return getAllDisciplines(userId).associateBy { it.id }
    }

    fun getDisciplineByLocalId(localId: Long): LocalDiscipline? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_DISCIPLINES,
            null,
            "$COLUMN_DISC_ID = ?",
            arrayOf(localId.toString()),
            null, null, null
        )

        var discipline: LocalDiscipline? = null
        if (cursor.moveToFirst()) {
            discipline = LocalDiscipline(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DISC_ID)),
                remoteId = if (cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_DISC_REMOTE_ID))) null else cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DISC_REMOTE_ID)),
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DISC_USER_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISC_NAME)),
                teacher = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISC_TEACHER)) ?: "",
                room = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISC_ROOM)) ?: "",
                color = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DISC_COLOR)),
                synced = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DISC_SYNCED)) == 1
            )
        }
        cursor.close()
        db.close()
        return discipline
    }

    fun insertTask(remoteId: Int?, userId: Int, title: String, description: String, status: Boolean, disciplineId: Long, expirationDate: String, synced: Boolean = false): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TASK_REMOTE_ID, remoteId)
            put(COLUMN_TASK_USER_ID, userId)
            put(COLUMN_TASK_TITLE, title)
            put(COLUMN_TASK_DESCRIPTION, description)
            put(COLUMN_TASK_STATUS, if (status) 1 else 0)
            put(COLUMN_TASK_DISC_ID, disciplineId)
            put(COLUMN_TASK_DATE, expirationDate)
            put(COLUMN_TASK_SYNCED, if (synced) 1 else 0)
        }
        val id = db.insert(TABLE_TASKS, null, values)
        db.close()
        return id
    }

    fun updateTask(localId: Long, remoteId: Int?, title: String, description: String, status: Boolean, disciplineId: Long, expirationDate: String, synced: Boolean = false) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TASK_REMOTE_ID, remoteId)
            put(COLUMN_TASK_TITLE, title)
            put(COLUMN_TASK_DESCRIPTION, description)
            put(COLUMN_TASK_STATUS, if (status) 1 else 0)
            put(COLUMN_TASK_DISC_ID, disciplineId)
            put(COLUMN_TASK_DATE, expirationDate)
            put(COLUMN_TASK_SYNCED, if (synced) 1 else 0)
        }
        db.update(TABLE_TASKS, values, "$COLUMN_TASK_ID = ?", arrayOf(localId.toString()))
        db.close()
    }

    fun deleteTask(localId: Long) {
        val db = writableDatabase
        db.delete(TABLE_TASKS, "$COLUMN_TASK_ID = ?", arrayOf(localId.toString()))
        db.close()
    }

    fun getAllTasks(userId: Int): List<LocalTask> {
        val tasks = mutableListOf<LocalTask>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TASKS,
            null,
            "$COLUMN_TASK_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        while (cursor.moveToNext()) {
            tasks.add(LocalTask(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TASK_ID)),
                remoteId = if (cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_TASK_REMOTE_ID))) null else cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_REMOTE_ID)),
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_USER_ID)),
                title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TITLE)),
                description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DESCRIPTION)) ?: "",
                status = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_STATUS)) == 1,
                disciplineId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TASK_DISC_ID)),
                expirationDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DATE)) ?: "",
                synced = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_SYNCED)) == 1
            ))
        }
        cursor.close()
        db.close()
        return tasks
    }

    fun getTaskByLocalId(localId: Long): LocalTask? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TASKS,
            null,
            "$COLUMN_TASK_ID = ?",
            arrayOf(localId.toString()),
            null, null, null
        )

        var task: LocalTask? = null
        if (cursor.moveToFirst()) {
            task = LocalTask(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TASK_ID)),
                remoteId = if (cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_TASK_REMOTE_ID))) null else cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_REMOTE_ID)),
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_USER_ID)),
                title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_TITLE)),
                description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DESCRIPTION)) ?: "",
                status = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_STATUS)) == 1,
                disciplineId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TASK_DISC_ID)),
                expirationDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TASK_DATE)) ?: "",
                synced = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TASK_SYNCED)) == 1
            )
        }
        cursor.close()
        db.close()
        return task
    }

    fun insertSchedule(remoteId: Int?, userId: Int, disciplineId: Long, dayOfWeek: Int, startTime: String, endTime: String, synced: Boolean = false): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_SCHED_REMOTE_ID, remoteId)
            put(COLUMN_SCHED_USER_ID, userId)
            put(COLUMN_SCHED_DISC_ID, disciplineId)
            put(COLUMN_SCHED_DAY, dayOfWeek)
            put(COLUMN_SCHED_START, startTime)
            put(COLUMN_SCHED_END, endTime)
            put(COLUMN_SCHED_SYNCED, if (synced) 1 else 0)
        }
        val id = db.insert(TABLE_SCHEDULES, null, values)
        db.close()
        return id
    }

    fun updateSchedule(localId: Long, remoteId: Int?, disciplineId: Long, dayOfWeek: Int, startTime: String, endTime: String, synced: Boolean = false) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_SCHED_REMOTE_ID, remoteId)
            put(COLUMN_SCHED_DISC_ID, disciplineId)
            put(COLUMN_SCHED_DAY, dayOfWeek)
            put(COLUMN_SCHED_START, startTime)
            put(COLUMN_SCHED_END, endTime)
            put(COLUMN_SCHED_SYNCED, if (synced) 1 else 0)
        }
        db.update(TABLE_SCHEDULES, values, "$COLUMN_SCHED_ID = ?", arrayOf(localId.toString()))
        db.close()
    }

    fun deleteSchedule(localId: Long) {
        val db = writableDatabase
        db.delete(TABLE_SCHEDULES, "$COLUMN_SCHED_ID = ?", arrayOf(localId.toString()))
        db.close()
    }

    fun getAllSchedules(userId: Int): List<LocalSchedule> {
        val schedules = mutableListOf<LocalSchedule>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SCHEDULES,
            null,
            "$COLUMN_SCHED_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        while (cursor.moveToNext()) {
            schedules.add(LocalSchedule(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SCHED_ID)),
                remoteId = if (cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_SCHED_REMOTE_ID))) null else cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCHED_REMOTE_ID)),
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCHED_USER_ID)),
                disciplineId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SCHED_DISC_ID)),
                dayOfWeek = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCHED_DAY)),
                startTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SCHED_START)),
                endTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SCHED_END)),
                synced = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCHED_SYNCED)) == 1
            ))
        }
        cursor.close()
        db.close()
        return schedules
    }

    fun getScheduleByLocalId(localId: Long): LocalSchedule? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SCHEDULES,
            null,
            "$COLUMN_SCHED_ID = ?",
            arrayOf(localId.toString()),
            null, null, null
        )

        var schedule: LocalSchedule? = null
        if (cursor.moveToFirst()) {
            schedule = LocalSchedule(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SCHED_ID)),
                remoteId = if (cursor.isNull(cursor.getColumnIndexOrThrow(COLUMN_SCHED_REMOTE_ID))) null else cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCHED_REMOTE_ID)),
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCHED_USER_ID)),
                disciplineId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SCHED_DISC_ID)),
                dayOfWeek = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCHED_DAY)),
                startTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SCHED_START)),
                endTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SCHED_END)),
                synced = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCHED_SYNCED)) == 1
            )
        }
        cursor.close()
        db.close()
        return schedule
    }
}

data class LocalDiscipline(
    val id: Long,
    val remoteId: Int?,
    val userId: Int,
    val name: String,
    val teacher: String,
    val room: String,
    val color: String,
    val synced: Boolean
)

data class LocalTask(
    val id: Long,
    val remoteId: Int?,
    val userId: Int,
    val title: String,
    val description: String,
    val status: Boolean,
    val disciplineId: Long,
    val expirationDate: String,
    val synced: Boolean
)

data class LocalSchedule(
    val id: Long,
    val remoteId: Int?,
    val userId: Int,
    val disciplineId: Long,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val synced: Boolean
)