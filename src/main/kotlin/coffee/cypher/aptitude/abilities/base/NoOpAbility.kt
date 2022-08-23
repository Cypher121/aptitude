package coffee.cypher.aptitude.abilities.base

import coffee.cypher.aptitude.abilities.base.client.VillagerAbilityClient
import coffee.cypher.aptitude.gui.AptitudeVillagerScreen
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot

open class NoOpAbility : VillagerAbility {
    override val slots: List<Slot> = emptyList()
    override val inventories: List<Inventory> = emptyList()

    override fun transfer(player: PlayerEntity, index: Int): ItemStack {
        return ItemStack.EMPTY
    }
}

@Environment(EnvType.CLIENT)
class NoOpAbilityClient : NoOpAbility(), VillagerAbilityClient {
    override fun AptitudeVillagerScreen.drawAbility(matrices: MatrixStack, mouseX: Int, mouseY: Int) {
    }
}
