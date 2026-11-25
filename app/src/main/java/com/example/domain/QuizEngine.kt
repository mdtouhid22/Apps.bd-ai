package com.example.domain

import java.math.BigInteger
import kotlin.random.Random

enum class QuizDifficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED
}

enum class QuizInputMode {
    MULTIPLE_CHOICE,
    TYPE_ANSWER
}

data class QuizQuestion(
    val id: Long,
    val promptNumber: String,
    val sourceSystem: NumberSystem,
    val targetSystem: NumberSystem,
    val correctAnswer: String,
    val options: List<String>,
    val inputMode: QuizInputMode,
    val difficulty: QuizDifficulty,
    val explanation: ConversionExplanation
)

object QuizEngine {

    private var questionCounter = 1L

    fun generateQuestion(
        difficulty: QuizDifficulty,
        preferredMode: QuizInputMode? = null,
        random: Random = Random.Default
    ): QuizQuestion {
        val (src, tgt) = pickBasePair(difficulty, random)
        val value = pickValue(difficulty, random)

        val decimalInput = value.toString(10)
        val (_, sourceConv) = NumberConverterEngine.convert(
            decimalInput,
            NumberSystem.DECIMAL,
            src
        )
        val sourceFormatted = sourceConv!!.valueFor(src)

        val (_, targetConv) = NumberConverterEngine.convert(
            sourceFormatted,
            src,
            tgt
        )
        val convResult = targetConv!!
        val correctAnswer = convResult.valueFor(tgt)
        val explanation = StepByStepExplanationEngine.generate(convResult)

        val mode = preferredMode ?: if (random.nextBoolean()) {
            QuizInputMode.MULTIPLE_CHOICE
        } else {
            QuizInputMode.TYPE_ANSWER
        }

        val options = if (mode == QuizInputMode.MULTIPLE_CHOICE) {
            buildSmartOptions(value, tgt, correctAnswer, random)
        } else {
            emptyList()
        }

        return QuizQuestion(
            id = questionCounter++,
            promptNumber = sourceFormatted,
            sourceSystem = src,
            targetSystem = tgt,
            correctAnswer = correctAnswer,
            options = options,
            inputMode = mode,
            difficulty = difficulty,
            explanation = explanation
        )
    }

    private fun pickBasePair(
        difficulty: QuizDifficulty,
        random: Random
    ): Pair<NumberSystem, NumberSystem> {
        val pairs = when (difficulty) {
            QuizDifficulty.BEGINNER -> listOf(
                NumberSystem.BINARY to NumberSystem.DECIMAL,
                NumberSystem.DECIMAL to NumberSystem.BINARY,
                NumberSystem.OCTAL to NumberSystem.DECIMAL,
                NumberSystem.DECIMAL to NumberSystem.OCTAL,
                NumberSystem.HEXADECIMAL to NumberSystem.DECIMAL,
                NumberSystem.DECIMAL to NumberSystem.HEXADECIMAL
            )
            QuizDifficulty.INTERMEDIATE, QuizDifficulty.ADVANCED -> {
                val all = mutableListOf<Pair<NumberSystem, NumberSystem>>()
                for (s in NumberSystem.entries) {
                    for (t in NumberSystem.entries) {
                        if (s != t) all.add(s to t)
                    }
                }
                all
            }
        }
        return pairs[random.nextInt(pairs.size)]
    }

    private fun pickValue(difficulty: QuizDifficulty, random: Random): BigInteger {
        return when (difficulty) {
            QuizDifficulty.BEGINNER -> BigInteger.valueOf(random.nextInt(5, 64).toLong())
            QuizDifficulty.INTERMEDIATE -> BigInteger.valueOf(random.nextInt(64, 4096).toLong())
            QuizDifficulty.ADVANCED -> {
                val mag = random.nextLong(4096L, 1_048_575L)
                if (random.nextInt(4) == 0) BigInteger.valueOf(-mag) else BigInteger.valueOf(mag)
            }
        }
    }

    private fun buildSmartOptions(
        trueValue: BigInteger,
        targetSystem: NumberSystem,
        correctAnswer: String,
        random: Random
    ): List<String> {
        val candidates = linkedSetOf(correctAnswer)
        val absVal = trueValue.abs()
        val isNeg = trueValue < BigInteger.ZERO
        val sign = if (isNeg) "-" else ""

        val deltas = listOf(1L, -1L, 2L, -2L, 4L, -4L, 8L, 16L, -8L)
        for (delta in deltas.shuffled(random)) {
            if (candidates.size >= 4) break
            val mutated = (absVal + BigInteger.valueOf(delta)).max(BigInteger.ONE)
            val formatted = sign + mutated.toString(targetSystem.base).uppercase()
            if (formatted != correctAnswer) {
                candidates.add(formatted)
            }
        }

        // Also try bit reversal or digit swap for plausible distractors
        var guard = 0
        while (candidates.size < 4 && guard < 20) {
            val offset = random.nextInt(3, 25).toLong()
            val candidateVal = (absVal + BigInteger.valueOf(offset))
            candidates.add(sign + candidateVal.toString(targetSystem.base).uppercase())
            guard++
        }

        return candidates.toList().shuffled(random)
    }

    fun checkAnswer(userAnswer: String, question: QuizQuestion): Boolean {
        val validation = NumberConverterEngine.validateAndParse(userAnswer, question.targetSystem)
        if (validation !is ValidationResult.Valid) return false
        val normalizedUser = (if (validation.isNegative) "-" else "") + validation.normalizedMagnitude
        return normalizedUser == question.correctAnswer
    }
}
