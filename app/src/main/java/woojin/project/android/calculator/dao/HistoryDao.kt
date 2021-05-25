package woojin.project.android.calculator.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import woojin.project.android.calculator.model.History

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history")
    fun getAll():List<History>

    @Insert
    fun insertHistory(history: History)

    @Query("DELETE FROM history")
    fun deleteAll()

    //한 개만 삭제
    @Delete
    fun delete(history: History)

    @Query("SELECT * FROM history WHERE result LIKE :result")
    fun findAllByResult(result:String):List<History>

    @Query("SELECT * FROM history WHERE result LIKE :result LIMIT 1")
    fun findByResult(result:String):History
}