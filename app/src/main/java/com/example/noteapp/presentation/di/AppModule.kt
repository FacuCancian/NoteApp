package com.example.noteapp.presentation.di

import android.content.Context
import androidx.room.Room
import com.example.noteapp.data.local.dao.NoteDao
import com.example.noteapp.data.local.db.NoteDataBase
import com.example.noteapp.data.repository.NoteRepositoryImpl
import com.example.noteapp.domain.repository.NoteRepository
import com.example.noteapp.domain.useCase.DeleteNote
import com.example.noteapp.domain.useCase.ExportNote
import com.example.noteapp.domain.useCase.GetAllNotes
import com.example.noteapp.domain.useCase.GetNoteByName
import com.example.noteapp.domain.useCase.InsertNote
import com.example.noteapp.domain.useCase.NoteUseCases
import com.example.noteapp.domain.useCase.ShareNote
import com.example.noteapp.presentation.util.AlarmScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    //unique instance
    @Provides
    @Singleton
    fun provideNoteDataBase(@ApplicationContext context: Context): NoteDataBase {
        return Room.databaseBuilder(context, NoteDataBase::class.java, "note_database").build()
    }

    @Provides
    fun provideNoteDao(dataBase: NoteDataBase): NoteDao {
        return dataBase.noteDao()
    }

    @Provides
    @Singleton
    fun providesNoteRepository(dao: NoteDao): NoteRepository {
        return NoteRepositoryImpl(dao)
    }

    @Provides
    fun provideNoteUseCases(repository: NoteRepository): NoteUseCases {
        return NoteUseCases(
            getAllNotes = GetAllNotes(repository),
            getNoteByName = GetNoteByName(repository),
            insertNote = InsertNote(repository),
            deleteNote = DeleteNote(repository),
            shareNote = ShareNote(repository),
            exportNote = ExportNote(repository),
        )
    }

    @Provides
    fun provideGetAllNotes(repository: NoteRepository): GetAllNotes =
        GetAllNotes(repository)

    @Provides
    fun provideInsertNote(repository: NoteRepository): InsertNote =
        InsertNote(repository)

    @Provides
    fun provideDeleteNote(repository: NoteRepository): DeleteNote =
        DeleteNote(repository)

    @Provides
    fun provideGetNoteByName(repository: NoteRepository): GetNoteByName =
        GetNoteByName(repository)

    @Provides
    @Singleton
    fun provideAlarmScheduler(
        @ApplicationContext context: Context
    ): AlarmScheduler {
        return AlarmScheduler(context)
    }
}