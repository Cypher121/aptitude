@file:JvmName("AptitudeVillagerDataUtil")

package coffee.cypher.aptitude.datamodel

import coffee.cypher.aptitude.mixinaccessors.AptitudeVillagerDataAccessor
import coffee.cypher.aptitude.registry.PROFESSION_EXTENSION_ATTACHMENT
import coffee.cypher.aptitude.util.codecFunction
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.registry.Registry
import net.minecraft.village.VillagerProfession
import kotlin.random.Random
import kotlin.random.nextInt

data class AptitudeVillagerData(
    val professionAptitudes: Map<VillagerProfession, Pair<AptitudeLevel, AptitudeLevel>>
) {
    fun withAptitudeLevels(
        profession: VillagerProfession,
        current: AptitudeLevel,
        max: AptitudeLevel
    ): AptitudeVillagerData {
        if (current > max) {
            throw IllegalArgumentException(
                "Current aptitude for ${profession.name} ($current) would exceed max $max"
            )
        }
        return copy(professionAptitudes = professionAptitudes + mapOf(profession to (current to max)))
    }

    fun withCurrentAptitude(
        profession: VillagerProfession,
        current: AptitudeLevel,
        allowElevateMax: Boolean = false
    ): AptitudeVillagerData {
        val currentMax = getMaxAptitude(profession)

        val newMax = if (current > currentMax && allowElevateMax)
            current
        else currentMax

        return withAptitudeLevels(profession, current, newMax)
    }

    fun withMaxAptitude(
        profession: VillagerProfession,
        max: AptitudeLevel
    ): AptitudeVillagerData {
        return withAptitudeLevels(profession, getCurrentAptitude(profession), max)
    }

    fun getAptitudeLevels(profession: VillagerProfession): Pair<AptitudeLevel, AptitudeLevel> {
        return professionAptitudes.getOrDefault(profession, AptitudeLevel.NONE to AptitudeLevel.NONE)
    }

    fun getCurrentAptitude(profession: VillagerProfession): AptitudeLevel {
        return professionAptitudes[profession]?.first ?: AptitudeLevel.NONE
    }

    fun getMaxAptitude(profession: VillagerProfession): AptitudeLevel {
        return professionAptitudes[profession]?.second ?: AptitudeLevel.NONE
    }

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

            if (choices.isEmpty()) {
                return AptitudeVillagerData(emptyMap())
            }

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
    get() = (this as AptitudeVillagerDataAccessor).`aptitude$aptitudeVillagerData`
    set(value) {
        (this as AptitudeVillagerDataAccessor).`aptitude$aptitudeVillagerData` = value
    }

fun PacketByteBuf.writeAptitudeMap(
    map: Map<VillagerProfession, Pair<AptitudeLevel, AptitudeLevel>>,
    active: VillagerProfession? = null
) {
    writeVarInt(map.size)

    var activeIndex = -1

    map.entries.forEachIndexed { index, it ->
        writeId(Registry.VILLAGER_PROFESSION, it.key)
        writeByte(it.value.first.ordinal)
        writeByte(it.value.second.ordinal)

        if (active == it.key) {
            activeIndex = index
        }
    }

    if (active != null) {
        writeVarInt(activeIndex)
    }
}

fun PacketByteBuf.readAptitudeMap(): Map<VillagerProfession, Pair<AptitudeLevel, AptitudeLevel>> {
    return List(readVarInt()) {
        val key = readById(Registry.VILLAGER_PROFESSION) ?: return@List null

        key to (enumValues<AptitudeLevel>()[readByte().toInt()] to enumValues<AptitudeLevel>()[readByte().toInt()])
    }.filterNotNull().toMap()
}

fun PacketByteBuf.readAptitudeMapWithActive(): Pair<Map<VillagerProfession, Pair<AptitudeLevel, AptitudeLevel>>, VillagerProfession?> {
    val list = List(readVarInt()) {
        val key = readById(Registry.VILLAGER_PROFESSION) ?: return@List null

        key to (enumValues<AptitudeLevel>()[readByte().toInt()] to enumValues<AptitudeLevel>()[readByte().toInt()])
    }

    val idx = readVarInt()

    if (idx < 0) {
        return list.filterNotNull().toMap() to null
    }

    return list.filterNotNull().toMap() to list[idx]?.first
}
