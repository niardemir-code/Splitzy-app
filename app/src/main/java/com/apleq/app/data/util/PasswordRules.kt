package com.apleq.app.data.util

/**
 * Requisitos mínimos de la contraseña al crear una cuenta. Lógica pura (sin Compose ni Firebase).
 * Se pide longitud (lo que más pesa), una letra y un número, y se descartan las contraseñas típicas.
 */
const val PASSWORD_MIN_LENGTH = 8

data class PasswordRequirements(
    val length: Boolean,
    val hasLetter: Boolean,
    val hasNumber: Boolean,
    /** false si es una contraseña de las más habituales o casi todo el mismo carácter. */
    val notCommon: Boolean
) {
    val basicsMet: Boolean get() = length && hasLetter && hasNumber
    val meets: Boolean get() = basicsMet && notCommon
}

private val COMMON_PASSWORDS = setOf(
    "12345678", "123456789", "1234567890", "12341234", "87654321", "11111111", "00000000",
    "password", "password1", "password12", "password123", "passw0rd", "p4ssw0rd",
    "contraseña1", "contrasena1", "contraseña123", "contrasena123", "contrasenya1",
    "qwerty123", "qwertyui", "qwertyuiop", "qwerty12", "asdf1234", "asdfghjk", "zxcvbnm1",
    "abc12345", "abcd1234", "a1234567", "a12345678", "admin123", "admin1234",
    "iloveyou", "iloveyou1", "welcome1", "welcome123", "letmein1", "monkey123", "dragon123",
    "apleq123", "apleq1234", "splitzy1", "splitzy123", "tecnofan1"
)

fun evaluatePassword(password: String): PasswordRequirements {
    val lower = password.lowercase()
    return PasswordRequirements(
        length = password.length >= PASSWORD_MIN_LENGTH,
        hasLetter = password.any { it.isLetter() },
        hasNumber = password.any { it.isDigit() },
        notCommon = lower !in COMMON_PASSWORDS && lower.toSet().size >= 4
    )
}
