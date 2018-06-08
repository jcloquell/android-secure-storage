package com.jcloquell.andriodsecurestoragetest

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.jcloquell.androidsecurestorage.SecureStorage

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val secureStorage = SecureStorage(this)
    val hola = Hola("que pasa", intArrayOf(1, 2), Array(2000, { "Hola tio $it" }))

    secureStorage.storeObject("hola", "holita")
    secureStorage.storeObject("hola2", 1)
    secureStorage.storeObject("hola3", hola)

    val a = secureStorage.getObject("hola", String::class.java)
    val b = secureStorage.getObject("hola2", Int::class.java)
    val c = secureStorage.getObject("hola3", Hola::class.java)

    Log.d("hola", a)
    Log.d("hola", b.toString())
    Log.d("hola", c.toString())
  }

  internal data class Hola(val que: String, val pasa: IntArray, val tio: Array<String>)
}
