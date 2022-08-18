package coffee.cypher.aptitude.gui

import coffee.cypher.aptitude.datamodel.AptitudeLevel
import coffee.cypher.aptitude.datamodel.readAptitudeMapWithActive
import coffee.cypher.aptitude.registry.APTITUDE_VILLAGER_SCREEN_HANDLER
import coffee.cypher.aptitude.util.transferWithinPlayerInventory
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.slot.Slot
import net.minecraft.village.VillagerProfession

sealed class AptitudeVillagerScreenHandler(
    syncId: Int,
    private val playerInventory: PlayerInventory
) : ScreenHandler(APTITUDE_VILLAGER_SCREEN_HANDLER, syncId) {
    companion object {
        const val INVENTORY_X = 108
        const val INVENTORY_Y = 115
        const val HOTBAR_Y = 173

        const val INTERNAL_SLOT_COUNT = 1
    }

    private val internalInventory = SimpleInventory(INTERNAL_SLOT_COUNT)

    init {
        this.addSlot(Slot(internalInventory, 0, 60, 60))

        repeat(3) { row ->
            repeat(9) { column ->
                this.addSlot(Slot(playerInventory, column + row * 9 + 9, INVENTORY_X + column * 18, INVENTORY_Y + row * 18))
            }
        }

        repeat(9) { hotbarSlot ->
            this.addSlot(Slot(playerInventory, hotbarSlot, INVENTORY_X + hotbarSlot * 18, HOTBAR_Y))
        }
    }

    override fun canInsertIntoSlot(slot: Slot): Boolean {
        return false
    }

    override fun transferSlot(player: PlayerEntity, index: Int): ItemStack {
        return transferWithinPlayerInventory(INTERNAL_SLOT_COUNT, player, index)
    }

    class Server(
        syncId: Int,
        playerInventory: PlayerInventory,
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

        val aptitudes: Map<VillagerProfession, Pair<AptitudeLevel, AptitudeLevel>>
        val active: VillagerProfession?

        init {
            val (aptitudes, active) = buf.readAptitudeMapWithActive()
            this.aptitudes = aptitudes
            this.active = active
        }
    }
}
