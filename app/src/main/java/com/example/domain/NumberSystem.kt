package com.example.domain

enum class NumberSystem(
    val base: Int,
    val shortCode: String,
    val subscript: String,
    val prefix: String,
    val validDigitsLabel: String,
    val validCharsRegex: Regex
) {
    BINARY(
        base = 2,
        shortCode = "BIN",
        subscript = "₂",
        prefix = "0b",
        validDigitsLabel = "0, 1",
        validCharsRegex = Regex("^[01]+$")
    ),
    OCTAL(
        base = 8,
        shortCode = "OCT",
        subscript = "₈",
        prefix = "0o",
        validDigitsLabel = "0–7",
        validCharsRegex = Regex("^[0-7]+$")
    ),
    DECIMAL(
        base = 10,
        shortCode = "DEC",
        subscript = "₁₀",
        prefix = "",
        validDigitsLabel = "0–9",
        validCharsRegex = Regex("^[0-9]+$")
    ),
    HEXADECIMAL(
        base = 16,
        shortCode = "HEX",
        subscript = "₁₆",
        prefix = "0x",
        validDigitsLabel = "0–9, A–F",
        validCharsRegex = Regex("^[0-9A-F]+$")
    );

    fun isValidDigit(ch: Char): Boolean {
        val upper = ch.uppercaseChar()
        return when (this) {
            BINARY -> upper == '0' || upper == '1'
            OCTAL -> upper in '0'..'7'
            DECIMAL -> upper in '0'..'9'
            HEXADECIMAL -> upper in '0'..'9' || upper in 'A'..'F'
        }
    }

    companion object {
        fun fromBase(base: Int): NumberSystem =
            entries.firstOrNull { it.base == base } ?: DECIMAL
    }
}
