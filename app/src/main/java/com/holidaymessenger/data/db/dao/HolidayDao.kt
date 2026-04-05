package com.holidaymessenger.data.db.dao

import androidx.room.*
import com.holidaymessenger.data.db.entity.Holiday
import com.holidaymessenger.data.db.entity.HolidayContactCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface HolidayDao {
    @Query("SELECT * FROM holidays ORDER BY monthDay ASC")
    fun getAllHolidays(): Flow<List<Holiday>>

    @Query("SELECT * FROM holidays WHERE enabled = 1")
    suspend fun getEnabledHolidays(): List<Holiday>

    @Query("SELECT * FROM holidays WHERE id = :id")
    suspend fun getHolidayById(id: Long): Holiday?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHoliday(holiday: Holiday): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolidays(holidays: List<Holiday>)

    @Update
    suspend fun updateHoliday(holiday: Holiday)

    @Delete
    suspend fun deleteHoliday(holiday: Holiday)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolidayContactCrossRef(crossRef: HolidayContactCrossRef)

    @Delete
    suspend fun deleteHolidayContactCrossRef(crossRef: HolidayContactCrossRef)

    @Query("SELECT contactId FROM holiday_contact_cross_ref WHERE holidayId = :holidayId")
    fun getContactIdsForHoliday(holidayId: Long): Flow<List<Long>>

    @Query("SELECT COUNT(*) FROM holidays")
    suspend fun getHolidayCount(): Int
}
