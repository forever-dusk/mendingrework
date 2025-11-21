package dusk.mendingrework.mixin;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ToolMaterial.class)
public class NetheriteToolMaterialMixin {
    @Mutable
    @Shadow
    @Final
    public static ToolMaterial NETHERITE;

    @Inject(method = "applyToolProperties", at = @At("HEAD"))
    public void changeNetheriteToolRepairMaterial(Item.Properties properties, TagKey<Block> mineableBlocks, float attackDamage, float attackSpeed, float disableBlockingForSeconfs, CallbackInfoReturnable<Item.Properties> cir) {
        NETHERITE = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2031, 9.0F, 4.0F, 15, ItemTags.REPAIRS_NETHERITE_ARMOR);
    }
}
