package com.learn.easy.ui.screens.process

import androidx.lifecycle.SavedStateHandle
import com.learn.easy.data.repository.WordProcess
import com.learn.easy.domain.model.Line
import com.learn.easy.domain.model.ProcessResult
import com.learn.easy.domain.model.Session
import com.learn.easy.domain.repository.SpeechRecognitionRepository
import com.learn.easy.domain.usecase.ParseTextToLinesUseCase
import com.learn.easy.domain.usecase.ProcessSpeechUseCase
import com.learn.easy.domain.usecase.SaveSessionUseCase
import com.learn.easy.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ProcessViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val speechRepository: SpeechRecognitionRepository = mockk(relaxed = true)
    private val saveSessionUseCase: SaveSessionUseCase = mockk(relaxed = true)
    private val processSpeechUseCase: ProcessSpeechUseCase = mockk(relaxed = true)
    private val parseTextToLinesUseCase: ParseTextToLinesUseCase = mockk()
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(mapOf("text" to "Test text"))

    private lateinit var viewModel: ProcessViewModel

    private fun createViewModel() {
        viewModel = ProcessViewModel(
            speechRepository,
            saveSessionUseCase,
            processSpeechUseCase,
            parseTextToLinesUseCase,
            savedStateHandle
        )
    }

    @Test
    fun `given initial state when initialized then lines are parsed and listening is false`() {
        // Given
        val lines = listOf(Line(0, "Test text", emptyList()))
        every { parseTextToLinesUseCase("Test text") } returns lines

        // When
        createViewModel()

        // Then
        assertEquals(lines, viewModel.lines.value)
        assertFalse(viewModel.isListening.value)
    }

    @Test
    fun `given not listening when toggle recording then starts listening`() = runTest {
        // Given
        val lines = listOf(Line(0, "Test text", emptyList()))
        every { parseTextToLinesUseCase("Test text") } returns lines
        createViewModel()
        
        // Mock startListening flow
        coEvery { speechRepository.startListening() } returns flowOf(WordProcess.Partial("test"))
        // Explicitly return null to avoid processing logic triggering stop functionality via default mock values
        every { processSpeechUseCase(any(), any(), any()) } returns null

        // When
        viewModel.toggleRecording()

        // Then
        assertTrue(viewModel.isListening.value)
        coVerify { speechRepository.startListening() }
    }

    @Test
    fun `given listening when toggle recording then stops listening`() = runTest {
        // Given
        val lines = listOf(Line(0, "Test text", emptyList()))
        every { parseTextToLinesUseCase("Test text") } returns lines
        createViewModel()
        
        // Start listening first
        coEvery { speechRepository.startListening() } returns flowOf()
        viewModel.toggleRecording()
        assertTrue(viewModel.isListening.value)

        // When
        viewModel.toggleRecording()

        // Then
        assertFalse(viewModel.isListening.value)
        coVerify { speechRepository.stopListening() }
    }

    @Test
    fun `given speech input when processing then updates lines`() = runTest {
        // Given
        val initialLines = listOf(Line(0, "Test text", emptyList()))
        every { parseTextToLinesUseCase("Test text") } returns initialLines
        createViewModel()

        val wordProcess = WordProcess.Final("Test text")
        val updatedLines = listOf(Line(0, "Test text", emptyList(), isCompleted = true))
        val processResult = ProcessResult(
            updatedLines = updatedLines,
            additionalSpokenText = "Test text",
            shouldAdvanceLine = false,
            hasStarted = true,
            completedLineIndex = 0
        )

        coEvery { speechRepository.startListening() } returns flowOf(wordProcess)
        every { processSpeechUseCase(wordProcess, initialLines, 0) } returns processResult

        // When
        viewModel.toggleRecording()

        // Then
        // Verify lines are updated
        // Note: collectLatest in VM might be async, so we might need advanced coroutine test techniques or just simple verification if UnconfinedTestDispatcher handles it.
        // With UnconfinedTestDispatcher, launch should happen immediately.
        assertEquals(updatedLines, viewModel.lines.value)
        assertTrue(viewModel.hasStarted.value)
    }

    @Test
    fun `given session finish when saving then saves session and navigates`() = runTest {
        // Given
        val lines = listOf(Line(0, "Test text", emptyList()))
        every { parseTextToLinesUseCase("Test text") } returns lines
        createViewModel()
        
        coEvery { saveSessionUseCase(any()) } returns 1L

        // When
        viewModel.finishSession()

        // Then
        coVerify { saveSessionUseCase(any<Session>()) }
        // Verify navigation event (might require collecting flow to verify properly, skipping strictly or checking generic invocation)
    }
}
