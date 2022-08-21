package coffee.cypher.aptitude.util

import coffee.cypher.aptitude.mixinaccessors.insertItem
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

fun ScreenHandler.transferWithinPlayerInventory(
    slotRanges: InventorySlots,
    player: PlayerEntity,
    index: Int
): ItemStack {
    if (index !in slots.indices) {
        return ItemStack.EMPTY
    }

    val slot = slots[index]

    if (!slot.hasStack()) {
        return ItemStack.EMPTY
    }

    val insertingStack = slot.stack
    val originalStack = insertingStack.copy()

    if (index in slotRanges.playerMainInventorySlots) {
        if (!insertItem(insertingStack, slotRanges.playerHotbarSlots)) {
            return ItemStack.EMPTY
        }
    } else if (index in slotRanges.playerHotbarSlots && !insertItem(
            insertingStack,
            slotRanges.playerMainInventorySlots
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
