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

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.sponge.commands.CommandUtils;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.world.World;

public class ExitFlag extends FlagValueChangeHandler<State> {

    private static final long MESSAGE_THRESHOLD = 1000 * 2;
    private Text storedMessage;
    private long lastMessage;

    public ExitFlag(Session session) {
        super(session, DefaultFlag.EXIT);
    }

    private void update(LocalPlayer localPlayer, ApplicableRegionSet set, boolean allowed) {
        if (!allowed) {
            storedMessage = set.queryValue(localPlayer, DefaultFlag.EXIT_DENY_MESSAGE);
        }
    }

    private void sendMessage(Player player) {
        long now = System.currentTimeMillis();

        if ((now - lastMessage) > MESSAGE_THRESHOLD && storedMessage != null) {
            player.sendMessage(CommandUtils.replaceColorMacros(Texts.toPlain(storedMessage)));
            lastMessage = now;
        }
    }

    @Override
    protected void onInitialValue(Player player, ApplicableRegionSet set, State value) {
        update(getPlugin().wrapPlayer(player), set, StateFlag.test(value));
    }

    @Override
    protected boolean onSetValue(Player player, Transform<World> from, Transform<World> to, ApplicableRegionSet toSet, State currentValue, State lastValue, MoveType moveType) {
        if (getSession().getManager().hasBypass(player, from.getExtent())) {
            return true;
        }

        boolean lastAllowed = StateFlag.test(lastValue);
        boolean allowed = StateFlag.test(currentValue);

        LocalPlayer localPlayer = getPlugin().wrapPlayer(player);

        if (allowed && !lastAllowed) {
            Boolean override = toSet.queryValue(localPlayer, DefaultFlag.EXIT_OVERRIDE);
            if (override == null || !override) {
                sendMessage(player);
                return false;
            }
        }

        update(localPlayer, toSet, allowed);
        return true;
    }

    @Override
    protected boolean onAbsentValue(Player player, Transform<World> from, Transform<World> to, ApplicableRegionSet toSet, State lastValue, MoveType moveType) {
        if (getSession().getManager().hasBypass(player, from.getExtent())) {
            return true;
        }

        boolean lastAllowed = StateFlag.test(lastValue);

        if (!lastAllowed) {
            Boolean override = toSet.queryValue(getPlugin().wrapPlayer(player), DefaultFlag.EXIT_OVERRIDE);
            if (override == null || !override) {
                sendMessage(player);
                return false;
            }
        }

        return true;
    }

}
