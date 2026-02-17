package com.learn.easy.domain.usecase

import com.learn.easy.domain.model.Line
import com.learn.easy.domain.model.Word
import javax.inject.Inject

class ParseTextToLinesUseCase @Inject constructor() {

    operator fun invoke(text: String): List<Line> {
        // Split by newlines or punctuation to get sentences/lines
        val rawLines = text.trim().split(Regex("(?<=[.!?])\\s+"))

        return rawLines.mapIndexed { index, lineText ->
            val words = lineText.trim().split("\\s+".toRegex()).map {
                Word(it, false)
            }
            Line(index, lineText.trim(), words)
        }
    }
}
