package com.example.expensemanager.database

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [User::class, Expense::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase: RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun expenseDao(): ExpenseDao




    companion object{
        @Volatile// đảm bảo giá trị của INSTANCE luôn cập nhật ngay lập tức, đồng bộ mà không bị ảnh hưởng bởi các luồng khác
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {

                // 1. Tạo bảng mới KHÔNG có cột đã xoá
                db.execSQL("""
            CREATE TABLE users_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                username TEXT NOT NULL,
                password TEXT NOT NULL,
                fullName TEXT NOT NULL,
                dateOfBirth TEXT NOT NULL,
                address TEXT NOT NULL,
                occupation TEXT NOT NULL
            )
        """)

                // 2. Copy dữ liệu từ bảng cũ
                db.execSQL("""
            INSERT INTO users_new (
                id, username, password, fullName, dateOfBirth, address, occupation
            )
            SELECT
                id, username, password, fullName, dateOfBirth, address, occupation
            FROM users
        """)

                // 3. Xoá bảng cũ
                db.execSQL("DROP TABLE users")

                // 4. Đổi tên bảng mới
                db.execSQL("ALTER TABLE users_new RENAME TO users")

                // 5. Tạo lại index UNIQUE username
                db.execSQL("""
            CREATE UNIQUE INDEX index_users_username ON users(username)
        """)
            }
        }

        fun getDatabase(context: Context): AppDatabase{
            // nếu INSTANCE không null, thì hãy trả về nó,
            // nếu null, hãy tạo cơ sở dữ liệu
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_manager_database"//tên cơ sở dữ liệu
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance//trả về instance
                instance
            }
        }
    }


}