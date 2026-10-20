package com.example.ui.hud

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VirtualFile
import com.example.ui.theme.JarvisAmber
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisGreenNominal
import com.example.ui.theme.JarvisHudBackground
import com.example.ui.theme.JarvisHudBorder
import com.example.ui.theme.JarvisHudSurface
import com.example.ui.theme.JarvisHudSurfaceElevated
import com.example.ui.theme.JarvisRedAlert
import com.example.ui.theme.JarvisTextDim
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.viewmodel.JarvisViewModel

@Composable
fun HudFilesTab(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val files by viewModel.workspaceFiles.collectAsState()
    val selectedFile by viewModel.selectedFile.collectAsState()
    val editorContent by viewModel.fileEditorContent.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisHudBackground)
            .padding(12.dp)
    ) {
        if (selectedFile != null) {
            // File Editor Screen
            FileEditorView(
                file = selectedFile!!,
                content = editorContent,
                onContentChange = { viewModel.onFileEditorContentChange(it) },
                onSave = { viewModel.saveCurrentFile() },
                onDelete = { viewModel.deleteSelectedFile() },
                onBack = { viewModel.closeFileEditor() }
            )
        } else {
            // Files List Screen
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(JarvisHudSurfaceElevated)
                    .border(1.dp, JarvisHudBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Workspace Files",
                        tint = JarvisAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "STARK_WORKSPACE // PROTOCOLS",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = JarvisAmber
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.refreshFiles() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Files",
                            tint = JarvisCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            newFileName = "protocols/new_protocol_${System.currentTimeMillis() % 1000}.sys"
                            showCreateDialog = true
                        },
                        modifier = Modifier
                            .testTag("create_file_btn")
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New File",
                            tint = JarvisCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(files, key = { it.path }) { file ->
                    FileRowItem(
                        file = file,
                        onClick = { viewModel.selectFile(file) }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text(
                    text = "CREATE NEW STARK DOCUMENT",
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisCyan
                )
            },
            text = {
                OutlinedTextField(
                    value = newFileName,
                    onValueChange = { newFileName = it },
                    label = { Text("Relative Path (e.g. scripts/task.py)", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JarvisCyan,
                        unfocusedBorderColor = JarvisHudBorder,
                        focusedTextColor = JarvisTextPrimary,
                        unfocusedTextColor = JarvisTextPrimary
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFileName.isNotBlank()) {
                            viewModel.createNewFile(newFileName, "# Initialized Stark Protocol\n")
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = Color.Black)
                ) {
                    Text("CREATE", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("CANCEL", color = JarvisTextSecondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            },
            containerColor = JarvisHudSurface,
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
private fun FileRowItem(
    file: VirtualFile,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(JarvisHudSurface)
            .border(1.dp, JarvisHudBorder, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = when (file.extension) {
                    "sys" -> JarvisCyan
                    "json" -> JarvisGold
                    "py" -> JarvisGreenNominal
                    else -> JarvisTextSecondary
                },
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = file.name,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = JarvisTextPrimary
                )
                Text(
                    text = "${file.path} • ${file.sizeBytes} bytes",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = JarvisTextDim
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(JarvisHudSurfaceElevated)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = ".${file.extension.uppercase()}",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = JarvisCyan
            )
        }
    }
}

@Composable
private fun FileEditorView(
    file: VirtualFile,
    content: String,
    onContentChange: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(JarvisHudSurfaceElevated)
                .border(1.dp, JarvisHudBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = JarvisCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = file.name,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = JarvisCyan
                )
            }

            Row {
                IconButton(onClick = onSave, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save File",
                        tint = JarvisGreenNominal,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete File",
                        tint = JarvisRedAlert,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = content,
            onValueChange = onContentChange,
            modifier = Modifier
                .fillMaxSize()
                .testTag("file_content_editor"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = JarvisCyan,
                unfocusedBorderColor = JarvisHudBorder,
                focusedTextColor = JarvisTextPrimary,
                unfocusedTextColor = JarvisTextPrimary,
                cursorColor = JarvisCyan,
                focusedContainerColor = Color(0xFF030913),
                unfocusedContainerColor = Color(0xFF030913)
            ),
            shape = RoundedCornerShape(8.dp),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        )
    }
}
