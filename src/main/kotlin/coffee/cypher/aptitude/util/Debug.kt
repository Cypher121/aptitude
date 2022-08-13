package coffee.cypher.aptitude.util

import coffee.cypher.aptitude.model.aptitudeData
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
                            Text.empty()
                                .append(
                                    target.aptitudeData.professionAptitudes.toList()
                                        .joinToString(", ") { "${it.first.name} - ${it.second.name}" })
                                .append("\n")
                                .append(
                                    target.aptitudeData.professionMaxAptitudes.toList()
                                        .joinToString(", ") { "${it.first.name} - ${it.second.name}" })
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
