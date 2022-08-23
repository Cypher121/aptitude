package coffee.cypher.aptitude.gui

import coffee.cypher.aptitude.abilities.base.VillagerAbility
import coffee.cypher.aptitude.abilities.base.client.VillagerAbilityClient
import coffee.cypher.aptitude.datamodel.*
import coffee.cypher.aptitude.registry.*
import coffee.cypher.aptitude.util.InventorySlots
import coffee.cypher.aptitude.util.transferWithinPlayerInventory
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
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

        const val ABILITY_START_X = 107
        const val ABILITY_START_Y = 25

        const val ABILITY_MAX_WIDTH = 162
        const val ABILITY_MAX_HEIGHT = 79

        fun writeBuffer(buf: PacketByteBuf, villager: VillagerEntity) {
            buf.writeAptitudeMap(
                villager.aptitudeData.professionAptitudes,
                villager.villagerData.profession
            )

            buf.writeByte(villager.villagerData.level)
            buf.writeVarInt(villager.brain.getOptionalMemory(TRACKED_UPGRADES_MEMORY_MODULE).orElse(0))
        }
    }

    abstract val ability: VillagerAbility?

    val slotRanges
        get() = InventorySlots(ability?.slots?.size ?: 0)

    protected fun addSlots() {
        ability?.slots?.forEach {
            addSlot(it)
        }

        repeat(3) { row ->
            repeat(9) { column ->
                addSlot(
                    Slot(
                        playerInventory,
                        column + row * 9 + 9,
                        INVENTORY_X + column * 18,
                        INVENTORY_Y + row * 18
                    )
                )
            }
        }

        repeat(9) { hotbarSlot ->
            this.addSlot(Slot(playerInventory, hotbarSlot, INVENTORY_X + hotbarSlot * 18, HOTBAR_Y))
        }
    }

    override fun transferSlot(player: PlayerEntity, index: Int): ItemStack {
        val currentAbility = ability

        if (currentAbility != null) {
            val abilityTransfer = currentAbility.transfer(player, index)

            if (!abilityTransfer.isEmpty) {
                return abilityTransfer
            }
        }

        return transferWithinPlayerInventory(slotRanges, player, index)
    }

    override fun close(player: PlayerEntity) {
        super.close(player)

        ability?.inventories?.forEach {
            dropInventory(player, it)
        }
    }

    class Server(
        syncId: Int,
        playerInventory: PlayerInventory,
        val villager: VillagerEntity
    ) : AptitudeVillagerScreenHandler(syncId, playerInventory) {
        override fun canUse(player: PlayerEntity): Boolean {
            return villager.currentCustomer == player
        }

        override val ability: VillagerAbility?

        init {
            ability = readAbility()

            addSlots()
        }

        private fun readAbility(): VillagerAbility? {
            val aptLevels = villager.aptitudeData.getAptitudeLevels(
                villager.villagerData.profession
            )

            if (aptLevels.first < AptitudeLevel.ADVANCED) {
                return null
            }


            val attachment = PROFESSION_EXTENSION_ATTACHMENT[villager.villagerData.profession].orElse(null)
                    as? ProfessionExtension.Regular ?: return null

            if (villager.brain.getOptionalMemory(TRACKED_UPGRADES_MEMORY_MODULE)
                    .orElse(0) < attachment.workstationUpgrade.count
            ) {
                return null
            }

            return ABILITY_REGISTRY.getValue(attachment.ability)(this)
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
        val professionLevel: Int

        val currentTargetsFound: Int
        val requiredTargetsFound: Int

        override val ability: VillagerAbilityClient?

        init {
            val (aptitudes, active) = buf.readAptitudeMapWithActive()
            this.aptitudes = aptitudes
            this.active = active

            professionLevel = buf.readByte().toInt()

            currentTargetsFound = buf.readVarInt()
            requiredTargetsFound = if (active == null) {
                0
            } else {
                val attachment = PROFESSION_EXTENSION_ATTACHMENT[active].orElse(null)

                if (attachment !is ProfessionExtension.Regular) {
                    0
                } else {
                    attachment.workstationUpgrade.count
                }
            }

            ability = readAbility()

            addSlots()
        }

        //TODO dedupe with screen logic
        private fun readAbility(): VillagerAbilityClient? {
            if (active == null) {
                return null
            }

            val aptLevels = aptitudes[active] ?: return null

            if (aptLevels.first < AptitudeLevel.ADVANCED) {
                return null
            }

            if (currentTargetsFound < requiredTargetsFound) {
                return null
            }

            val attachment = PROFESSION_EXTENSION_ATTACHMENT[active].orElse(null)
                    as? ProfessionExtension.Regular ?: return null

            return CLIENT_ABILITY_REGISTRY.getValue(attachment.ability)(this)
        }
    }
}
