package com.frdmplus.tv

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * A mouse-style pointer drawn over the WebView. The remote's arrow keys move it,
 * and MainActivity turns OK presses into taps at its position. This makes any
 * website usable on a TV, even one never designed for a remote.
 */
class CursorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val density = resources.displayMetrics.density
    private val radius = 11f * density

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xCCFFFFFF.toInt()
        style = Paint.Style.FILL
    }
    private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF000000.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 2.5f * density
    }
    private val pressedFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFE10600.toInt()
        style = Paint.Style.FILL
    }

    var cursorX = -1f
        private set
    var cursorY = -1f
        private set

    var clicking = false
        set(value) {
            field = value
            invalidate()
        }

    init {
        isFocusable = false
        isClickable = false
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (cursorX < 0) {
            cursorX = w / 2f
            cursorY = h / 2f
        } else {
            cursorX = cursorX.coerceIn(0f, w - 1f)
            cursorY = cursorY.coerceIn(0f, h - 1f)
        }
    }

    /**
     * Moves the pointer and returns how far it was blocked by the screen edge
     * (so the caller can scroll the page by that amount instead).
     */
    fun moveBy(dx: Float, dy: Float): Pair<Float, Float> {
        val maxX = width - 1f
        val maxY = height - 1f
        val wantX = cursorX + dx
        val wantY = cursorY + dy
        cursorX = wantX.coerceIn(0f, maxX)
        cursorY = wantY.coerceIn(0f, maxY)
        invalidate()
        return Pair(wantX - cursorX, wantY - cursorY)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (cursorX < 0) return
        canvas.drawCircle(cursorX, cursorY, radius, if (clicking) pressedFill else fill)
        canvas.drawCircle(cursorX, cursorY, radius, ring)
    }
}
