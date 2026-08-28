package com.idealista.challenge.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.idealista.challenge.R
import dagger.hilt.android.AndroidEntryPoint

/** Single-activity host; the actual UI lives in the [androidx.navigation.fragment.NavHostFragment] destinations. */
@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main)
