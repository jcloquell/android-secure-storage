package com.jcloquell.andriodsecurestoragetest

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.jcloquell.androidsecurestorage.SecureStorage
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

  @Rule
  var activityRule = ActivityTestRule(MainActivity::class.java)

  @Test
  fun should() {
    val secureStorage = SecureStorage(activityRule.activity.applicationContext)
    val text = "text"

    onView(withId(R.id.stringEditText))
        .perform(typeText(text))
        .perform(closeSoftKeyboard())
    onView(withId(R.id.encryptStringButton))
        .perform(click())

    assertThat(text).isEqualTo(secureStorage.getObject("stringKey", String::class.java))
  }
}
