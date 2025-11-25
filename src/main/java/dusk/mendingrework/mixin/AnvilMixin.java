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
import org.spongepowered.asm.mixin.Final;
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
    @Shadow
    private int repairItemCountCost;

    @Shadow
    public abstract int getCost();

    @Shadow
    @Final
    private DataSlot cost;

    @Shadow
    @Nullable
    private String itemName;

    @Unique
    public boolean mendingrework_1_21_10_fabric$onlyRepairing = false;

    @Unique
    public boolean mendingrework_1_21_10_fabric$mendingRepair = false;

    public AnvilMixin(@Nullable MenuType<?> menuType, int i, Inventory inventory, ContainerLevelAccess containerLevelAccess, ItemCombinerMenuSlotDefinition itemCombinerMenuSlotDefinition) {
        super(menuType, i, inventory, containerLevelAccess, itemCombinerMenuSlotDefinition);
    }

    @Inject(method = "mayPickup", at = @At("TAIL"), cancellable = true)
    public void allowZeroCost(Player player, boolean hasItem, CallbackInfoReturnable<Boolean> cir) {
        if (mendingrework_1_21_10_fabric$onlyRepairing) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "createResult", at = @At("HEAD"))
    public void resetRepairingStatus(CallbackInfo ci) {
        mendingrework_1_21_10_fabric$onlyRepairing = false;
    }

    @Inject(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AnvilMenu;broadcastChanges()V"))
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

            mendingrework_1_21_10_fabric$onlyRepairing = true;
            if (this.itemName != null && !StringUtil.isBlank(this.itemName)) {
                if (!this.itemName.equals(inputItem.getHoverName().getString())) {
                    outputItem.set(DataComponents.CUSTOM_NAME, Component.literal(this.itemName));
                    mendingrework_1_21_10_fabric$onlyRepairing = false;
                }
            } else if (inputItem.has(DataComponents.CUSTOM_NAME)) {
                outputItem.remove(DataComponents.CUSTOM_NAME);
                mendingrework_1_21_10_fabric$onlyRepairing = false;
            }

            if (mendingrework_1_21_10_fabric$onlyRepairing) {
                this.cost.set(0);
            } else {
                this.cost.set(Math.min(39, this.getCost() - this.repairItemCountCost));
            }
            this.resultSlots.setItem(0, outputItem);
        }
    }

    @Inject(method = "onTake", at = @At("HEAD"))
    public void detectMendingRepair(Player player, ItemStack itemStack, CallbackInfo ci) {
        if (Objects.requireNonNull(this.inputSlots.getItem(0)
                .get(DataComponents.ENCHANTMENTS)).toString().contains("Mending")) {
            if (this.repairItemCountCost > 0 || this.inputSlots.getItem(1).isEmpty()) {
                mendingrework_1_21_10_fabric$mendingRepair = true;
            }
        }
    }

    @Redirect(method = "onTake", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/ContainerLevelAccess;execute(Ljava/util/function/BiConsumer;)V"))
    public void reduceAnvilDamage(ContainerLevelAccess instance, BiConsumer<Level, BlockPos> levelPosConsumer) {
        this.access.execute((level, blockPos) -> {
            BlockState oldAnvilBlockState = level.getBlockState(blockPos);
            if (!this.player.hasInfiniteMaterials() && oldAnvilBlockState.is(BlockTags.ANVIL)
                    && !mendingrework_1_21_10_fabric$mendingRepair && this.player.getRandom().nextFloat() < 0.06F) {
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
        mendingrework_1_21_10_fabric$mendingRepair = false;
    }
}
