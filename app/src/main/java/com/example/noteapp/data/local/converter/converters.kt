package com.example.noteapp.data.local.converter

import androidx.room.TypeConverter

class NoteAppConverter {
    @TypeConverter
    fun fromListIntToString(list: List<Int>?): String? {
        return list?.joinToString(",") // "1,2,3"
    }

    @TypeConverter
    fun fromStringToListInt(value: String?): List<Int>? {
        return value?.split(",")?.mapNotNull { it.toIntOrNull() } // ["1","2","3"] → [1,2,3]
    }
}
