package com.jcloquell.androidsecurestorage

import android.content.SharedPreferences

fun SharedPreferences.Editor.save(isAsynchronous: Boolean) {
  when {
    isAsynchronous -> apply()
    else -> commit()
  }
}