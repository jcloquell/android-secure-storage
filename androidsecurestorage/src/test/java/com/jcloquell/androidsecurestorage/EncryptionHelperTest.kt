package com.jcloquell.androidsecurestorage

import android.content.SharedPreferences
import com.nhaarman.mockito_kotlin.given
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.security.InvalidKeyException
import javax.crypto.SecretKey

class EncryptionHelperTest {

  private val sharedPreferences = mock<SharedPreferences>()
  private val cipherHelper = mock<CipherHelper>()
  private val keyStoreHelper = mock<KeyStoreHelper>()

  private lateinit var encryptionHelper: EncryptionHelper

  @Before
  fun setUp() {
    encryptionHelper = EncryptionHelper(sharedPreferences, cipherHelper, keyStoreHelper, true)
  }

  @Test
  fun `should encrypt the received String`() {
    //given
    val sharedPreferencesKey = "sharedPreferencesKey"
    val textToEncrypt = "textToEncrypt"
    val encryptedText = "encryptedText"
    val secretKey = mock<SecretKey>()
    whenever(keyStoreHelper.getSecretKey()).thenReturn(secretKey)
    whenever(cipherHelper.encrypt(sharedPreferencesKey, textToEncrypt, secretKey)).thenReturn(
        encryptedText)

    //when
    val returnedText = encryptionHelper.encrypt(sharedPreferencesKey, textToEncrypt)

    //then
    assertThat(returnedText).isEqualTo(encryptedText)
  }

  @Test
  fun `should decrypt the received String`() {
    //given
    val sharedPreferencesKey = "sharedPreferencesKey"
    val textToDecrypt = "textToDecrypt"
    val decryptedText = "decryptedText"
    val secretKey = mock<SecretKey>()
    whenever(keyStoreHelper.getSecretKey()).thenReturn(secretKey)
    whenever(cipherHelper.decrypt(sharedPreferencesKey, textToDecrypt, secretKey)).thenReturn(
        decryptedText)

    //when
    val returnedText = encryptionHelper.decrypt(sharedPreferencesKey, textToDecrypt)

    //then
    assertThat(returnedText).isEqualTo(decryptedText)
  }

  @Test
  fun `should remove the SecretKey if decrypting the received String throws an InvalidKeyException`() {
    //given
    val sharedPreferencesKey = "sharedPreferencesKey"
    val textToDecrypt = "textToDecrypt"
    val secretKey = mock<SecretKey>()
    val editor = mock<SharedPreferences.Editor>()
    whenever(keyStoreHelper.getSecretKey()).thenReturn(secretKey)
    whenever(sharedPreferences.edit()).thenReturn(editor)
    whenever(editor.remove(sharedPreferencesKey)).thenReturn(editor)
    given(cipherHelper.decrypt(sharedPreferencesKey, textToDecrypt, secretKey))
        .willAnswer { throw InvalidKeyException() }

    //when
    val returnedText = encryptionHelper.decrypt(sharedPreferencesKey, textToDecrypt)

    //then
    verify(keyStoreHelper).removeSecretKey()
    verify(editor).remove(sharedPreferencesKey)
    verify(editor).apply()
    assertThat(returnedText).isEmpty()
  }
}