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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.InjectedModContainer;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.relauncher.ReflectionHelper;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import static net.dries007.tfctweaks.util.Constants.*;

/**
 * @author Dries007
 */
public class Helper
{
    private static final Field modifiersField = ReflectionHelper.findField(Field.class, "modifiers");

    /**
     * Pure evil
     * http://stackoverflow.com/a/3301720
     */
    public static void setFinalStatic(Field field, Object newValue)
    {
        try
        {
            int modifiers = field.getModifiers();
            modifiersField.setInt(field, modifiers & ~Modifier.FINAL);
            field.set(null, newValue);
            modifiersField.setInt(field, modifiers);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * We must force the following load order, otherwise many things break:
     *  - TFC
     *  - This mod
     *  - Anything else
     */
    public static void doLoadOrderHaxing()
    {
        File injectedDepFile = new File(Loader.instance().getConfigDir(), "injectedDependencies.json");

        JsonArray deps = new JsonArray();
        JsonObject dep = new JsonObject();
        dep.addProperty("type", "after");
        dep.addProperty("target", TFC);
        deps.add(dep);

        for (ModContainer container : Loader.instance().getModList())
        {
            if (container instanceof DummyModContainer || container instanceof InjectedModContainer) continue;
            String modid = container.getModId();
            if (modid.equals(MODID) || modid.equals(TFC)) continue;
            dep = new JsonObject();
            dep.addProperty("type", "before");
            dep.addProperty("target", modid);
            deps.add(dep);
        }

        JsonArray root = new JsonArray();
        JsonObject mod = new JsonObject();
        mod.addProperty("modId", MODID);
        mod.add("deps", deps);
        root.add(mod);

        try
        {
            FileUtils.write(injectedDepFile, GSON.toJson(root));
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
