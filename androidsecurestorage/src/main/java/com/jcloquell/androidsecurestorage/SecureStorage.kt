package com.jcloquell.androidsecurestorage

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.lang.reflect.Type

class SecureStorage(private val context: Context) {

  private val sharedPreferences: SharedPreferences =
      PreferenceManager.getDefaultSharedPreferences(context)
  private val gson: Gson = GsonBuilder().create()

  fun storeObject(key: String, objectToStore: Any) {
    sharedPreferences.edit().putString(key, gson.toJson(objectToStore)).apply()
  }

  fun <T> getObject(key: String, clazz: Class<T>): T {
    return gson.fromJson(sharedPreferences.getString(key, ""), clazz)
  }

  fun <T> getObject(key: String, type: Type): T {
    return gson.fromJson(sharedPreferences.getString(key, ""), type)
  }

  fun removeObject(key: String) {
    sharedPreferences.edit().remove(key).apply()
  }

  fun containsObject(key: String): Boolean {
    return sharedPreferences.contains(key)
  }
}
