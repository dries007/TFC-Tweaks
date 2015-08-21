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
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.relauncher.Side;
import net.dries007.tfctweaks.cmd.CmdWorldExplorer;
import net.dries007.tfctweaks.util.WorldExplorer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static net.dries007.tfctweaks.util.Constants.MODID;
import static net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL;

/**
 * @author Dries007
 */
@Mod(modid = MODID)
public class TFCTweaks
{
    public static Logger log;

    @Mod.Instance(MODID)
    public static TFCTweaks instance;

    private Configuration cfg;

    private boolean autoPregen_enabled = false;
    private int autoPregen_size = 1000;

    @Mod.EventHandler
    public void preinit(FMLPreInitializationEvent event)
    {
        log = event.getModLog();

        FMLCommonHandler.instance().bus().register(EventHandlers.I);
        MinecraftForge.EVENT_BUS.register(EventHandlers.I);

        cfg = new Configuration(event.getSuggestedConfigurationFile());
        doConfig(cfg);
    }

    @Mod.EventHandler
    public void serverstart(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CmdWorldExplorer());
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event)
    {
        try
        {
            FileUtils.writeStringToFile(new File(DimensionManager.getCurrentSaveRootDirectory(), "seed.txt"), String.valueOf(DimensionManager.getWorld(0).getSeed()));
            WorldExplorer.jsonFile = new File(DimensionManager.getCurrentSaveRootDirectory(), "WorldExplorer.json");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        if (autoPregen_enabled && !WorldExplorer.jsonFile.exists())
        {
            ChunkCoordinates spawn = MinecraftServer.getServer().worldServers[0].getSpawnPoint();
            WorldExplorer.start(MinecraftServer.getServer(), spawn.posX >> 4, spawn.posZ >> 4, autoPregen_size / 16);
        }
    }

    @SubscribeEvent
    public void onConfigChange(ConfigChangedEvent.OnConfigChangedEvent eventArgs)
    {
        doConfig(cfg);
    }

    private void doConfig(Configuration cfg)
    {
        EventHandlers.fuelOnFireMaxAge = cfg.getInt("fuelOnFireMaxAge", CATEGORY_GENERAL, 6000, 0, Integer.MAX_VALUE, "Despawn time of fuel thrown on a firepit in ticks. Setting it lower then 6000 (5 minutes) has no effect. 0 will extend the lifetime infinitely.");
        EventHandlers.stackOnPickup = cfg.getBoolean("stackOnPickup", CATEGORY_GENERAL, false, "Auto-stack food together on pickup.");
        autoPregen_enabled = cfg.getBoolean("enabled", CATEGORY_GENERAL + ".autoPregen", autoPregen_enabled, "Enable the automatic pregeneration of the world once the server starts. Only happens when WorldExplorer.json doesn't exist in the world folder.");
        autoPregen_size = cfg.getInt("size", CATEGORY_GENERAL + ".autoPregen", autoPregen_size, 0, Integer.MAX_VALUE, "The size, in blocks, of the autoPregen.");
        if (cfg.hasChanged()) cfg.save();
    }

    @NetworkCheckHandler
    public boolean checkVersion(Map<String, String> mods, Side side)
    {
        return true;
    }
}
