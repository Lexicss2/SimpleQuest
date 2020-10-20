package com.lex.simplequest.presentation.utils

fun String.formattedCardNumber(): String =
    if (length >= 4) {
        "**** **** **** %s".format(this.substring(this.length - 4))
    } else this

fun String.shortFormattedCardNumber(): String =
    if (length >= 4) {
        "**** %s".format(this.substring(this.length - 4))
    } else this