package coffee.cypher.aptitude.util

import coffee.cypher.aptitude.actions.increaseAptitude
import coffee.cypher.aptitude.datamodel.AptitudeLevel
import coffee.cypher.aptitude.datamodel.ProfessionExtension
import coffee.cypher.aptitude.datamodel.aptitudeData
import coffee.cypher.aptitude.registry.PROFESSION_EXTENSION_ATTACHMENT
import net.minecraft.command.CommandSource
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.text.Text
import net.minecraft.util.registry.Registry
import org.quiltmc.qkl.wrapper.minecraft.brigadier.*
import org.quiltmc.qkl.wrapper.minecraft.brigadier.argument.*
import org.quiltmc.qkl.wrapper.minecraft.brigadier.util.sendFeedback
import org.quiltmc.qkl.wrapper.qsl.commands.onCommandRegistration
import org.quiltmc.qkl.wrapper.qsl.registerEvents
import kotlin.jvm.optionals.getOrNull

@OptIn(ExperimentalStdlibApi::class)
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

            register("level_aptitude") {
                required(entity("villager")) { villager ->
                    executeWithResult {
                        val target = villager().value()

                        if (target is VillagerEntity) {
                            val (current, max) = target.aptitudeData.getAptitudeLevels(target.villagerData.profession)

                            if (current < max) {
                                if (target.villagerData.level >= current.next.professionLevel) {
                                    increaseAptitude(target)

                                    sendFeedback(Text.literal("Aptitude increased"))

                                    CommandResult.success()
                                } else {
                                    CommandResult.failure(Text.literal("Profession not high enough"))
                                }
                            } else {
                                CommandResult.failure(Text.literal("Max aptitude reached"))
                            }
                        } else {
                            CommandResult.failure(Text.literal("Not a villager"))
                        }
                    }
                }
            }

            register("set_aptitude") {
                required(entity("villager")) { villager ->
                    required(identifier("profession")) { profession ->
                        suggests { _, builder ->
                            CommandSource.suggestIdentifiers(Registry.VILLAGER_PROFESSION.ids, builder)
                        }

                        required(integer("level")) { level ->
                            optional(enum("type", listOf("current", "max"))) { type ->
                                executeWithResult {
                                    val target = villager().value()
                                    val comType = this[type]?.value()

                                    if (target is VillagerEntity) {
                                        val profId = profession().value()

                                        if (Registry.VILLAGER_PROFESSION.containsId(profId)) {
                                            val prof = Registry.VILLAGER_PROFESSION[profId]

                                            val extension = PROFESSION_EXTENSION_ATTACHMENT[prof].getOrNull()

                                            if (extension is ProfessionExtension.Regular) {
                                                if (comType != "max") {
                                                    val newLevel = level().value()

                                                    target.aptitudeData = target.aptitudeData.withCurrentAptitude(
                                                        prof, enumValues<AptitudeLevel>()[newLevel], allowElevateMax = true
                                                    )

                                                    sendFeedback(Text.literal("Level set"))

                                                    CommandResult.success()

                                                } else {
                                                    target.aptitudeData = target.aptitudeData.withMaxAptitude(
                                                        prof, enumValues<AptitudeLevel>()[level().value()]
                                                    )

                                                    sendFeedback(Text.literal("Max level set"))
                                                    CommandResult.success()
                                                }
                                            } else {
                                                CommandResult.failure(Text.literal("Not an Aptitude-enabled profession"))
                                            }
                                        } else {
                                            CommandResult.failure(Text.literal("Unknown profession"))
                                        }
                                    } else {
                                        CommandResult.failure(Text.literal("Not a villager"))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
