package com.example.domain

import java.math.BigInteger

data class BilingualText(
    val en: String,
    val bn: String
) {
    fun get(isBangla: Boolean): String = if (isBangla) bn else en
}

data class ExplanationRow(
    val leftExpression: String,
    val rightResult: String,
    val highlight: Boolean = false
)

data class ExplanationStep(
    val stepNumber: Int,
    val title: BilingualText,
    val description: BilingualText,
    val formula: String? = null,
    val rows: List<ExplanationRow> = emptyList(),
    val note: BilingualText? = null
)

data class ConversionExplanation(
    val sourceSystem: NumberSystem,
    val targetSystem: NumberSystem,
    val sourceDisplay: String,
    val targetDisplay: String,
    val steps: List<ExplanationStep>,
    val finalAnswerExpression: String,
    val whyItWorks: BilingualText
) {
    fun toPlainTextReport(isBangla: Boolean): String {
        val sb = StringBuilder()
        sb.appendLine("NumiX — ${if (isBangla) "ধাপে ধাপে সমাধান" else "Step-by-Step Explanation"}")
        sb.appendLine("${sourceDisplay} = ${targetDisplay}")
        sb.appendLine("=".repeat(42))
        steps.forEach { step ->
            sb.appendLine()
            sb.appendLine("${if (isBangla) "ধাপ" else "Step"} ${step.stepNumber}: ${step.title.get(isBangla)}")
            sb.appendLine(step.description.get(isBangla))
            if (!step.formula.isNullOrBlank()) {
                sb.appendLine("  ${step.formula}")
            }
            step.rows.forEach { row ->
                sb.appendLine("  • ${row.leftExpression}  ->  ${row.rightResult}")
            }
            step.note?.let {
                sb.appendLine("  * ${it.get(isBangla)}")
            }
        }
        sb.appendLine()
        sb.appendLine("${if (isBangla) "চূড়ান্ত উত্তর" else "Final Answer"}: $finalAnswerExpression")
        sb.appendLine()
        sb.appendLine("${if (isBangla) "এটি কীভাবে কাজ করে?" else "Why does this work?"}")
        sb.appendLine(whyItWorks.get(isBangla))
        return sb.toString()
    }
}

object StepByStepExplanationEngine {

    private const val MAX_DETAILED_ROWS = 48

    private val superscriptDigits = mapOf(
        '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
        '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹'
    )

    private fun toSuperscript(n: Int): String =
        n.toString().map { superscriptDigits[it] ?: it }.joinToString("")

