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
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.common.extensions.IItemStackExtension;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@JeiPlugin
public class JEIMendingReworkPlugin implements IModPlugin {
    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(MendingRework.MODID, "jei_plugin");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        List<IJeiAnvilRecipe> anvilRecipes = new ArrayList<>(jeiRuntime.getRecipeManager()
                .createRecipeLookup(RecipeTypes.ANVIL).get().toList());

        ResourceLocation repairRecipe = ResourceLocation.parse("minecraft:anvil.materials_repair.");
        List<IJeiAnvilRecipe> recipesToRemove = anvilRecipes.stream().filter(recipe -> (
                Objects.equals(recipe.getUid(), repairRecipe))).toList();

        jeiRuntime.getRecipeManager().hideRecipes(RecipeTypes.ANVIL, recipesToRemove);
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        List<ItemStack> allIngredients = new ArrayList<>(
                registration.getIngredientManager().getAllIngredients(VanillaTypes.ITEM_STACK).stream().toList()
        );

        List<ItemStack> allDamageableItems = new ArrayList<>(
                allIngredients.stream().filter(IItemStackExtension::isRepairable).toList()
        );

        allIngredients.removeAll(allDamageableItems);

        IVanillaRecipeFactory vanillaRecipeFactory = registration.getVanillaRecipeFactory();
        List<IJeiAnvilRecipe> repairRecipes = new ArrayList<>();
        assert Minecraft.getInstance().level != null;
        HolderGetter<Enchantment> enchantmentRegistry = Minecraft.getInstance().level.holderLookup(Registries.ENCHANTMENT);

        for (ItemStack damageableItem : allDamageableItems) {
            ItemStack itemToRepair = damageableItem.copy();
            List<ItemStack> repairMaterials = new ArrayList<>();

            for (ItemStack ingredient : allIngredients) {
                ItemStack repairMaterial = ingredient.copy();

                if (itemToRepair.getItem().isValidRepairItem(itemToRepair, repairMaterial)) {
                    repairMaterials.add(repairMaterial);
                }
            }

            if (!repairMaterials.isEmpty()) {
                int maxDamage = itemToRepair.getMaxDamage();
                ItemStack itemRepaired = itemToRepair.copy();

                itemToRepair.setDamageValue(maxDamage);
                itemRepaired.setDamageValue((int) (maxDamage * 0.75));

                repairRecipes.add(vanillaRecipeFactory.createAnvilRecipe(
                        itemToRepair, repairMaterials, Collections.singletonList(itemRepaired),
                        ResourceLocation.fromNamespaceAndPath(MendingRework.MODID, "repair")
                ));

                if (itemToRepair.is(ItemTags.DURABILITY_ENCHANTABLE)) {
                    ItemStack tempMendingItem = itemToRepair.copy();
                    tempMendingItem.enchant(enchantmentRegistry.getOrThrow(Enchantments.MENDING), 1);

                    ItemStack tempRepairedItem = tempMendingItem.copy();
                    tempRepairedItem.setDamageValue((int) (maxDamage * 0.65));

                    repairRecipes.add(vanillaRecipeFactory.createAnvilRecipe(
                            tempMendingItem, repairMaterials, Collections.singletonList(tempRepairedItem),
                            ResourceLocation.fromNamespaceAndPath(MendingRework.MODID, "repair_mending")
                    ));
                }
            }
        }

        registration.addRecipes(RecipeTypes.ANVIL, repairRecipes);
    }
}
