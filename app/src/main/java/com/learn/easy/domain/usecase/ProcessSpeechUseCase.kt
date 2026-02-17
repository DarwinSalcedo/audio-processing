package com.learn.easy.domain.usecase

import com.learn.easy.data.repository.WordProcess
import com.learn.easy.domain.model.Line
import com.learn.easy.domain.model.ProcessResult
import javax.inject.Inject

class ProcessSpeechUseCase @Inject constructor() {

    operator fun invoke(
        spoken: WordProcess,
        currentLines: List<Line>,
        currentLineIndex: Int
    ): ProcessResult? {
        if (currentLineIndex >= currentLines.size) return null
        if (spoken.words.trim().isEmpty()) return null

        val currentLine = currentLines[currentLineIndex]
        val spokenWords = spoken.words.lowercase().split("\\s+".toRegex()).filter { it.isNotEmpty() }
        var spokenIndex = 0

        val updatedWords = currentLine.words.map { originalWord ->
            val cleanOriginal = originalWord.text.lowercase().replace(Regex("[^a-z0-9]"), "")
            if (cleanOriginal.isEmpty()) {
                originalWord.copy(isMatched = false)
            } else {
                var isMatch = false
                // Progressive matching: find the first occurrence of the target word
                // in the spoken words starting from the last matched position.
                for (i in spokenIndex until spokenWords.size) {
                    if (spokenWords[i] == cleanOriginal) {
                        isMatch = true
                        spokenIndex = i + 1
                        break
                    }
                }
                originalWord.copy(isMatched = isMatch)
            }
        }

        if (spoken is WordProcess.Partial) {
            // PARTIAL: Only update visual matching
            val updatedLine = currentLine.copy(words = updatedWords)
            val newLines = currentLines.toMutableList()
            newLines[currentLineIndex] = updatedLine
            
            return ProcessResult(
                updatedLines = newLines,
                additionalSpokenText = null,
                shouldAdvanceLine = false,
                hasStarted = true
            )
        } else if (spoken is WordProcess.Final) {
            // FINAL: Re-evaluate with final text, commit score, and ADVANCE
            val matchedCount = updatedWords.count { it.isMatched }
            val totalWords = updatedWords.size
            val accuracy = if (totalWords > 0) (matchedCount * 100) / totalWords else 0

            val updatedLine = currentLine.copy(
                words = updatedWords,
                accuracy = accuracy,
                isCompleted = true
            )

            val newLines = currentLines.toMutableList()
            newLines[currentLineIndex] = updatedLine

            val shouldAdvance = currentLineIndex < currentLines.size - 1

            return ProcessResult(
                updatedLines = newLines,
                additionalSpokenText = spoken.words,
                shouldAdvanceLine = shouldAdvance,
                hasStarted = true,
                completedLineIndex = currentLineIndex
            )
        }
        return null
    }
}
