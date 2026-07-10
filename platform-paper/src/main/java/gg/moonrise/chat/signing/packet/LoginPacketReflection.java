package gg.moonrise.chat.signing.packet;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

final class LoginPacketReflection {

    static final String LOGIN_PACKET = "net.minecraft.network.protocol.game.ClientboundLoginPacket";

    private final Constructor<?> loginConstructor;
    private final Method playerId;
    private final Method hardcore;
    private final Method levels;
    private final Method maxPlayers;
    private final Method chunkRadius;
    private final Method simulationDistance;
    private final Method reducedDebugInfo;
    private final Method showDeathScreen;
    private final Method doLimitedCrafting;
    private final Method commonPlayerSpawnInfo;
    private final Method onlineMode;

    private LoginPacketReflection(
            Constructor<?> loginConstructor,
            Method playerId,
            Method hardcore,
            Method levels,
            Method maxPlayers,
            Method chunkRadius,
            Method simulationDistance,
            Method reducedDebugInfo,
            Method showDeathScreen,
            Method doLimitedCrafting,
            Method commonPlayerSpawnInfo,
            Method onlineMode
    ) {
        this.loginConstructor = loginConstructor;
        this.playerId = playerId;
        this.hardcore = hardcore;
        this.levels = levels;
        this.maxPlayers = maxPlayers;
        this.chunkRadius = chunkRadius;
        this.simulationDistance = simulationDistance;
        this.reducedDebugInfo = reducedDebugInfo;
        this.showDeathScreen = showDeathScreen;
        this.doLimitedCrafting = doLimitedCrafting;
        this.commonPlayerSpawnInfo = commonPlayerSpawnInfo;
        this.onlineMode = onlineMode;
    }

    static LoginPacketReflection load() throws ReflectiveOperationException {
        Class<?> loginPacket = Class.forName(LOGIN_PACKET);

        Method levels = loginPacket.getMethod("levels");
        Method commonPlayerSpawnInfo = loginPacket.getMethod("commonPlayerSpawnInfo");

        return new LoginPacketReflection(
                loginPacket.getConstructor(
                        int.class,
                        boolean.class,
                        levels.getReturnType(),
                        int.class,
                        int.class,
                        int.class,
                        boolean.class,
                        boolean.class,
                        boolean.class,
                        commonPlayerSpawnInfo.getReturnType(),
                        boolean.class,
                        boolean.class
                ),
                loginPacket.getMethod("playerId"),
                loginPacket.getMethod("hardcore"),
                levels,
                loginPacket.getMethod("maxPlayers"),
                loginPacket.getMethod("chunkRadius"),
                loginPacket.getMethod("simulationDistance"),
                loginPacket.getMethod("reducedDebugInfo"),
                loginPacket.getMethod("showDeathScreen"),
                loginPacket.getMethod("doLimitedCrafting"),
                commonPlayerSpawnInfo,
                loginPacket.getMethod("onlineMode")
        );
    }

    Object rewriteClaimingSecureChat(Object packet) throws ReflectiveOperationException {
        return loginConstructor.newInstance(
                playerId.invoke(packet),
                hardcore.invoke(packet),
                levels.invoke(packet),
                maxPlayers.invoke(packet),
                chunkRadius.invoke(packet),
                simulationDistance.invoke(packet),
                reducedDebugInfo.invoke(packet),
                showDeathScreen.invoke(packet),
                doLimitedCrafting.invoke(packet),
                commonPlayerSpawnInfo.invoke(packet),
                onlineMode.invoke(packet),
                true
        );
    }
}
