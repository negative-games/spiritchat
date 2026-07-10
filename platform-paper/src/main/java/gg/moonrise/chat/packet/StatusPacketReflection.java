package gg.moonrise.chat.packet;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

final class StatusPacketReflection {

    static final String STATUS_RESPONSE_PACKET = "net.minecraft.network.protocol.status.ClientboundStatusResponsePacket";

    private static final String SERVER_STATUS = "net.minecraft.network.protocol.status.ServerStatus";
    private static final String FRIENDLY_BYTE_BUF = "net.minecraft.network.FriendlyByteBuf";
    private static final int STATUS_RESPONSE_PACKET_ID = 0;

    private final Method status;
    private final Object codec;
    private final Object jsonOpsInstance;
    private final Method encodeStart;
    private final Method getOrThrow;
    private final Method addProperty;
    private final Constructor<?> friendlyByteBufConstructor;
    private final Method writeVarInt;
    private final Method writeUtf;

    private StatusPacketReflection(
            Method status,
            Object codec,
            Object jsonOpsInstance,
            Method encodeStart,
            Method getOrThrow,
            Method addProperty,
            Constructor<?> friendlyByteBufConstructor,
            Method writeVarInt,
            Method writeUtf
    ) {
        this.status = status;
        this.codec = codec;
        this.jsonOpsInstance = jsonOpsInstance;
        this.encodeStart = encodeStart;
        this.getOrThrow = getOrThrow;
        this.addProperty = addProperty;
        this.friendlyByteBufConstructor = friendlyByteBufConstructor;
        this.writeVarInt = writeVarInt;
        this.writeUtf = writeUtf;
    }

    static StatusPacketReflection load() throws ReflectiveOperationException {
        Class<?> statusPacket = Class.forName(STATUS_RESPONSE_PACKET);
        Class<?> serverStatus = Class.forName(SERVER_STATUS);
        Class<?> codec = Class.forName("com.mojang.serialization.Codec");
        Class<?> dynamicOps = Class.forName("com.mojang.serialization.DynamicOps");
        Class<?> jsonOps = Class.forName("com.mojang.serialization.JsonOps");
        Class<?> dataResult = Class.forName("com.mojang.serialization.DataResult");
        Class<?> jsonObject = Class.forName("com.google.gson.JsonObject");
        Class<?> friendlyByteBuf = Class.forName(FRIENDLY_BYTE_BUF);

        Field codecField = serverStatus.getField("CODEC");
        Field instanceField = jsonOps.getField("INSTANCE");

        return new StatusPacketReflection(
                statusPacket.getMethod("status"),
                codecField.get(null),
                instanceField.get(null),
                codec.getMethod("encodeStart", dynamicOps, Object.class),
                dataResult.getMethod("getOrThrow"),
                jsonObject.getMethod("addProperty", String.class, Boolean.class),
                friendlyByteBuf.getConstructor(ByteBuf.class),
                friendlyByteBuf.getMethod("writeVarInt", int.class),
                friendlyByteBuf.getMethod("writeUtf", String.class)
        );
    }

    ByteBuf rewrite(ChannelHandlerContext context, Object packet) throws ReflectiveOperationException {
        Object encoded = encodeStart.invoke(codec, jsonOpsInstance, status.invoke(packet));
        Object json = getOrThrow.invoke(encoded);
        addProperty.invoke(json, "preventsChatReports", true);

        ByteBuf output = context.alloc().buffer();
        Object buffer = friendlyByteBufConstructor.newInstance(output);
        writeVarInt.invoke(buffer, STATUS_RESPONSE_PACKET_ID);
        writeUtf.invoke(buffer, json.toString());
        return output;
    }
}
