package com.jcloquell.androidsecurestorage

import android.content.SharedPreferences
import com.google.gson.Gson
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Type

class SecureStorageTest {

  private val sharedPreferences = mock<SharedPreferences>()
  private val gson = mock<Gson>()
  private val encryptionHelper = mock<EncryptionHelper>()

  private lateinit var secureStorage: SecureStorage

  @Before
  fun setUp() {
    secureStorage = SecureStorage(sharedPreferences, gson, encryptionHelper)
  }

  @Test
  fun `should save the encrypted object in SharedPreferences`() {
    //given
    val key = "sharedPreferencesKey"
    val objectToStoreAsJsonString = "{ objectToStoreAsJsonString }"
    val encryptedObject = "encryptedObject"
    val editor = mock<SharedPreferences.Editor>()
    val objectToStore = mock<Any>()
    whenever(gson.toJson(objectToStore)).thenReturn(objectToStoreAsJsonString)
    whenever(encryptionHelper.encrypt(key, objectToStoreAsJsonString)).thenReturn(encryptedObject)
    whenever(sharedPreferences.edit()).thenReturn(editor)
    whenever(editor.putString(key, encryptedObject)).thenReturn(editor)

    //when
    secureStorage.storeObject(key, objectToStore)

    //then
    verify(editor).putString(key, encryptedObject)
    verify(editor).apply()
  }

  @Test
  fun `should get the decrypted object when passing the Class`() {
    //given
    val clazz = Any::class.java
    val key = "sharedPreferencesKey"
    val encryptedObject = "encryptedObject"
    val decryptedObjectAsJsonString = "decryptedObjectAsJsonString"
    val decryptedObject = mock<Any>()
    whenever(sharedPreferences.getString(key, null)).thenReturn(encryptedObject)
    whenever(encryptionHelper.decrypt(key, encryptedObject)).thenReturn(decryptedObjectAsJsonString)
    whenever(gson.fromJson(decryptedObjectAsJsonString, clazz)).thenReturn(decryptedObject)

    //when
    val storedObject = secureStorage.getObject(key, clazz)

    //then
    assertThat(storedObject).isEqualTo(decryptedObject)
  }

  @Test
  fun `should get the decrypted object when passing the Type`() {
    //given
    val key = "sharedPreferencesKey"
    val encryptedObject = "encryptedObject"
    val decryptedObjectAsJsonString = "decryptedObjectAsJsonString"
    val decryptedObject = mock<Any>()
    val type = mock<Type>()
    whenever(sharedPreferences.getString(key, null)).thenReturn(encryptedObject)
    whenever(encryptionHelper.decrypt(key, encryptedObject)).thenReturn(decryptedObjectAsJsonString)
    whenever(gson.fromJson<Any>(decryptedObjectAsJsonString, type)).thenReturn(decryptedObject)

    //when
    val storedObject = secureStorage.getObject<Any>(key, type)

    //then
    assertThat(storedObject).isEqualTo(decryptedObject)
  }

  @Test
  fun `should get null when trying to get an object passing the Class`() {
    //given
    val clazz = Any::class.java
    val key = "sharedPreferencesKey"
    whenever(sharedPreferences.getString(key, null)).thenReturn(null)

    //when
    val storedObject = secureStorage.getObject(key, clazz)

    //then
    assertThat(storedObject).isNull()
  }

  @Test
  fun `should get null when trying to get an object passing the Type`() {
    //given
    val key = "sharedPreferencesKey"
    val type = mock<Type>()
    whenever(sharedPreferences.getString(key, null)).thenReturn(null)

    //when
    val storedObject = secureStorage.getObject<Any?>(key, type)

    //then
    assertThat(storedObject).isNull()
  }

  @Test
  fun `should remove the object from SharedPreferences`() {
    //given
    val key = "sharedPreferencesKey"
    val editor = mock<SharedPreferences.Editor>()
    whenever(sharedPreferences.edit()).thenReturn(editor)
    whenever(editor.remove(key)).thenReturn(editor)

    //when
    secureStorage.removeObject(key)

    //then
    verify(editor).remove(key)
    verify(editor).apply()
  }

  @Test
  fun `should contain the object`() {
    //given
    val key = "sharedPreferencesKey"
    whenever(sharedPreferences.contains(key)).thenReturn(true)

    //when
    val contained = secureStorage.containsObject(key)

    //then
    assertThat(contained).isTrue()
  }

  @Test
  fun `should not contain the object`() {
    //given
    val key = "sharedPreferencesKey"
    whenever(sharedPreferences.contains(key)).thenReturn(false)

    //when
    val contained = secureStorage.containsObject(key)

    //then
    assertThat(contained).isFalse()
  }
}