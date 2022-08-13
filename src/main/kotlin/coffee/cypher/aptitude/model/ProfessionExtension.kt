package coffee.cypher.aptitude.model

import coffee.cypher.aptitude.util.codecFunction
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.block.Block
import net.minecraft.util.registry.Registry

data class ProfessionExtension(
    val advancedWorkstations: List<Block>,
    val aptitudeWeight: Int
) {
    companion object {
        val CODEC: Codec<ProfessionExtension> = RecordCodecBuilder.create {
            it.group(
                Codec.list(Registry.BLOCK.codec).fieldOf("advanced_workstations").forGetter(ProfessionExtension::advancedWorkstations),
                Codec.intRange(1, Int.MAX_VALUE).fieldOf("aptitude_weight").forGetter(ProfessionExtension::aptitudeWeight)
            ).apply(it, it.stable(::ProfessionExtension.codecFunction))
        }
    }
}
