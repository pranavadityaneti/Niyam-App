package com.myniyam.app.permissions

import org.junit.Assert.assertEquals
import org.junit.Test

class OemAutostartHelperTest {

    @Test
    fun `xiaomi maps to MIUI`() {
        assertEquals(OemAutostartHelper.OemFlow.MIUI, OemAutostartHelper.flowFor("xiaomi"))
    }

    @Test
    fun `redmi maps to MIUI`() {
        assertEquals(OemAutostartHelper.OemFlow.MIUI, OemAutostartHelper.flowFor("Redmi"))
    }

    @Test
    fun `poco maps to MIUI`() {
        assertEquals(OemAutostartHelper.OemFlow.MIUI, OemAutostartHelper.flowFor("POCO"))
    }

    @Test
    fun `oppo maps to ColorOS`() {
        assertEquals(OemAutostartHelper.OemFlow.COLOR_OS, OemAutostartHelper.flowFor("oppo"))
    }

    @Test
    fun `realme maps to ColorOS`() {
        assertEquals(OemAutostartHelper.OemFlow.COLOR_OS, OemAutostartHelper.flowFor("REALME"))
    }

    @Test
    fun `vivo maps to FuntouchOS`() {
        assertEquals(OemAutostartHelper.OemFlow.FUNTOUCH_OS, OemAutostartHelper.flowFor("vivo"))
    }

    @Test
    fun `iqoo maps to FuntouchOS`() {
        assertEquals(OemAutostartHelper.OemFlow.FUNTOUCH_OS, OemAutostartHelper.flowFor("IQOO"))
    }

    @Test
    fun `oneplus maps to OxygenOS`() {
        assertEquals(OemAutostartHelper.OemFlow.OXYGEN_OS, OemAutostartHelper.flowFor("OnePlus"))
    }

    @Test
    fun `samsung maps to OneUI`() {
        assertEquals(OemAutostartHelper.OemFlow.ONE_UI, OemAutostartHelper.flowFor("samsung"))
    }

    @Test
    fun `pixel maps to Generic`() {
        assertEquals(OemAutostartHelper.OemFlow.GENERIC, OemAutostartHelper.flowFor("Google"))
    }

    @Test
    fun `unknown manufacturer maps to Generic`() {
        assertEquals(OemAutostartHelper.OemFlow.GENERIC, OemAutostartHelper.flowFor("UnknownBrand"))
    }

    @Test
    fun `empty string maps to Generic`() {
        assertEquals(OemAutostartHelper.OemFlow.GENERIC, OemAutostartHelper.flowFor(""))
    }
}
