package com.lex.simplequest.presentation.utils

import androidx.recyclerview.widget.DiffUtil

class SimpleDiffUtilsCallback<T>(
    private var oldItems: List<T> = listOf(),
    private var newItems: List<T> = listOf()
) : DiffUtil.Callback() {

    fun set(oldItems: List<T>, newItems: List<T>) {
        this.oldItems = oldItems
        this.newItems = newItems
    }

    override fun getOldListSize(): Int =
        oldItems.size

    override fun getNewListSize(): Int =
        newItems.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldItems[oldItemPosition] == newItems[newItemPosition]

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldItems[oldItemPosition] == newItems[newItemPosition]
}