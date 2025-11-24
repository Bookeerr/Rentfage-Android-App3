package com.example.rentfage.data.remote.dto

import com.example.rentfage.data.local.entity.UserEntity
import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id_usuario")
    val idUsuario: Long? = null,
    val nombre: String,
    val apellido: String,
    val email: String,
    val contrasena: String,
    val telefono: String,
    val direccion: String? = null,
    val rol: RolDto? = null,
    val estado: EstadoDto? = null
)

data class RolDto(
    @SerializedName("id_rol")
    val idRol: Long? = null,
    val nombre: String? = null
)

data class EstadoDto(
    @SerializedName("id_estado")
    val idEstado: Long? = null,
    val nombre: String? = null
)

fun UserDto.toEntity(existingPass: String = ""): UserEntity {
    val fullName = listOf(nombre, apellido)
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { nombre }

    val resolvedPass = existingPass.ifBlank { contrasena }

    return UserEntity(
        id = idUsuario ?: 0L,
        name = fullName,
        email = email,
        phone = telefono,
        pass = resolvedPass,
        role = rol?.nombre ?: "USER"
    )
}

fun buildRegisterUserDto(
    name: String,
    email: String,
    phone: String,
    pass: String,
    address: String? = null
): UserDto {
    val trimmedName = name.trim()
    val nombre = trimmedName.substringBefore(" ").ifBlank { trimmedName }
    val apellido = trimmedName.substringAfter(" ", "")

    return UserDto(
        nombre = nombre,
        apellido = apellido,
        email = email,
        contrasena = pass,
        telefono = phone,
        direccion = address ?: ""
    )
}
