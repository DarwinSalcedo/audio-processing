package com.learn.easy.domain.usecase

import com.learn.easy.data.repository.WordProcess
import com.learn.easy.domain.model.Line
import com.learn.easy.domain.model.Word
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProcessSpeechUseCaseTest {

    private lateinit var useCase: ProcessSpeechUseCase

    @Before
    fun setUp() {
        useCase = ProcessSpeechUseCase()
    }

    @Test
    fun `given partial match when matching word then word is matched`() {
        // Given
        val givenLine = createLine("The first word")
        val currentLines = listOf(givenLine)
        val currentIndex = 0
        val spokenText = "The"
        val wordProcess = WordProcess.Partial(spokenText)

        // When
        val result = useCase(wordProcess, currentLines, currentIndex)

        // Then
        assertNotNull(result)
        val updatedLine = result!!.updatedLines[0]
        assertTrue(updatedLine.words[0].isMatched) // "The"
        assertFalse(updatedLine.words[1].isMatched) // "first"
        assertFalse(updatedLine.words[2].isMatched) // "word"
    }

    @Test
    fun `given repeating words when matching then progressive matching is applied`() {
        // Given
        val givenLine = createLine("The first word is not the last word")
        val currentLines = listOf(givenLine)
        val currentIndex = 0
        val spokenText = "The first word" // matched indices: 0, 1, 2
        // If we said "word" again, it should match index 7, but here we just test that strict order is respected if we had repeated input.
        // Let's test the specific bug case: "The" matches index 0, should NOT match index 5.
        val spokenTextBugCase = "The"
        val wordProcess = WordProcess.Partial(spokenTextBugCase)

        // When
        val result = useCase(wordProcess, currentLines, currentIndex)

        // Then
        assertNotNull(result)
        val updatedLine = result!!.updatedLines[0]
        assertTrue(updatedLine.words[0].isMatched) // "The" (index 0)
        assertTrue(updatedLine.words[1].text == "first")
        assertFalse(updatedLine.words[5].isMatched) // "the" (index 5) - Should NOT be matched
    }

    @Test
    fun `given multiple repeating words sequence when matching then matches correctly`() {
        // Given
        val givenLine = createLine("The first word is not the last word")
        val currentLines = listOf(givenLine)
        val currentIndex = 0
        
        // "The" (0), "first"(1), "word"(2) ... "the"(5), "last"(6), "word"(7)
        // Spoken: "The word" -> matches "The"(0) and "word"(2). "first"(1) skipped.
        val spokenText = "The word"
        val wordProcess = WordProcess.Partial(spokenText)

        // When
        val result = useCase(wordProcess, currentLines, currentIndex)

        // Then
        assertNotNull(result)
        val updatedWords = result!!.updatedLines[0].words
        assertTrue(updatedWords[0].isMatched) // "The"
        assertFalse(updatedWords[1].isMatched) // "first" (skipped)
        assertTrue(updatedWords[2].isMatched) // "word"
        assertFalse(updatedWords[5].isMatched) // "the" (later)
        assertFalse(updatedWords[7].isMatched) // "word" (later)
    }

    @Test
    fun `given final result when processing then line is completed and accuracy calculated`() {
        // Given
        val givenLine = createLine("The first word")
        val currentLines = listOf(givenLine)
        val currentIndex = 0
        val spokenText = "The first word"
        val wordProcess = WordProcess.Final(spokenText)

        // When
        val result = useCase(wordProcess, currentLines, currentIndex)

        // Then
        assertNotNull(result)
        val updatedLine = result!!.updatedLines[0]
        assertTrue(updatedLine.isCompleted)
        assertEquals(100, updatedLine.accuracy)

        // Actually, logic is: shouldAdvance = currentLineIndex < currentLines.size - 1
        // Here 0 < 1 - 1 is FALSE. So it shouldn't advance?
        // Wait, logic in usecase: val shouldAdvance = currentLineIndex < currentLines.size - 1
        // If 1 line (size 1), index 0. 0 < 0 is false. Correct.
        assertFalse(result.shouldAdvanceLine)
        assertEquals(0, result.completedLineIndex)
    }

    @Test
    fun `given empty input when processing then returns null`() {
        // Given
        val givenLine = createLine("Some line")
        val currentLines = listOf(givenLine)
        val currentIndex = 0
        val spokenText = ""
        val wordProcess = WordProcess.Partial(spokenText)

        // When
        val result = useCase(wordProcess, currentLines, currentIndex)

        // Then
        assertEquals(null, result)
    }

    private fun createLine(text: String): Line {
        val words = text.split(" ").map { Word(it, false) }
        return Line(0, text, words)
    }
}
