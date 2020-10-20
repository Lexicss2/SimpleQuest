package com.lex.simplequest.presentation.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView


class SpacingItemDecoration(spacing: Int, orientation: Int) : RecyclerView.ItemDecoration() {

    companion object {
        const val HORIZONTAL = 0
        const val VERTICAL = 1
        private val ATTRS = intArrayOf(android.R.attr.orientation)
    }

    private var orientation: Int = 0
    private var spacing: Int = 0

    init {
        setOrientation(orientation)
        setSpacing(spacing)
    }

    private fun setOrientation(orientation: Int) {
        require(HORIZONTAL == orientation || VERTICAL == orientation) { "Incorrect orientation" }
        this.orientation = orientation
    }

    fun setSpacing(spacing: Int) {
        require(spacing >= 0) { "Incorrect spacing" }
        this.spacing = spacing
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val halfSpacing = spacing / 2
        if (HORIZONTAL == orientation) {
            outRect.set(halfSpacing, 0, halfSpacing, 0)
        } else {
            outRect.set(0, halfSpacing, 0, halfSpacing)
        }
    }
}
