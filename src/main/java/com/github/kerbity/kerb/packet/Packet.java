/*
 * Kerb
 * Event and request distributor server software.
 *
 * Copyright (C) 2023  Smuddgge
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.kerbity.kerb.packet;

import com.github.squishylib.configuration.implementation.MemoryConfigurationSection;
import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a packet of data stored
 * as a map of string to object.
 * For more infomation on packets see the package info.
 */
public class Packet extends MemoryConfigurationSection {

    /**
     * Used to create an empty packet.
     */
    public Packet() {
        super(new HashMap<>());
    }

    /**
     * Used to create a packet.
     *
     * @param map The instance of the map the
     *            packet should use.
     */
    public Packet(@NotNull Map<String, Object> map) {
        super(map);
    }

    /**
     * Used to packet the map into a json string.
     *
     * @return The packet as a string.
     */
    public @NotNull String getPacketString() {
        Gson gson = new Gson();
        return gson.toJson(this.data);
    }

    /**
     * Used to get the type of packet.
     *
     * @return The type of packet.
     */
    public @Nullable PacketType getType() {
        return PacketType.fromIdentifier(this.getString("type"));
    }

    /**
     * Used to get the packet's identifier.
     *
     * @return The packet's identifier.
     */
    public @Nullable String getIdentifier() {
        return this.getString("identifier");
    }

    /**
     * Used to get the sequence identifier.
     * This keeps similar event calls seperated.
     *
     * @return The sequence identifier.
     */
    public @Nullable String getSequenceIdentifier() {
        return this.getString("sequence_identifier", null);
    }

    /**
     * Used to get the raw data.
     *
     * @return Teh rew data.
     */
    public @Nullable String getData() {
        return this.getString("data");
    }

    /**
     * Used to get the data as a class.
     *
     * @param clazz The instance of the class.
     * @param <T>   The type of class.
     * @return The requested class as data.
     */
    public @Nullable <T> T getData(@NotNull Class<T> clazz) {
        Gson gson = new Gson();
        return gson.fromJson(this.getString("data"), clazz);
    }

    /**
     * Used to get the details of the target.
     * The details of the target.
     * <ul>
     *     <li>address:port</li>
     * </ul>
     *
     * @return The details of the target.
     */
    public @Nullable String getSource() {
        return this.getString("source", null);
    }

    /**
     * Used to set the type of packet.
     * For example, "event".
     *
     * @param packetType The type of packet.
     * @return This instance.
     */
    public @NotNull Packet setType(@NotNull PacketType packetType) {
        this.set("type", packetType.getIdentifier());
        return this;
    }

    /**
     * Used to set the packet's identifier.
     * This should be unique for each type
     * of event or request.
     *
     * @param identifier The packet's identifier.
     * @return This instance.
     */
    public @NotNull Packet setIdentifier(@NotNull String identifier) {
        this.set("identifier", identifier);
        return this;
    }

    /**
     * Used to set the sequence identifier.
     * Used to keep similar events seperated.
     *
     * @param sequenceIdentifier The sequence identifier.
     * @return This instance.
     */
    public @NotNull Packet setSequenceIdentifier(@NotNull String sequenceIdentifier) {
        this.set("sequence_identifier", sequenceIdentifier);
        return this;
    }

    /**
     * Used to set the packet's data.
     * This can be any class object.
     *
     * @param object The instance of an object.
     * @return This instance.
     */
    public @NotNull Packet setData(@NotNull Object object) {
        Gson gson = new Gson();
        this.set("data", gson.toJson(object));
        return this;
    }

    /**
     * Used to set the target ip address and port.
     *
     * @param targetIdentifier The target identifier.
     * @return This instance.
     */
    public @NotNull Packet setSource(@NotNull String targetIdentifier) {
        this.set("source", targetIdentifier);
        return this;
    }

    /**
     * Used to get a json as a packet.
     *
     * @param json The instance of a json.
     * @return The instance of the packet.
     */
    @SuppressWarnings("unchecked")
    public static @NotNull Packet getPacket(@NotNull String json) {
        Gson gson = new Gson();
        return new Packet(gson.fromJson(json, Map.class));
    }

    @Override
    public String toString() {
        return this.getMap().toString();
    }
}
