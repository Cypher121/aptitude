@file:Environment(EnvType.CLIENT)

package coffee.cypher.aptitude.gui

import coffee.cypher.aptitude.Aptitude
import coffee.cypher.aptitude.datamodel.AptitudeLevel
import coffee.cypher.aptitude.gui.packets.AptitudeToggleVillagerScreenC2SPacket
import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.ButtonWidget.PressAction
import net.minecraft.client.gui.widget.TexturedButtonWidget
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.Language
import net.minecraft.util.registry.Registry
import net.minecraft.village.VillagerProfession
import org.quiltmc.qkl.wrapper.qsl.client.screen.buttons
import java.util.function.Consumer
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
    companion object {
        const val TEXTURE_PATH = "textures/gui/aptitude_villager.png"

        const val TEX_WIDTH = 512
        const val TEX_HEIGHT = 256
    }

    private val list = handler.aptitudes.toList()
        .sortedByDescending { it.second.second.ordinal }


    private var entries = listOf<Entry>()

    init {
        backgroundWidth = 276
        backgroundHeight = 196

        playerInventoryTitleX = 107
        playerInventoryTitleY = backgroundHeight - 94
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
    }

    override fun init() {
        super.init()

        titleX = (backgroundWidth - textRenderer.getWidth(Language.getInstance().reorder(title))) / 2

        buttons += ReturnToMerchantButton()

        val maxPossibleLevel = list.maxOf { it.second.second }.ordinal
        entries = list.mapIndexed { index, it ->
            Entry(this, 5, 26, index, it, it.first == handler.active, maxPossibleLevel)
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

            const val TEXT_COLOR = 0x404040
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
