package com.example.localization

import com.example.data.preferences.AppLanguage
import com.example.domain.NumberSystem
import com.example.domain.ValidationResult

enum class NavDestination {
    DASHBOARD,
    CONVERTER,
    LEARN,
    QUIZ,
    TABLE,
    HISTORY,
    SETTINGS,
    ABOUT
}

data class LessonContent(
    val system: NumberSystem,
    val title: String,
    val subtitle: String,
    val definition: String,
    val placeValuesExplanation: String,
    val exampleFormula: String,
    val exampleBreakdown: List<String>,
    val realWorldUses: List<String>,
    val trySampleInput: String
)

object AppStrings {

    fun systemName(system: NumberSystem, lang: AppLanguage): String {
        val bn = lang == AppLanguage.BANGLA
        return when (system) {
            NumberSystem.BINARY -> if (bn) "বাইনারি" else "Binary"
            NumberSystem.OCTAL -> if (bn) "অক্টাল" else "Octal"
            NumberSystem.DECIMAL -> if (bn) "ডেসিম্যাল" else "Decimal"
            NumberSystem.HEXADECIMAL -> if (bn) "হেক্সাডেসিম্যাল" else "Hexadecimal"
        }
    }

    fun systemShortDesc(system: NumberSystem, lang: AppLanguage): String {
        val bn = lang == AppLanguage.BANGLA
        return when (system) {
            NumberSystem.BINARY -> if (bn) "ডিজিটাল লজিক ও কম্পিউটার প্রসেসরের ভিত্তি (০ ও ১)।" else "Foundation of digital logic & processors using 0 and 1."
            NumberSystem.OCTAL -> if (bn) "৩-বিট বাইনারি গ্রুপের সংক্ষিপ্ত রূপ (০–৭)।" else "Compact 3-bit grouping used in Unix permissions & systems."
            NumberSystem.DECIMAL -> if (bn) "দৈনন্দিন গণনা ও মানবীয় সংখ্যার আদর্শ পদ্ধতি (০–৯)।" else "Standard everyday base-10 arithmetic used worldwide."
            NumberSystem.HEXADECIMAL -> if (bn) "মেমোরি অ্যাড্রেস ও কালার কোডের ৪-বিট সংক্ষেপণ (০–৯, A–F)।" else "Compact 4-bit notation for memory, bytes & hex colors."
        }
    }

    fun navLabel(dest: NavDestination, lang: AppLanguage): String {
        val bn = lang == AppLanguage.BANGLA
        return when (dest) {
            NavDestination.DASHBOARD -> if (bn) "ড্যাশবোর্ড" else "Dashboard"
            NavDestination.CONVERTER -> if (bn) "কনভার্টার" else "Converter"
            NavDestination.LEARN -> if (bn) "সংখ্যা পদ্ধতি শিখুন" else "Learn Systems"
            NavDestination.QUIZ -> if (bn) "অনুশীলন ও কুইজ" else "Practice & Quiz"
            NavDestination.TABLE -> if (bn) "রূপান্তর সারণি" else "Reference Table"
            NavDestination.HISTORY -> if (bn) "ইতিহাস" else "History"
            NavDestination.SETTINGS -> if (bn) "সেটিংস" else "Settings"
            NavDestination.ABOUT -> if (bn) "NumiX সম্পর্কে" else "About NumiX"
        }
    }

    fun navShortLabel(dest: NavDestination, lang: AppLanguage): String {
        val bn = lang == AppLanguage.BANGLA
        return when (dest) {
            NavDestination.DASHBOARD -> if (bn) "হোম" else "Home"
            NavDestination.CONVERTER -> if (bn) "কনভার্ট" else "Convert"
            NavDestination.LEARN -> if (bn) "শিখুন" else "Learn"
            NavDestination.QUIZ -> if (bn) "কুইজ" else "Quiz"
            NavDestination.TABLE -> if (bn) "টেবিল" else "Table"
            NavDestination.HISTORY -> if (bn) "ইতিহাস" else "History"
            NavDestination.SETTINGS -> if (bn) "সেটিংস" else "Settings"
            NavDestination.ABOUT -> if (bn) "তথ্য" else "About"
        }
    }

    fun validationMessage(result: ValidationResult, lang: AppLanguage): String {
        val bn = lang == AppLanguage.BANGLA
        return when (result) {
            ValidationResult.Empty ->
                if (bn) "রূপান্তর শুরু করতে একটি পূর্ণসংখ্যা লিখুন।"
                else "Enter an integer value to convert and inspect steps."
            ValidationResult.SignOnly ->
                if (bn) "চিহ্নের পরে অন্তত একটি অঙ্ক লিখুন।"
                else "Enter at least one digit after the sign."
            ValidationResult.FractionalNotSupported ->
                if (bn) "ভগ্নাংশ সমর্থিত নয়: NumiX শুধুমাত্র নির্ভুল BigInteger পূর্ণসংখ্যা রূপান্তর করে।"
                else "Fractional numbers are not supported: NumiX converts exact arbitrary-size integers only."
            is ValidationResult.InvalidCharacters ->
                if (bn) "অবৈধ অঙ্ক (${result.invalidChars})। ${systemName(result.system, lang)}-এর বৈধ অঙ্ক: ${result.system.validDigitsLabel}"
                else "Invalid digit(s): ${result.invalidChars}. Allowed for ${systemName(result.system, lang)}: ${result.system.validDigitsLabel}"
            is ValidationResult.Valid -> ""
        }
    }

