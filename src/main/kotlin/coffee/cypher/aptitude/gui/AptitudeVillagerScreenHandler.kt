package coffee.cypher.aptitude.gui

import coffee.cypher.aptitude.registry.APTITUDE_VILLAGER_SCREEN_HANDLER
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler

sealed class AptitudeVillagerScreenHandler(
    syncId: Int,
    private val playerInventory: PlayerInventory
) : ScreenHandler(APTITUDE_VILLAGER_SCREEN_HANDLER, syncId) {
    override fun transferSlot(player: PlayerEntity?, index: Int): ItemStack {
        TODO("Not yet implemented")
    }

    class Server(
        syncId: Int,
        playerInventory: PlayerInventory,
        private val villagerEntity: VillagerEntity
    ) : AptitudeVillagerScreenHandler(syncId, playerInventory) {
        override fun canUse(player: PlayerEntity): Boolean {
            return villagerEntity.currentCustomer == player
        }
    }

    class Client(
        syncId: Int,
        playerInventory: PlayerInventory
    ) : AptitudeVillagerScreenHandler(syncId, playerInventory) {
        override fun canUse(player: PlayerEntity): Boolean {
            return true
        }
    }
}
