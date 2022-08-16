package coffee.cypher.aptitude.model

import coffee.cypher.aptitude.util.ConstCodec
import coffee.cypher.aptitude.util.asSuperType
import coffee.cypher.aptitude.util.codecFunction
import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.EitherCodec
import com.mojang.serialization.codecs.OptionalFieldCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.block.Block
import net.minecraft.tag.TagKey
import net.minecraft.util.registry.Registry
import java.util.*

sealed interface ProfessionExtension {
    object Nitwit : ProfessionExtension

    data class Regular(
        val workstationUpgrade: Upgrade,
        val aptitudeWeight: Int
    ) : ProfessionExtension {
        data class Upgrade(
            val station: Either<Block, TagKey<Block>>,
            val count: Int,
            val descriptionTranslationKey: String
        ) {
            companion object {
                val CODEC: Codec<Upgrade> = RecordCodecBuilder.create { upgrade ->
                    upgrade.group(
                        EitherCodec(
                            Registry.BLOCK.codec,
                            TagKey.createHashedCodec(Registry.BLOCK_KEY)
                        ).fieldOf("upgrade").forGetter(Upgrade::station),
                        OptionalFieldCodec(
                            "count",
                            Codec.intRange(1, Int.MAX_VALUE)
                        ).xmap({ it.orElse(1) }, { Optional.of(it) }).forGetter(Upgrade::count),
                        Codec.STRING.fieldOf("description")
                            .forGetter(Upgrade::descriptionTranslationKey)
                    ).apply(
                        upgrade,
                        upgrade.stable((Regular::Upgrade).codecFunction)
                    )
                }
            }
        }

        companion object {
            val CODEC: Codec<Regular> = RecordCodecBuilder.create { regular ->
                regular.group(
                    Upgrade.CODEC.fieldOf("workstation_upgrade")
                        .forGetter(Regular::workstationUpgrade),
                    Codec.intRange(1, Int.MAX_VALUE).fieldOf("aptitude_weight")
                        .forGetter(Regular::aptitudeWeight)
                ).apply(regular, regular.stable(::Regular.codecFunction))
            }
        }
    }

    companion object {
        val CODEC: Codec<ProfessionExtension> = EitherCodec(
            ConstCodec("nitwit").typed(Nitwit),
            Regular.CODEC
        ).asSuperType()
    }
}
