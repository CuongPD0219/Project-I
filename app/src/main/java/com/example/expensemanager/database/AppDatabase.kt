package com.example.expensemanager.database

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room

@Database(
    entities = [User::class, Expense::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase: RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun expenseDao(): ExpenseDao


    companion object{
        @Volatile// đảm bảo giá trị của INSTANCE luôn cập nhật ngay lập tức, đồng bộ mà không bị ảnh hưởng bởi các luồng khác
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase{
            // nếu INSTANCE không null, thì hãy trả về nó,
            // nếu null, hãy tạo cơ sở dữ liệu
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_manager_database"//tên cơ sở dữ liệu
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance//trả về instance
                instance
            }
        }
    }
}