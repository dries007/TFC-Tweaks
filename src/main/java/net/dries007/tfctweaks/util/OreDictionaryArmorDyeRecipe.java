/*
 * Copyright (c) 2015 Dries007
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted (subject to the limitations in the
 * disclaimer below) provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the
 *    distribution.
 *
 *  * Neither the name of Dries007 nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE
 * GRANTED BY THIS LICENSE.  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT
 * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.dries007.tfctweaks.util;

import net.minecraft.block.BlockColored;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A copy of RecipesArmorDyes, but with OreDictionary support
 *
 * @author Dries007
 */
public class OreDictionaryArmorDyeRecipe implements IRecipe
{
    public static final OreDictionaryArmorDyeRecipe INSTANCE = new OreDictionaryArmorDyeRecipe();
    private static final Map<Integer, Integer> dyeMap = new HashMap<>();
    static
    {
        String[] dyes = {"Black", "Red", "Green", "Brown", "Blue", "Purple", "Cyan", "LightGray", "Gray", "Pink", "Lime", "Yellow", "LightBlue", "Magenta", "Orange", "White"};
        for(int i = 0; i < 16; i++) dyeMap.put(OreDictionary.getOreID("dye" + dyes[i]), i);
    }

    private OreDictionaryArmorDyeRecipe()
    {
    }

    @Override
    public boolean matches(InventoryCrafting p_77569_1_, World p_77569_2_)
    {
        ItemStack output = null;
        ArrayList<ItemStack> usedStacks = new ArrayList<>();

        for (int i = 0; i < p_77569_1_.getSizeInventory(); ++i)
        {
            ItemStack stackInSlot = p_77569_1_.getStackInSlot(i);

            if (stackInSlot != null)
            {
                if (stackInSlot.getItem() instanceof ItemArmor)
                {
                    ItemArmor itemarmor = (ItemArmor) stackInSlot.getItem();

                    if (itemarmor.getArmorMaterial() != ItemArmor.ArmorMaterial.CLOTH || output != null)
                    {
                        return false;
                    }

                    output = stackInSlot;
                }
                else
                {
                    if (getDyeColor(stackInSlot) == -1)
                    {
                        return false;
                    }

                    usedStacks.add(stackInSlot);
                }
            }
        }

        return output != null && !usedStacks.isEmpty();
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting p_77572_1_)
    {
        ItemStack output = null;
        int[] outColor = new int[3]; // RGB
        int highestColorAmount = 0;
        int numberOfItems = 0;
        ItemArmor itemarmor = null;

        for (int k = 0; k < p_77572_1_.getSizeInventory(); ++k)
        {
            ItemStack stackInSlot = p_77572_1_.getStackInSlot(k);

            if (stackInSlot != null)
            {
                if (stackInSlot.getItem() instanceof ItemArmor)
                {
                    itemarmor = (ItemArmor) stackInSlot.getItem();

                    if (itemarmor.getArmorMaterial() != ItemArmor.ArmorMaterial.CLOTH || output != null)
                    {
                        return null;
                    }

                    output = stackInSlot.copy();
                    output.stackSize = 1;

                    if (itemarmor.hasColor(stackInSlot))
                    {
                        int color = itemarmor.getColor(output);
                        float r = (float) (color >> 16 & 255) / 255.0F;
                        float g = (float) (color >> 8 & 255) / 255.0F;
                        float b = (float) (color & 255) / 255.0F;
                        highestColorAmount += (int) (Math.max(r, Math.max(g, b)) * 255.0F);
                        outColor[0] = (int) ((float) outColor[0] + r * 255.0F);
                        outColor[1] = (int) ((float) outColor[1] + g * 255.0F);
                        outColor[2] = (int) ((float) outColor[2] + b * 255.0F);
                        ++numberOfItems;
                    }
                }
                else
                {
                    int dye = getDyeColor(stackInSlot);
                    if (dye == -1)
                    {
                        return null;
                    }

                    float[] afloat = EntitySheep.fleeceColorTable[BlockColored.func_150032_b(dye)];
                    int j1 = (int) (afloat[0] * 255.0F);
                    int k1 = (int) (afloat[1] * 255.0F);
                    int l1 = (int) (afloat[2] * 255.0F);
                    highestColorAmount += Math.max(j1, Math.max(k1, l1));
                    outColor[0] += j1;
                    outColor[1] += k1;
                    outColor[2] += l1;
                    ++numberOfItems;
                }
            }
        }

        if (itemarmor == null)
        {
            return null;
        }
        else
        {
            int r = outColor[0] / numberOfItems;
            int g = outColor[1] / numberOfItems;
            int b = outColor[2] / numberOfItems;
            float multiplier = (float) highestColorAmount / (float) numberOfItems;
            float divider = (float) Math.max(r, Math.max(g, b));
            r = (int) ((float) r * multiplier / divider);
            g = (int) ((float) g * multiplier / divider);
            b = (int) ((float) b * multiplier / divider);
            itemarmor.func_82813_b(output, (r << 16) + (g << 8) + b);
            return output;
        }
    }

    public int getRecipeSize()
    {
        return 10;
    }

    public ItemStack getRecipeOutput()
    {
        return null;
    }

    public static int getDyeColor(ItemStack stack)
    {
        if (stack.getItem() == Items.dye) return stack.getItemDamage();
        for (int oreId : OreDictionary.getOreIDs(stack))
        {
            if (dyeMap.containsKey(oreId)) return dyeMap.get(oreId);
        }
        return -1;
    }
}