    fun generate(result: ConversionResult): ConversionExplanation {
        val src = result.sourceSystem
        val tgt = result.targetSystem
        val srcDisplay = "${result.normalizedSource}${src.subscript}"
        val tgtValue = result.valueFor(tgt)
        val tgtDisplay = "$tgtValue${tgt.subscript}"
        val finalExpr = "$srcDisplay = $tgtDisplay"

        val magnitudeStr = result.normalizedSource.removePrefix("-")
        val absDecimal = result.decimalBigInt.abs()
        val isNeg = result.isNegative

        if (src == tgt) {
            return ConversionExplanation(
                sourceSystem = src,
                targetSystem = tgt,
                sourceDisplay = srcDisplay,
                targetDisplay = tgtDisplay,
                steps = listOf(
                    ExplanationStep(
                        stepNumber = 1,
                        title = BilingualText(
                            "Identical Number System",
                            "একই সংখ্যা পদ্ধতি"
                        ),
                        description = BilingualText(
                            "Source and target are both ${src.name.lowercase().replaceFirstChar { it.uppercase() }} (Base ${src.base}), so every digit keeps its exact value.",
                            "উৎস এবং লক্ষ্য উভয়ই ভিত্তি ${src.base} সংখ্যা পদ্ধতি হওয়ায় প্রতিটি অঙ্কের মান অপরিবর্তিত থাকে।"
                        ),
                        formula = finalExpr
                    )
                ),
                finalAnswerExpression = finalExpr,
                whyItWorks = BilingualText(
                    "Converting within the same base (Base ${src.base} to Base ${src.base}) is an identity mapping because the positional weights (${src.base}⁰, ${src.base}¹, ${src.base}², …) are identical on both sides.",
                    "একই ভিত্তির (Base ${src.base} থেকে Base ${src.base}) মধ্যে রূপান্তরে স্থানীয় মান (${src.base}⁰, ${src.base}¹, ${src.base}², …) হুবহু একই থাকে, তাই সংখ্যাটি পরিবর্তিত হয় না।"
                )
            )
        }

        if (absDecimal == BigInteger.ZERO) {
            return ConversionExplanation(
                sourceSystem = src,
                targetSystem = tgt,
                sourceDisplay = srcDisplay,
                targetDisplay = tgtDisplay,
                steps = listOf(
                    ExplanationStep(
                        stepNumber = 1,
                        title = BilingualText("Zero Identity", "শূন্যের রূপান্তর"),
                        description = BilingualText(
                            "Zero represents the absence of quantity in every positional number system, so 0${src.subscript} is always 0${tgt.subscript}.",
                            "সব স্থানীয় মানভিত্তিক সংখ্যা পদ্ধতিতে শূন্যের মান সমান, তাই 0${src.subscript} সবসময় 0${tgt.subscript} হয়।"
                        ),
                        formula = "0${src.subscript} = 0${tgt.subscript}"
                    )
                ),
                finalAnswerExpression = finalExpr,
                whyItWorks = BilingualText(
                    "In any base b, 0 × b⁰ = 0. Therefore zero has the exact same representation across Binary, Octal, Decimal, and Hexadecimal.",
                    "যেকোনো ভিত্তি b-তে 0 × b⁰ = 0। তাই বাইনারি, অক্টাল, ডেসিম্যাল ও হেক্সাডেসিম্যাল সব পদ্ধতিতেই শূন্য একই থাকে।"
                )
            )
        }

        val steps = mutableListOf<ExplanationStep>()
        if (isNeg) {
            steps.add(
                ExplanationStep(
                    stepNumber = 1,
                    title = BilingualText(
                        "Preserve the Negative Sign",
                        "ঋণাত্মক চিহ্ন আলাদা রাখা"
                    ),
                    description = BilingualText(
                        "Separate the minus (−) sign first, convert the positive magnitude $magnitudeStr${src.subscript}, and re-attach (−) to the final result.",
                        "প্রথমে ঋণাত্মক চিহ্ন (−) আলাদা রেখে ধনাত্মক মান $magnitudeStr${src.subscript} রূপান্তর করা হয় এবং শেষে ফলাফলের সামনে (−) বসানো হয়।"
                    ),
                    formula = "−($magnitudeStr${src.subscript}) → convert $magnitudeStr${src.subscript}"
                )
            )
        }

        val coreSteps: List<ExplanationStep> = when {
            src != NumberSystem.DECIMAL && tgt == NumberSystem.DECIMAL ->
                buildToDecimalSteps(magnitudeStr, src, absDecimal, steps.size + 1)

            src == NumberSystem.DECIMAL && tgt != NumberSystem.DECIMAL ->
                buildDecimalToTargetSteps(absDecimal, tgt, steps.size + 1)

            src == NumberSystem.BINARY && (tgt == NumberSystem.OCTAL || tgt == NumberSystem.HEXADECIMAL) ->
                buildBinaryToGroupedBaseSteps(magnitudeStr, tgt, steps.size + 1)

            (src == NumberSystem.OCTAL || src == NumberSystem.HEXADECIMAL) && tgt == NumberSystem.BINARY ->
                buildGroupedBaseToBinarySteps(magnitudeStr, src, steps.size + 1)

            src == NumberSystem.OCTAL && tgt == NumberSystem.HEXADECIMAL ->
                buildOctalHexIndirectSteps(magnitudeStr, src, tgt, steps.size + 1)

            src == NumberSystem.HEXADECIMAL && tgt == NumberSystem.OCTAL ->
                buildOctalHexIndirectSteps(magnitudeStr, src, tgt, steps.size + 1)

            else -> emptyList()
        }

        steps.addAll(coreSteps)

        if (isNeg) {
            val posTarget = tgtValue.removePrefix("-")
            steps.add(
                ExplanationStep(
                    stepNumber = steps.size + 1,
                    title = BilingualText(
                        "Apply the Negative Sign",
                        "ঋণাত্মক চিহ্ন যুক্ত করা"
                    ),
                    description = BilingualText(
                        "Attach the preserved minus sign to $posTarget${tgt.subscript} to complete the signed integer conversion.",
                        "আগে রাখা ঋণাত্মক চিহ্নটি $posTarget${tgt.subscript}-এর সামনে বসিয়ে চূড়ান্ত উত্তর পাওয়া যায়।"
                    ),
                    formula = "−($posTarget${tgt.subscript}) = $tgtDisplay"
                )
            )
        }

        val whyItWorks = buildWhyItWorks(src, tgt)

        return ConversionExplanation(
            sourceSystem = src,
            targetSystem = tgt,
            sourceDisplay = srcDisplay,
            targetDisplay = tgtDisplay,
            steps = steps,
            finalAnswerExpression = finalExpr,
            whyItWorks = whyItWorks
        )
    }

