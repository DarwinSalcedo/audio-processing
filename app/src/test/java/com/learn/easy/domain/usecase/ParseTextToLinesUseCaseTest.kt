package com.learn.easy.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class ParseTextToLinesUseCaseTest {

    private lateinit var useCase: ParseTextToLinesUseCase

    @Before
    fun setUp() {
        useCase = ParseTextToLinesUseCase()
    }

    @Test
    fun `given simple sentence when parsing then returns single line`() {
        // Given
        val text = "This is a test."

        // When
        val result = useCase(text)

        // Then
        assertEquals(1, result.size)
        assertEquals("This is a test.", result[0].text)
        assertEquals(4, result[0].words.size)
        assertEquals("This", result[0].words[0].text)
    }

    @Test
    fun `given multiple sentences when parsing then returns multiple lines`() {
        // Given
        val text = "First sentence. Second sentence! Third one?"

        // When
        val result = useCase(text)

        // Then
        assertEquals(3, result.size)
        assertEquals("First sentence.", result[0].text)
        assertEquals("Second sentence!", result[1].text)
        assertEquals("Third one?", result[2].text)
    }

    @Test
    fun `given text with extra spaces when parsing then trims correctly`() {
        // Given
        val text = "  Space start.   Space middle. Space end.  "

        // When
        val result = useCase(text)

        // Then
        assertEquals(3, result.size)
        assertEquals("Space start.", result[0].text)
        assertEquals("Space middle.", result[1].text)
        assertEquals("Space end.", result[2].text)
    }

    @Test
    fun `given text without punctuation when parsing then returns single line`() {
        // Given
        val text = "No punctuation here"

        // When
        val result = useCase(text)

        // Then
        assertEquals(1, result.size)
        assertEquals("No punctuation here", result[0].text)
    }
}
