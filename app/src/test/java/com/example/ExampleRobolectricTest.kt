package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.service.JarvisToolRouter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("J.A.R.V.I.S.", appName)
  }

  @Test
  fun `verify tool router initializes default stark files`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val toolRouter = JarvisToolRouter(context)
    val files = toolRouter.listFiles()
    assertTrue("Stark workspace should have initialized files", files.isNotEmpty())
    assertTrue("Should contain mark_vii_launch protocol", files.any { it.name.contains("mark_vii_launch") })
  }
}
