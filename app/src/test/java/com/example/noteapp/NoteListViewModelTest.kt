package com.example.noteapp

import com.example.noteapp.data.local.entities.Note
import com.example.noteapp.domain.useCase.DeleteNote
import com.example.noteapp.domain.useCase.GetAllNotes
import com.example.noteapp.domain.useCase.GetNoteByName
import com.example.noteapp.domain.useCase.InsertNote
import com.example.noteapp.presentation.noteList.NoteListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

/*@OptIn(ExperimentalCoroutinesApi::class)
class NoteListViewModelTest {
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepo : FakeRepository
    private lateinit var viewModel : NoteListViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeRepository()
        viewModel = NoteListViewModel(
            GetAllNotes(fakeRepo),
            InsertNote(fakeRepo),
            DeleteNote(fakeRepo),
            GetNoteByName(fakeRepo)
        )
    }
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    @Test
    fun `add an item to the list of items`() = runTest{

        viewModel.insertNote("content of note", "note1")
        advanceUntilIdle()//wait to all corrutines bc using "StandardTestDispatcher"

        val notaTest = Note(null,"content of note", "note1")
        val notes = viewModel.notes.value
        assertEquals(notes.first(), notaTest)
    }
    @Test
    fun `delete an item from the list`() = runTest{

        viewModel.insertNote("content of note", "note1")
        advanceUntilIdle()//wait to all corrutines bc using "StandardTestDispatcher"

        val notaTest = Note(null,"content of note", "note1")
        viewModel.deleteNote(notaTest)
        advanceUntilIdle()
        val notes = viewModel.notes.value
        assertFalse(notes.contains(notaTest))// not in the list
    }
    @Test
    fun `get all elements from the list`() = runTest{

        viewModel.insertNote("content of note", "note1")
        viewModel.insertNote("content of note", "note2")
        advanceUntilIdle()//wait to all corrutines bc using "StandardTestDispatcher"

        advanceUntilIdle()
        val notes = viewModel.notes.value
        assertNotNull(notes)
        println(notes)
    }
    @Test
    fun `get by name from the list`() = runTest{

        viewModel.insertNote("content of note", "note1")
        val note = viewModel.notes.value
        advanceUntilIdle()

        val notes = viewModel.notes.value

        assertTrue(notes.isNotEmpty())
        assertTrue(notes.any { it?.name == "note1" && it.content == "content of note" })

        println(notes)
    }



}*/