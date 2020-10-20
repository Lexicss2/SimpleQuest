package com.lex.simplequest.presentation.utils

import android.content.Context
import android.text.Spannable
import android.text.style.ImageSpan
import androidx.core.content.ContextCompat
import java.util.regex.Pattern


object TextWithImages {

    fun create(context: Context, text: CharSequence, height: Float): Spannable {
        val spannable = Spannable.Factory.getInstance().newSpannable(text)
        addImages(context, spannable, height)
        return spannable
    }

    private fun addImages(context: Context, spannable: Spannable, height: Float): Boolean {
        val refImg = Pattern.compile("\\Q[img src=\\E([a-zA-Z0-9_]+?)\\Q/]\\E")
        var hasChanges = false

        val matcher = refImg.matcher(spannable)
        while (matcher.find()) {
            var set = true
            for (span in spannable.getSpans(matcher.start(), matcher.end(), CenteredImageSpan::class.java)) {
                if (spannable.getSpanStart(span) >= matcher.start() && spannable.getSpanEnd(span) <= matcher.end()) {
                    spannable.removeSpan(span)
                } else {
                    set = false
                    break
                }
            }
            val resName = spannable.subSequence(matcher.start(1), matcher.end(1)).toString().trim { it <= ' ' }
            val id = context.resources.getIdentifier(resName, "drawable", context.packageName)
            val drawable = ContextCompat.getDrawable(context, id)!!
            drawable.setBounds(
                0,
                0,
                (height.toInt() * drawable.intrinsicWidth) / drawable.intrinsicHeight,
                height.toInt()
            )
            if (set) {
                hasChanges = true
                spannable.setSpan(
                    CenteredImageSpan(drawable),
                    matcher.start(),
                    matcher.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        return hasChanges
    }
}