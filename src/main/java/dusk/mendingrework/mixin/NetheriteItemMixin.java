package dusk.mendingrework.mixin;

import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TieredItem.class)
public class NetheriteItemMixin {
    @Shadow
    @Final
    private Tier tier;

    @Inject(method = "isValidRepairItem", at = @At("HEAD"), cancellable = true)
    public void changeRepairItem(ItemStack toRepair, ItemStack repair, CallbackInfoReturnable<Boolean> cir) {
        if (this.tier.getRepairIngredient().test(new ItemStack(Items.NETHERITE_INGOT))) {
            if (repair.is(Items.DIAMOND) || repair.is(Items.NETHERITE_SCRAP)) {
                cir.setReturnValue(true);
            } else if (repair.is(Items.NETHERITE_INGOT)) {
                cir.setReturnValue(false);
            }
        }

        if (repair.is(Items.EXPERIENCE_BOTTLE)) {
            cir.setReturnValue(false);
        }
    }
}
