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

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.relauncher.Side;
import net.dries007.tfctweaks.util.Constants;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Logger;

import java.util.Map;

import static net.dries007.tfcnei.util.Constants.MODID;
import static net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL;

/**
 * @author Dries007
 */
@Mod(modid = Constants.MODID)
public class TFCTweaks
{
    public static Logger log;

    @Mod.Instance(MODID)
    public static TFCTweaks instance;

    private Configuration cfg;

    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event)
    {
        log = event.getModLog();

        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(new EventHandlers());

        cfg = new Configuration(event.getSuggestedConfigurationFile());
        doConfig(cfg);
    }

    @SubscribeEvent
    public void onConfigChange(ConfigChangedEvent.OnConfigChangedEvent eventArgs)
    {
        EventHandlers.fuelOnFireMaxAge = cfg.getInt("fuelOnFireMaxAge", CATEGORY_GENERAL, 6000, 0, Integer.MAX_VALUE, "Despawn time of fuel thrown on a firepit in ticks. Setting it lower then 6000 (5 minutes) has no effect. 0 will extend the lifetime infinitely.");
        doConfig(cfg);
    }

    private void doConfig(Configuration cfg)
    {
        if (cfg.hasChanged()) cfg.save();
    }

    @NetworkCheckHandler
    public boolean checkVersion(Map<String, String> mods, Side side)
    {
        return true;
    }
}
