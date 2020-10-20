package com.lex.simplequest.presentation.utils

import android.graphics.*
import android.graphics.drawable.Drawable


class ArcDrawable(
    private val arcColor: Int,
    private val lineWidth: Int
) : Drawable() {

    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = arcColor
        strokeWidth = lineWidth.toFloat()
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
    }
    private var path: Path = Path().apply {
        fillType = Path.FillType.EVEN_ODD
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        path.reset()
        path.moveTo(bounds.left.toFloat(), bounds.bottom.toFloat())
        path.quadTo(
            ((bounds.left + bounds.right) / 2).toFloat(), bounds.top.toFloat(),
            bounds.right.toFloat(), bounds.bottom.toFloat()
        )
    }

    override fun draw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        paint.colorFilter = cf
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }
}