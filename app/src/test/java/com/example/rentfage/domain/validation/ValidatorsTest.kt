package com.example.rentfage.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ValidatorsTest {

    // --- TEST EMAIL ---
    // Regla: "Formato de email inválido"
    @Test
    fun `validateEmail con email invalido devuelve mensaje correcto`() {
        val resultado = validateEmail("correo-malo")
        assertEquals("Formato de email inválido", resultado)
    }

    @Test
    fun `validateEmail con email valido devuelve null (sin error)`() {
        val resultado = validateEmail("juan@gmail.com")
        assertNull(resultado)
    }

    // --- TEST NOMBRE ---
    // Regla: "Solo letras y espacios"
    @Test
    fun `validateNameLettersOnly con numeros devuelve error`() {
        val resultado = validateNameLettersOnly("Juan 123")
        assertEquals("Solo letras y espacios", resultado)
    }

    @Test
    fun `validateNameLettersOnly con nombre valido devuelve null`() {
        val resultado = validateNameLettersOnly("Juan Perez")
        assertNull(resultado)
    }

    // --- TEST TELEFONO ---
    // Regla: "Solo números" o "Debe tener entre 8 y 15 dígitos"
    @Test
    fun `validatePhoneDigitsOnly con letras devuelve error`() {
        val resultado = validatePhoneDigitsOnly("1234abcd")
        assertEquals("Solo números", resultado)
    }

    @Test
    fun `validatePhoneDigitsOnly muy corto devuelve error`() {
        val resultado = validatePhoneDigitsOnly("123")
        assertEquals("Debe tener entre 8 y 15 dígitos", resultado)
    }

    // --- TEST PASSWORD ---
    // Reglas: "Mínimo 8 caracteres", "Debe incluir un número", etc.
    
    @Test
    fun `validateStrongPassword valida devuelve null`() {
        // Cumple todo: >8 chars, Mayus, Minus, Numero, Simbolo
        val resultado = validateStrongPassword("Admin123!")
        assertNull(resultado)
    }

    @Test
    fun `validateStrongPassword corta devuelve error`() {
        val resultado = validateStrongPassword("Ab1!")
        assertEquals("Mínimo 8 caracteres", resultado)
    }

    @Test
    fun `validateStrongPassword sin numero devuelve error`() {
        val resultado = validateStrongPassword("AdminQwerty!")
        assertEquals("Debe incluir un número", resultado)
    }

    @Test
    fun `validateStrongPassword sin mayuscula devuelve error`() {
        val resultado = validateStrongPassword("admin123!")
        assertEquals("Debe incluir una mayúscula", resultado)
    }

    @Test
    fun `validateStrongPassword sin simbolo devuelve error`() {
        val resultado = validateStrongPassword("Admin12345")
        assertEquals("Debe incluir un símbolo", resultado)
    }

    // --- TEST CONFIRMAR ---
    @Test
    fun `validateConfirm devuelve error si no coinciden`() {
        val resultado = validateConfirm("Clave123", "Clave999")
        assertEquals("Las contraseñas no coinciden", resultado)
    }
}
