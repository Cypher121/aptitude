package coffee.cypher.aptitude.abilities

import coffee.cypher.aptitude.Aptitude
import coffee.cypher.aptitude.abilities.base.VillagerAbility
import coffee.cypher.aptitude.abilities.base.client.VillagerAbilityClient
import coffee.cypher.aptitude.gui.AptitudeVillagerScreen
import coffee.cypher.aptitude.gui.AptitudeVillagerScreenHandler
import coffee.cypher.aptitude.mixinaccessors.insertItem
import coffee.cypher.kettle.inventory.get
import coffee.cypher.kettle.inventory.set
import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.EnchantmentLevelEntry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.CraftingResultInventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.EnchantedBookItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text
import org.quiltmc.qkl.wrapper.qsl.client.screen.textRenderer

open class UpgradeEnchantedBookAbility(private val handler: AptitudeVillagerScreenHandler) : VillagerAbility {
    companion object {
        const val FIRST_INPUT_X = AptitudeVillagerScreenHandler.ABILITY_START_X + 33
        const val FIRST_INPUT_Y = AptitudeVillagerScreenHandler.ABILITY_START_Y + 31

        const val SECOND_INPUT_X = AptitudeVillagerScreenHandler.ABILITY_START_X + 56
        const val SECOND_INPUT_Y = AptitudeVillagerScreenHandler.ABILITY_START_Y + 31

        const val OUTPUT_X = AptitudeVillagerScreenHandler.ABILITY_START_X + 114
        const val OUTPUT_Y = AptitudeVillagerScreenHandler.ABILITY_START_Y + 31
    }

    val input = SimpleInventory(2)
    val output = CraftingResultInventory()

    override val inventories = listOf(input, output)

    init {
        input.addListener {
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

                output[0] = resultStack
            }
        }
    }

    override val slots: List<Slot> = buildList {
        add(object : Slot(input, 0, FIRST_INPUT_X, FIRST_INPUT_Y) {
            override fun canInsert(stack: ItemStack): Boolean {
                return stack.isOf(Items.ENCHANTED_BOOK)
            }

            override fun getMaxItemCount(): Int {
                return 1
            }
        })
        add(object : Slot(input, 1, SECOND_INPUT_X, SECOND_INPUT_Y) {
            override fun canInsert(stack: ItemStack): Boolean {
                return stack.isOf(Items.END_CRYSTAL)
            }
        })
        add(object : Slot(output, 2, OUTPUT_X, OUTPUT_Y) {
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

    override fun transfer(player: PlayerEntity, index: Int): ItemStack {
        val slot = handler.slots[index]
        val transferStack = slot.stack
        val originalStack = transferStack.copy()

        if (index in 0..2) {
            if (!handler.insertItem(transferStack, handler.slotRanges.playerInventorySlots)) {
                return ItemStack.EMPTY
            }

            slot.onQuickTransfer(transferStack, originalStack)
        } else if (index in handler.slotRanges.playerInventorySlots) {
            val targetSlot = when (originalStack.item) {
                Items.ENCHANTED_BOOK -> 0
                Items.END_CRYSTAL -> 1
                else -> -1
            }

            if (targetSlot >= 0) {
                if (!handler.insertItem(transferStack, targetSlot..targetSlot)) {
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
}

@Environment(EnvType.CLIENT)
class UpgradeEnchantedBookAbilityClient(handler: AptitudeVillagerScreenHandler) : UpgradeEnchantedBookAbility(handler),
    VillagerAbilityClient {

    private val titleText = Text.translatable("aptitude.gui.special_ability.upgrade_enchanted_book.title")

    override fun AptitudeVillagerScreen.drawAbility(matrices: MatrixStack, mouseX: Int, mouseY: Int) {
        RenderSystem.setShaderTexture(0, Aptitude.id("textures/gui/upgrade_enchanted_book.png"))
        DrawableHelper.drawTexture(
            matrices,
            0, 0, 0,
            0f, 0f,
            AptitudeVillagerScreenHandler.ABILITY_MAX_WIDTH, AptitudeVillagerScreenHandler.ABILITY_MAX_HEIGHT,
            256, 256
        )

        textRenderer.draw(
            matrices,
            titleText,
            (AptitudeVillagerScreenHandler.ABILITY_MAX_WIDTH - textRenderer.getWidth(titleText)) / 2f, 10f,
            AptitudeVillagerScreen.TEXT_COLOR
        )
    }
}
