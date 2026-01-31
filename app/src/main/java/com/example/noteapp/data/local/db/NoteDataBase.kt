package com.example.noteapp.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.noteapp.data.local.dao.NoteDao
import com.example.noteapp.data.local.entities.Note
import com.example.noteapp.data.local.converter.NoteAppConverter


@Database(
    entities = [Note::class],
    version = 3,
    exportSchema = false,
)
@TypeConverters(NoteAppConverter::class)
abstract class NoteDataBase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: NoteDataBase? = null

        fun getNoteDataBase(context: Context): NoteDataBase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDataBase::class.java,
                    "note_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()

                INSTANCE = instance
                instance
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Tu migración original
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS Notes_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        content TEXT NOT NULL,
                        name TEXT NOT NULL
                    )
                """
                )
                database.execSQL(
                    """
                    INSERT INTO Notes_new (id, content, name)
                    SELECT id, content, name FROM Notes
                """
                )
                database.execSQL("DROP TABLE Notes")
                database.execSQL("ALTER TABLE Notes_new RENAME TO Notes")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_Notes_name ON Notes (name)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Notes ADD COLUMN reminderDateTime INTEGER")
                database.execSQL("ALTER TABLE Notes ADD COLUMN repeatDays TEXT")
                database.execSQL("ALTER TABLE Notes ADD COLUMN hasReminder INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
