package dusk.mendingrework.compat;

import dusk.mendingrework.MendingRework;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.Repairable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@JeiPlugin
public class JEIMendingReworkPlugin implements IModPlugin {
    @Override
    public @NotNull Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(MendingRework.MOD_ID, "jei_plugin");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        List<IJeiAnvilRecipe> anvilRecipes = new ArrayList<>(jeiRuntime.getRecipeManager()
                .createRecipeLookup(RecipeTypes.ANVIL).get().toList());

        Identifier repairRecipe = Identifier.parse("minecraft:anvil.materials_repair.");
        List<IJeiAnvilRecipe> recipesToRemove = anvilRecipes.stream().filter(recipe -> (
                Objects.equals(recipe.getUid(), repairRecipe))).toList();

        jeiRuntime.getRecipeManager().hideRecipes(RecipeTypes.ANVIL, recipesToRemove);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        List<ItemStack> allDamageableItems = new ArrayList<>(
                registration.getIngredientManager().getAllIngredients(VanillaTypes.ITEM_STACK).
                stream().filter(item ->
                (item.has(DataComponents.MAX_DAMAGE))).toList());

        List<ItemStack> allRepairableItems = new ArrayList<>(allDamageableItems.stream()
                .filter(item ->
                        (item.has(DataComponents.REPAIRABLE))).toList());

        allDamageableItems.removeAll(allRepairableItems);

        ItemStack missedBow = new ItemStack(Items.BOW);
        ItemStack missedFlint = new ItemStack(Items.FLINT_AND_STEEL);
        missedBow.set(DataComponents.REPAIRABLE, new Repairable(HolderSet.direct(Items.STRING.builtInRegistryHolder())));
        missedFlint.set(DataComponents.REPAIRABLE, new Repairable(HolderSet.direct(Items.FLINT.builtInRegistryHolder())));

        allRepairableItems.add(missedBow);
        allRepairableItems.add(missedFlint);

        List<ItemStack> allExperienceRepairableItems = new ArrayList<>(allDamageableItems.stream()
                .filter(item -> (item.is(ItemTags.DURABILITY_ENCHANTABLE))).toList());
        allExperienceRepairableItems.removeIf(item -> (item.is(Items.BOW) || item.is(Items.FLINT_AND_STEEL)));

        IVanillaRecipeFactory vanillaRecipeFactory = registration.getVanillaRecipeFactory();
        List<IJeiAnvilRecipe> repairRecipes = new ArrayList<>();
        assert Minecraft.getInstance().level != null;
        HolderGetter<@NotNull Enchantment> enchantmentRegistry = Minecraft.getInstance().level.holderLookup(Registries.ENCHANTMENT);

        List<ItemStack> itemsToRepair = new ArrayList<>();
        List<List<ItemStack>> repairMaterials = new ArrayList<>();
        List<List<ItemStack>> itemsRepaired = new ArrayList<>();

        for (int i = 0; i < allRepairableItems.size(); i++) {
            itemsToRepair.add(allRepairableItems.get(i).copy());
            itemsRepaired.add(List.of(itemsToRepair.get(i).copy()));

            int maxDamage = itemsToRepair.get(i).getMaxDamage();
            itemsToRepair.get(i).setDamageValue(maxDamage);
            itemsRepaired.get(i).getFirst().setDamageValue((int) (maxDamage * 0.75));

            repairMaterials.add(new ArrayList<>());
            HolderSet<@NotNull Item> materials =  Objects.requireNonNull(itemsToRepair.get(i).get(DataComponents.REPAIRABLE)).items();
            for (int j = 0; j < materials.size(); j++) {
                repairMaterials.get(i).add(materials.get(j).value().getDefaultInstance());
            }

            repairRecipes.add(vanillaRecipeFactory.createAnvilRecipe(
                    itemsToRepair.get(i), repairMaterials.get(i), itemsRepaired.get(i),
                    Identifier.fromNamespaceAndPath(MendingRework.MOD_ID, "repair")
            ));

            if (itemsToRepair.get(i).is(ItemTags.DURABILITY_ENCHANTABLE)) {
                ItemStack tempMendingItem = itemsToRepair.get(i).copy();
                tempMendingItem.enchant(enchantmentRegistry.getOrThrow(Enchantments.MENDING), 1);

                ItemStack tempRepairedItem = tempMendingItem.copy();
                tempRepairedItem.setDamageValue((int) (maxDamage * 0.65));

                repairRecipes.add(vanillaRecipeFactory.createAnvilRecipe(
                        tempMendingItem, repairMaterials.get(i), Collections.singletonList(tempRepairedItem),
                        Identifier.fromNamespaceAndPath(MendingRework.MOD_ID, "repair_mending")
                ));
            }
        }

        itemsToRepair.clear();
        itemsRepaired.clear();

        for (int i = 0; i < allExperienceRepairableItems.size(); i++) {
            itemsToRepair.add(allExperienceRepairableItems.get(i).copy());
            int maxDamage = itemsToRepair.get(i).getMaxDamage();

            itemsToRepair.get(i).setDamageValue(maxDamage);
            itemsRepaired.add(List.of(itemsToRepair.get(i).copy()));
            itemsRepaired.get(i).getFirst().setDamageValue((int) (maxDamage * 0.75));

            repairRecipes.add(vanillaRecipeFactory.createAnvilRecipe(
                    itemsToRepair.get(i), List.of(Items.EXPERIENCE_BOTTLE.getDefaultInstance()), itemsRepaired.get(i),
                    Identifier.fromNamespaceAndPath(MendingRework.MOD_ID, "repair_default")
            ));

            ItemStack tempMendingItem = itemsToRepair.get(i).copy();
            tempMendingItem.enchant(enchantmentRegistry.getOrThrow(Enchantments.MENDING), 1);

            ItemStack tempRepairedItem = tempMendingItem.copy();
            tempRepairedItem.setDamageValue((int) (maxDamage * 0.65));

            repairRecipes.add(vanillaRecipeFactory.createAnvilRecipe(
                    tempMendingItem, List.of(Items.EXPERIENCE_BOTTLE.getDefaultInstance()), Collections.singletonList(tempRepairedItem),
                    Identifier.fromNamespaceAndPath(MendingRework.MOD_ID, "repair_mending")
            ));
        }

        registration.addRecipes(RecipeTypes.ANVIL, repairRecipes);
    }
}
