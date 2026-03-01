package com.example.noteapp.presentation.noteList

import com.example.noteapp.data.local.entities.Note
import com.example.noteapp.domain.repository.NoteRepository
import com.example.noteapp.domain.useCase.RescheduleAlarmUseCase
import com.example.noteapp.presentation.alarm.AlarmScheduler
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class RescheduleAlarmUseCaseTest {
    private lateinit var  repository: NoteRepository
    private lateinit var scheduler: AlarmScheduler
    private lateinit var useCase: RescheduleAlarmUseCase

    @Before
    fun init(){
        repository = mockk()
        scheduler = mockk()
        useCase = RescheduleAlarmUseCase(repository, scheduler)
    }
    private val testNote = Note(
        id = 1,
        name = "Mi nota",
        content = "Contenido de prueba",
        repeatForever = false
    )
    @Test
    fun `repeat just once a day, not a week`() = runTest {
// Arrange
        coEvery { repository.getNoteById(1) } returns testNote
        coEvery { repository.insertNote(any()) } returns 1L  // necesario porque insertNote es suspend

// Act
        val result = useCase.execute(1)

// Assert
        assertEquals(null, result)
    }
    @Test
    fun `repeat just forever a day, not a week`() = runTest {
        val testNoteForever = Note(
            id = 1,
            name = "Mi nota",
            content = "Contenido de prueba",
            repeatForever = true,
            repeatDays = listOf(1, 2, 3, 4, 5, 6, 7),
            reminderDateTime = System.currentTimeMillis() - 60000 // hace 1 minuto, ya pasó
        )
        coEvery { repository.getNoteById(1) } returns testNoteForever
        coEvery { repository.insertNote(any()) } returns 1L
        every { scheduler.schedule(any()) } just runs
        val result = useCase.execute(1)
// Assert
        assertNotNull(result)
    }

}