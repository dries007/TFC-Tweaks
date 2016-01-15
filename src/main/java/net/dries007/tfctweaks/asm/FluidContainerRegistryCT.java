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

import cpw.mods.fml.common.FMLLog;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;

import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.POP;

/**
 * @author Dries007
 */
public class FluidContainerRegistryCT implements IClassTransformer
{
    public static final int DONE = 3; // lava bucket + water bucket + water bottle
    public static int done = 0;

    @Override
    public byte[] transform(String originalName, String transformedName, byte[] bytes)
    {
        if (originalName.equals("net.minecraftforge.fluids.FluidContainerRegistry")) return magic(bytes);
        return bytes;
    }

    private byte[] magic(byte[] bytes)
    {
        FMLLog.info("Found the FluidContainerRegistry class...");
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);
        for (MethodNode m : classNode.methods)
        {
            if (m.name.equals("<clinit>") && m.desc.equals("()V"))
            {
                FMLLog.info("Found the <clinit> method...");

                ListIterator<AbstractInsnNode> i = m.instructions.iterator();
                while (i.hasNext())
                {
                    AbstractInsnNode node = i.next();
                    if (!(node instanceof FieldInsnNode) || node.getOpcode() != GETSTATIC) continue;
                    FieldInsnNode fieldInsnNode = ((FieldInsnNode) node);
                    if (!fieldInsnNode.owner.equals("net/minecraftforge/fluids/FluidRegistry")) continue;
                    if (!fieldInsnNode.name.equals("WATER") && !fieldInsnNode.name.equals("LAVA")) continue;
                    if (!fieldInsnNode.desc.equals("Lnet/minecraftforge/fluids/Fluid;")) continue;
                    do
                    {
                        i.remove();
                        node = i.next();
                    }
                    while (node.getOpcode() != POP);
                    i.remove(); // remove last pop
                    FMLLog.info("[FluidContainerRegistryCT] Removed the " + fieldInsnNode.name + " registration.");
                    done++;
                }
            }
        }

        if (done != DONE)
        {
            FMLLog.severe("\n######################################################################################\n" +
                    "######################################################################################\n" +
                    "######################################################################################\n" +
                    "OUR ASM FLUID HACK FAILED! PLEASE MAKE AN ISSUE REPORT ON GITHUB WITH A COMPLETE MODLIST! https://github.com/dries007/TFC-Tweaks\n" +
                    "Done %d out of %d ASM tweaks on class FluidContainerRegistry\n" +
                    "########################################################################################\n" +
                    "########################################################################################\n" +
                    "########################################################################################\n\n", done, DONE);
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }
}
