package com.example.rentfage.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.rentfage.data.local.dao.CasaDao
import com.example.rentfage.data.local.dao.UserDao
import com.example.rentfage.data.local.entity.CasaEntity
import com.example.rentfage.data.local.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [UserEntity::class, CasaEntity::class],
    version = 5, // Incremento la versión para forzar actualización si se usa migración destructiva
    exportSchema = true
)
abstract class AppDatabase: RoomDatabase(){
    abstract fun userDao(): UserDao
    abstract fun casaDao(): CasaDao

    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private const val DB_NAME = "rentfage.db"

        fun getInstance(context: Context): AppDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .addCallback(object : RoomDatabase.Callback(){
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                // Seed para Usuarios
                                val userDao = getInstance(context).userDao()
                                if (userDao.count() == 0) {
                                    // USUARIO ADMIN RESTAURADO
                                    val userSeed = listOf(
                                        UserEntity(
                                            name = "Administrador", 
                                            email = "admin@rent.cl", 
                                            phone = "99999999", 
                                            pass = "Admin123!", // Cumple con las reglas de seguridad
                                            role = "ADMIN"
                                        ),
                                        UserEntity(
                                            name = "Usuario Prueba", 
                                            email = "usuario@rent.cl", 
                                            phone = "12345678", 
                                            pass = "User123!", 
                                            role = "USER"
                                        )
                                    )
                                    userSeed.forEach { userDao.insertar(it) }
                                }

                                // Seed para Casas
                                val casaDao = getInstance(context).casaDao()
                                if (casaDao.count() == 0) {
                                    val casaSeed = listOf(
                                        CasaEntity(
                                            price = "$250.000 CLP",
                                            address = "Las Condes, Santiago",
                                            details = "Acogedora casa de un piso, con jardín formado. Ideal para una vida tranquila cerca de la ciudad.",
                                            imageUri = "android.resource://com.example.rentfage/drawable/casa1",
                                            latitude = -33.4489,
                                            longitude = -70.6693
                                        ),
                                        CasaEntity(
                                            price = "$450.000 CLP",
                                            address = "Providencia, Santiago",
                                            details = "Amplia casa de 3 dormitorios y 2 baños, con un gran patio trasero y estacionamiento. Perfecta para una familia.",
                                            imageUri = "android.resource://com.example.rentfage/drawable/casa2",
                                            latitude = -33.4314,
                                            longitude = -70.6083
                                        ),
                                        CasaEntity(
                                            price = "$320.000 CLP",
                                            address = "Los Trapenses, La Dehesa",
                                            details = "Moderna casa estilo mediterráneo con excelente iluminación natural. Cuenta con piscina y quincho.",
                                            imageUri = "android.resource://com.example.rentfage/drawable/casa3",
                                            latitude = -33.4164,
                                            longitude = -70.5679
                                        )
                                    )
                                    casaDao.insertarTodas(casaSeed)
                                }
                            }
                        }
                    }).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}