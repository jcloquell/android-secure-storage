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
package com.jcloquell.andriodsecurestoragetest

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import com.google.gson.reflect.TypeToken
import com.jcloquell.androidsecurestorage.SecureStorage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.form_encrypt_heavy_object.*
import kotlinx.android.synthetic.main.form_encrypt_integer.*
import kotlinx.android.synthetic.main.form_encrypt_list.*
import kotlinx.android.synthetic.main.form_encrypt_string.*

class MainActivity : AppCompatActivity() {

  companion object {
    private const val STRING_KEY = "stringKey"
    private const val INTEGER_KEY = "integerKey"
    private const val OBJECT_KEY = "objectKey"
    private const val LIST_KEY = "listKey"
  }

  private lateinit var secureStorage: SecureStorage
  private lateinit var sharedPreferences: SharedPreferences

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    createLateInitInstances()
    setUpViews()
  }

  private fun createLateInitInstances() {
    secureStorage = SecureStorage(this)
    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
  }

  private fun setUpViews() {
    setUpStringEncryptionForm()
    setUpIntegerEncryptionForm()
    setUpObjectEncryptionForm()
    setUpListEncryptionForm()
  }

  private fun setUpStringEncryptionForm() {
    encryptStringButton.setOnClickListener {
      val stringToEncrypt = stringEditText.text.toString()
      secureStorage.storeObject(STRING_KEY, stringToEncrypt)
      encryptedStringTextView.text = sharedPreferences.getString(STRING_KEY, "")
    }
    decryptStringButton.setOnClickListener {
      val decryptedString = secureStorage.getObject(STRING_KEY, String::class.java)
      decryptedStringTextView.text = decryptedString
    }
  }

  private fun setUpIntegerEncryptionForm() {
    encryptIntegerButton.setOnClickListener {
      val integerToEncrypt = integerEditText.text.toString().toInt()
      secureStorage.storeObject(INTEGER_KEY, integerToEncrypt)
      encryptedIntegerTextView.text = sharedPreferences.getString(INTEGER_KEY, "")
    }
    decryptIntegerButton.setOnClickListener {
      val decryptedInteger = secureStorage.getObject(INTEGER_KEY, Int::class.java)
      decryptedIntegerTextView.text = decryptedInteger.toString()
    }
  }

  private fun setUpObjectEncryptionForm() {
    objectToEncryptTextView.text = generateHeavyObjectBasedOnUserInput("").toString()
    objectEditText.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(p0: Editable) {
        //do nothing
      }

      override fun beforeTextChanged(p0: CharSequence, p1: Int, p2: Int, p3: Int) {
        //do nothing
      }

      override fun onTextChanged(text: CharSequence, p1: Int, p2: Int, p3: Int) {
        val objectToEncrypt = generateHeavyObjectBasedOnUserInput(text.toString())
        objectToEncryptTextView.text = objectToEncrypt.toString()
      }
    })
    encryptObjectButton.setOnClickListener {
      val objectToEncrypt = generateHeavyObjectBasedOnUserInput(objectEditText.text.toString())
      secureStorage.storeObject(OBJECT_KEY, objectToEncrypt)
      encryptedObjectTextView.text = sharedPreferences.getString(OBJECT_KEY, "")
    }
    decryptObjectButton.setOnClickListener {
      val decryptedObject = secureStorage.getObject(OBJECT_KEY, HeavyObject::class.java)
      decryptedObjectTextView.text = decryptedObject.toString()
    }
  }

  private fun setUpListEncryptionForm() {
    listToEncryptTextView.text = generateListBasedOnUserInput("").toString()
    listEditText.addTextChangedListener(object : TextWatcher {
      override fun afterTextChanged(p0: Editable) {
        //do nothing
      }

      override fun beforeTextChanged(p0: CharSequence, p1: Int, p2: Int, p3: Int) {
        //do nothing
      }

      override fun onTextChanged(text: CharSequence, p1: Int, p2: Int, p3: Int) {
        val listToEncrypt = generateListBasedOnUserInput(text.toString())
        listToEncryptTextView.text = listToEncrypt.toString()
      }
    })
    encryptListButton.setOnClickListener {
      val listToEncrypt = generateListBasedOnUserInput(listEditText.text.toString())
      secureStorage.storeObject(LIST_KEY, listToEncrypt)
      encryptedListTextView.text = sharedPreferences.getString(LIST_KEY, "")
    }
    decryptListButton.setOnClickListener {
      // Have in mind that to decrypt any kind of Collection directly, the TypeToken class,
      // which belongs to the Gson library, is necessary
      val decryptedList = secureStorage.getObject<List<String>>(LIST_KEY,
          object : TypeToken<List<String>>() {}.type)
      decryptedListTextView.text = decryptedList.toString()
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.stringMode -> {
        encryptStringForm.visibility = VISIBLE
        encryptIntegerForm.visibility = GONE
        encryptObjectForm.visibility = GONE
        encryptListForm.visibility = GONE
        true
      }
      R.id.integerMode -> {
        encryptStringForm.visibility = GONE
        encryptIntegerForm.visibility = VISIBLE
        encryptObjectForm.visibility = GONE
        encryptListForm.visibility = GONE
        true
      }
      R.id.objectMode -> {
        encryptStringForm.visibility = GONE
        encryptIntegerForm.visibility = GONE
        encryptObjectForm.visibility = VISIBLE
        encryptListForm.visibility = GONE
        true
      }
      R.id.listMode -> {
        encryptStringForm.visibility = GONE
        encryptIntegerForm.visibility = GONE
        encryptObjectForm.visibility = GONE
        encryptListForm.visibility = VISIBLE
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  private fun generateHeavyObjectBasedOnUserInput(text: String): HeavyObject {
    val count = if (text.isEmpty()) 0 else text.toInt()
    return HeavyObject("This is a list of Hello Worlds:",
        Array(count, { "Hello World! $it" }))
  }

  private fun generateListBasedOnUserInput(text: String): List<String> {
    val count = if (text.isEmpty()) 0 else text.toInt()
    val list = mutableListOf<String>()
    (0 until count).forEach {
      list.add("Hello World! $it")
    }
    return list
  }

  private data class HeavyObject(val title: String, val details: Array<String>)
}
