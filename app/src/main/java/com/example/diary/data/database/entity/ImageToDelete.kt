package com.example.diary.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.diary.util.Constants.IMAGES_TO_DELETE_TABLE

@Entity(tableName = IMAGES_TO_DELETE_TABLE)
data class ImageToDelete (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val remoteImagePath: String
)