    private fun buildToDecimalSteps(
        magnitude: String,
        src: NumberSystem,
        decimalAbs: BigInteger,
        startStep: Int
    ): List<ExplanationStep> {
        val steps = mutableListOf<ExplanationStep>()
        val base = src.base
        val baseBig = BigInteger.valueOf(base.toLong())
        val len = magnitude.length

        val hexLegend = if (src == NumberSystem.HEXADECIMAL && magnitude.any { it in 'A'..'F' }) {
            val usedLetters = magnitude.filter { it in 'A'..'F' }.toSet().sorted()
            val mapping = usedLetters.joinToString(", ") { "$it = ${it - 'A' + 10}" }
            BilingualText(
                "Hexadecimal letter mapping: A=10, B=11, C=12, D=13, E=14, F=15. Used here: $mapping.",
                "হেক্সাডেসিম্যাল অক্ষরের মান: A=10, B=11, C=12, D=13, E=14, F=15। এখানে ব্যবহৃত: $mapping।"
            )
        } else null

        val positionsStr = (len - 1 downTo 0).take(6).joinToString("  ") { "$base${toSuperscript(it)}" } +
            if (len > 6) " … $base⁰" else ""

        steps.add(
            ExplanationStep(
                stepNumber = startStep,
                title = BilingualText(
                    "Identify Positional Weights (Base $base)",
                    "স্থানীয় মান নির্ণয় (ভিত্তি $base)"
                ),
                description = BilingualText(
                    "Each digit in $magnitude${src.subscript} is multiplied by $base raised to its position index, counting from 0 on the rightmost digit.",
                    "$magnitude${src.subscript} সংখ্যাটির ডানদিক থেকে ০ ধরে প্রতিটি অঙ্কের স্থানীয় মান $base-এর ঘাত হিসেবে নির্ণয় করি।"
                ),
                formula = "Weights: $positionsStr",
                note = hexLegend
            )
        )

        val rows = mutableListOf<ExplanationRow>()
        val terms = mutableListOf<String>()
        val limit = minOf(len, MAX_DETAILED_ROWS)

        for (i in 0 until limit) {
            val ch = magnitude[i]
            val power = len - 1 - i
            val digitVal = Character.digit(ch, base)
            val weight = baseBig.pow(power)
            val product = BigInteger.valueOf(digitVal.toLong()).multiply(weight)
            val digitDisplay = if (ch in 'A'..'F') "$ch($digitVal)" else "$ch"
            rows.add(
                ExplanationRow(
                    leftExpression = "$digitDisplay × $base${toSuperscript(power)}",
                    rightResult = "$product",
                    highlight = digitVal > 0
                )
            )
            terms.add(product.toString())
        }

        val truncatedNote = if (len > MAX_DETAILED_ROWS) {
            BilingualText(
                "Showing the highest $MAX_DETAILED_ROWS positional terms out of $len total digits for readability; all $len digits are summed accurately.",
                "সহজে পড়ার জন্য মোট ${len}টি অঙ্কের মধ্যে সর্বোচ্চ ${MAX_DETAILED_ROWS}টি ধাপ দেখানো হয়েছে; চূড়ান্ত যোগফলে সব অঙ্কই নিখুঁতভাবে হিসাব করা হয়েছে।"
            )
        } else null

        steps.add(
            ExplanationStep(
                stepNumber = startStep + 1,
                title = BilingualText(
                    "Multiply Each Digit by Its Power of $base",
                    "প্রতিটি অঙ্ককে $base-এর ঘাত দিয়ে গুণ"
                ),
                description = BilingualText(
                    "Calculate each digit × ${base}ⁿ term from left (most significant) to right (least significant).",
                    "বাম দিক (সর্বোচ্চ স্থানীয় মান) থেকে ডান দিকে প্রতিটি অঙ্ক × ${base}ⁿ গুণফল বের করি।"
                ),
                rows = rows,
                note = truncatedNote
            )
        )

        val sumFormula = if (terms.size <= 10 && len <= MAX_DETAILED_ROWS) {
            "${terms.joinToString(" + ")} = $decimalAbs"
        } else {
            "Σ (digit × ${base}ⁿ) = $decimalAbs"
        }

        steps.add(
            ExplanationStep(
                stepNumber = startStep + 2,
                title = BilingualText(
                    "Sum All Positional Products",
                    "সব গুণফল যোগ করা"
                ),
                description = BilingualText(
                    "Add every calculated term together to obtain the exact Decimal (Base 10) integer.",
                    "প্রাপ্ত সবগুলো মান যোগ করলেই কাঙ্ক্ষিত ডেসিম্যাল (ভিত্তি ১০) মান পাওয়া যায়।"
                ),
                formula = sumFormula,
                rows = listOf(
                    ExplanationRow(
                        leftExpression = "$magnitude${src.subscript}",
                        rightResult = "$decimalAbs₁₀",
                        highlight = true
                    )
                )
            )
        )

        return steps
    }

