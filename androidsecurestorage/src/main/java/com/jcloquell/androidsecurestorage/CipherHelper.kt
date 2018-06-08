package com.jcloquell.androidsecurestorage

import android.content.SharedPreferences
import android.util.Base64
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec

class CipherHelper constructor(private val sharedPreferences: SharedPreferences) {

  companion object {
    private const val TRANSFORMATION_ASYMMETRIC = "RSA/ECB/PKCS1Padding"
    private const val TRANSFORMATION_SYMMETRIC = "AES/CBC/PKCS7Padding"
    private const val AES_ALGORITHM = "AES"
    private const val IV_PREFERENCES_KEY = "IvPreferencesKey"
  }

  private val asymmetricCipher = Cipher.getInstance(TRANSFORMATION_ASYMMETRIC)
  private val symmetricCipher = Cipher.getInstance(TRANSFORMATION_SYMMETRIC)

  internal fun encrypt(data: String, key: Key?): String {
    symmetricCipher.init(Cipher.ENCRYPT_MODE, key)
    val bytes = symmetricCipher.doFinal(data.toByteArray())
    saveIvInSharedPreferences(symmetricCipher.iv)
    return Base64.encodeToString(bytes, Base64.DEFAULT)
  }

  internal fun decrypt(data: String, key: Key?): String {
    symmetricCipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(getIvFromSharedPreferences()))
    val encryptedData = Base64.decode(data, Base64.DEFAULT)
    val decodedData = symmetricCipher.doFinal(encryptedData)
    return String(decodedData)
  }

  internal fun wrapKey(keyToBeWrapped: Key?, keyToWrapWith: Key?): String {
    asymmetricCipher.init(Cipher.WRAP_MODE, keyToWrapWith)
    val bytes = asymmetricCipher.wrap(keyToBeWrapped)
    return Base64.encodeToString(bytes, Base64.DEFAULT)
  }

  internal fun unwrapKey(wrappedKeyData: String, keyToUnwrapWith: Key?): Key {
    asymmetricCipher.init(Cipher.UNWRAP_MODE, keyToUnwrapWith)
    val encryptedData = Base64.decode(wrappedKeyData, Base64.DEFAULT)
    return asymmetricCipher.unwrap(encryptedData, AES_ALGORITHM, Cipher.SECRET_KEY)
  }

  private fun saveIvInSharedPreferences(iv: ByteArray) {
    val encodedIv = Base64.encodeToString(iv, Base64.DEFAULT)
    sharedPreferences.edit().putString(IV_PREFERENCES_KEY, encodedIv).apply()
  }

  private fun getIvFromSharedPreferences(): ByteArray {
    val encodedIv = sharedPreferences.getString(IV_PREFERENCES_KEY, null)
    return Base64.decode(encodedIv, Base64.DEFAULT)
  }
}