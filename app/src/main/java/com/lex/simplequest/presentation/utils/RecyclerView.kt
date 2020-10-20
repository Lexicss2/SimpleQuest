package com.lex.simplequest.presentation.utils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.scrollToPositionIfItemNotVisible(position: Int, smooth: Boolean = true) {
    if (RecyclerView.NO_POSITION != position) {
        (this.layoutManager as? LinearLayoutManager)?.let { layoutManager ->
            val firstVisiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition()
            val lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition()
            if (position < firstVisiblePosition || position > lastVisiblePosition) {
                if (smooth) this.smoothScrollToPosition(position)
                else this.scrollToPosition(position)
            }
        }
    }
}