package woojin.project.android.calculator

import androidx.room.Database
import androidx.room.RoomDatabase
import woojin.project.android.calculator.dao.HistoryDao
import woojin.project.android.calculator.model.History

@Database(entities = [History::class], version = 1)
abstract class AppDatabase : RoomDatabase(){
    abstract fun historyDao():HistoryDao
}