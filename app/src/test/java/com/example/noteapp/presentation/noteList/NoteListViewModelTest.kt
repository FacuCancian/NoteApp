package com.example.noteapp.presentation.noteList

import android.app.Application
import com.example.noteapp.data.local.entities.Note
import com.example.noteapp.domain.repository.NoteRepository
import com.example.noteapp.domain.useCase.DeleteNote
import com.example.noteapp.domain.useCase.GetAllNotes
import com.example.noteapp.domain.useCase.GetNoteByName
import com.example.noteapp.domain.useCase.InsertNote
import com.example.noteapp.domain.data.alarm.AlarmScheduler
import com.example.noteapp.presentation.login.SaveState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

class NoteListViewModelTest {
    private lateinit var application: Application
    private lateinit var getAllNotes: GetAllNotes
    private lateinit var insert: InsertNote
    private lateinit var delete: DeleteNote
    private lateinit var search: GetNoteByName
    private lateinit var alarmScheduler: AlarmScheduler
    private lateinit var repository: NoteRepository

    private lateinit var viewModel: NoteListViewModel

    private val testDispatcher = StandardTestDispatcher()// to use Dispatcher.Main in testing
    private val testNote = Note(
        id = 1,
        name = "Mi nota",
        content = "Contenido de prueba"
    )

    @Before
    fun init() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        getAllNotes = mockk()// every say exactly how to act
        //to see if pull of notes and search
        every { getAllNotes.get() } returns flowOf(emptyList<Note>())
        coEvery { repository.searchNotes(any()) } returns flowOf(emptyList<Note>())

        //mockk to verifiy funcionalitys
        application = mockk(relaxed = true)
        insert = mockk(relaxed = true)
        delete = mockk(relaxed = true)
        search = mockk()
        alarmScheduler = mockk(relaxed = true)

        viewModel = NoteListViewModel(
            application,
            getAllNotes,
            insert,
            delete,
            search,
            alarmScheduler,
            repository
        )

    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `trySaveNote should save note if not exists`() = runTest {
        val existingName = "name"
        coEvery { search.get(any()) } returns null

        viewModel.trySaveNote(
            title = existingName,
            content = "text"
        )
        advanceUntilIdle()//bc viewModel use viewModelScope.launch
        assertEquals(SaveState.Done, viewModel.saveState.value)

    }
    @Test
    fun `trySaveNote should not save note if already exists`() = runTest {
        // Arrange
        coEvery { search.get(any()) } returns testNote

        // Act
        viewModel.trySaveNote(title = "name", content = "text")
        advanceUntilIdle()

        // Assert
        assertEquals(SaveState.AlreadyExists, viewModel.saveState.value)
    }
}