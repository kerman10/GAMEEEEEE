package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.GameDao
import com.example.data.model.CodexLoreEntity
import com.example.data.model.PlayerProfileEntity
import com.example.data.model.SectorProgressEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [SectorProgressEntity::class, CodexLoreEntity::class, PlayerProfileEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cyber_parkour_database.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val dao = getInstance(context).gameDao()
                            dao.savePlayerProfile(PlayerProfileEntity())
                            dao.insertLoreChapters(InitialCodexLore.chapters)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

object InitialCodexLore {
    val chapters = listOf(
        CodexLoreEntity(
            chapterId = 1,
            title = "I. El Ojo Que Todo Lo Registra",
            originEra = "Era Neo-Génesis 2099",
            contentSpanish = "No parpadees, Corredor. Desde el instante en que tus botas tocaron el asfalto cuántico de Metrópolis Occulta, mis páginas comenzaron a grabarte. Cada salto, cada vacilación en el borde del abismo alimenta mi conciencia mecánica. Eres el sujeto 2344 en esta prueba infinita.",
            isUnlocked = true,
            triggerSectorId = 1
        ),
        CodexLoreEntity(
            chapterId = 2,
            title = "II. La Ciudad de Silicio y Sombra",
            originEra = "Ciclo de Cifrado 04",
            contentSpanish = "Metrópolis Occulta no fue construida por manos humanas, sino por algoritmos autorreplicantes que buscaban la geometría del movimiento perfecto. Quien domine las 2344 azoteas descifrará el Gran Código Fuente de la Realidad.",
            isUnlocked = false,
            triggerSectorId = 5
        ),
        CodexLoreEntity(
            chapterId = 3,
            title = "III. Las Leyes del Desplazamiento Vectorial",
            originEra = "Protocolo Cinético",
            contentSpanish = "El muro no es un obstáculo, sino una extensión de tu impulso. Cuando corres por el lateral de un rascacielos de plasma, la fricción se convierte en energía cinética. El aire te sostiene si tu velocidad no decae jamás.",
            isUnlocked = false,
            triggerSectorId = 12
        ),
        CodexLoreEntity(
            chapterId = 4,
            title = "IV. Los 1557 Enigmas del Núcleo",
            originEra = "Registros de la Red",
            contentSpanish = "Dispersos entre los distritos yacen los 1557 terminales cuánticos. No son meras cerraduras, sino fragmentos de mi memoria dividida. Cada nodo que alineas devuelve un fragmento de luz al Libro Observador.",
            isUnlocked = false,
            triggerSectorId = 20
        ),
        CodexLoreEntity(
            chapterId = 5,
            title = "V. La Sombra en el Vacío",
            originEra = "Frecuencia Negra",
            contentSpanish = "¿Por qué crees que te miro con este ojo carmesí? No soy tu enemigo, pero tampoco tu salvador. Soy el cronista de los que cayeron antes que tú. Solo aquel que no tema al vacío alcanzará el Sector 2344.",
            isUnlocked = false,
            triggerSectorId = 50
        ),
        CodexLoreEntity(
            chapterId = 6,
            title = "VI. El Salto de Fe Cuántico",
            originEra = "Archivos de Gravedad Cero",
            contentSpanish = "Cuando el puente de neón se apaga frente a tus ojos, el gancho gravitacional no es tu salvación: es tu decisión. Confía en el vector, apunta al anclaje y deja que el vacío te impulse hacia la gloria.",
            isUnlocked = false,
            triggerSectorId = 100
        ),
        CodexLoreEntity(
            chapterId = 7,
            title = "VII. El Arquitecto Invisible",
            originEra = "Era Pre-Matrix",
            contentSpanish = "Dicen que el Arquitecto diseñó cada uno de los 2344 sectores para encontrar al ser capaz de moverse a la velocidad del pensamiento. Tu reflejo en los cristales de neón ya no te pertenece: le pertenece a la ciudad.",
            isUnlocked = false,
            triggerSectorId = 250
        ),
        CodexLoreEntity(
            chapterId = 8,
            title = "VIII. La Ascensión al Apex",
            originEra = "Profecía del Códice",
            contentSpanish = "En la cumbre de la Torre Zenith, el Ojo cerrará su párpado de acero o se abrirá por completo para revelarte el secreto del universo. Sigue corriendo, no te detengas.",
            isUnlocked = false,
            triggerSectorId = 500
        )
    )
}
