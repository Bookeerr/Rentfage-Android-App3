package com.example.rentfage.data.repository

import com.example.rentfage.data.local.dao.UserDao
import com.example.rentfage.data.local.entity.UserEntity
import com.example.rentfage.data.remote.RemoteModule
import com.example.rentfage.data.remote.UsuariosApiService
import com.example.rentfage.data.remote.dto.UserDto
import com.example.rentfage.data.remote.dto.buildRegisterUserDto
import com.example.rentfage.data.remote.dto.toEntity
import retrofit2.HttpException

class UserRepository(
    private val userDao: UserDao,
    private val usuariosApi: UsuariosApiService = RemoteModule.usuariosApi
) {

    //  REMOTO
    suspend fun syncUsuarios(): Result<Unit> =
        runCatching {
            val response = usuariosApi.obtenerUsuarios()
            if (!response.isSuccessful) throw HttpException(response)

            val dtoList: List<UserDto> = response.body().orEmpty()
            val localPasswords = userDao.getAll().associateBy({ it.email }, { it.pass })
            val entities = dtoList.map { dto ->
                val fallbackPass = localPasswords[dto.email] ?: ""
                dto.toEntity(fallbackPass)
            }
            
            // CORRECCIÓN: Usamos upsert en lugar de borrar todo.
            // Esto conserva los usuarios locales (como el admin) que no vienen del servidor.
            if (entities.isNotEmpty()) {
                userDao.insertarTodos(entities)
            }
        }

    //inicio sesion app
    suspend fun login(email: String, pass: String): Result<UserEntity> =
        runCatching {
            val response = usuariosApi.login(email, pass)
            if (!response.isSuccessful) throw HttpException(response)

            // Sincronizamos después del login para traer datos actualizados
            syncUsuarios().getOrElse { /* ignoramos para no bloquear login */ }

            // Buscamos el usuario en la BD local (que ahora debe estar actualizada)
            val user = userDao.getByEmail(email)
                ?: throw IllegalArgumentException("Usuario no encontrado después de sincronizar")

            // Actualizamos la contraseña local por si acaso
            val updatedUser = user.copy(pass = pass)
            userDao.upsert(updatedUser)
            updatedUser
        }

    //registro
    suspend fun register(name: String, email: String, phone: String, pass: String): Result<Long> =
        runCatching {
            val payload = buildRegisterUserDto(
                name = name,
                email = email,
                phone = phone,
                pass = pass
            )

            val response = usuariosApi.register(payload)
            if (!response.isSuccessful) throw HttpException(response)

            val remoteUser = (response.body() ?: payload).toEntity(pass)
            userDao.upsert(remoteUser)
            remoteUser.id
        }

    // Obtener usuario por email
    suspend fun getUserByEmail(email: String): UserEntity? {
        return userDao.getByEmail(email)
    }

    // Obtener todos los usuarios para la pantalla de admin
    suspend fun getAllUsers(): List<UserEntity> {
        syncUsuarios().getOrElse { /* devolvemos caché local */ }
        return userDao.getAll()
    }

    // Funciones para que el admin gestione roles (solo local por ahora)
    suspend fun promoteToAdmin(user: UserEntity) {
        if (user.role != "ADMIN") { 
            val updatedUser = user.copy(role = "ADMIN")
            userDao.updateUser(updatedUser)
        }
    }

    suspend fun demoteToUser(user: UserEntity) {
        if (user.role != "USER") { 
            val updatedUser = user.copy(role = "USER")
            userDao.updateUser(updatedUser)
        }
    }

    // Nueva función para cambiar contraseña (local).
    suspend fun changePassword(email: String, currentPass: String, newPass: String): Result<Unit> {
        val user = userDao.getByEmail(email)
            ?: return Result.failure(IllegalArgumentException("Usuario no encontrado"))

        if (user.pass != currentPass) {
            return Result.failure(IllegalArgumentException("La contraseña actual es incorrecta"))
        }

        val updatedUser = user.copy(pass = newPass)
        userDao.updateUser(updatedUser)
        return Result.success(Unit)
    }

    // Nueva función para actualizar perfil (nombre y teléfono) - local.
    suspend fun updateProfile(email: String, newName: String, newPhone: String): Result<Unit> {
        val user = userDao.getByEmail(email)
            ?: return Result.failure(IllegalArgumentException("Usuario no encontrado"))

        val updatedUser = user.copy(name = newName, phone = newPhone)
        userDao.updateUser(updatedUser)
        return Result.success(Unit)
    }
}

private fun <T> List<T>?.orElseEmpty(): List<T> = this ?: emptyList()
