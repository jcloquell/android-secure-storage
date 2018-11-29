/*
 * Copyright (C) 2018, Jorge Cloquell Ribera, Nadine Kost
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jcloquell.androidsecurestorage

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.annotation.VisibleForTesting
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.lang.reflect.Type

@SuppressLint("CommitPrefEdits")
class SecureStorage {

  private val sharedPreferences: SharedPreferences
  private val gson: Gson
  private val encryptionHelper: EncryptionHelper
  private val isAsynchronous: Boolean

  constructor(context: Context, isAsynchronous: Boolean = true) :
      this(PreferenceManager.getDefaultSharedPreferences(context), GsonBuilder().create(),
          EncryptionHelper(context, isAsynchronous), isAsynchronous)

  @VisibleForTesting
  internal constructor(sharedPreferences: SharedPreferences, gson: Gson,
      encryptionHelper: EncryptionHelper, isAsynchronous: Boolean) {
    this.sharedPreferences = sharedPreferences
    this.gson = gson
    this.encryptionHelper = encryptionHelper
    this.isAsynchronous = isAsynchronous
  }

  fun storeObject(key: String, objectToStore: Any) {
    val encryptedObject = encryptionHelper.encrypt(key, gson.toJson(objectToStore))
    sharedPreferences.edit().putString(key, encryptedObject).save(isAsynchronous)
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
    sharedPreferences.edit().remove(key).save(isAsynchronous)
  }

  fun containsObject(key: String): Boolean {
    return sharedPreferences.contains(key)
  }
}
