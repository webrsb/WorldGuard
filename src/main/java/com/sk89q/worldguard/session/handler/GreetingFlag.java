/*
 * WorldGuard, a suite of tools for Minecraft
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldguard.session.handler;

import com.google.common.collect.Sets;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.sponge.commands.CommandUtils;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class GreetingFlag extends Handler {

    private Set<Text> lastMessageStack = Collections.emptySet();

    public GreetingFlag(Session session) {
        super(session);
    }

    @Override
    public boolean onCrossBoundary(Player player, Transform<World> from, Transform<World> to, ApplicableRegionSet toSet, Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType) {
        Collection<Text> messages = toSet.queryAllValues(getPlugin().wrapPlayer(player), DefaultFlag.GREET_MESSAGE);

        for (Text message : messages) {
            if (!lastMessageStack.contains(message)) {
                String plain = Texts.toPlain(message);
                plain = getPlugin().replaceMacros(player, plain);
                plain = plain.replaceAll("\\\\n", "\n");
                player.sendMessage(CommandUtils.replaceColorMacros(plain));
                break;
            }
        }

        lastMessageStack = Sets.newHashSet(messages);

        if (!lastMessageStack.isEmpty()) {
            // Due to flag priorities, we have to collect the lower
            // priority flag values separately
            for (ProtectedRegion region : toSet) {
                Text message = region.getFlag(DefaultFlag.GREET_MESSAGE);
                if (message != null) {
                    lastMessageStack.add(message);
                }
            }
        }

        return true;
    }
}
