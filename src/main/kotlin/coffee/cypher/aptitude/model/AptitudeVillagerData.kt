@file:JvmName("AptitudeVillagerDataUtil")

package coffee.cypher.aptitude.model

import coffee.cypher.aptitude.registry.PROFESSION_EXTENSION_ATTACHMENT
import coffee.cypher.aptitude.util.codecFunction
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandler
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.registry.Registry
import net.minecraft.village.VillagerProfession
import kotlin.random.Random
import kotlin.random.nextInt

data class AptitudeVillagerData(
    val professionAptitudes: Map<VillagerProfession, AptitudeLevel>,
    val professionMaxAptitudes: Map<VillagerProfession, AptitudeLevel>
) {
    companion object {
        @JvmField
        val CODEC: Codec<AptitudeVillagerData> = RecordCodecBuilder.create {
            it.group(
                Codec.simpleMap(
                    Registry.VILLAGER_PROFESSION.codec,
                    AptitudeLevel.CODEC,
                    Registry.VILLAGER_PROFESSION
                )
                    .fieldOf("CurrentAptitudes")
                    .forGetter(AptitudeVillagerData::professionAptitudes),
                Codec.simpleMap(
                    Registry.VILLAGER_PROFESSION.codec,
                    AptitudeLevel.CODEC,
                    Registry.VILLAGER_PROFESSION
                )
                    .fieldOf("MaxAptitudes")
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

            val totalProficiencies = Random.nextInt(3..5)
            val lv2ProficiencyCount = Random.nextInt(1..2)
            val lv1ProficiencyCount = totalProficiencies - lv2ProficiencyCount

            val choices = PROFESSION_EXTENSION_ATTACHMENT.entryIterator()
                .asSequence()
                .associate { it.entry to it.value.aptitudeWeight }
                .toMutableMap()

            val lv2Proficiencies = List(lv2ProficiencyCount) {
                pickAndRemove(choices)
            }

            val lv1Proficiencies = List(lv1ProficiencyCount) {
                pickAndRemove(choices)
            }

            val starting = (lv1Proficiencies + lv2Proficiencies).associateWith {
                AptitudeLevel.NONE
            }

            val maximum = lv1Proficiencies.associateWith { AptitudeLevel.SKILLED } +
                    lv2Proficiencies.associateWith { AptitudeLevel.ADVANCED }

            return AptitudeVillagerData(starting, maximum)
        }
    }
}

var VillagerEntity.aptitudeData: AptitudeVillagerData
    get() = dataTracker.get(TRACKING_KEY)
    set(value) {
        dataTracker.set(TRACKING_KEY, value)
    }

fun DataTracker.startTrackingAptitude() {
    startTracking(TRACKING_KEY, AptitudeVillagerData(emptyMap(), emptyMap()))
}


private val TRACKING_KEY: TrackedData<AptitudeVillagerData> =
    DataTracker.registerData(VillagerEntity::class.java, TrackingHandler)

private object TrackingHandler : TrackedDataHandler.SimpleHandler<AptitudeVillagerData> {
    override fun write(buf: PacketByteBuf, value: AptitudeVillagerData) {
        buf.writeMap(value.professionAptitudes)
        buf.writeMap(value.professionMaxAptitudes)
    }

    override fun read(buf: PacketByteBuf): AptitudeVillagerData {
        return AptitudeVillagerData(buf.readMap(), buf.readMap())
    }

    private fun PacketByteBuf.writeMap(map: Map<VillagerProfession, AptitudeLevel>) {
        writeVarInt(map.size)

        map.forEach {
            writeId(Registry.VILLAGER_PROFESSION, it.key)
            writeByte(it.value.ordinal)
        }
    }

    private fun PacketByteBuf.readMap(): Map<VillagerProfession, AptitudeLevel> {
        return mutableMapOf<VillagerProfession, AptitudeLevel>().also { r ->
            repeat(readVarInt()) {
                val key = readById(Registry.VILLAGER_PROFESSION) ?: return@repeat

                r += key to enumValues<AptitudeLevel>()[readByte().toInt()]
            }
        }
    }
}
