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

package net.dries007.tfctweaks.cmd;

import net.dries007.tfctweaks.util.WorldExplorer;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;

import java.util.List;

/**
 * @author Dries007
 */
public class CmdWorldExplorer extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "worldexplorer";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_)
    {
        return "Pregen the world & check for the presents resources.";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        int x, z, size = 1000;
        int argIndex = 0;

        if (args.length > 0 && args[0].equalsIgnoreCase("stop"))
        {
            WorldExplorer.stop();
            return;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("spawn"))
        {
            ChunkCoordinates spawn = MinecraftServer.getServer().worldServers[argIndex++].getSpawnPoint();
            x = spawn.posX;
            z = spawn.posZ;
        }
        else // get X & Z, first as 2 args, then as a player location, then as the senders location
        {
            try
            {
                x = parseInt(sender, args[argIndex++]);
                z = parseInt(sender, args[argIndex++]);
            }
            catch (NumberInvalidException | IndexOutOfBoundsException ignored)
            {
                EntityPlayerMP player;
                argIndex = 0;
                try
                {
                    player = getPlayer(sender, args[argIndex++]);
                    x = MathHelper.floor_double(player.posX);
                    z = MathHelper.floor_double(player.posZ);
                }
                catch (PlayerNotFoundException | IndexOutOfBoundsException e)
                {
                    try
                    {
                        argIndex = 0;
                        player = getCommandSenderAsPlayer(sender);
                        x = MathHelper.floor_double(player.posX);
                        z = MathHelper.floor_double(player.posZ);
                    }
                    catch (PlayerNotFoundException e1)
                    {
                        ChunkCoordinates spawn = MinecraftServer.getServer().worldServers[0].getSpawnPoint();
                        x = spawn.posX;
                        z = spawn.posZ;
                    }
                }
            }
        }
        if (args.length > argIndex) size = parseIntWithMin(sender, args[argIndex++], 0);
        if (args.length > argIndex) throw new CommandException("Unused arguments " + partialJoin(args, argIndex));

        WorldExplorer.start(sender, x >> 4, z >> 4, size / 16);
    }

    public static String partialJoin(String[] args, int argIndex)
    {
        if (argIndex >= args.length) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = argIndex; i < args.length; i++) sb.append(" ").append(args[i]);
        return sb.substring(1);
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender p_71519_1_)
    {
        return MinecraftServer.getServer().isSinglePlayer() || p_71519_1_ instanceof MinecraftServer;
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args)
    {
        if (args.length == 1) return getListOfStringsMatchingLastWord(args, "stop", "spawn");
        return null;
    }
}
