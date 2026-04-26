package com.laguipemo.nefroped.core.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.laguipemo.nefroped.core.local.room.entity.ExternalLinkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SupportDao {
    @Query("SELECT * FROM external_links WHERE topicId = :topicId ORDER BY `order` ASC")
    fun observeExternalLinks(topicId: String): Flow<List<ExternalLinkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExternalLinks(links: List<ExternalLinkEntity>)

    @Query("DELETE FROM external_links WHERE id = :linkId")
    suspend fun deleteExternalLink(linkId: String)

    @Query("DELETE FROM external_links WHERE topicId = :topicId")
    suspend fun deleteExternalLinksByTopic(topicId: String)
}
