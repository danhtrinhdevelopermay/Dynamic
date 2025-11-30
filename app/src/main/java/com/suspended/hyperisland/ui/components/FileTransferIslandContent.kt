package com.suspended.hyperisland.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suspended.hyperisland.model.FileTransferState
import com.suspended.hyperisland.model.IslandMode
import com.suspended.hyperisland.ui.theme.IslandColors

@Composable
fun FileTransferIslandContent(
    mode: IslandMode,
    fileTransferState: FileTransferState,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    when (mode) {
        IslandMode.COMPACT -> FileTransferCompact(fileTransferState)
        IslandMode.MEDIUM -> FileTransferMedium(fileTransferState, onAccept, onDecline)
        IslandMode.EXPANDED -> FileTransferExpanded(fileTransferState, onAccept, onDecline)
    }
}

@Composable
private fun FileTransferCompact(fileTransferState: FileTransferState) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = null,
            tint = IslandColors.AccentBlue,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun FileTransferMedium(
    fileTransferState: FileTransferState,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(IslandColors.AccentBlue.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                tint = IslandColors.AccentBlue,
                modifier = Modifier.size(22.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Received ${getFileDescription(fileTransferState.fileName)}",
                color = IslandColors.TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = "From ${fileTransferState.senderDevice}",
                color = IslandColors.TextSecondary,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(IslandColors.ButtonDecline)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDecline() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Decline",
                    color = IslandColors.TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(IslandColors.ButtonBlue)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onAccept() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Accept",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun FileTransferExpanded(
    fileTransferState: FileTransferState,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(IslandColors.AccentBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getFileIcon(fileTransferState.fileName),
                        contentDescription = null,
                        tint = IslandColors.AccentBlue,
                        modifier = Modifier.size(26.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = "Received ${getFileDescription(fileTransferState.fileName)}",
                        color = IslandColors.TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Text(
                        text = "From ${fileTransferState.senderDevice}",
                        color = IslandColors.TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onDecline,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = IslandColors.ButtonDecline
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Decline",
                    color = IslandColors.TextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Button(
                onClick = onAccept,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = IslandColors.ButtonBlue
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Accept",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private fun getFileDescription(fileName: String): String {
    val extension = fileName.substringAfterLast('.', "").lowercase()
    return when (extension) {
        "jpg", "jpeg", "png", "gif", "webp" -> "1 photo"
        "mp4", "mov", "avi", "mkv" -> "1 video"
        "mp3", "wav", "aac", "flac" -> "1 audio"
        "pdf" -> "1 PDF"
        "doc", "docx" -> "1 document"
        else -> "1 file"
    }
}

private fun getFileIcon(fileName: String): androidx.compose.ui.graphics.vector.ImageVector {
    val extension = fileName.substringAfterLast('.', "").lowercase()
    return when (extension) {
        "jpg", "jpeg", "png", "gif", "webp" -> Icons.Default.Image
        "mp4", "mov", "avi", "mkv" -> Icons.Default.VideoFile
        "mp3", "wav", "aac", "flac" -> Icons.Default.AudioFile
        "pdf" -> Icons.Default.PictureAsPdf
        else -> Icons.Default.InsertDriveFile
    }
}