    fun lessons(lang: AppLanguage): List<LessonContent> {
        val bn = lang == AppLanguage.BANGLA
        return listOf(
            LessonContent(
                system = NumberSystem.BINARY,
                title = if (bn) "বাইনারি সংখ্যা পদ্ধতি (Base 2)" else "Binary Number System (Base 2)",
                subtitle = if (bn) "ডিজিটাল ইলেকট্রনিক্সের মৌলিক ভাষা" else "The Core Language of Digital Computing",
                definition = if (bn)
                    "বাইনারি হলো ২-ভিত্তিক একটি সংখ্যা পদ্ধতি যেখানে শুধুমাত্র দুটি অঙ্ক ব্যবহার করা হয়: 0 (বন্ধ/Low) এবং 1 (চালু/High)। প্রতিটি বাইনারি অঙ্ককে এক 'বিট' (Bit) বলা হয়।"
                else
                    "Binary is a Base-2 positional numeral system using only two digits: 0 (Off/Low voltage) and 1 (On/High voltage). Each binary digit is called a bit.",
                placeValuesExplanation = if (bn)
                    "ডানদিক থেকে বামদিকে প্রতিটি ঘরের মান ২-এর ঘাত হিসেবে দ্বিগুণ হয়: 2⁰ (1), 2¹ (2), 2² (4), 2³ (8), 2⁴ (16), 2⁵ (32)..."
                else
                    "From right to left, each place value doubles as a power of 2: 2⁰ (1), 2¹ (2), 2² (4), 2³ (8), 2⁴ (16), 2⁵ (32)...",
                exampleFormula = "101101₂ = (1×32) + (0×16) + (1×8) + (1×4) + (0×2) + (1×1) = 45₁₀",
                exampleBreakdown = listOf(
                    "2⁵ = 32  →  1 × 32 = 32",
                    "2⁴ = 16  →  0 × 16 = 0",
                    "2³ = 8   →  1 × 8  = 8",
                    "2² = 4   →  1 × 4  = 4",
                    "2¹ = 2   →  0 × 2  = 0",
                    "2⁰ = 1   →  1 × 1  = 1"
                ),
                realWorldUses = if (bn) listOf(
                    "সিগন্যাল প্রসেসিং ও কম্পিউটার সিপিইউ ট্রানজিস্টর",
                    "ডিজিটাল লজিক গেট ও মেশিন কোড নির্দেশনা",
                    "নেটওয়ার্ক সাবনেট মাস্ক ও IPv4 বিটমাস্ক"
                ) else listOf(
                    "CPU transistors, registers, and machine-code instructions",
                    "Digital logic gates (AND, OR, XOR) and boolean algebra",
                    "IPv4 subnet masks, bitflags, and network packets"
                ),
                trySampleInput = "101101"
            ),
            LessonContent(
                system = NumberSystem.DECIMAL,
                title = if (bn) "ডেসিম্যাল সংখ্যা পদ্ধতি (Base 10)" else "Decimal Number System (Base 10)",
                subtitle = if (bn) "দৈনন্দিন গণনা ও মানবীয় মানদণ্ড" else "Universal Human Arithmetic System",
                definition = if (bn)
                    "ডেসিম্যাল বা দশমিক পদ্ধতি হলো ১০-ভিত্তিক সংখ্যা পদ্ধতি, যেখানে 0 থেকে 9 পর্যন্ত মোট ১০টি অঙ্ক ব্যবহৃত হয়।"
                else
                    "Decimal is a Base-10 positional numeral system employing ten digits from 0 through 9. It is the global standard for everyday counting, science, and finance.",
                placeValuesExplanation = if (bn)
                    "প্রতিটি অবস্থান ১০-এর ঘাত নির্দেশ করে: 10⁰ (একক), 10¹ (দশক), 10² (শতক), 10³ (হাজার)।"
                else
                    "Each column represents a power of 10: 10⁰ (Ones), 10¹ (Tens), 10² (Hundreds), 10³ (Thousands).",
                exampleFormula = "45₁₀ = (4 × 10¹) + (5 × 10⁰) = 40 + 5",
                exampleBreakdown = listOf(
                    "10¹ = 10  →  4 × 10 = 40",
                    "10⁰ = 1   →  5 × 1  = 5"
                ),
                realWorldUses = if (bn) listOf(
                    "দৈনন্দিন হিসাব-নিকাশ, ব্যাংকিং ও বাণিজ্য",
                    "মেট্রিক পরিমাপ ও বৈজ্ঞানিক গণনা",
                    "অন্যান্য সংখ্যা পদ্ধতি বোঝার ভিত্তি মানদণ্ড"
                ) else listOf(
                    "Everyday commerce, currency, and accounting",
                    "Metric scientific measurements and engineering units",
                    "Human-readable reference bridge for Base-2/8/16 conversions"
                ),
                trySampleInput = "45"
            ),
            LessonContent(
                system = NumberSystem.OCTAL,
                title = if (bn) "অক্টাল সংখ্যা পদ্ধতি (Base 8)" else "Octal Number System (Base 8)",
                subtitle = if (bn) "৩-বিট বাইনারির সংক্ষিপ্ত প্রকাশ" else "Compact 3-Bit Binary Shorthand",
                definition = if (bn)
                    "অক্টাল হলো ৮-ভিত্তিক সংখ্যা পদ্ধতি যেখানে 0 থেকে 7 পর্যন্ত ৮টি অঙ্ক থাকে। যেহেতু 8 = 2³, তাই প্রতিটি অক্টাল অঙ্ক ঠিক ৩টি বাইনারি বিটের সমান।"
                else
                    "Octal is a Base-8 system using digits 0–7. Because 8 = 2³, each octal digit neatly encodes exactly 3 binary bits.",
                placeValuesExplanation = if (bn)
                    "ডানদিক থেকে ঘরের মান ৮-এর ঘাত হিসেবে বাড়ে: 8⁰ (1), 8¹ (8), 8² (64), 8³ (512)।"
                else
                    "Place values increase by powers of 8: 8⁰ (1), 8¹ (8), 8² (64), 8³ (512).",
                exampleFormula = "55₈ = (5 × 8¹) + (5 × 8⁰) = 40 + 5 = 45₁₀",
                exampleBreakdown = listOf(
                    "8¹ = 8  →  5 × 8 = 40  (3-bit: 101₂)",
                    "8⁰ = 1  →  5 × 1 = 5   (3-bit: 101₂)"
                ),
                realWorldUses = if (bn) listOf(
                    "লিনাক্স ও ইউনিক্স ফাইল পারমিশন (যেমন chmod 755)",
                    "লিগ্যাসি মেইনফ্রেম ও মাইক্রোকন্ট্রোলার রেজিস্টার",
                    "ডিজিটাল ডিসপ্লে ও ৩-বিট এনকোডিং"
                ) else listOf(
                    "Unix/Linux file permissions (e.g., chmod 755 = rwxr-xr-x)",
                    "Aviation transponder codes (4 octal digits)",
                    "Compact shorthand for 12-bit and 24-bit words"
                ),
                trySampleInput = "55"
            ),
            LessonContent(
                system = NumberSystem.HEXADECIMAL,
                title = if (bn) "হেক্সাডেসিম্যাল সংখ্যা পদ্ধতি (Base 16)" else "Hexadecimal Number System (Base 16)",
                subtitle = if (bn) "আধুনিক কম্পিউটিংয়ের ৪-বিট সুপারশক্তি" else "Modern Computing's 4-Bit Powerhouse",
                definition = if (bn)
                    "হেক্সাডেসিম্যাল হলো ১৬-ভিত্তিক সংখ্যা পদ্ধতি। এতে 0–9 অঙ্কগুলোর পাশাপাশি 10 থেকে 15 বোঝাতে A, B, C, D, E, F অক্ষর ব্যবহার করা হয়।"
                else
                    "Hexadecimal is a Base-16 system using digits 0–9 and letters A–F (where A=10, B=11, C=12, D=13, E=14, F=15). One hex digit equals 4 bits (a nibble).",
                placeValuesExplanation = if (bn)
                    "প্রতিটি ঘর ১৬-এর ঘাত: 16⁰ (1), 16¹ (16), 16² (256), 16³ (4096)। দুটি হেক্স অঙ্ক মিলে পুরো ১ বাইট (০–২৫৫) প্রকাশ করে।"
                else
                    "Place values scale by powers of 16: 16⁰ (1), 16¹ (16), 16² (256), 16³ (4096). Two hex digits represent an entire 8-bit byte (0–255).",
                exampleFormula = "2D₁₆ = (2 × 16¹) + (13 × 16⁰) = 32 + 13 = 45₁₀",
                exampleBreakdown = listOf(
                    "16¹ = 16 →  2 × 16 = 32",
                    "16⁰ = 1  →  D(13) × 1 = 13",
                    "A=10, B=11, C=12, D=13, E=14, F=15"
                ),
                realWorldUses = if (bn) listOf(
                    "ওয়েব ও UI ডিজাইনে RGB কালার কোড (#3B82F6)",
                    "মেমোরি অ্যাড্রেস, MAC অ্যাড্রেস ও IPv6 ঠিকানা",
                    "ক্রিপ্টোগ্রাফিক হ্যাশ (SHA-256) ও অ্যাসেম্বলি ডিবাগিং"
                ) else listOf(
                    "RGB color codes in UI & web design (e.g., #3B82F6)",
                    "RAM memory addresses, MAC addresses, and IPv6",
                    "Cryptographic hashes, UUIDs, and binary inspection"
                ),
                trySampleInput = "2D"
            )
        )
    }
}
