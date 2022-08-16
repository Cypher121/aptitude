package coffee.cypher.aptitude.gui

import coffee.cypher.aptitude.registry.APTITUDE_VILLAGER_SCREEN_HANDLER
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler

class AptitudeVillagerScreenHandler(
    syncId: Int,
    val playerInventory: PlayerInventory,
    val villager: VillagerEntity? = null
) : ScreenHandler(APTITUDE_VILLAGER_SCREEN_HANDLER, syncId) {
    override fun transferSlot(player: PlayerEntity?, index: Int): ItemStack {
        TODO("Not yet implemented")
    }

    override fun canUse(player: PlayerEntity?): Boolean {
        TODO("Not yet implemented")
    }
}
