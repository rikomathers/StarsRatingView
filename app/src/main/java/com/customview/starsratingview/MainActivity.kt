package com.customview.starsratingview

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.customview.starsrating.StarsRatingView
import com.customview.starsratingview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
    }

    private fun setListeners() {
        setSeekBarListener()
        setStarsViewListener()
        setSwitchListener()
    }

    private fun setSeekBarListener() = with(binding) {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                starsRatingView.rating = progress.toFloat() / 20
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    private fun setStarsViewListener() = with(binding) {
        starsRatingView.setOnRatingChangeListener {
            seekBar.progress = (it * 20).toInt()
        }
    }

    private fun setSwitchListener() = with(binding) {
        switchView.setOnCheckedChangeListener { _, isChecked ->
            starsRatingView.isEnabled = isChecked
        }
    }
}