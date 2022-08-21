@file:Environment(EnvType.CLIENT)

package coffee.cypher.aptitude.gui

import coffee.cypher.aptitude.Aptitude
import coffee.cypher.aptitude.datamodel.AptitudeLevel
import coffee.cypher.aptitude.datamodel.ProfessionExtension
import coffee.cypher.aptitude.gui.packets.AptitudeToggleVillagerScreenC2SPacket
import coffee.cypher.aptitude.registry.PROFESSION_EXTENSION_ATTACHMENT
import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.Block
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ButtonWidget.PressAction
import net.minecraft.client.gui.widget.TexturedButtonWidget
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.OrderedText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Holder
import net.minecraft.util.Language
import net.minecraft.util.registry.Registry
import net.minecraft.village.VillagerProfession
import org.quiltmc.qkl.wrapper.qsl.client.screen.buttons
import org.quiltmc.qsl.tag.api.TagRegistry
import java.util.function.Consumer
import kotlin.math.floor
import org.quiltmc.qkl.wrapper.qsl.client.screen.client as clientNotNull


@Environment(EnvType.CLIENT)
class AptitudeVillagerScreen(
    screenHandler: AptitudeVillagerScreenHandler.Client,
    playerInventory: PlayerInventory,
    text: Text
) : HandledScreen<AptitudeVillagerScreenHandler.Client>(
    screenHandler,
    playerInventory,
    text
) {
    @Environment(EnvType.CLIENT)
    companion object {
        const val TEXTURE_PATH = "textures/gui/aptitude_villager.png"

        const val TEX_WIDTH = 512
        const val TEX_HEIGHT = 256

        const val CURRENT_APTITUDE_X = 107
        const val CURRENT_APTITUDE_Y = 5

        const val NEXT_APTITUDE_X = 107
        const val NEXT_APTITUDE_Y = 15

        const val ABILITY_UNAVAILABLE_CENTER_X = 190
        const val ABILITY_UNAVAILABLE_CENTER_Y = 35

        const val UNAVAILABLE_REASON_CENTER_X = 190
        const val UNAVAILABLE_REASON_CENTER_Y = 45

        const val UPGRADE_COUNT_CENTER_X = 190
        const val UPGRADE_COUNT_CENTER_Y = 90

        const val UPGRADE_ITEM_X = 182
        const val UPGRADE_ITEM_Y = 62
        const val UPGRADE_ITEM_SIZE = 16

        const val UPGRADE_CYCLE_DELAY = 40f

        const val MAX_TOOLTIP_WIDTH = 150

        const val TEXT_COLOR = 0x404040

        val DEFAULT_COLOR_STYLE = { style: Style -> style.withColor(TEXT_COLOR) }
    }

    private val list = handler.aptitudes.toList()
        .sortedByDescending { it.second.second.ordinal }

    private var time = 0f

    @Environment(EnvType.CLIENT)
    private data class AptitudeDrawData(
        val currentAptitude: AptitudeLevel,
        val maxAptitude: AptitudeLevel,

        val currentAptitudeDescriptionText: Text,
        val currentAptitudeLevelText: Text,
        val nextAptitudeDescriptionText: Text,
        val nextAptitudeLevelText: Text,

        val currentAptitudeDescriptionOffsetX: Int,
        val currentAptitudeLevelOffsetX: Int,
        val nextAptitudeDescriptionOffsetX: Int,
        val nextAptitudeLevelOffsetX: Int,

        val currentAptitudeLevelWidth: Int,
        val nextAptitudeLevelWidth: Int,

        val tooltipText: List<OrderedText>,
        val shouldDrawNext: Boolean,

        val abilityUnavailableText: Text,
        val unavailableReasonText: Text,
        val abilityUnavailableWidth: Int,
        val unavailableReasonWidth: Int,
        val abilityUnavailable: Boolean,

        val upgradeCountText: Text,
        val upgradeCountWidth: Int,
        val shouldShowUpgradeCount: Boolean,

        val upgradeBlockList: List<Block>,
        val upgradeTooltip: List<OrderedText>
    )

    private var drawData: AptitudeDrawData? = null

    private var entries = listOf<Entry>()

    init {
        backgroundWidth = 276
        backgroundHeight = 196

        playerInventoryTitleX = 107
        playerInventoryTitleY = backgroundHeight - 94
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        time += delta
        super.render(matrices, mouseX, mouseY, delta)

        drawMouseoverTooltip(matrices, mouseX, mouseY)
    }

    override fun drawBackground(
        matrices: MatrixStack,
        delta: Float,
        mouseX: Int,
        mouseY: Int
    ) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader)
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        RenderSystem.setShaderTexture(0, Aptitude.id(TEXTURE_PATH))
        drawTexture(
            matrices,
            x, y, zOffset,
            0.0f, 0.0f,
            backgroundWidth, backgroundHeight,
            TEX_WIDTH, TEX_HEIGHT
        )
    }

    override fun drawForeground(matrices: MatrixStack, mouseX: Int, mouseY: Int) {
        super.drawForeground(matrices, mouseX, mouseY)
        entries.forEach {
            it.render(matrices)
        }

        drawData?.let {
            textRenderer.draw(
                matrices,
                it.currentAptitudeDescriptionText,
                CURRENT_APTITUDE_X.toFloat() + it.currentAptitudeDescriptionOffsetX, CURRENT_APTITUDE_Y.toFloat(),
                0xFFFFFF
            )

            textRenderer.draw(
                matrices,
                it.currentAptitudeLevelText,
                CURRENT_APTITUDE_X.toFloat() + it.currentAptitudeLevelOffsetX, CURRENT_APTITUDE_Y.toFloat(),
                0xFFFFFF
            )

            if (it.shouldDrawNext) {
                textRenderer.draw(
                    matrices,
                    it.nextAptitudeDescriptionText,
                    NEXT_APTITUDE_X.toFloat() + it.nextAptitudeDescriptionOffsetX, NEXT_APTITUDE_Y.toFloat(),
                    0xFFFFFF
                )

                textRenderer.draw(
                    matrices,
                    it.nextAptitudeLevelText,
                    NEXT_APTITUDE_X.toFloat() + it.nextAptitudeLevelOffsetX, NEXT_APTITUDE_Y.toFloat(),
                    0xFFFFFF
                )
            }

            val relMouseX = mouseX - x
            val relMouseY = mouseY - y

            if (
                (!it.shouldDrawNext &&
                        relMouseX in (CURRENT_APTITUDE_X + it.currentAptitudeLevelOffsetX) until (CURRENT_APTITUDE_X + it.currentAptitudeLevelOffsetX + it.currentAptitudeLevelWidth) &&
                        relMouseY in CURRENT_APTITUDE_Y until (CURRENT_APTITUDE_Y + textRenderer.fontHeight)
                        ) ||
                (it.shouldDrawNext &&
                        relMouseX in (NEXT_APTITUDE_X + it.nextAptitudeLevelOffsetX) until (NEXT_APTITUDE_X + it.nextAptitudeLevelOffsetX + it.nextAptitudeLevelWidth) &&
                        relMouseY in NEXT_APTITUDE_Y until (NEXT_APTITUDE_Y + textRenderer.fontHeight)
                        )
            ) {
                renderOrderedTooltip(
                    matrices,
                    it.tooltipText,
                    relMouseX, relMouseY
                )
            }

            if (it.shouldShowUpgradeCount &&
                    relMouseX in UPGRADE_ITEM_X until (UPGRADE_ITEM_X + UPGRADE_ITEM_SIZE) &&
                    relMouseY in UPGRADE_ITEM_Y until (UPGRADE_ITEM_Y + UPGRADE_ITEM_SIZE)) {
                renderOrderedTooltip(
                    matrices,
                    it.upgradeTooltip,
                    relMouseX, relMouseY
                )
            }

            if (it.abilityUnavailable) {
                textRenderer.draw(
                    matrices,
                    it.abilityUnavailableText,
                    ABILITY_UNAVAILABLE_CENTER_X.toFloat() - it.abilityUnavailableWidth / 2,
                    ABILITY_UNAVAILABLE_CENTER_Y.toFloat(),
                    0xFFFFFF
                )

                textRenderer.draw(
                    matrices,
                    it.unavailableReasonText,
                    UNAVAILABLE_REASON_CENTER_X.toFloat() - it.unavailableReasonWidth / 2,
                    UNAVAILABLE_REASON_CENTER_Y.toFloat(),
                    0xFFFFFF
                )

                if (it.shouldShowUpgradeCount) {
                    textRenderer.draw(
                        matrices,
                        it.upgradeCountText,
                        UPGRADE_COUNT_CENTER_X.toFloat() - it.upgradeCountWidth / 2,
                        UPGRADE_COUNT_CENTER_Y.toFloat(),
                        0xFFFFFF
                    )

                    val index = (floor(time / UPGRADE_CYCLE_DELAY) % it.upgradeBlockList.size).toInt()

                    itemRenderer.renderInGui(
                        it.upgradeBlockList[index].asItem().defaultStack,
                        UPGRADE_ITEM_X, UPGRADE_ITEM_Y
                    )
                }
            }
        }
    }

    override fun init() {
        super.init()

        titleX = 5

        buttons += ReturnToMerchantButton()

        val maxPossibleLevel = list.maxOf { it.second.second }.ordinal
        entries = list.mapIndexed { index, it ->
            Entry(this, 5, 26, index, it, it.first == handler.active, maxPossibleLevel)
        }

        val profAptitudes = handler.active?.let { handler.aptitudes[it] }

        if (profAptitudes != null) {
            val (currentAptitude, maxAptitude) = profAptitudes

            val currentAptitudeDescriptionText = Text.translatable("aptitude.gui.current_aptitude")
                .styled(DEFAULT_COLOR_STYLE)

            val currentAptitudeLevelText = Text.translatable("aptitude.level.${currentAptitude.ordinal}")
                .styled(DEFAULT_COLOR_STYLE)

            val nextAptitudeDescriptionText = Text.translatable("aptitude.gui.next_aptitude")
                .styled(DEFAULT_COLOR_STYLE)

            val canReachNext = currentAptitude.next.professionLevel <= handler.professionLevel

            val nextAptitudeLevelText = Text.translatable("aptitude.level.${currentAptitude.next.ordinal}")
                .styled {
                    if (canReachNext) {
                        it.withColor(Formatting.DARK_GREEN)
                    } else {
                        it.withColor(Formatting.DARK_RED)
                    }
                }

            val shouldDrawNext = currentAptitude < maxAptitude

            val tooltipBase = when {
                !shouldDrawNext -> Text.translatable("aptitude.gui.max_reached")
                !canReachNext -> Text.translatable(
                    "aptitude.gui.insufficient_profession",
                    Text.translatable("merchant.level.${currentAptitude.next.professionLevel}")
                )

                else -> Text.translatable("aptitude.gui.level_ready")
            }

            val tooltipText = textRenderer.wrapLines(
                tooltipBase,
                MAX_TOOLTIP_WIDTH
            )

            val language = Language.getInstance()

            val currentAptitudeLevelWidth = textRenderer.getWidth(language.reorder(currentAptitudeLevelText))
            val nextAptitudeLevelWidth = textRenderer.getWidth(language.reorder(nextAptitudeLevelText))

            val currentAptitudeDescriptionOffsetX: Int
            val currentAptitudeLevelOffsetX: Int
            val nextAptitudeDescriptionOffsetX: Int
            val nextAptitudeLevelOffsetX: Int

            if (language.isRightToLeft) {
                currentAptitudeDescriptionOffsetX = currentAptitudeLevelWidth
                currentAptitudeLevelOffsetX = 0

                nextAptitudeDescriptionOffsetX = nextAptitudeLevelWidth
                nextAptitudeLevelOffsetX = 0
            } else {
                currentAptitudeDescriptionOffsetX = 0
                currentAptitudeLevelOffsetX = textRenderer.getWidth(language.reorder(currentAptitudeDescriptionText))

                nextAptitudeDescriptionOffsetX = 0
                nextAptitudeLevelOffsetX = textRenderer.getWidth(language.reorder(nextAptitudeDescriptionText))
            }

            val abilityUnavailable =
                currentAptitude < AptitudeLevel.ADVANCED || (handler.currentTargetsFound < handler.requiredTargetsFound)

            val abilityUnavailableText: Text
            val unavailableReasonText: Text

            val shouldShowUpgradeCount = currentAptitude >= AptitudeLevel.ADVANCED && abilityUnavailable
            val upgradeCountText: Text

            val upgradeBlockList: List<Block>
            val upgradeTooltip: List<OrderedText>

            when {
                !abilityUnavailable -> {
                    abilityUnavailableText = Text.empty()
                    unavailableReasonText = Text.empty()
                    upgradeCountText = Text.empty()

                    upgradeBlockList = emptyList()
                    upgradeTooltip = emptyList()
                }

                !shouldShowUpgradeCount -> {
                    abilityUnavailableText = Text.translatable("aptitude.gui.special_ability.unavailable")
                        .styled(DEFAULT_COLOR_STYLE)

                    unavailableReasonText = Text.translatable("aptitude.gui.special_ability.insufficient_aptitude")
                        .styled(DEFAULT_COLOR_STYLE)

                    upgradeCountText = Text.empty()

                    upgradeBlockList = emptyList()
                    upgradeTooltip = emptyList()
                }

                else -> {
                    abilityUnavailableText = Text.translatable("aptitude.gui.special_ability.unavailable")
                        .styled(DEFAULT_COLOR_STYLE)
                    unavailableReasonText = Text.translatable("aptitude.gui.special_ability.not_enough_upgrades")
                        .styled(DEFAULT_COLOR_STYLE)
                    upgradeCountText = Text.translatable(
                        "aptitude.gui.special_ability.workstation_upgrade",
                        Text.translatable(
                            "aptitude.gui.special_ability.fraction_format",
                            handler.currentTargetsFound,
                            handler.requiredTargetsFound
                        )
                    ).styled(DEFAULT_COLOR_STYLE)

                    val extension = PROFESSION_EXTENSION_ATTACHMENT[handler.active].get() as ProfessionExtension.Regular

                    upgradeBlockList =
                        extension.workstationUpgrade.station.map(
                            { listOf(it) },
                            { TagRegistry.getTag(it).map(Holder<Block>::value) })

                    upgradeTooltip = textRenderer.wrapLines(
                        Text.translatable(extension.workstationUpgrade.descriptionTranslationKey),
                        MAX_TOOLTIP_WIDTH
                    )
                }
            }


            val abilityUnavailableWidth = textRenderer.getWidth(abilityUnavailableText)
            val unavailableReasonWidth = textRenderer.getWidth(unavailableReasonText)
            val upgradeCountWidth = textRenderer.getWidth(upgradeCountText)

            drawData = AptitudeDrawData(
                currentAptitude,
                maxAptitude,
                currentAptitudeDescriptionText,
                currentAptitudeLevelText,
                nextAptitudeDescriptionText,
                nextAptitudeLevelText,
                currentAptitudeDescriptionOffsetX,
                currentAptitudeLevelOffsetX,
                nextAptitudeDescriptionOffsetX,
                nextAptitudeLevelOffsetX,
                currentAptitudeLevelWidth,
                nextAptitudeLevelWidth,
                tooltipText,
                shouldDrawNext,
                abilityUnavailableText,
                unavailableReasonText,
                abilityUnavailableWidth,
                unavailableReasonWidth,
                abilityUnavailable,
                upgradeCountText,
                upgradeCountWidth,
                shouldShowUpgradeCount,
                upgradeBlockList,
                upgradeTooltip
            )
        } else {
            drawData = null
        }
    }

    //TODO positioning constants
    @Environment(EnvType.CLIENT)
    inner class ReturnToMerchantButton : TexturedButtonWidget(
        (width + backgroundWidth) / 2 - 24,
        (height - backgroundHeight) / 2 + 4,
        20,
        20,
        279,
        3,
        20,
        Aptitude.id(TEXTURE_PATH),
        TEX_WIDTH, TEX_HEIGHT,
        PressAction {
            AptitudeToggleVillagerScreenC2SPacket(
                screenHandler.syncId,
                false
            ).send()
        },
        object : TooltipSupplier {
            override fun onTooltip(
                buttonWidget: ButtonWidget,
                matrixStack: MatrixStack,
                i: Int,
                j: Int
            ) {
                renderOrderedTooltip(
                    matrixStack,
                    clientNotNull.textRenderer.wrapLines(
                        Text.translatable("aptitude.gui.close_screen"),
                        100
                    ),
                    i, j
                )
            }

            override fun supply(consumer: Consumer<Text>) {
                consumer.accept(Text.translatable("aptitude.gui.close_screen"))
            }
        },
        Text.empty()
    )

    @Environment(EnvType.CLIENT)
    private class Entry(
        val screen: AptitudeVillagerScreen,
        val entryX: Int, val entryY: Int,
        val index: Int,
        val entry: Pair<VillagerProfession, Pair<AptitudeLevel, AptitudeLevel>>,
        val isActive: Boolean,
        val maxPossibleLevel: Int
    ) {
        companion object {
            const val LEVEL_BASE_U = 13f
            const val LEVEL_V = 199f

            const val LEVEL_WIDTH = 8
            const val LEVEL_HEIGHT = 8

            const val LEVEL_OFFSET_BOTTOM = 2

            const val LEVEL_RENDER_SPACING = 1
            const val LEVEL_RIGHT_MARGIN = 1

            const val NAME_OFFSET_LEFT = 1f
            const val NAME_OFFSET_TOP = 1f

            const val ENTRY_WIDTH = 88
            const val ENTRY_HEIGHT = 20

            const val BACKGROUND_U = 13f
            const val BACKGROUND_V = 208f
            const val ACTIVE_TEXT_COLOR = 0xFFAA00

            const val LEVEL_TOP = ENTRY_HEIGHT - LEVEL_OFFSET_BOTTOM - LEVEL_HEIGHT
        }

        fun render(
            matrices: MatrixStack
        ) {
            matrices.push()
            matrices.translate(entryX.toDouble(), (entryY + index * ENTRY_HEIGHT).toDouble(), 0.0)

            RenderSystem.setShader(GameRenderer::getPositionTexShader)
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
            RenderSystem.setShaderTexture(0, Aptitude.id(TEXTURE_PATH))

            drawTexture(
                matrices,
                0, 0,
                BACKGROUND_U, BACKGROUND_V,
                ENTRY_WIDTH, ENTRY_HEIGHT,
                TEX_WIDTH, TEX_HEIGHT
            )

            val (profession, levels) = entry
            val (current, max) = levels
            val professionDisplayKey =
                "entity.minecraft.villager.${Registry.VILLAGER_PROFESSION.getId(profession).path}"

            screen.textRenderer.draw(
                matrices,
                Text.translatable(professionDisplayKey),
                NAME_OFFSET_LEFT,
                NAME_OFFSET_TOP,
                if (isActive) ACTIVE_TEXT_COLOR else TEXT_COLOR
            )

            val firstLevelLeft = ENTRY_WIDTH - LEVEL_RIGHT_MARGIN -
                    maxPossibleLevel * LEVEL_WIDTH -
                    (maxPossibleLevel - 1) * LEVEL_RENDER_SPACING

            RenderSystem.setShader(GameRenderer::getPositionTexShader)
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
            RenderSystem.setShaderTexture(0, Aptitude.id(TEXTURE_PATH))

            for (i in 1..current.ordinal) {
                drawTexture(
                    matrices,
                    firstLevelLeft + (LEVEL_WIDTH + LEVEL_RENDER_SPACING) * (i - 1), LEVEL_TOP,
                    LEVEL_BASE_U + (LEVEL_WIDTH * i), LEVEL_V,
                    LEVEL_WIDTH, LEVEL_HEIGHT,
                    TEX_WIDTH, TEX_HEIGHT
                )
            }

            for (i in (current.ordinal + 1)..max.ordinal) {
                drawTexture(
                    matrices,
                    firstLevelLeft + (LEVEL_WIDTH + LEVEL_RENDER_SPACING) * (i - 1), LEVEL_TOP,
                    LEVEL_BASE_U, LEVEL_V,
                    LEVEL_WIDTH, LEVEL_HEIGHT,
                    TEX_WIDTH, TEX_HEIGHT
                )
            }

            matrices.pop()
        }
    }
}
