package dusk.mendingrework.mixin;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShearsItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShearsItem.class)
public class ShearsMixin {
    @Inject(method = "<init>", at = @At("HEAD"))
    private static void addRepairItem(Item.Properties properties, CallbackInfo ci) {
        properties.repairable(Items.IRON_INGOT);
    }
}