    private fun buildDecimalToTargetSteps(
        decimalAbs: BigInteger,
        tgt: NumberSystem,
        startStep: Int
    ): List<ExplanationStep> {
        val base = tgt.base
        val baseBig = BigInteger.valueOf(base.toLong())
        val steps = mutableListOf<ExplanationStep>()

        val divisionRows = mutableListOf<ExplanationRow>()
        val remainders = mutableListOf<String>()
        var current = decimalAbs
        var stepCount = 0

        while (current > BigInteger.ZERO) {
            val qr = current.divideAndRemainder(baseBig)
            val quotient = qr[0]
            val remInt = qr[1].toInt()
            val remChar = remInt.toString(base).uppercase()
            remainders.add(remChar)

            if (stepCount < MAX_DETAILED_ROWS) {
                val remLabel = if (tgt == NumberSystem.HEXADECIMAL && remInt >= 10) {
                    "remainder $remInt ($remChar)"
                } else {
                    "remainder $remInt"
                }
                divisionRows.add(
                    ExplanationRow(
                        leftExpression = "$current ÷ $base = $quotient",
                        rightResult = remLabel,
                        highlight = true
                    )
                )
            }
            current = quotient
            stepCount++
        }

        val hexNote = if (tgt == NumberSystem.HEXADECIMAL) {
            BilingualText(
                "Remainders 10–15 map to letters: 10→A, 11→B, 12→C, 13→D, 14→E, 15→F.",
                "ভাগশেষ ১০–১৫ হলে অক্ষরে লেখা হয়: 10→A, 11→B, 12→C, 13→D, 14→E, 15→F।"
            )
        } else null

        val truncatedNote = if (stepCount > MAX_DETAILED_ROWS) {
            BilingualText(
                "Showing first $MAX_DETAILED_ROWS of $stepCount divisions; all $stepCount remainders are included in the final answer.",
                "মোট ${stepCount}টি ভাগের মধ্যে প্রথম ${MAX_DETAILED_ROWS}টি দেখানো হয়েছে; চূড়ান্ত উত্তরে সব ভাগশেষ অন্তর্ভুক্ত আছে।"
            )
        } else hexNote

        steps.add(
            ExplanationStep(
                stepNumber = startStep,
                title = BilingualText(
                    "Repeatedly Divide by $base",
                    "$base দিয়ে ধারাবাহিক ভাগ"
                ),
                description = BilingualText(
                    "Divide $decimalAbs by $base repeatedly, recording the integer quotient and remainder at every step until the quotient reaches 0.",
                    "ভাগফল ০ না হওয়া পর্যন্ত $decimalAbs-কে বারবার $base দিয়ে ভাগ করি এবং প্রতিটি ধাপের ভাগশেষ লিখে রাখি।"
                ),
                rows = divisionRows,
                note = truncatedNote
            )
        )

        val reversedRemainders = remainders.reversed().joinToString("")
        val previewSeq = if (remainders.size <= 16) {
            remainders.reversed().joinToString(" ")
        } else {
            "${remainders.reversed().take(8).joinToString(" ")} … ${remainders.reversed().takeLast(4).joinToString(" ")}"
        }

        steps.add(
            ExplanationStep(
                stepNumber = startStep + 1,
                title = BilingualText(
                    "Read Remainders Bottom-to-Top",
                    "নিচ থেকে উপরে ভাগশেষ পড়া"
                ),
                description = BilingualText(
                    "The last remainder obtained is the most significant digit (leftmost), and the first remainder is the least significant digit (rightmost).",
                    "সর্বশেষ প্রাপ্ত ভাগশেষটি সবচেয়ে বামে এবং প্রথম ভাগশেষটি সবচেয়ে ডানে বসিয়ে সংখ্যাটি গঠন করি।"
                ),
                formula = "Bottom → Top: $previewSeq",
                rows = listOf(
                    ExplanationRow(
                        leftExpression = "$decimalAbs₁₀",
                        rightResult = "$reversedRemainders${tgt.subscript}",
                        highlight = true
                    )
                )
            )
        )

        return steps
    }

