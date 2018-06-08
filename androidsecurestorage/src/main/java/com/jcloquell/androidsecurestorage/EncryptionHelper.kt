package com.jcloquell.androidsecurestorage

import android.content.Context
import android.preference.PreferenceManager
import java.security.InvalidKeyException

class EncryptionHelper constructor(context: Context) {

  private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
  private val cipherHelper = CipherHelper(sharedPreferences)
  private val keyStoreHelper = KeyStoreHelper(context, sharedPreferences, cipherHelper)

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