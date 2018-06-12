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

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.annotation.VisibleForTesting
import java.security.InvalidKeyException

class EncryptionHelper {

  private val sharedPreferences: SharedPreferences
  private val cipherHelper: CipherHelper
  private val keyStoreHelper: KeyStoreHelper

  constructor(context: Context) {
    this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    this.cipherHelper = CipherHelper(sharedPreferences)
    this.keyStoreHelper = KeyStoreHelper(context, sharedPreferences, cipherHelper)
  }

  @VisibleForTesting
  internal constructor(sharedPreferences: SharedPreferences, cipherHelper: CipherHelper,
      keyStoreHelper: KeyStoreHelper) {
    this.sharedPreferences = sharedPreferences
    this.cipherHelper = cipherHelper
    this.keyStoreHelper = keyStoreHelper
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
    }
    return ""
  }
}