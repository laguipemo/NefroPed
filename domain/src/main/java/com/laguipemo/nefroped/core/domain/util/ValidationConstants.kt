package com.laguipemo.nefroped.core.domain.util

object ValidationConstants {

    private val EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()

    const val MINIMAL_PASS_LENGTH = 8

    fun String.isValidEmail(): Boolean = this.matches(EMAIL_REGEX)
}