    private fun buildBinaryToGroupedBaseSteps(
        binaryMagnitude: String,
        tgt: NumberSystem,
        startStep: Int
    ): List<ExplanationStep> {
        val groupSize = if (tgt == NumberSystem.OCTAL) 3 else 4
        val rem = binaryMagnitude.length % groupSize
        val padCount = if (rem == 0) 0 else groupSize - rem
        val padded = "0".repeat(padCount) + binaryMagnitude
        val groups = padded.chunked(groupSize)

        val steps = mutableListOf<ExplanationStep>()
        steps.add(
            ExplanationStep(
                stepNumber = startStep,
                title = BilingualText(
                    "Group Binary Digits into $groupSize-Bit Chunks",
                    "বাইনারি অঙ্কগুলোকে $groupSize-বিটের দলে ভাগ করা"
                ),
                description = BilingualText(
                    if (padCount > 0)
                        "Group from right to left in sets of $groupSize bits. Pad with $padCount leading zero(s) on the left so the leftmost group has $groupSize bits."
                    else
                        "Group from right to left in sets of $groupSize bits. The length (${binaryMagnitude.length}) divides evenly by $groupSize.",
                    if (padCount > 0)
                        "ডানদিক থেকে $groupSize বিট করে দল তৈরি করি। বাম দিকের দলটি পূর্ণ করতে বামে ${padCount}টি শূন্য (0) যোগ করি।"
                    else
                        "ডানদিক থেকে $groupSize বিট করে দল তৈরি করি। বিট সংখ্যা (${binaryMagnitude.length}) সরাসরি $groupSize দিয়ে বিভাজ্য।"
                ),
                formula = groups.take(12).joinToString(" | ") + if (groups.size > 12) " | …" else ""
            )
        )

        val rows = groups.take(MAX_DETAILED_ROWS).map { bits ->
            val digitVal = bits.toInt(2)
            val digitChar = digitVal.toString(tgt.base).uppercase()
            val rightLabel = if (tgt == NumberSystem.HEXADECIMAL && digitVal >= 10) {
                "$digitVal → $digitChar"
            } else {
                digitChar
            }
            ExplanationRow(
                leftExpression = "$bits₂",
                rightResult = "$rightLabel${tgt.subscript}",
                highlight = true
            )
        }

        val combined = groups.joinToString("") { it.toInt(2).toString(tgt.base).uppercase() }
            .trimStart('0').ifEmpty { "0" }

        steps.add(
            ExplanationStep(
                stepNumber = startStep + 1,
                title = BilingualText(
                    "Convert Each $groupSize-Bit Group to ${if (tgt == NumberSystem.OCTAL) "Octal" else "Hexadecimal"}",
                    "প্রতিটি $groupSize-বিট দলকে ${if (tgt == NumberSystem.OCTAL) "অক্টালে" else "হেক্সাডেসিমেলে"} রূপান্তর"
                ),
                description = BilingualText(
                    "Convert each $groupSize-bit binary group using positional weights (${if (groupSize == 3) "4, 2, 1" else "8, 4, 2, 1"}) and join the digits.",
                    "প্রতিটি $groupSize-বিট দলের মান (${if (groupSize == 3) "4, 2, 1" else "8, 4, 2, 1"} স্থানীয় মান অনুযায়ী) বের করে পাশাপাশি বসাই।"
                ),
                rows = rows,
                formula = "$binaryMagnitude₂ = $combined${tgt.subscript}"
            )
        )
        return steps
    }

