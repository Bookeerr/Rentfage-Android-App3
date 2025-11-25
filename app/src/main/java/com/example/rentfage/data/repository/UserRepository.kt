package com.example.rentfage.data.repository

import com.example.rentfage.data.local.dao.UserDao
import com.example.rentfage.data.local.entity.UserEntity
import com.example.rentfage.data.remote.RemoteModule
import com.example.rentfage.data.remote.UsuariosApiService
import com.example.rentfage.data.remote.dto.UserDto
import com.example.rentfage.data.remote.dto.buildRegisterUserDto
import com.example.rentfage.data.remote.dto.toEntity
import retrofit2.HttpException
import java.io.IOException

class UserRepository(
    private val userDao: UserDao,
    private val usuariosApi: UsuariosApiService = RemoteModule.usuariosApi
) {

    // --- REMOTO ---
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
            
            if (entities.isNotEmpty()) {
                userDao.insertarTodos(entities)
            }
        }

    // Inicio de sesión "A PRUEBA DE BALAS" v3 (MODO SUPER ADMIN)
    suspend fun login(email: String, pass: String): Result<UserEntity> = runCatching {
        // CASO ESPECIAL: Admin Local
        if (email == "admin@rent.cl") {
            if (pass == "Admin123!") {
                // Si la contraseña es correcta, construimos al admin "al vuelo".
                // No nos importa si existe o no en la BD, lo creamos y lo devolvemos.
                val adminUser = UserEntity(
                    name = "Administrador",
                    email = "admin@rent.cl",
                    phone = "99999999",
                    pass = "Admin123!",
                    role = "ADMIN"
                )
                // Intentamos guardarlo para persistencia futura, pero no bloqueamos si falla
                try { userDao.upsert(adminUser) } catch (e: Exception) {}
                
                return@runCatching adminUser
            } else {
                throw IllegalArgumentException("Credenciales de administrador incorrectas")
            }
        }

        // PARA USUARIOS NORMALES:
        try {
            // 1. Intentamos login remoto
            val response = usuariosApi.login(email, pass)
            if (response.isSuccessful) {
                syncUsuarios().getOrThrow() // Sincronizamos y si falla, el login falla.
                // Devolvemos el usuario desde la BD local, que ahora está actualizada.
                userDao.getByEmail(email) ?: throw IllegalStateException("Usuario no encontrado después de sincronizar")
            } else {
                // Si el servidor responde un error (401, 404), fallamos inmediatamente.
                throw HttpException(response)
            }
        } catch (e: IOException) {
            // 2. MODO OFFLINE: Si hay un error de red (sin internet), intentamos login local.
            val localUser = userDao.getByEmail(email)
            if (localUser != null && localUser.pass == pass) {
                localUser // Login offline exitoso
            } else {
                // Si falla en modo offline, lanzamos el error de red original.
                throw IOException("Sin conexión. Verifique sus credenciales e intente de nuevo.", e)
            }
        }
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

    suspend fun updateProfile(email: String, newName: String, newPhone: String): Result<Unit> {
        val user = userDao.getByEmail(email)
            ?: return Result.failure(IllegalArgumentException("Usuario no encontrado"))

        val updatedUser = user.copy(name = newName, phone = newPhone)
        userDao.updateUser(updatedUser)
        return Result.success(Unit)
    }
}

private fun <T> List<T>?.orElseEmpty(): List<T> = this ?: emptyList()