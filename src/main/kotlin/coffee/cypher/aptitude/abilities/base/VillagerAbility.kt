package coffee.cypher.aptitude.abilities.base

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.screen.slot.Slot

interface VillagerAbility {
    val slots: List<Slot>

    val inventories: List<Inventory>

    fun transfer(player: PlayerEntity, index: Int): ItemStack
}

