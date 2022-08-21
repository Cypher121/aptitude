package coffee.cypher.aptitude.abilities

import coffee.cypher.aptitude.abilities.base.VillagerAbility
import coffee.cypher.aptitude.abilities.base.client.VillagerAbilityClient
import coffee.cypher.aptitude.gui.AptitudeVillagerScreen
import coffee.cypher.aptitude.gui.AptitudeVillagerScreenHandler
import coffee.cypher.aptitude.mixinaccessors.insertItem
import coffee.cypher.kettle.inventory.get
import coffee.cypher.kettle.inventory.set
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.EnchantmentLevelEntry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.CraftingResultInventory
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.EnchantedBookItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.slot.Slot
import net.minecraft.server.network.ServerPlayerEntity

open class UpgradeEnchantedBookAbility : VillagerAbility {
    val input = SimpleInventory(2)
    val output = CraftingResultInventory()

    override val inventories = listOf(input, output)

    override val slots: List<Slot> = buildList {
        add(object : Slot(input, 0, 100, 90) {
            override fun canInsert(stack: ItemStack): Boolean {
                return stack.isOf(Items.ENCHANTED_BOOK)
            }

            override fun getMaxItemCount(): Int {
                return 1
            }
        })
        add(object : Slot(input, 1, 120, 90) {
            override fun canInsert(stack: ItemStack): Boolean {
                return stack.isOf(Items.END_CRYSTAL)
            }
        })
        add(object : Slot(output, 2, 140, 90) {
            override fun canInsert(stack: ItemStack): Boolean {
                return false
            }

            override fun canTakeItems(playerEntity: PlayerEntity): Boolean {
                return input[0].isOf(Items.ENCHANTED_BOOK) &&
                        input[1].isOf(Items.END_CRYSTAL) &&
                        input[1].count >= 4
            }

            override fun onTakeItem(player: PlayerEntity, stack: ItemStack) {
                super.onTakeItem(player, stack)
                input[0] = ItemStack.EMPTY
                input[1].decrement(4)
            }
        })
    }

    override fun AptitudeVillagerScreenHandler.transfer(player: PlayerEntity, index: Int): ItemStack {
        val slot = slots[index]
        val transferStack = slot.stack
        val originalStack = transferStack.copy()

        if (index in 0..2) {
            if (!insertItem(transferStack, slotRanges.playerInventorySlots)) {
                return ItemStack.EMPTY
            }

            slot.onQuickTransfer(transferStack, originalStack)
        } else if (index in slotRanges.playerInventorySlots) {
            val targetSlot = when (originalStack.item) {
                Items.ENCHANTED_BOOK -> 0
                Items.END_CRYSTAL -> 1
                else -> -1
            }

            if (targetSlot >= 0) {
                if (!insertItem(transferStack, targetSlot..targetSlot)) {
                    return ItemStack.EMPTY
                }
            }
        }

        if (transferStack.isEmpty) {
            slot.stack = ItemStack.EMPTY
        } else {
            slot.markDirty()
        }

        if (transferStack.count == originalStack.count) {
            return ItemStack.EMPTY
        }

        slot.onTakeItem(player, transferStack)

        return originalStack
    }

    override fun AptitudeVillagerScreenHandler.contentChanged(inventory: Inventory) {
        if (!input[0].isOf(Items.ENCHANTED_BOOK) || !input[1].isOf(Items.END_CRYSTAL) || input[1].count < 4) {
            output[0] = ItemStack.EMPTY
        } else {
            val inputEnchantments = EnchantmentHelper.fromNbt(EnchantedBookItem.getEnchantmentNbt(input[0]))

            val resultEnchantments = inputEnchantments.mapValues {
                if (it.value < it.key.maxLevel) {
                    it.value + 1
                } else {
                    it.value
                }
            }

            val resultStack = Items.ENCHANTED_BOOK.defaultStack

            resultEnchantments.forEach {
                EnchantedBookItem.addEnchantment(resultStack, EnchantmentLevelEntry(it.key, it.value))
            }
        }
    }

    override fun AptitudeVillagerScreenHandler.onClose(player: PlayerEntity) {
        if (!player.isAlive || (player as? ServerPlayerEntity)?.isDisconnected == true) {
            input.removeStack(0).takeIf { !it.isEmpty }?.let { player.dropItem(it, false) }
            input.removeStack(1).takeIf { !it.isEmpty }?.let { player.dropItem(it, false) }
        } else if (player is ServerPlayerEntity) {
            player.inventory.offerOrDrop(input.removeStack(0))
            player.inventory.offerOrDrop(input.removeStack(1))
        }
    }
}

@Environment(EnvType.CLIENT)
class UpgradeEnchantedBookAbilityClient : UpgradeEnchantedBookAbility(), VillagerAbilityClient {
    override fun AptitudeVillagerScreenHandler.onClose(player: PlayerEntity) {
    }

    override fun AptitudeVillagerScreen.drawAbility(matrices: MatrixStack, mouseX: Int, mouseY: Int) {
    }
}
