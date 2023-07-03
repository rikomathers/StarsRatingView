package com.customview.starsrating

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import kotlin.math.max

class StarsRatingView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private var onRatingChangeListener: OnRatingChangeListener? = null
    private lateinit var fillStarPaint: Paint
    private lateinit var emptyStarPaint: Paint
    private var fillStarColor = 0
    private var emptyStarColor = 0
    private val path = Path()
    var rating = 0f
        set(value) {
            if (field != value) {
                field = if (value > 5) {
                    5f
                } else if (value < 0) {
                    0f
                } else {
                    value
                }
                invalidate()
                onRatingChangeListener?.onRatingChanged(rating)
            }
        }

    private var starWidth = 0.0f
    private var starHeight = 0.0f
    private var paddingBetweenStars = 0.0f
    private var paddingVertical = 0f
    private var paddingHorizontal = 0f

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    init {
        if (attrs != null) initAttrs(attrs, defStyleAttr, defStyleRes)
        else {
            fillStarColor = fillStarDefaultColor
            emptyStarColor = emptyStarDefaultColor
        }
        initPaints()
    }

    private fun initAttrs(attrs: AttributeSet, defStyleAttrs: Int, defStyleRes: Int) {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.RatingStarsView,
            defStyleAttrs,
            defStyleRes
        )
        fillStarColor =
            typedArray.getColor(R.styleable.RatingStarsView_fillStarsColor, fillStarDefaultColor)
        emptyStarColor =
            typedArray.getColor(R.styleable.RatingStarsView_emptyStarsColor, emptyStarDefaultColor)
        rating = typedArray.getFloat(R.styleable.RatingStarsView_rating, 0f)
        typedArray.recycle()
    }

    private fun initPaints() {
        emptyStarPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        emptyStarPaint.color = emptyStarColor
        fillStarPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        fillStarPaint.color = fillStarColor
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val starWidth = fromDipToPix(defaultStarWidth)
        val starWidthMargin = starWidth / 9
        val desiredWidth = max(minWidth, (starWidth * 5 + starWidthMargin * 4).toInt())
        val minHeight = suggestedMinimumHeight + paddingBottom + paddingTop
        val starHeight = fromDipToPix(defaultStarHeight).toInt()
        val desiredHeight = max(minHeight, starHeight)
        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }

    private fun fromDipToPix(dip: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, resources.displayMetrics)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateViewSizes()
    }

    private fun updateViewSizes() {
        val safeWidth = width - paddingLeft - paddingRight
        val safeHeight = height - paddingTop - paddingRight
        var starWidth = (safeWidth * 9).toFloat() / 49
        var starPadding = starWidth / 9
        var starHeight = starWidth * heightCoefficient
        if (starHeight > safeHeight) {
            starHeight = safeHeight.toFloat()
            starWidth = starHeight / heightCoefficient
            starPadding = starWidth / 9
        }
        this.starWidth = starWidth
        this.starHeight = starHeight
        this.paddingBetweenStars = starPadding

        val viewWidth = starWidth * 5 + starPadding * 4
        val viewHeight = starHeight
        paddingHorizontal = paddingLeft + (safeWidth - viewWidth) / 2
        paddingVertical = paddingTop + (safeHeight - viewHeight) / 2
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas != null) {
            printStars(canvas)
        }
    }

    private fun printStars(canvas: Canvas) {
        var rating = this.rating
        for (i in 0 until 5) {
            val paddingHorizontal = (paddingBetweenStars + starWidth) * i + paddingHorizontal
            drawEmptyStar(canvas, paddingHorizontal)
            if (rating >= 1) {
                fillStarByPercent(canvas, paddingHorizontal, 100f, fillStarPaint)
                rating -= 1
            } else if (rating > 0) {
                fillStarByPercent(canvas, paddingHorizontal, rating * 100, fillStarPaint)
                rating = 0f
            }
        }
    }

    private fun drawEmptyStar(canvas: Canvas, paddingHorizontal: Float) {
        fillStarByPercent(canvas, paddingHorizontal, 100f, emptyStarPaint)
    }

    private fun fillStarByPercent(
        canvas: Canvas,
        paddingHorizontal: Float,
        percent: Float,
        paint: Paint
    ) {
        val currentPercent = starWidth / 100 * percent
        if (percent <= 18.75) {
            val yCord = (18 / 25f) * (currentPercent) + (paddingVertical + starWidth * 27 / 80)
            path.moveTo(paddingHorizontal + 0f, paddingVertical + starWidth * 27 / 80)
            path.lineTo(paddingHorizontal + currentPercent, paddingVertical + starWidth * 27 / 80)
            path.lineTo(paddingHorizontal + currentPercent, yCord)
            path.close()
        } else if (percent > 18.75 && percent <= 31.25) {
            val yCord1 = (18 / 25f) * (currentPercent) + (paddingVertical + starWidth * 27 / 80)
            val yCord2 = -3f * (currentPercent) + (paddingVertical + starWidth * 1.5f)
            val yCord3 = -(18 / 25f) * (currentPercent) + (paddingVertical + starWidth * 1.06f)
            path.moveTo(paddingHorizontal + 0f, paddingVertical + starWidth * 27 / 80)
            path.lineTo(paddingHorizontal + currentPercent, paddingVertical + starWidth * 27 / 80)
            path.lineTo(paddingHorizontal + currentPercent, yCord1)
            path.close()
            canvas.drawPath(path, fillStarPaint)
            path.reset()
            path.moveTo(paddingHorizontal + currentPercent, yCord2)
            path.lineTo(paddingHorizontal + currentPercent, yCord3)
            path.lineTo(
                paddingHorizontal + starWidth * 3 / 16,
                paddingVertical + starWidth * 111 / 120
            )
            path.close()
        } else if (percent > 31.25 && percent <= 38.75) {
            val yCord = -(18 / 25f) * (currentPercent) + (paddingVertical + starWidth * 1.06f)
            path.moveTo(paddingHorizontal + 0f, paddingVertical + starWidth * 27 / 80)
            path.lineTo(paddingHorizontal + currentPercent, paddingVertical + starWidth * 27 / 80)
            path.lineTo(paddingHorizontal + currentPercent, yCord)
            path.lineTo(
                paddingHorizontal + starWidth * 3 / 16,
                paddingVertical + starWidth * 111 / 120
            )
            path.lineTo(
                paddingHorizontal + starWidth * 5 / 16,
                paddingVertical + starWidth * 9 / 16
            )
            path.close()
        } else if (percent > 38.75 && percent <= 50) {
            val yTop = -3f * (currentPercent) + (paddingVertical + starWidth * 1.5f)
            val yCord = -(18 / 25f) * (currentPercent) + (paddingVertical + starWidth * 1.06f)
            path.moveTo(paddingHorizontal + 0f, paddingVertical + starWidth * 27 / 80)
            path.lineTo(
                paddingHorizontal + starWidth * 31 / 80,
                paddingVertical + starWidth * 27 / 80
            )
            path.lineTo(paddingHorizontal + currentPercent, yTop)
            path.lineTo(paddingHorizontal + currentPercent, yCord)
            path.lineTo(
                paddingHorizontal + starWidth * 3 / 16,
                paddingVertical + starWidth * 111 / 120
            )
            path.lineTo(
                paddingHorizontal + starWidth * 5 / 16,
                paddingVertical + starWidth * 9 / 16
            )
            path.close()
        } else if (percent > 50 && percent <= 61.25) {
            val yTop = 3f * (currentPercent) + (paddingVertical + starWidth * -1.5f)
            val yCord = 0.72f * (currentPercent) + (paddingVertical + starWidth * 0.34f)
            path.moveTo(paddingHorizontal + 0f, paddingVertical + starWidth * 27 / 80)
            path.lineTo(
                paddingHorizontal + starWidth * 31 / 80,
                paddingVertical + starWidth * 27 / 80
            )
            path.lineTo(paddingHorizontal + starWidth / 2, paddingVertical + 0f)
            path.lineTo(paddingHorizontal + currentPercent, yTop)
            path.lineTo(paddingHorizontal + currentPercent, yCord)
            path.lineTo(paddingHorizontal + starWidth / 2, paddingVertical + starWidth * 0.7f)
            path.lineTo(
                paddingHorizontal + starWidth * 3 / 16,
                paddingVertical + starWidth * 111 / 120
            )
            path.lineTo(
                paddingHorizontal + starWidth * 5 / 16,
                paddingVertical + starWidth * 9 / 16
            )
            path.close()
        } else if (percent > 61.25 && percent <= 68.75) {
            val yCord = 0.72f * (currentPercent) + (paddingVertical + starWidth * 0.34f)
            path.moveTo(paddingHorizontal + 0f, paddingVertical + starWidth * 27 / 80)
            path.lineTo(
                paddingHorizontal + starWidth * 31 / 80,
                paddingVertical + starWidth * 27 / 80
            )
            path.lineTo(paddingHorizontal + starWidth / 2, paddingVertical + 0f)
            path.lineTo(
                paddingHorizontal + starWidth * 49 / 80,
                paddingVertical + starWidth * 27 / 80
            )
            path.lineTo(paddingHorizontal + currentPercent, paddingVertical + starWidth * 27 / 80)
            path.lineTo(paddingHorizontal + currentPercent, yCord)
            path.lineTo(paddingHorizontal + starWidth / 2, paddingVertical + starWidth * 0.7f)
            path.lineTo(
                paddingHorizontal + starWidth * 3 / 16,
                paddingVertical + starWidth * 111 / 120
            )
            path.lineTo(
                paddingHorizontal + starWidth * 5 / 16,
                paddingVertical + starWidth * 9 / 16
            )
            path.close()
        } else if (percent > 68.75 && percent <= 81.25) {
            val yCord1 = -(18 / 25f) * (currentPercent) + (paddingVertical + starWidth * 1.06f)
            val yCord2 = 3f * (currentPercent) + (paddingVertical + starWidth * -1.5f)
            val yCord3 = 0.72f * (currentPercent) + (paddingVertical + starWidth * 0.34f)
            path.moveTo(paddingHorizontal + 0f, paddingVertical + starWidth * 27 / 80)
            path.lineTo(
                paddingHorizontal + starWidth * 31 / 80,
                paddingVertical + starWidth * 27 / 80
            )
            path.lineTo(paddingHorizontal + starWidth / 2, paddingVertical + 0f)
            path.lineTo(
                paddingHorizontal + starWidth * 49 / 80,
                paddingVertical + starWidth * 27 / 80
            )
            path.lineTo(paddingHorizontal + currentPercent, paddingVertical + starWidth * 27 / 80)
            path.lineTo(paddingHorizontal + currentPercent, yCord1)
            path.lineTo(
                paddingHorizontal + starWidth * 11 / 16,
                paddingVertical + starWidth * 9 / 16
            )
            path.lineTo(paddingHorizontal + currentPercent, yCord2)
            path.lineTo(paddingHorizontal + currentPercent, yCord3)
            path.lineTo(paddingHorizontal + starWidth / 2, paddingVertical + starWidth * 0.7f)
            path.lineTo(
                paddingHorizontal + starWidth * 3 / 16,
                paddingVertical + starWidth * 111 / 120
            )
            path.lineTo(
                paddingHorizontal + starWidth * 5 / 16,
                paddingVertical + starWidth * 9 / 16
            )
            path.close()
        } else {
            val yCord = -(18 / 25f) * (currentPercent) + (paddingVertical + starWidth * 1.06f)
            path.moveTo(paddingHorizontal + 0f, paddingVertical + starWidth * 27 / 80)
            path.lineTo(
                paddingHorizontal + starWidth * 31 / 80,
                paddingVertical + starWidth * 27 / 80
            )
            path.lineTo(paddingHorizontal + starWidth / 2, paddingVertical + 0f)
            path.lineTo(
                paddingHorizontal + starWidth * 49 / 80,
                paddingVertical + starWidth * 27 / 80
            )
            path.lineTo(paddingHorizontal + currentPercent, paddingVertical + starWidth * 27 / 80)
            path.lineTo(paddingHorizontal + currentPercent, yCord)
            path.lineTo(
                paddingHorizontal + starWidth * 11 / 16,
                paddingVertical + starWidth * 9 / 16
            )
            path.lineTo(
                paddingHorizontal + starWidth * 13 / 16,
                paddingVertical + starWidth * 111 / 120
            )
            path.lineTo(paddingHorizontal + starWidth / 2, paddingVertical + starWidth * 0.7f)
            path.lineTo(
                paddingHorizontal + starWidth * 3 / 16,
                paddingVertical + starWidth * 111 / 120
            )
            path.lineTo(
                paddingHorizontal + starWidth * 5 / 16,
                paddingVertical + starWidth * 9 / 16
            )
            path.close()
        }
        canvas.drawPath(path, paint)
        path.reset()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (isEnabled) {
                    if (isTouchInSafeSpace(event)) {
                        val rating = (event.x - paddingHorizontal) / (starWidth + paddingBetweenStars) + 1
                        val remainderOfDivision = rating - rating.toInt()
                        if (remainderOfDivision <= 0.9) {
                            this.rating = rating.toInt().toFloat()
                        }
                    }
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun isTouchInSafeSpace(event: MotionEvent): Boolean {
        return (event.y - paddingVertical > 0 && event.y - paddingVertical < starHeight) &&
                event.x - paddingHorizontal > 0 && event.x - paddingHorizontal < (starWidth * 5 + paddingBetweenStars * 4)
    }

    fun setOnRatingChangeListener(listener: OnRatingChangeListener){
        onRatingChangeListener = listener
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()!!
        val savedState = SavedState(superState)
        savedState.starsRating = rating
        savedState.fillStarsColor = fillStarColor
        savedState.emptyStarsColor = emptyStarColor
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        rating = savedState.starsRating
        fillStarColor = savedState.fillStarsColor
        emptyStarColor = savedState.emptyStarsColor
    }

    companion object {
        private val fillStarDefaultColor = Color.parseColor("#FFD20B")
        private val emptyStarDefaultColor = Color.parseColor("#E1E1E1")
        private const val defaultStarWidth = 25f
        private const val heightCoefficient = 37 / 40.toFloat()
        private const val defaultStarHeight = heightCoefficient * defaultStarWidth
    }

    fun interface OnRatingChangeListener {
        fun onRatingChanged(rating: Float)
    }

    class SavedState : BaseSavedState {
        var starsRating: Float = 0f
        var fillStarsColor: Int = 0
        var emptyStarsColor: Int = 0

        constructor(superState: Parcelable) : super(superState)
        constructor(parcel: Parcel) : super(parcel) {
            starsRating = parcel.readFloat()
            fillStarsColor = parcel.readInt()
            emptyStarsColor = parcel.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeFloat(starsRating)
            out.writeInt(fillStarsColor)
            out.writeInt(emptyStarsColor)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState {
                    return SavedState(source)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return Array(size) { null }
                }

            }
        }
    }
}