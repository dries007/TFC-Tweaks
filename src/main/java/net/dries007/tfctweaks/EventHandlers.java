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

package net.dries007.tfctweaks;

import com.bioxx.tfc.Food.ItemFoodTFC;
import com.bioxx.tfc.TileEntities.TEFirepit;
import com.bioxx.tfc.api.Constant.Global;
import com.bioxx.tfc.api.Food;
import com.bioxx.tfc.api.TFCBlocks;
import com.bioxx.tfc.api.TFCItems;
import com.bioxx.tfc.api.Util.Helper;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;

/**
 * @author Dries007
 */
public class EventHandlers
{
    public static final EventHandlers I = new EventHandlers();
    public static int fuelOnFireMaxAge;
    public static boolean stackOnPickup;

    /**
     * Handles:
     * - fuel on firepit despawn prevention
     */
    @SubscribeEvent
    public void itemDespawn(ItemExpireEvent event)
    {
        EntityItem entity = event.entityItem;
        World world = entity.worldObj;
        if (!world.isRemote)
        {
            Item item = entity.getEntityItem().getItem();
            if (item == TFCItems.Logs || item == Item.getItemFromBlock(TFCBlocks.Peat))
            {
                if (world.getTileEntity(MathHelper.floor_double(entity.posX), MathHelper.floor_double(entity.posY), MathHelper.floor_double(entity.posZ)) instanceof TEFirepit)
                {
                    if (fuelOnFireMaxAge == 0) event.setCanceled(true);
                    else if (entity.age < fuelOnFireMaxAge)
                    {
                        event.extraLife = fuelOnFireMaxAge - entity.age;
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    /**
     * Handles:
     *  - Combine foods on pickup
     */
    @SubscribeEvent
    public void itemPickup(EntityItemPickupEvent event)
    {
        ItemStack pickup = event.item.getEntityItem();
        if (pickup == null || pickup.getItem() == null || event.entityPlayer == null || event.entityPlayer.inventory == null) return;
        if (stackOnPickup && pickup.getItem() instanceof ItemFoodTFC)
        {
            ItemFoodTFC pickupItem = (ItemFoodTFC)pickup.getItem();
            InventoryPlayer inventoryPlayer = event.entityPlayer.inventory;

            /*
             * All information to make sure the stacks are 'identical'
             */
            final int[] fuelTasteProfile = Food.getFuelProfile(pickup);
            final int[] cookedTasteProfile = Food.getCookedProfile(pickup);
            final float cookedTime = Food.getCooked(pickup);
            final int roundedCookedTime = roundCookTime(cookedTime);
            final boolean salted = Food.isSalted(pickup);
            final boolean pickled = Food.isPickled(pickup);
            final boolean brined = Food.isBrined(pickup);
            final boolean dried = Food.isDried(pickup);
            final int sweet = pickupItem.getTasteSweetMod(pickup);
            final int sour = pickupItem.getTasteSourMod(pickup);
            final int salty = pickupItem.getTasteSaltyMod(pickup);
            final int bitter = pickupItem.getTasteBitterMod(pickup);
            final int savory = pickupItem.getTasteSavoryMod(pickup);

            /*
             * Weight & decay of the itemstack to add to the inventory
             */
            float weight = Food.getWeight(pickup);
            float decay = Food.getDecay(pickup);
            final float decayRatio = decay / weight;

            for (int slot = 0; slot < inventoryPlayer.getSizeInventory() && weight > 0; slot++) // got slots to go, and still weight to distribute
            {
                ItemStack stack = inventoryPlayer.getStackInSlot(slot);

                /*
                 * if (stack == null || stack.getItem() == null || stack.getItem() != pickupItem) continue;
                 * if (!Food.isSameSmoked(fuelTasteProfile, Food.getFuelProfile(stack))) continue;
                 * if (!Food.isSameSmoked(cookedTasteProfile, Food.getCookedProfile(stack))) continue;
                 * if (roundedCookedTime != roundCookTime(Food.getCooked(stack))) continue;
                 * if (salted != Food.isSalted(stack)) continue;
                 * if (pickled != Food.isPickled(stack)) continue;
                 * if (brined != Food.isBrined(stack)) continue;
                 * if (dried != Food.isDried(stack)) continue;
                 * if (pickupItem.getTasteSweetMod(stack) != sweet) continue;
                 * if (pickupItem.getTasteSourMod(stack) != sour) continue;
                 * if (pickupItem.getTasteSaltyMod(stack) != salty) continue;
                 * if (pickupItem.getTasteBitterMod(stack) != bitter) continue;
                 * if (pickupItem.getTasteSavoryMod(stack) != savory) continue;
                 */
                if ((stack == null || stack.getItem() == null || stack.getItem() != pickupItem) || (!Food.isSameSmoked(fuelTasteProfile, Food.getFuelProfile(stack))) || (!Food.isSameSmoked(cookedTasteProfile, Food.getCookedProfile(stack))) || (roundedCookedTime != roundCookTime(Food.getCooked(stack))) || (salted != Food.isSalted(stack)) || (pickled != Food.isPickled(stack)) || (brined != Food.isBrined(stack)) || (dried != Food.isDried(stack)) || (pickupItem.getTasteSweetMod(stack) != sweet) || (pickupItem.getTasteSourMod(stack) != sour) || (pickupItem.getTasteSaltyMod(stack) != salty) || (pickupItem.getTasteBitterMod(stack) != bitter) || (pickupItem.getTasteSavoryMod(stack) != savory)) continue;

                final float stackWeight = Food.getWeight(stack);
                final float stackDecay = Food.getDecay(stack);

                if (stackWeight >= Global.FOOD_MAX_WEIGHT) continue;

                // delta = add to stack in slot, remove from pickup stack
                final float deltaWeight = Math.min(weight, Global.FOOD_MAX_WEIGHT - stackWeight); // We can add 160 - stackWeight, with a maximum of the weight of the item picked up
                final float newStackWeight = stackWeight + deltaWeight;
                weight -= deltaWeight;

                // delta = add to stack in slot, remove from pickup stack
                final float deltaDecay = deltaWeight * decayRatio;
                final float newStackDecay = stackDecay + deltaDecay;
                decay -= deltaDecay;

                Food.setWeight(stack, Helper.roundNumber(newStackWeight, 100));
                Food.setDecay(stack, Helper.roundNumber(newStackDecay, 100));

                /**
                 * If the item picked up is assimilated into the existing food stacks, cancel the event.
                 */
                if (weight < 0.001)
                {
                    event.setCanceled(true);
                    break;
                }
            }
            Food.setWeight(pickup, Helper.roundNumber(weight, 100));
            Food.setDecay(pickup, Helper.roundNumber(decay, 100));
        }
    }

    /**
     * Used to compare with mild accuracy
     */
    private int roundCookTime(float cookedTime)
    {
        return ((int) cookedTime - 600) / 120;
    }
}
