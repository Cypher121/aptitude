package coffee.cypher.aptitude.util

import coffee.cypher.aptitude.datamodel.aptitudeData
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.text.Text
import org.quiltmc.qkl.wrapper.minecraft.brigadier.*
import org.quiltmc.qkl.wrapper.minecraft.brigadier.argument.entity
import org.quiltmc.qkl.wrapper.minecraft.brigadier.argument.value
import org.quiltmc.qkl.wrapper.minecraft.brigadier.util.sendFeedback
import org.quiltmc.qkl.wrapper.qsl.commands.onCommandRegistration
import org.quiltmc.qkl.wrapper.qsl.registerEvents

fun registerDebugUtils() {
    registerEvents {
        onCommandRegistration { _, _ ->
            register("check_aptitudes") {
                required(entity("villager")) { villager ->
                    executeWithResult {
                        val target = villager().value()

                        if (target is VillagerEntity) {
                            Text.literal(
                                target.aptitudeData.professionAptitudes.toList()
                                    .joinToString(", ") { "${it.first.name} - ${it.second.first.name} / ${it.second.second.name}" }
                            )
                                .let(this::sendFeedback)

                            CommandResult.success()
                        } else {
                            CommandResult.failure(Text.literal("Not a villager"))
                        }
                    }
                }
            }
        }
    }
}
