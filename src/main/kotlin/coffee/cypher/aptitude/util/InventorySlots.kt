package coffee.cypher.aptitude.util

import coffee.cypher.aptitude.mixins.AptitudeScreenHandlerMixin
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler

@JvmInline
value class InventorySlots(val internalInventorySize: Int) {
    val internalSlots
        get() = 0 until internalInventorySize

    val playerInventorySlots
        get() = internalInventorySize until internalInventorySize + 36

    val playerMainInventorySlots
        get() = internalInventorySize until internalInventorySize + 27

    val playerHotbarSlots
        get() = internalInventorySize + 27 until internalInventorySize + 36
}

fun ScreenHandler.transferWithinPlayerInventory(playerInventoryOffset: Int, player: PlayerEntity, index: Int): ItemStack {
    val insertItem = (this as AptitudeScreenHandlerMixin)::callInsertItem

    if (index !in slots.indices) {
        return ItemStack.EMPTY
    }

    val slot = slots[index]

    if (!slot.hasStack()) {
        return ItemStack.EMPTY
    }

    val insertingStack = slot.stack
    val originalStack = insertingStack.copy()

    val slotRanges = InventorySlots(playerInventoryOffset)

    if (index in slotRanges.playerMainInventorySlots) {
        if (!insertItem(
                insertingStack,
                slotRanges.playerHotbarSlots.first,
                slotRanges.playerHotbarSlots.last,
                false
            )
        ) {
            return ItemStack.EMPTY
        }
    } else if (index in slotRanges.playerHotbarSlots && !insertItem(
            insertingStack,
            slotRanges.playerMainInventorySlots.first,
            slotRanges.playerMainInventorySlots.last,
            false
        )
    ) {
        return ItemStack.EMPTY
    }

    if (insertingStack.isEmpty) {
        slot.stack = ItemStack.EMPTY
    } else {
        slot.markDirty()
    }

    if (originalStack.count == insertingStack.count) {
        return ItemStack.EMPTY
    }

    slot.onTakeItem(player, insertingStack)

    return originalStack
}
