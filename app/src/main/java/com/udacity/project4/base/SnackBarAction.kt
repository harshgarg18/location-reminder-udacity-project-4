package com.udacity.project4.base

import android.view.View

data class SnackBarAction(
    val message: String,
    val actionText: String,
    val listener: View.OnClickListener
)
