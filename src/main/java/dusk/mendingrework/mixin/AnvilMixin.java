package dusk.mendingrework.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.function.BiConsumer;

@Mixin(AnvilMenu.class)
public abstract class AnvilMixin extends ItemCombinerMenu {
    public AnvilMixin(@Nullable MenuType<?> menuType, int containerId, Inventory inventory, ContainerLevelAccess access, ItemCombinerMenuSlotDefinition slotDefinition) {
        super(menuType, containerId, inventory, access, slotDefinition);
    }

    @Shadow
    public int repairItemCountCost;

    @Shadow
    public abstract void setCost(int value);

    @Shadow
    @javax.annotation.Nullable
    private String itemName;

    @Shadow
    public abstract int getCost();

    @Unique
    public boolean mendingrework_neo$onlyRepairing = false;

    @Unique
    public boolean mendingrework_neo$mendingRepair = false;

    @Inject(method = "mayPickup", at = @At("TAIL"), cancellable = true)
    public void allowZeroCost(Player player, boolean hasItem, CallbackInfoReturnable<Boolean> cir) {
        if (mendingrework_neo$onlyRepairing) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "createResultInternal", at = @At("HEAD"))
    public void resetRepairingStatus(CallbackInfo ci) {
        mendingrework_neo$onlyRepairing = false;
    }

    @Inject(method = "createResultInternal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AnvilMenu;broadcastChanges()V"))
    public void removeRepairCost(CallbackInfo ci) {
        if (this.repairItemCountCost > 0) {
            ItemStack inputItem = this.inputSlots.getItem(0).copy();
            ItemStack outputItem = this.inputSlots.getItem(0).copy();

            if (Objects.requireNonNull(inputItem.get(DataComponents.ENCHANTMENTS)).toString().contains("Mending")) {
                int repairAmount = Math.min(inputItem.getDamageValue(), (int) (inputItem.getMaxDamage() * 0.35));
                int timesRepaired;

                for (timesRepaired = 0; repairAmount > 0 && timesRepaired < this.repairItemCountCost; timesRepaired++) {
                    int damageLeft = outputItem.getDamageValue() - repairAmount;
                    outputItem.setDamageValue(damageLeft);
                    repairAmount = Math.min(outputItem.getDamageValue(), repairAmount);
                }

                this.repairItemCountCost = timesRepaired;
            } else {
                int repairAmount = Math.min(inputItem.getDamageValue(), inputItem.getMaxDamage() / 4);
                int timesRepaired;

                for (timesRepaired = 0; repairAmount > 0 && timesRepaired < this.repairItemCountCost; timesRepaired++) {
                    int damageLeft = outputItem.getDamageValue() - repairAmount;
                    outputItem.setDamageValue(damageLeft);
                    repairAmount = Math.min(outputItem.getDamageValue(), repairAmount);
                }

                this.repairItemCountCost = timesRepaired;
            }

            mendingrework_neo$onlyRepairing = true;
            if (this.itemName != null && !StringUtil.isBlank(this.itemName)) {
                if (!this.itemName.equals(inputItem.getHoverName().getString())) {
                    outputItem.set(DataComponents.CUSTOM_NAME, Component.literal(this.itemName));
                    mendingrework_neo$onlyRepairing = false;
                }
            } else if (inputItem.has(DataComponents.CUSTOM_NAME)) {
                outputItem.remove(DataComponents.CUSTOM_NAME);
                mendingrework_neo$onlyRepairing = false;
            }

            if (mendingrework_neo$onlyRepairing) {
                this.setCost(0);
            } else {
                this.setCost(Math.min(39, this.getCost() - this.repairItemCountCost));
            }
            this.resultSlots.setItem(0, outputItem);
        }
    }

    @Inject(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/event/entity/player/AnvilCraftEvent$Pre;isCanceled()Z"))
    public void detectMendingRepair(Player p_150474_, ItemStack p_150475_, CallbackInfo ci) {
        if (!ci.isCancelled()) {
            if (Objects.requireNonNull(this.inputSlots.getItem(0)
                    .get(DataComponents.ENCHANTMENTS)).toString().contains("Mending")) {
                if (this.repairItemCountCost > 0 || this.inputSlots.getItem(1).isEmpty()) {
                    mendingrework_neo$mendingRepair = true;
                }
            }
        }
    }

    @Redirect(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ContainerLevelAccess;execute(Ljava/util/function/BiConsumer;)V"))
    public void reduceAnvilDamage(ContainerLevelAccess instance, BiConsumer<Level, BlockPos> levelPosConsumer) {
        this.access.execute((level, blockPos) -> {
            BlockState oldAnvilBlockState = level.getBlockState(blockPos);
            if (!this.player.hasInfiniteMaterials() && oldAnvilBlockState.is(BlockTags.ANVIL)
                    && !mendingrework_neo$mendingRepair && this.player.getRandom().nextFloat() < 0.06F) {
                BlockState newAnvilBlockState = AnvilBlock.damage(oldAnvilBlockState);
                if (newAnvilBlockState == null) {
                    level.removeBlock(blockPos, false);
                    level.levelEvent(1029, blockPos, 0);
                } else {
                    level.setBlock(blockPos, newAnvilBlockState, 2);
                    level.levelEvent(1030, blockPos, 0);
                }
            } else {
                level.levelEvent(1030, blockPos, 0);
            }
        });
        mendingrework_neo$mendingRepair = false;
    }
}
