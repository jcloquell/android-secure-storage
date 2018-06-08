package com.jcloquell.androidsecurestorage

import android.content.Context
import android.preference.PreferenceManager
import com.google.gson.GsonBuilder
import java.lang.reflect.Type

class SecureStorage constructor(context: Context) {

  private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
  private val gson = GsonBuilder().create()
  private val encryptionHelper = EncryptionHelper(context)

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
