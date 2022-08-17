@file:JvmName("AptitudeVillagerDataUtil")

package coffee.cypher.aptitude.datamodel

import coffee.cypher.aptitude.registry.PROFESSION_EXTENSION_ATTACHMENT
import coffee.cypher.aptitude.util.codecFunction
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandler
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.registry.Registry
import net.minecraft.village.VillagerProfession
import kotlin.random.Random
import kotlin.random.nextInt

data class AptitudeVillagerData(
    val professionAptitudes: Map<VillagerProfession, Pair<AptitudeLevel, AptitudeLevel>>
) {
    companion object {
        private val ENTRY_CODEC: Codec<Pair<AptitudeLevel, AptitudeLevel>> = RecordCodecBuilder.create { pair ->
            pair.group(
                AptitudeLevel.CODEC.fieldOf("Current").forGetter { it.first },
                AptitudeLevel.CODEC.fieldOf("Max").forGetter { it.second }
            ).apply(pair, pair.stable({ a: AptitudeLevel, b: AptitudeLevel -> a to b }.codecFunction))
        }

        @JvmField
        val CODEC: Codec<AptitudeVillagerData> = RecordCodecBuilder.create {
            it.group(
                Codec.simpleMap(
                    Registry.VILLAGER_PROFESSION.codec,
                    ENTRY_CODEC,
                    Registry.VILLAGER_PROFESSION
                )
                    .fieldOf("Aptitudes")
                    .forGetter(AptitudeVillagerData::professionAptitudes)
            ).apply(it, it.stable(::AptitudeVillagerData.codecFunction))
        }

        @JvmStatic
        fun createRandom(): AptitudeVillagerData {
            fun <T> pickAndRemove(choices: MutableMap<T, Int>): T {
                var counter = Random.nextInt(choices.values.sum())

                val added = choices.keys.first { choice ->
                    (counter < choices.getValue(choice))
                        .also { counter -= choices.getValue(choice) }
                }

                choices -= added
                return added
            }

            val totalAptitudes = Random.nextInt(3..5)
            val lv2AptitudeCount = Random.nextInt(1..2)
            val lv1AptitudeCount = totalAptitudes - lv2AptitudeCount

            val choices = PROFESSION_EXTENSION_ATTACHMENT.entryIterator()
                .asSequence()
                .mapNotNull { (it.value as? ProfessionExtension.Regular)?.let { r -> it.entry to r.aptitudeWeight } }
                .toMap()
                .toMutableMap()

            val lv2Aptitudes = List(lv2AptitudeCount) {
                pickAndRemove(choices)
            }

            val lv1Aptitudes = List(lv1AptitudeCount) {
                pickAndRemove(choices)
            }


            val aptitudes = lv1Aptitudes.associateWith { AptitudeLevel.SKILLED } +
                    lv2Aptitudes.associateWith { AptitudeLevel.ADVANCED }

            return AptitudeVillagerData(aptitudes.mapValues { AptitudeLevel.NONE to it.value })
        }
    }
}

var VillagerEntity.aptitudeData: AptitudeVillagerData
    get() = dataTracker.get(TRACKING_KEY)
    set(value) {
        dataTracker.set(TRACKING_KEY, value)
    }

fun VillagerEntity.startTrackingAptitude() {
    dataTracker.startTracking(TRACKING_KEY, AptitudeVillagerData.createRandom())
}


private val TRACKING_KEY: TrackedData<AptitudeVillagerData> =
    DataTracker.registerData(VillagerEntity::class.java, TrackingHandler)

private object TrackingHandler : TrackedDataHandler.SimpleHandler<AptitudeVillagerData> {
    init {
        TrackedDataHandlerRegistry.register(this)
    }

    override fun write(buf: PacketByteBuf, value: AptitudeVillagerData) {
        buf.writeAptitudeMap(value.professionAptitudes)
    }

    override fun read(buf: PacketByteBuf): AptitudeVillagerData {
        return AptitudeVillagerData(buf.readAptitudeMap())
    }
}

fun PacketByteBuf.writeAptitudeMap(map: Map<VillagerProfession, Pair<AptitudeLevel, AptitudeLevel>>) {
    writeVarInt(map.size)

    map.forEach {
        writeId(Registry.VILLAGER_PROFESSION, it.key)
        writeByte(it.value.first.ordinal)
        writeByte(it.value.second.ordinal)
    }
}

fun PacketByteBuf.readAptitudeMap(): Map<VillagerProfession, Pair<AptitudeLevel, AptitudeLevel>> {
    return List(readVarInt()) {
        val key = readById(Registry.VILLAGER_PROFESSION) ?: return@List null

        key to (enumValues<AptitudeLevel>()[readByte().toInt()] to enumValues<AptitudeLevel>()[readByte().toInt()])
    }.filterNotNull().toMap()
}
