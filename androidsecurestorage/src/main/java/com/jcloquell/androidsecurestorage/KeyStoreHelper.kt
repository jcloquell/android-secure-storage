package com.jcloquell.androidsecurestorage

import android.annotation.TargetApi
import android.app.Application
import android.content.SharedPreferences
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.util.*
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.security.auth.x500.X500Principal

class KeyStoreHelper constructor(private val application: Application,
    private val sharedPreferences: SharedPreferences,
    private val cipherHelper: CipherHelper) {

  companion object {
    private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    private const val RSA_ALGORITHM = "RSA"
    private const val AES_ALGORITHM = "AES"
    private const val ALIAS_KEY = "aliasKey"
  }

  private val keyStore = createKeyStore()

  internal fun getSecretKey(): SecretKey? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (keyStore.containsAlias(ALIAS_KEY)) {
        keyStore.getKey(ALIAS_KEY, null) as SecretKey?
      } else {
        createAndroidMAndAboveSecretKey()
      }
    } else {
      if (sharedPreferences.contains(ALIAS_KEY)) {
        val encryptedSecretKey = sharedPreferences.getString(ALIAS_KEY, null)
        return cipherHelper.unwrapKey(encryptedSecretKey, getRsaKeyPair()?.private) as SecretKey
      } else {
        createPreAndroidMSecretKey()
      }
    }
  }

  internal fun removeSecretKey() {
    keyStore.aliases().toList().forEach {
      keyStore.deleteEntry(it)
    }
    sharedPreferences.edit().clear().apply()
  }

  @TargetApi(Build.VERSION_CODES.M)
  private fun createAndroidMAndAboveSecretKey(): SecretKey {
    val keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM, ANDROID_KEY_STORE)
    val builder = KeyGenParameterSpec.Builder(ALIAS_KEY,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
    keyGenerator.init(builder.build())
    return keyGenerator.generateKey()
  }

  private fun createPreAndroidMSecretKey(): SecretKey {
    val keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM)
    val secretKey = keyGenerator.generateKey()
    val encryptedSecretKey = cipherHelper.wrapKey(secretKey, getRsaKeyPair()?.public)
    sharedPreferences.edit().putString(ALIAS_KEY, encryptedSecretKey).apply()
    return secretKey
  }

  private fun getRsaKeyPair(): KeyPair? {
    val privateKey = keyStore.getKey(ALIAS_KEY, null) as PrivateKey?
    val publicKey = keyStore.getCertificate(ALIAS_KEY)?.publicKey
    return when {
      (privateKey != null) and (publicKey != null) -> KeyPair(publicKey, privateKey)
      !keyStore.containsAlias(ALIAS_KEY) -> createRsaKeyPair()
      else -> null
    }
  }

  private fun createRsaKeyPair(): KeyPair {
    val keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM, ANDROID_KEY_STORE)
    val start = Calendar.getInstance()
    val end = Calendar.getInstance()
    end.add(Calendar.YEAR, 30)
    val builder = KeyPairGeneratorSpec.Builder(application)
        .setAlias(ALIAS_KEY)
        .setSubject(X500Principal("CN=$ALIAS_KEY"))
        .setSerialNumber(BigInteger.TEN)
        .setStartDate(start.time)
        .setEndDate(end.time)
    keyPairGenerator.initialize(builder.build())
    return keyPairGenerator.generateKeyPair()
  }

  private fun createKeyStore(): KeyStore {
    val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
    keyStore.load(null)
    return keyStore
  }
}