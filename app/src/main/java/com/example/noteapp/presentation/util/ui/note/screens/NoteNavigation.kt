package com.example.noteapp.presentation.util.ui.note.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.noteapp.presentation.login.NoteListUiState
import com.example.noteapp.presentation.noteList.NoteListViewModel
import com.example.noteapp.presentation.util.ui.note.NoteScreen
import com.example.noteapp.presentation.util.ui.note.components.TopBarState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteApp(
    navController: NavHostController = rememberNavController()
) {
    var topBarState by remember { mutableStateOf(TopBarState()) }
    var fieldValue by remember { mutableStateOf(TextFieldValue("")) }
    LaunchedEffect(topBarState.title) {
        if (topBarState.title != fieldValue.text) {
            fieldValue = TextFieldValue(
                text = topBarState.title,
                selection = TextRange(topBarState.title.length) // cursor al final
            )
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (topBarState.editableTitle && topBarState.onTitleChange != null) {
                        BasicTextField(
                            value = fieldValue,
                            onValueChange = { newValue ->
                                fieldValue = newValue
                                topBarState.onTitleChange!!.invoke(newValue.text)
                            },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                            decorationBox = { innerTextField ->
                                Box {
                                    if (topBarState.title.isEmpty()) {
                                        Text(
                                            "Nueva nota",
                                            style = TextStyle(
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface.copy(
                                                    alpha = 0.4f
                                                )
                                            )
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                    } else {
                        Text(topBarState.title)
                    }
                },
                navigationIcon = {
                    if (topBarState.showBack && topBarState.onBack != null) {
                        IconButton(onClick = topBarState.onBack!!) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    }
                },
                actions = {
                    if (topBarState.showGridToggle && topBarState.onToggleGrid != null) {
                        IconButton(onClick = topBarState.onToggleGrid!!) {
                            Icon(
                                imageVector = if (topBarState.isGridView) Icons.Default.List else Icons.Default.GridView,
                                contentDescription = "Cambiar vista"
                            )
                        }
                    }
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
                val noteListViewModel: NoteListViewModel = hiltViewModel()
                NoteList(
                    noteListViewModel = noteListViewModel,
                    onNoteClick = { note ->
                        navController.navigate("${NoteScreen.NoteInfo.name}/${note.id}")
                    },
                    onButtonClick = {
                        navController.navigate(NoteScreen.New.name)
                    },
                    setTopBar = { topBarState = it }
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
                val uiState by viewModel.uiState.collectAsState()

                val note = when (uiState) {
                    is NoteListUiState.Success -> (uiState as NoteListUiState.Success).notes.firstOrNull { it.id == noteId }
                    else -> null
                }
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