@file:Environment(EnvType.CLIENT)

package coffee.cypher.aptitude.gui

import coffee.cypher.aptitude.Aptitude
import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text

@Environment(EnvType.CLIENT)
class AptitudeVillagerScreen(
    screenHandler: AptitudeVillagerScreenHandler,
    playerInventory: PlayerInventory,
    text: Text
) : HandledScreen<AptitudeVillagerScreenHandler>(screenHandler, playerInventory, text) {
    override fun drawBackground(matrices: MatrixStack, delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader)
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        RenderSystem.setShaderTexture(0, Aptitude.id("textures/gui/aptitude_villager_bg.png"))
        val i = (width - backgroundWidth) / 2
        val j = (height - backgroundHeight) / 2
        drawTexture(
            matrices, i, j,
            zOffset, 0.0f, 0.0f, backgroundWidth, backgroundHeight, 512, 256
        )
    }

    override fun init() {
        super.init()
    }
}
