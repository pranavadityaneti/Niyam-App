package com.myniyam.app.notifications

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompletionNotifierTest {

    @Test
    fun `posts when toggle on and permission granted`() {
        assertTrue(CompletionNotifier.shouldPost(notifyOn = true, permissionGranted = true))
    }

    @Test
    fun `no post when toggle off`() {
        assertFalse(CompletionNotifier.shouldPost(notifyOn = false, permissionGranted = true))
    }

    @Test
    fun `no post when permission denied`() {
        assertFalse(CompletionNotifier.shouldPost(notifyOn = true, permissionGranted = false))
    }
}
