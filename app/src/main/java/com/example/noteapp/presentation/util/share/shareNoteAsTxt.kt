package com.example.noteapp.presentation.util.share

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.noteapp.R
import com.example.noteapp.data.local.entities.Note
import java.io.File

fun shareNoteAsTxt(context: Context, note: Note){
    // temporal file in cache
    val file = File(context.cacheDir, "${note.name}.txt")
    file.writeText(note.content)

    // secure URI with fileprovider
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = context.getString(R.string.share_text_plain)
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_note_text)))
}