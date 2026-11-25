package com.example

import com.example.domain.NumberConverterEngine
import com.example.domain.NumberSystem
import com.example.domain.QuizDifficulty
import com.example.domain.QuizEngine
import com.example.domain.StepByStepExplanationEngine
import com.example.domain.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NumixEngineUnitTest {

    @Test
    fun `verify all required 45 conversions across bases`() {
        // 101101 Binary = 45 Decimal
        val (_, binToDec) = NumberConverterEngine.convert("101101", NumberSystem.BINARY, NumberSystem.DECIMAL)
        assertEquals("45", binToDec?.decimal)

        // 55 Octal = 45 Decimal
        val (_, octToDec) = NumberConverterEngine.convert("55", NumberSystem.OCTAL, NumberSystem.DECIMAL)
        assertEquals("45", octToDec?.decimal)

        // 2D Hexadecimal = 45 Decimal (also lowercase 2d)
        val (_, hexToDec) = NumberConverterEngine.convert("2d", NumberSystem.HEXADECIMAL, NumberSystem.DECIMAL)
        assertEquals("45", hexToDec?.decimal)

        // 45 Decimal = 101101 Binary, 55 Octal, 2D Hexadecimal
        val (_, decToAll) = NumberConverterEngine.convert("45", NumberSystem.DECIMAL, NumberSystem.BINARY)
        assertNotNull(decToAll)
        assertEquals("101101", decToAll!!.binary)
        assertEquals("55", decToAll.octal)
        assertEquals("2D", decToAll.hexadecimal)
    }

    @Test
    fun `verify all 12 conversion directions and dynamic bilingual explanations`() {
        val sampleFor45 = mapOf(
            NumberSystem.BINARY to "101101",
            NumberSystem.OCTAL to "55",
            NumberSystem.DECIMAL to "45",
            NumberSystem.HEXADECIMAL to "2D"
        )

        var directionCount = 0
        for (src in NumberSystem.entries) {
            for (tgt in NumberSystem.entries) {
                if (src == tgt) continue
                directionCount++
                val input = sampleFor45.getValue(src)
                val expected = sampleFor45.getValue(tgt)
                val (validation, result) = NumberConverterEngine.convert(input, src, tgt)
                assertTrue("Validation failed for $src -> $tgt", validation is ValidationResult.Valid)
                assertNotNull(result)
                assertEquals("Conversion failed for $src -> $tgt", expected, result!!.valueFor(tgt))

                val explanation = StepByStepExplanationEngine.generate(result)
                assertTrue("Steps empty for $src -> $tgt", explanation.steps.isNotEmpty())
                assertTrue(explanation.whyItWorks.en.isNotBlank())
                assertTrue(explanation.whyItWorks.bn.isNotBlank())
                assertTrue(explanation.finalAnswerExpression.contains(expected))
            }
        }
        assertEquals(12, directionCount)
    }

    @Test
    fun `verify arbitrary precision BigInteger beyond Long MAX_VALUE`() {
        // 2^80 + 45 = 1208925819614629174706221
        val hugeDecimal = "1208925819614629174706221"
        val (validation, result) = NumberConverterEngine.convert(hugeDecimal, NumberSystem.DECIMAL, NumberSystem.HEXADECIMAL)
        assertTrue(validation is ValidationResult.Valid)
        assertNotNull(result)
        assertEquals("10000000000000000002D", result!!.hexadecimal)

        // Round-trip back from Hex to Decimal
        val (_, roundTrip) = NumberConverterEngine.convert(result.hexadecimal, NumberSystem.HEXADECIMAL, NumberSystem.DECIMAL)
        assertEquals(hugeDecimal, roundTrip?.decimal)
    }

    @Test
    fun `verify negative integers, zero, leading zeros, and strict validation`() {
        val (_, negRes) = NumberConverterEngine.convert("-2d", NumberSystem.HEXADECIMAL, NumberSystem.DECIMAL)
        assertEquals("-45", negRes?.decimal)
        assertEquals("-101101", negRes?.binary)
        assertEquals("-55", negRes?.octal)

        val (_, zeroNeg) = NumberConverterEngine.convert("-000", NumberSystem.BINARY, NumberSystem.HEXADECIMAL)
        assertEquals("0", zeroNeg?.hexadecimal)
        assertFalse(zeroNeg!!.isNegative)

        val (fracVal, _) = NumberConverterEngine.convert("45.5", NumberSystem.DECIMAL, NumberSystem.BINARY)
        assertTrue(fracVal is ValidationResult.FractionalNotSupported)

        val (invalidBin, _) = NumberConverterEngine.convert("1021", NumberSystem.BINARY, NumberSystem.DECIMAL)
        assertTrue(invalidBin is ValidationResult.InvalidCharacters)

        val (invalidOct, _) = NumberConverterEngine.convert("789", NumberSystem.OCTAL, NumberSystem.DECIMAL)
        assertTrue(invalidOct is ValidationResult.InvalidCharacters)
    }

    @Test
    fun `verify algorithmic quiz generation and validation`() {
        QuizDifficulty.entries.forEach { diff ->
            val q = QuizEngine.generateQuestion(diff)
            assertTrue(QuizEngine.checkAnswer(q.correctAnswer, q))
            assertTrue(QuizEngine.checkAnswer(q.correctAnswer.lowercase(), q))
            assertTrue(q.explanation.steps.isNotEmpty())
        }
    }
}
