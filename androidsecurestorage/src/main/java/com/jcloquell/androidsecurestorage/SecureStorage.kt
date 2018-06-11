package com.jcloquell.androidsecurestorage

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.annotation.VisibleForTesting
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.lang.reflect.Type

class SecureStorage {

  private val sharedPreferences: SharedPreferences
  private val gson: Gson
  private val encryptionHelper: EncryptionHelper

  constructor(context: Context) : this(PreferenceManager.getDefaultSharedPreferences(context),
      GsonBuilder().create(), EncryptionHelper(context))

  @VisibleForTesting
  internal constructor(sharedPreferences: SharedPreferences, gson: Gson,
      encryptionHelper: EncryptionHelper) {
    this.sharedPreferences = sharedPreferences
    this.gson = gson
    this.encryptionHelper = encryptionHelper
  }

  fun storeObject(key: String, objectToStore: Any) {
    val encryptedObject = encryptionHelper.encrypt(key, gson.toJson(objectToStore))
    sharedPreferences.edit().putString(key, encryptedObject).apply()
  }

  fun <T> getObject(key: String, clazz: Class<T>): T? {
    val encryptedObject = sharedPreferences.getString(key, null)
    return encryptedObject?.let {
      val decryptedObject = encryptionHelper.decrypt(key, encryptedObject)
      gson.fromJson(decryptedObject, clazz)
    }
  }

  fun <T> getObject(key: String, type: Type): T? {
    val encryptedObject = sharedPreferences.getString(key, null)
    return encryptedObject?.let {
      val decryptedObject = encryptionHelper.decrypt(key, encryptedObject)
      gson.fromJson(decryptedObject, type)
    }
  }

  fun removeObject(key: String) {
    sharedPreferences.edit().remove(key).apply()
  }

  fun containsObject(key: String): Boolean {
    return sharedPreferences.contains(key)
  }
}
