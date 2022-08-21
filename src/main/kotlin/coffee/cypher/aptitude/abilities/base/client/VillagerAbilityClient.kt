package coffee.cypher.aptitude.abilities.base.client

import coffee.cypher.aptitude.abilities.base.VillagerAbility
import coffee.cypher.aptitude.gui.AptitudeVillagerScreen
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.util.math.MatrixStack

@Environment(EnvType.CLIENT)
interface VillagerAbilityClient : VillagerAbility {
    fun AptitudeVillagerScreen.drawAbility(matrices: MatrixStack, mouseX: Int, mouseY: Int)
}
