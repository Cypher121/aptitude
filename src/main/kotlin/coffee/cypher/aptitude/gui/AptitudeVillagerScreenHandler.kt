package coffee.cypher.aptitude.gui

import coffee.cypher.aptitude.datamodel.readAptitudeMap
import coffee.cypher.aptitude.registry.APTITUDE_VILLAGER_SCREEN_HANDLER
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
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
        val playerInventory: PlayerInventory,
        val villager: VillagerEntity
    ) : AptitudeVillagerScreenHandler(syncId, playerInventory) {
        override fun canUse(player: PlayerEntity): Boolean {
            return villager.currentCustomer == player
        }

        override fun close(player: PlayerEntity) {
            super.close(player)
            //set twice so it runs resetCustomer() properly
            villager.currentCustomer = null
            villager.currentCustomer = null
        }
    }

    class Client(
        syncId: Int,
        playerInventory: PlayerInventory,
        buf: PacketByteBuf
    ) : AptitudeVillagerScreenHandler(syncId, playerInventory) {
        override fun canUse(player: PlayerEntity): Boolean {
            return true
        }

        val aptitudes = buf.readAptitudeMap()
    }
}
