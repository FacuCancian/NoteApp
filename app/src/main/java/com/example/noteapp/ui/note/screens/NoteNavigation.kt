package com.example.noteapp.ui.note.screens

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.noteapp.R
import com.example.noteapp.presentation.noteList.NoteListViewModel
import com.example.noteapp.ui.note.NoteScreen
import com.example.noteapp.ui.note.components.TopBarState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteApp(
    navController: NavHostController = rememberNavController()
){
    var topBarState by remember { mutableStateOf(TopBarState()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(topBarState.title) },
                navigationIcon = {
                    if (topBarState.showBack && topBarState.onBack != null) {
                        IconButton(onClick = topBarState.onBack!!) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    }
                },
                actions = {
                    if (topBarState.showSave && topBarState.onSave != null) {
                        IconButton(onClick = topBarState.onSave!!) {
                            Icon(Icons.Default.Check, contentDescription = "Guardar")
                        }
                    }
                }
            )
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = NoteScreen.Start.name,
            modifier = Modifier.padding(padding)
        ) {
            composable(route = NoteScreen.Start.name) {
                val appName = stringResource(R.string.app_name)

                LaunchedEffect(Unit) {
                    topBarState = TopBarState(
                        title = appName
                    )
                }

                NoteList(
                    noteListViewModel = hiltViewModel(),
                    modifier = Modifier,
                    onNoteClick = { note ->
                        navController.navigate("${NoteScreen.NoteInfo.name}/${note.id}")
                    },
                    onButtonClick = {
                        navController.navigate(NoteScreen.New.name)
                    }
                )
            }
            composable(
                route = "${NoteScreen.NoteInfo.name}/{noteId}",//route with parameter
                arguments = listOf(navArgument("noteId") {
                    type = NavType.IntType
                })//listOf bc could e more arguments
            ) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getInt("noteId")
                    ?: return@composable//recover argument or dont go to composable
                val viewModel: NoteListViewModel = hiltViewModel()
                val notes by viewModel.notes.collectAsState()

                // find note to show on screen
                val note = notes.firstOrNull { it.id == noteId }
                note?.let {
                    NoteInfo(
                        note = it,
                        onSave = { updatedNote ->
                            viewModel.insertNote(
                                updatedNote
                            )
                        },
                        onNoteSaved = { navController.popBackStack() },
                        setTopBar = { topBarState = it }

                    )
                }
            }
            composable(route = NoteScreen.New.name) {
                NewNote(
                    viewModel = hiltViewModel(),
                    back = { navController.popBackStack() },
                    setTopBar = { topBarState = it }
                )
            }
        }
    }

}