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

package net.dries007.tfctweaks.asm;

import com.bioxx.tfc.api.TFCFluids;
import com.google.common.collect.BiMap;
import net.dries007.tfctweaks.util.FluidHacks;
import net.minecraft.block.Block;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.List;
import java.util.Map;

/**
 * Don't call anything in here.
 * Used for its bytecode
 *
 * @author Dries007
 */
@SuppressWarnings("ALL")
public class DummyCode
{
    private class TestWoC
    {
        private Block block;
        private Block[] blocks;
        private List<Block> blockGeneric;
        private Map<String, Block[]> blocksGeneric;

        {

        }
    }

    private static class TestWC
    {
        public TestWC()
        {
            this(42);
            FluidHacksCT.initObject("TestWC", this);
            somecode();
            if (0 == 0) return;
            else somecodeAgain();

        }

        public TestWC(int theAnswerToLifeTheUniverseAndEverything)
        {
            somecode();
        }

        private void somecodeAgain()
        {

        }

        private void somecode()
        {

        }

        private Block block;
        private Block[] blocks;
        private List<Block> blockGeneric;
        private Map<String, Block[]> blocksGeneric;
    }

    private DummyCode()
    {
    }

    private static BiMap<String, Fluid> fluids = null;

    /*
    ORIGINAL:
        GETSTATIC net/minecraftforge/fluids/FluidRegistry.fluids : Lcom/google/common/collect/BiMap;
        ALOAD 0
        INVOKEINTERFACE com/google/common/collect/BiMap.get (Ljava/lang/Object;)Ljava/lang/Object;
        CHECKCAST net/minecraftforge/fluids/Fluid
        ARETURN
     */
    private static Fluid getFluid(String fluidName)
    {
        if (FluidHacks.makeAllWaterFTCWater && fluidName.equals("water")) return TFCFluids.FRESHWATER;
        else if (FluidHacks.makeAllLavaFTCLava && fluidName.equals("lava")) return TFCFluids.LAVA;

        return fluids.get(fluidName);
    }

    /*
    ORIGINAL:
        ALOAD 0
        IFNULL L0
        GETSTATIC net/minecraftforge/fluids/FluidRegistry.fluids : Lcom/google/common/collect/BiMap;
        ALOAD 0
        INVOKEVIRTUAL net/minecraftforge/fluids/Fluid.getName ()Ljava/lang/String;
        INVOKEINTERFACE com/google/common/collect/BiMap.containsKey (Ljava/lang/Object;)Z
        IFEQ L0
        ICONST_1
        GOTO L1
        L0
        ICONST_0
        L1
        IRETURN
     */
    private static boolean isFluidRegistered(Fluid fluid)
    {
        return fluid != null && FluidRegistry.isFluidRegistered(fluid.getName());
    }

    /*
    ORIGINAL:
        GETSTATIC net/minecraftforge/fluids/FluidRegistry.fluids : Lcom/google/common/collect/BiMap;
        ALOAD 0
        INVOKEINTERFACE com/google/common/collect/BiMap.containsKey (Ljava/lang/Object;)Z
        IRETURN
     */
    private static boolean isFluidRegistered(String fluidName)
    {
        return "water".equals(fluidName) || "lava".equals(fluidName) || fluids.containsKey(fluidName);
    }
}
