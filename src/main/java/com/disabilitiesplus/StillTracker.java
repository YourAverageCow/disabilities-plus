package com.disabilitiesplus;

import net.minecraft.server.level.ServerPlayer;

final class StillTracker {
    private static final double MOVED_SQ = 0.01 * 0.01;
    double lastX = 0.0;
    double lastZ = 0.0;
    int ticks = 0;

    int update(ServerPlayer sp) {
        double dx = sp.getX() - this.lastX;
        double dz = sp.getZ() - this.lastZ;
        this.lastX = sp.getX();
        this.lastZ = sp.getZ();
        this.ticks = sp.isShiftKeyDown() && dx * dx + dz * dz < MOVED_SQ ? this.ticks + 1 : 0;
        return this.ticks;
    }

    void reset() {
        this.ticks = 0;
    }
}
