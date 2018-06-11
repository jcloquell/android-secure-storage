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
import com.jcloquell.androidsecurestorage.SecureStorage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.form_encrypt_heavy_object.*
import kotlinx.android.synthetic.main.form_encrypt_integer.*
import kotlinx.android.synthetic.main.form_encrypt_string.*

class MainActivity : AppCompatActivity() {

  companion object {
    private const val STRING_KEY = "stringKey"
    private const val INTEGER_KEY = "integerKey"
    private const val OBJECT_KEY = "objectKey"
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

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    val inflater = menuInflater
    inflater.inflate(R.menu.menu, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.stringMode -> {
        encryptStringForm.visibility = VISIBLE
        encryptIntegerForm.visibility = GONE
        encryptObjectForm.visibility = GONE
        true
      }
      R.id.integerMode -> {
        encryptStringForm.visibility = GONE
        encryptIntegerForm.visibility = VISIBLE
        encryptObjectForm.visibility = GONE
        true
      }
      R.id.objectMode -> {
        encryptStringForm.visibility = GONE
        encryptIntegerForm.visibility = GONE
        encryptObjectForm.visibility = VISIBLE
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

  private data class HeavyObject(val title: String, val details: Array<String>)
}
