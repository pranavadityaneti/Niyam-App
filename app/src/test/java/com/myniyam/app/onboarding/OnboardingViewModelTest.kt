package com.myniyam.app.onboarding

import com.myniyam.app.data.BlockList
import com.myniyam.app.data.Intention
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingViewModelTest {

    @Test
    fun `intention continue gating`() {
        val vm = OnboardingViewModel()
        assertFalse(vm.canContinueFromIntention())
        vm.selectIntention(Intention.CALM)
        assertTrue(vm.canContinueFromIntention())
        assertEquals(Intention.CALM, vm.selectedIntention)
    }

    @Test
    fun `intention selection is exposed for persistence`() {
        val vm = OnboardingViewModel()
        vm.selectIntention(Intention.DHARMA)
        assertEquals(Intention.DHARMA, vm.selectedIntention)
    }

    @Test
    fun `mantra continue gating`() {
        val vm = OnboardingViewModel()
        assertFalse(vm.canContinueFromMantra())
        vm.selectMantra("mahamrityunjaya")
        assertTrue(vm.canContinueFromMantra())
    }

    @Test
    fun `apps default to BlockList defaults and toggle`() {
        val vm = OnboardingViewModel()
        assertEquals(BlockList.DEFAULT_PACKAGES, vm.selectedPackages)
        vm.togglePackage("com.twitter.android")
        assertTrue("com.twitter.android" in vm.selectedPackages)
        vm.togglePackage("com.instagram.android")
        assertFalse("com.instagram.android" in vm.selectedPackages)
    }

    @Test
    fun `cannot continue from apps with empty selection`() {
        val vm = OnboardingViewModel()
        BlockList.DEFAULT_PACKAGES.forEach { vm.togglePackage(it) }
        assertFalse(vm.canContinueFromApps())
    }
}
