package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AvailabilityFilter
import com.example.data.model.DietaryFilter
import com.example.data.model.FoodItem
import com.example.data.model.MealTypeFilter
import com.example.data.model.Recipe
import com.example.data.model.SecurityUtil
import com.example.data.repository.RecipeRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("FoodWaste Manager", appName)
  }

  @Test
  fun `verify password hashing and verification`() {
    val password = "SecretPassword123!"
    val salt = SecurityUtil.generateSalt()
    val hash = SecurityUtil.hashPassword(password, salt)

    assertTrue("Password hash must verify correctly", SecurityUtil.verifyPassword(password, salt, hash))
    assertTrue("Wrong password must fail", !SecurityUtil.verifyPassword("WrongPassword", salt, hash))
  }
}
