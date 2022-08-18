package coffee.cypher.aptitude.mixins;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ScreenHandler.class)
public interface AptitudeScreenHandlerMixin {
    @Invoker
    public boolean callInsertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast);
}