    private fun buildGroupedBaseToBinarySteps(
        magnitude: String,
        src: NumberSystem,
        startStep: Int
    ): List<ExplanationStep> {
        val groupSize = if (src == NumberSystem.OCTAL) 3 else 4
        val rows = magnitude.take(MAX_DETAILED_ROWS).map { ch ->
            val v = Character.digit(ch, src.base)
            val bits = v.toString(2).padStart(groupSize, '0')
            val left = if (src == NumberSystem.HEXADECIMAL && v >= 10) "$ch ($v)" else "$ch"
            ExplanationRow(
                leftExpression = "$left${src.subscript}",
                rightResult = "$bits₂",
                highlight = true
            )
        }

        val fullBits = magnitude.map { ch ->
            Character.digit(ch, src.base).toString(2).padStart(groupSize, '0')
        }.joinToString("")
        val trimmedBits = fullBits.trimStart('0').ifEmpty { "0" }

        return listOf(
            ExplanationStep(
                stepNumber = startStep,
                title = BilingualText(
                    "Expand Each Digit to $groupSize Bits",
                    "প্রতিটি অঙ্ককে $groupSize-বিট বাইনারিতে বিস্তার"
                ),
                description = BilingualText(
                    "Replace every ${src.name.lowercase()} digit with its exact $groupSize-bit binary equivalent.",
                    "প্রতিটি অঙ্ককে তার সমতুল্য $groupSize-বিট বাইনারি মানে রূপান্তর করি।"
                ),
                rows = rows
            ),
            ExplanationStep(
                stepNumber = startStep + 1,
                title = BilingualText(
                    "Combine Binary Groups",
                    "সব বাইনারি দল একত্র করা"
                ),
                description = BilingualText(
                    "Concatenate all $groupSize-bit groups in order and drop any redundant leading zeros.",
                    "সব $groupSize-বিট দল ক্রমানুসারে একসাথে লিখি এবং শুরুর অতিরিক্ত শূন্য বাদ দিই।"
                ),
                formula = "${magnitude.take(8).map { Character.digit(it, src.base).toString(2).padStart(groupSize, '0') }.joinToString(" ")} → $trimmedBits₂"
            )
        )
    }

    private fun buildOctalHexIndirectSteps(
        magnitude: String,
        src: NumberSystem,
        tgt: NumberSystem,
        startStep: Int
    ): List<ExplanationStep> {
        val srcBits = if (src == NumberSystem.OCTAL) 3 else 4
        val tgtBits = if (tgt == NumberSystem.OCTAL) 3 else 4

        val expandRows = magnitude.take(MAX_DETAILED_ROWS).map { ch ->
            val v = Character.digit(ch, src.base)
            ExplanationRow(
                leftExpression = "$ch${src.subscript}",
                rightResult = "${v.toString(2).padStart(srcBits, '0')}₂",
                highlight = true
            )
        }

        val rawBinary = magnitude.map { ch ->
            Character.digit(itOrChar(ch), src.base).toString(2).padStart(srcBits, '0')
        }.joinToString("")
        val cleanBinary = rawBinary.trimStart('0').ifEmpty { "0" }

        val rem = cleanBinary.length % tgtBits
        val padCount = if (rem == 0) 0 else tgtBits - rem
        val paddedForTarget = "0".repeat(padCount) + cleanBinary
        val targetGroups = paddedForTarget.chunked(tgtBits)

        val regroupRows = targetGroups.take(MAX_DETAILED_ROWS).map { bits ->
            val v = bits.toInt(2)
            val d = v.toString(tgt.base).uppercase()
            ExplanationRow(
                leftExpression = "$bits₂",
                rightResult = "$d${tgt.subscript}",
                highlight = true
            )
        }

        val finalTarget = targetGroups.joinToString("") {
            it.toInt(2).toString(tgt.base).uppercase()
        }.trimStart('0').ifEmpty { "0" }

        return listOf(
            ExplanationStep(
                stepNumber = startStep,
                title = BilingualText(
                    "Step A: Convert ${src.shortCode} to Binary ($srcBits-Bit Bridge)",
                    "ধাপ ক: ${src.shortCode} থেকে বাইনারিতে রূপান্তর ($srcBits-বিট করে)"
                ),
                description = BilingualText(
                    "Since ${src.base} and ${tgt.base} are both powers of 2, Binary acts as an exact bridge. Expand each ${src.shortCode} digit into $srcBits bits.",
                    "${src.base} এবং ${tgt.base} উভয়ই ২-এর ঘাত হওয়ায় বাইনারিকে মধ্যবর্তী সেতু হিসেবে ব্যবহার করা সহজ। প্রতিটি অঙ্ককে $srcBits-বিটে লিখি।"
                ),
                rows = expandRows,
                formula = "Intermediate Binary = $cleanBinary₂"
            ),
            ExplanationStep(
                stepNumber = startStep + 1,
                title = BilingualText(
                    "Step B: Regroup Binary into $tgtBits-Bit Chunks",
                    "ধাপ খ: বাইনারিকে $tgtBits-বিটের দলে পুনর্গঠন"
                ),
                description = BilingualText(
                    "Take the intermediate binary $cleanBinary₂, group it from right to left in $tgtBits-bit chunks (padding left with 0s if needed), and convert each chunk to ${tgt.shortCode}.",
                    "প্রাপ্ত বাইনারি $cleanBinary₂-কে ডানদিক থেকে $tgtBits-বিট করে ভাগ করে প্রতিটি দলকে ${tgt.shortCode} অঙ্কে রূপান্তর করি।"
                ),
                rows = regroupRows,
                formula = "$magnitude${src.subscript} → $cleanBinary₂ → $finalTarget${tgt.subscript}"
            )
        )
    }

