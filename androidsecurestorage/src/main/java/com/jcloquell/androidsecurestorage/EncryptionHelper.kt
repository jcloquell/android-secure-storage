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
import java.security.InvalidKeyException

@SuppressLint("CommitPrefEdits")
class EncryptionHelper {

  private val sharedPreferences: SharedPreferences
  private val cipherHelper: CipherHelper
  private val keyStoreHelper: KeyStoreHelper
  private val isAsynchronous: Boolean

  constructor(context: Context, isAsynchronous: Boolean = true) {
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    cipherHelper = CipherHelper(sharedPreferences, isAsynchronous)
    keyStoreHelper = KeyStoreHelper(context, sharedPreferences, cipherHelper, isAsynchronous)
    this.isAsynchronous = isAsynchronous
  }

  @VisibleForTesting
  internal constructor(sharedPreferences: SharedPreferences, cipherHelper: CipherHelper,
      keyStoreHelper: KeyStoreHelper, isAsynchronous: Boolean) {
    this.sharedPreferences = sharedPreferences
    this.cipherHelper = cipherHelper
    this.keyStoreHelper = keyStoreHelper
    this.isAsynchronous = isAsynchronous
  }

  fun encrypt(sharedPreferencesKey: String, textToEncrypt: String): String {
    val key = keyStoreHelper.getSecretKey()
    return cipherHelper.encrypt(sharedPreferencesKey, textToEncrypt, key)
  }

  fun decrypt(sharedPreferencesKey: String, textToDecrypt: String): String {
    try {
      val key = keyStoreHelper.getSecretKey()
      return cipherHelper.decrypt(sharedPreferencesKey, textToDecrypt, key)
    } catch (exception: InvalidKeyException) {
      keyStoreHelper.removeSecretKey()
      sharedPreferences.edit().remove(sharedPreferencesKey).save(isAsynchronous)
    }
    return ""
  }
}