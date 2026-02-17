package com.learn.easy.ui.screens.process

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.learn.easy.R
import com.learn.easy.domain.model.Line
import com.learn.easy.ui.theme.BlueAccent
import com.learn.easy.ui.theme.RedError

@Composable
fun ProcessScreen(
    viewModel: ProcessViewModel = hiltViewModel(),
    onFinish: (Long) -> Unit
) {
    val lines by viewModel.lines.collectAsState()
    val currentLineIndex by viewModel.currentLineIndex.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val hasStarted by viewModel.hasStarted.collectAsState()

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.navigateToResult.collect { sessionId ->
            onFinish(sessionId)
        }
    }

    LaunchedEffect(currentLineIndex) {
        if (lines.isNotEmpty()) {
            listState.animateScrollToItem(currentLineIndex)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            if (!isListening && hasStarted) {
                // Show Control Buttons
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.resetSession() },
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = Color.White,
                        icon = { Icon(Icons.Default.Refresh, "Try Again") },
                        text = { Text("Try Again") }
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    ExtendedFloatingActionButton(
                        onClick = { viewModel.finishSession() },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        icon = { Icon(Icons.Default.Check, "Finish") },
                        text = { Text("Finish") }
                    )
                }
            } else {
                // Show Play/Stop
                FloatingActionButton(
                    onClick = { viewModel.toggleRecording() },
                    containerColor = if (isListening) RedError else MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Close else Icons.Default.PlayArrow,
                        contentDescription = if (isListening) "Stop" else "Start",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        },
        floatingActionButtonPosition = androidx.compose.material3.FabPosition.Center
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val headerText = when {
                !isListening && !hasStarted -> "Click on play to start"
                !isListening && hasStarted -> "Complete"
                else -> "Read Aloud"
            }

            Text(
                text = headerText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (lines.isNotEmpty()) {
                val progress = lines.count { it.isCompleted }.toFloat() / lines.size.toFloat()
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 100.dp) // Add padding for FAB
            ) {
                itemsIndexed(lines) { index, line ->
                    val isCurrent = index == currentLineIndex

                    LineItem(
                        line = line,
                        isCurrent = isCurrent
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun LineItem(line: Line, isCurrent: Boolean) {
    val annotatedString = buildAnnotatedString {
        line.words.forEachIndexed { index, word ->
            val isMatched = word.isMatched

            val wordColor = when {
                isMatched -> BlueAccent
                else -> MaterialTheme.colorScheme.onSurface
            }

            val weight = if (isMatched) FontWeight.Bold else FontWeight.Normal

            withStyle(
                style = SpanStyle(
                    color = wordColor,
                    fontWeight = weight,
                    fontSize = if (isCurrent) 28.sp else 20.sp
                )
            ) {
                append(word.text)
            }
            append(" ")
        }
    }

    val alpha = if (isCurrent) 1f else 0.4f

    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyLarge,
        lineHeight = if (isCurrent) 40.sp else 30.sp,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = alpha)
    )
}
