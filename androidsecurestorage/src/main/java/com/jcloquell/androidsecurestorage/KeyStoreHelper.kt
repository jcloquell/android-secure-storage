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
import android.annotation.TargetApi
import android.content.Context
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

@SuppressLint("CommitPrefEdits")
internal class KeyStoreHelper(private val context: Context,
    private val sharedPreferences: SharedPreferences,
    private val cipherHelper: CipherHelper,
    private val isAsynchronous: Boolean) {

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
    if (keyStore.containsAlias(ALIAS_KEY)) {
      keyStore.deleteEntry(ALIAS_KEY)
    }
    sharedPreferences.edit().remove(ALIAS_KEY).save(isAsynchronous)
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
    sharedPreferences.edit().putString(ALIAS_KEY, encryptedSecretKey).save(isAsynchronous)
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
    start.add(Calendar.DAY_OF_YEAR, -2)
    val end = Calendar.getInstance()
    end.add(Calendar.YEAR, 30)
    val builder = KeyPairGeneratorSpec.Builder(context)
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