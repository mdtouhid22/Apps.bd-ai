package com.example.domain

import java.math.BigInteger

sealed class ValidationResult {
    data object Empty : ValidationResult()
    data object FractionalNotSupported : ValidationResult()
    data class InvalidCharacters(
        val invalidChars: String,
        val system: NumberSystem
    ) : ValidationResult()
    data object SignOnly : ValidationResult()
    data class Valid(
        val normalizedMagnitude: String,
        val isNegative: Boolean,
        val decimalValue: BigInteger
    ) : ValidationResult()
}

data class ConversionResult(
    val sourceSystem: NumberSystem,
    val targetSystem: NumberSystem,
    val rawInput: String,
    val normalizedSource: String,
    val isNegative: Boolean,
    val decimalBigInt: BigInteger,
    val binary: String,
    val octal: String,
    val decimal: String,
    val hexadecimal: String
) {
    fun valueFor(system: NumberSystem): String = when (system) {
        NumberSystem.BINARY -> binary
        NumberSystem.OCTAL -> octal
        NumberSystem.DECIMAL -> decimal
        NumberSystem.HEXADECIMAL -> hexadecimal
    }

    fun formattedValueFor(system: NumberSystem, groupDigits: Boolean): String {
        val raw = valueFor(system)
        if (!groupDigits) return raw
        val sign = if (raw.startsWith("-")) "-" else ""
        val body = raw.removePrefix("-")
        return sign + when (system) {
            NumberSystem.BINARY -> groupFromRight(body, 4, " ")
            NumberSystem.OCTAL -> groupFromRight(body, 3, " ")
            NumberSystem.DECIMAL -> groupFromRight(body, 3, ",")
            NumberSystem.HEXADECIMAL -> groupFromRight(body, 4, " ")
        }
    }

    companion object {
        fun groupFromRight(digits: String, groupSize: Int, separator: String): String {
            if (digits.length <= groupSize) return digits
            val sb = StringBuilder()
            var count = 0
            for (i in digits.length - 1 downTo 0) {
                if (count > 0 && count % groupSize == 0) {
                    sb.append(separator)
                }
                sb.append(digits[i])
                count++
            }
            return sb.reverse().toString()
        }
    }
}

object NumberConverterEngine {

    /**
     * Validates and parses an integer input of arbitrary size using BigInteger.
     * Supports leading/trailing whitespace, optional leading '+' or '-',
     * optional common radix prefixes (0b, 0o, 0x) only when matching the selected system,
     * leading zeros, and lowercase/uppercase hex digits.
     * Rejects fractional inputs (e.g. "10.5") explicitly rather than silently truncating.
     */
    fun validateAndParse(rawInput: String, sourceSystem: NumberSystem): ValidationResult {
        val trimmed = rawInput.trim().replace(" ", "").replace("_", "")
        if (trimmed.isEmpty()) return ValidationResult.Empty

        if (trimmed.contains('.')) {
            return ValidationResult.FractionalNotSupported
        }

        val isNegative = trimmed.startsWith("-")
        var body = when {
            trimmed.startsWith("-") || trimmed.startsWith("+") -> trimmed.substring(1)
            else -> trimmed
        }

        if (body.isEmpty()) return ValidationResult.SignOnly

        // Strip matching radix prefix if user pasted 0xFF, 0b101, 0o77
        val upperBody = body.uppercase()
        body = when {
            sourceSystem == NumberSystem.HEXADECIMAL && upperBody.startsWith("0X") && upperBody.length > 2 ->
                upperBody.substring(2)
            sourceSystem == NumberSystem.BINARY && upperBody.startsWith("0B") && upperBody.length > 2 ->
                upperBody.substring(2)
            sourceSystem == NumberSystem.OCTAL && upperBody.startsWith("0O") && upperBody.length > 2 ->
                upperBody.substring(2)
            else -> upperBody
        }

        val invalid = body.filter { !sourceSystem.isValidDigit(it) }.toSet()
        if (invalid.isNotEmpty()) {
            return ValidationResult.InvalidCharacters(
                invalidChars = invalid.joinToString(", "),
                system = sourceSystem
            )
        }

        // Normalize leading zeros while preserving "0"
        val strippedZeros = body.trimStart('0').ifEmpty { "0" }
        val effectiveNegative = isNegative && strippedZeros != "0"

        val magnitude = BigInteger(strippedZeros, sourceSystem.base)
        val signedValue = if (effectiveNegative) magnitude.negate() else magnitude

        return ValidationResult.Valid(
            normalizedMagnitude = strippedZeros,
            isNegative = effectiveNegative,
            decimalValue = signedValue
        )
    }

    fun convert(
        rawInput: String,
        sourceSystem: NumberSystem,
        targetSystem: NumberSystem
    ): Pair<ValidationResult, ConversionResult?> {
        val validation = validateAndParse(rawInput, sourceSystem)
        if (validation !is ValidationResult.Valid) {
            return validation to null
        }

        val value = validation.decimalValue
        val sign = if (validation.isNegative) "-" else ""
        val absValue = value.abs()

        val binary = sign + absValue.toString(2)
        val octal = sign + absValue.toString(8)
        val decimal = sign + absValue.toString(10)
        val hexadecimal = sign + absValue.toString(16).uppercase()

        val normalizedSource = sign + validation.normalizedMagnitude

        val result = ConversionResult(
            sourceSystem = sourceSystem,
            targetSystem = targetSystem,
            rawInput = rawInput,
            normalizedSource = normalizedSource,
            isNegative = validation.isNegative,
            decimalBigInt = value,
            binary = binary,
            octal = octal,
            decimal = decimal,
            hexadecimal = hexadecimal
        )
        return validation to result
    }
}
