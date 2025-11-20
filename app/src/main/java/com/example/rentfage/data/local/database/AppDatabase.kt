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
    version = 5, 
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
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val userDao = getInstance(context).userDao()
                                
                                // ADMIN
                                val existingAdmin = userDao.getByEmail("admin@rent.cl")
                                if (existingAdmin != null) {
                                    val fixedAdmin = existingAdmin.copy(pass = "Admin123!", role = "ADMIN")
                                    if (existingAdmin.pass != "Admin123!" || existingAdmin.role != "ADMIN") {
                                        userDao.updateUser(fixedAdmin)
                                    }
                                } else {
                                    userDao.insertar(UserEntity(name = "Administrador", email = "admin@rent.cl", phone = "99999999", pass = "Admin123!", role = "ADMIN"))
                                }

                                // VICTOR ROSENDO
                                if (userDao.getByEmail("victor@rent.cl") == null) {
                                    userDao.insertar(UserEntity(name = "Victor Rosendo", email = "victor@rent.cl", phone = "12345678", pass = "User123!", role = "USER"))
                                }

                                // GERALD SANTANA
                                if (userDao.getByEmail("gerald@rent.cl") == null) {
                                    userDao.insertar(UserEntity(name = "Gerald Santana", email = "gerald@rent.cl", phone = "87654321", pass = "User123!", role = "USER"))
                                }

                                // Seed para Casas
                                val casaDao = getInstance(context).casaDao()
                                if (casaDao.count() == 0) {
                                    val casaSeed = listOf(
                                        CasaEntity(
                                            price = "$250.000 CLP",
                                            address = "Las Condes, Santiago",
                                            details = "Acogedora casa de un piso con excelente conectividad. Cuenta con amplios espacios luminosos, cocina equipada, jardín formado y estacionamiento para dos vehículos. Ideal para una vida tranquila cerca de la ciudad, a pasos de parques y centros comerciales.",
                                            imageUri = "android.resource://com.example.rentfage/drawable/casa1",
                                            latitude = -33.4489,
                                            longitude = -70.6693
                                        ),
                                        CasaEntity(
                                            price = "$450.000 CLP",
                                            address = "Providencia, Santiago",
                                            details = "Amplia casa de 3 dormitorios y 2 baños, con un gran patio trasero y estacionamiento techado. Ubicada en un barrio residencial consolidado, esta propiedad es perfecta para una familia que busca comodidad, seguridad y cercanía a colegios, universidades y transporte público.",
                                            imageUri = "android.resource://com.example.rentfage/drawable/casa2",
                                            latitude = -33.4314,
                                            longitude = -70.6083
                                        ),
                                        CasaEntity(
                                            price = "$320.000 CLP",
                                            address = "Los Trapenses, La Dehesa",
                                            details = "Espectacular casa moderna estilo mediterráneo con finas terminaciones y excelente iluminación natural. Disfruta de una vista privilegiada, piscina, quincho y amplias áreas verdes. Ubicada en condominio exclusivo con seguridad 24/7.",
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