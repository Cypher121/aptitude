package coffee.cypher.aptitude.datamodel

import com.mojang.serialization.Codec

enum class AptitudeLevel(val professionLevel: Int) {
    NONE(0), SKILLED(2), ADVANCED(4), ADEPT(5);

    val next
        get() = when (this) {
            NONE -> SKILLED
            SKILLED -> ADVANCED
            ADVANCED -> ADEPT
            ADEPT -> ADEPT
        }

    companion object {
        val CODEC: Codec<AptitudeLevel> =
            Codec.intRange(0, values().lastIndex)
                .xmap(values()::get, AptitudeLevel::ordinal)
    }
}
