package com.playhills.lasertag.utils;

import com.google.gson.JsonElement;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_8_R3.PacketDataSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutCustomPayload;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.nio.charset.StandardCharsets;

public class LabyModProtocol {

    public static void sendClientMessage(Player player, String key, JsonElement messageContent) {
        byte[] bytes = getBytes(key, messageContent.toString());
        PacketDataSerializer pds = new PacketDataSerializer( Unpooled.wrappedBuffer(bytes));
        PacketPlayOutCustomPayload payloadPacket = new PacketPlayOutCustomPayload( "labymod3:main", pds );
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket( payloadPacket );
    }

    public static byte[] getBytes( String messageKey, String messageContents) {
        ByteBuf byteBuf = Unpooled.buffer();
        write( byteBuf, messageKey );
        write( byteBuf, messageContents );
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes( bytes );
        byteBuf.release();
        return bytes;
    }

    private static void write(ByteBuf buf, String string) {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        buf.writeByte(bytes.length);
        buf.writeBytes(bytes);
    }
}