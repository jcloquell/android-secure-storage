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