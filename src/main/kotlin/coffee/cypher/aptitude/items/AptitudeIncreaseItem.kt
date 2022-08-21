package coffee.cypher.aptitude.items

import coffee.cypher.aptitude.actions.increaseAptitude
import coffee.cypher.aptitude.datamodel.aptitudeData
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import org.quiltmc.qkl.wrapper.qsl.items.itemSettingsOf

object AptitudeIncreaseItem : Item(itemSettingsOf()) {
    override fun useOnEntity(stack: ItemStack, user: PlayerEntity, entity: LivingEntity, hand: Hand): ActionResult {
        if (entity !is VillagerEntity) {
            return ActionResult.PASS
        }

        if (user !is ServerPlayerEntity) {
            return ActionResult.SUCCESS
        }

        val (current, max) = entity.aptitudeData.getAptitudeLevels(entity.villagerData.profession)

        if (current == max) {
            user.sendSystemMessage(Text.translatable("aptitude.notification.max_reached"))

            return ActionResult.CONSUME
        }

        if (current.next.professionLevel > entity.villagerData.level) {
            user.sendSystemMessage(Text.translatable("aptitude.notification.insufficient_profession"))
            return ActionResult.CONSUME
        }

        increaseAptitude(entity)

        stack.decrement(1)
        return ActionResult.PASS
    }
}
