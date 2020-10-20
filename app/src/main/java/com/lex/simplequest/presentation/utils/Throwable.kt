package com.lex.simplequest.presentation.utils

import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Throwable.isInternetConnectivityError(): Boolean =
    (this is UnknownHostException || this is SocketTimeoutException ||
            this is SocketException)