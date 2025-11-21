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
    @Test
    fun validateEmail_con_email_invalido_devuelve_mensaje_correcto() {
        val resultado = validateEmail("correo-malo")
        assertEquals("Formato de email inválido", resultado)
    }

    @Test
    fun validateEmail_con_email_valido_devuelve_null() {
        val resultado = validateEmail("juan@gmail.com")
        assertNull(resultado)
    }

    // --- TEST NOMBRE ---
    @Test
    fun validateNameLettersOnly_con_numeros_devuelve_error() {
        val resultado = validateNameLettersOnly("Juan 123")
        assertEquals("Solo letras y espacios", resultado)
    }

    @Test
    fun validateNameLettersOnly_con_nombre_valido_devuelve_null() {
        val resultado = validateNameLettersOnly("Juan Perez")
        assertNull(resultado)
    }

    // --- TEST TELEFONO ---
    @Test
    fun validatePhoneDigitsOnly_con_letras_devuelve_error() {
        val resultado = validatePhoneDigitsOnly("1234abcd")
        assertEquals("Solo números", resultado)
    }

    @Test
    fun validatePhoneDigitsOnly_muy_corto_devuelve_error() {
        val resultado = validatePhoneDigitsOnly("123")
        assertEquals("Debe tener entre 8 y 15 dígitos", resultado)
    }

    // --- TEST PASSWORD ---
    @Test
    fun validateStrongPassword_valida_devuelve_null() {
        val resultado = validateStrongPassword("Admin123!")
        assertNull(resultado)
    }

    @Test
    fun validateStrongPassword_corta_devuelve_error() {
        val resultado = validateStrongPassword("Ab1!")
        assertEquals("Mínimo 8 caracteres", resultado)
    }

    @Test
    fun validateStrongPassword_sin_numero_devuelve_error() {
        val resultado = validateStrongPassword("AdminQwerty!")
        assertEquals("Debe incluir un número", resultado)
    }

    @Test
    fun validateStrongPassword_sin_mayuscula_devuelve_error() {
        val resultado = validateStrongPassword("admin123!")
        assertEquals("Debe incluir una mayúscula", resultado)
    }

    @Test
    fun validateStrongPassword_sin_simbolo_devuelve_error() {
        val resultado = validateStrongPassword("Admin12345")
        assertEquals("Debe incluir un símbolo", resultado)
    }

    // --- TEST CONFIRMAR ---
    @Test
    fun validateConfirm_devuelve_error_si_no_coinciden() {
        val resultado = validateConfirm("Clave123", "Clave999")
        assertEquals("Las contraseñas no coinciden", resultado)
    }
}
