package com.jcloquell.androidsecurestorage

import java.security.InvalidKeyException

class EncryptionHelper constructor(private val keyStoreHelper: KeyStoreHelper,
    private val cipherHelper: CipherHelper) {

  fun encrypt(text: String): String {
    val key = keyStoreHelper.getSecretKey()
    return cipherHelper.encrypt(text, key)
  }

  fun decrypt(text: String): String {
    try {
      val key = keyStoreHelper.getSecretKey()
      return cipherHelper.decrypt(text, key)
    } catch (exception: InvalidKeyException) {
      keyStoreHelper.removeSecretKey()
    }
    return ""
  }
}