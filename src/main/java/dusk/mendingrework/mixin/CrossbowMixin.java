package dusk.mendingrework.mixin;

import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrossbowItem.class)
public class CrossbowMixin {
    @Inject(method = "<init>", at = @At("HEAD"))
    private static void addRepairItem(Item.Properties properties, CallbackInfo ci) {
        properties.repairable(Items.IRON_INGOT);
    }
}
