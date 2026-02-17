package com.audio.test.ui.screens.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.audio.test.R
import com.audio.test.ui.components.AppButton
import com.audio.test.ui.components.ScoreIndicator

@Composable
fun ResultScreen(
    viewModel: ResultViewModel = hiltViewModel(),
    onNavigateHome: () -> Unit
) {
    val session by viewModel.session.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        session?.let { currentSession ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = stringResource(R.string.result_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Big Score Circle
                BigScoreCircle(score = currentSession.score)
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Line-by-Line Results
                Text(
                    text = stringResource(R.string.result_breakdown_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                currentSession.lines.forEachIndexed { index, lineResult ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${stringResource(R.string.result_line_prefix)} ${index + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "${lineResult.accuracy}%",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (lineResult.accuracy >= 80) Color(0xFF00E676) else if (lineResult.accuracy >= 50) Color(0xFFFFC107) else Color(0xFFFF5252)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val annotatedString = buildAnnotatedString {
                                lineResult.words.forEach { word ->
                                    val color = if (word.isMatched) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    withStyle(style = SpanStyle(color = color, fontWeight = if (word.isMatched) FontWeight.Bold else FontWeight.Normal)) {
                                        append(word.originalWord)
                                    }
                                    append(" ")
                                }
                            }
                            
                            Text(text = annotatedString, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(modifier = Modifier.fillMaxWidth()) {
                    AppButton(
                        text = stringResource(R.string.result_home_button),
                        onClick = onNavigateHome,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } ?: run {
             Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                 CircularProgressIndicator()
             }
        }
    }
}

@Composable
fun BigScoreCircle(score: Int) {
    val color = when {
        score >= 80 -> Color(0xFF00E676)
        score >= 50 -> Color(0xFFFFC107)
        else -> Color(0xFFFF5252)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(200.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.1f))
            .background(MaterialTheme.colorScheme.surface.copy(alpha=0.5f)) // layering
    ) {
         CircularProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier.size(200.dp),
            color = color,
            strokeWidth = 12.dp,
            trackColor = color.copy(alpha = 0.2f),
        )
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score%",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = stringResource(R.string.result_match_label),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
