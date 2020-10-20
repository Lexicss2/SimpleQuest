package com.lex.simplequest.presentation.base

class SettableToolbarData(private val changedHandler: ((ToolbarData) -> Unit)?) : ToolbarData {

    private var _title: String? = null
    override val title: String?
        get() = _title
    private var _subtitle: String? = null
    override val subtitle: String?
        get() = _subtitle

    fun setTitle(title: String) {
        if (this._title != title) {
            this._title = title
            notifyChanged()
        }
    }

    fun setSubtitle(subtitle: String) {
        if (this.subtitle != subtitle) {
            this._subtitle = subtitle
            notifyChanged()
        }
    }

    fun invalidate() {
        notifyChanged()
    }

    private fun notifyChanged() {
        changedHandler?.invoke(this)
    }
}