    private fun itOrChar(c: Char): Char = c

    private fun buildWhyItWorks(src: NumberSystem, tgt: NumberSystem): BilingualText {
        return when {
            src != NumberSystem.DECIMAL && tgt == NumberSystem.DECIMAL -> BilingualText(
                "Positional notation expresses any number as a polynomial in its base b: dₙ·bⁿ + … + d₁·b¹ + d₀·b⁰. Evaluating that polynomial in Base 10 reveals its Decimal value.",
                "স্থানীয় মান পদ্ধতিতে যেকোনো সংখ্যাকে তার ভিত্তি b-এর বহুপদী (dₙ·bⁿ + … + d₁·b¹ + d₀·b⁰) হিসেবে লেখা যায়। এই যোগফল নির্ণয় করলেই দশমিক মান পাওয়া যায়।"
            )
            src == NumberSystem.DECIMAL && tgt != NumberSystem.DECIMAL -> BilingualText(
                "Repeated division by base b peels off the polynomial coefficients from lowest power (b⁰) to highest (bⁿ). Each remainder is the coefficient at that position.",
                "ভিত্তি b দিয়ে বারবার ভাগ করলে সংখ্যার সর্বনিম্ন ঘাত (b⁰) থেকে শুরু করে প্রতিটি স্থানীয় মানের গুণনীয়ক ভাগশেষ হিসেবে বেরিয়ে আসে, তাই নিচ থেকে উপরে পড়লে সঠিক সংখ্যা পাওয়া যায়।"
            )
            (src == NumberSystem.BINARY && tgt == NumberSystem.OCTAL) ||
                (src == NumberSystem.OCTAL && tgt == NumberSystem.BINARY) -> BilingualText(
                "Because 8 = 2³, every single Octal digit corresponds 1-to-1 with an exact 3-bit Binary pattern (000 through 111), making conversion instantaneous without visiting Decimal.",
                "যেহেতু 8 = 2³, তাই প্রতিটি অক্টাল অঙ্ক ঠিক ৩টি বাইনারি বিটের (000 থেকে 111) সমান। এজন্য ডেসিম্যালে না গিয়েই সরাসরি রূপান্তর করা যায়।"
            )
            (src == NumberSystem.BINARY && tgt == NumberSystem.HEXADECIMAL) ||
                (src == NumberSystem.HEXADECIMAL && tgt == NumberSystem.BINARY) -> BilingualText(
                "Because 16 = 2⁴, every single Hexadecimal digit (0–F) corresponds 1-to-1 with an exact 4-bit Binary nibble (0000 through 1111).",
                "যেহেতু 16 = 2⁴, তাই প্রতিটি হেক্সাডেসিম্যাল অঙ্ক (0–F) ঠিক ৪টি বাইনারি বিটের (0000 থেকে 1111) সমান।"
            )
            else -> BilingualText(
                "Both 8 (2³) and 16 (2⁴) are powers of 2. Converting through Binary avoids large division arithmetic and works seamlessly even for arbitrarily huge integers.",
                "8 (2³) এবং 16 (2⁴) উভয়ই ২-এর ঘাত। তাই বাইনারির মাধ্যমে রূপান্তর করলে বড় সংখ্যার জটিল ভাগ ছাড়াই নিখুঁতভাবে উত্তর পাওয়া যায়।"
            )
        }
    }
}
