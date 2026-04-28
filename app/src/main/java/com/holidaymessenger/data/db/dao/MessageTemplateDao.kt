package com.holidaymessenger.data.db.dao

import androidx.room.*
import com.holidaymessenger.data.db.entity.Category
import com.holidaymessenger.data.db.entity.MessageTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageTemplateDao {
    @Query("SELECT * FROM message_templates")
    fun getAllTemplates(): Flow<List<MessageTemplate>>

    @Query("SELECT * FROM message_templates WHERE category = :category")
    fun getTemplatesByCategory(category: Category): Flow<List<MessageTemplate>>

    @Query("SELECT * FROM message_templates WHERE id = :id")
    suspend fun getTemplateById(id: Long): MessageTemplate?

    @Query("SELECT * FROM message_templates WHERE holidayId = :holidayId LIMIT 1")
    suspend fun getTemplateForHoliday(holidayId: Long): MessageTemplate?

    @Query("SELECT * FROM message_templates WHERE category = :category LIMIT 1")
    suspend fun getFirstTemplateByCategory(category: Category): MessageTemplate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: MessageTemplate): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplates(templates: List<MessageTemplate>)

    @Update
    suspend fun updateTemplate(template: MessageTemplate)

    @Delete
    suspend fun deleteTemplate(template: MessageTemplate)
    @Query("DELETE FROM message_templates")
    suspend fun deleteAllTemplates()
}
