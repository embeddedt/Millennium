package com.cleanroommc.millennium.network;

import com.cleanroommc.millennium.poi.IPOICapability;
import com.cleanroommc.millennium.poi.PointOfInterest;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.ForgeRegistry;

public class POIUpdateMessage implements IMessage {
    public long pos;
    public int poiId;

    public POIUpdateMessage() {

    }

    public POIUpdateMessage(long pos, PointOfInterest poi) {
        this.pos = pos;
        this.poiId = poi != null ? poi.serialize() : -1;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = buf.readLong();
        poiId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(pos);
        buf.writeInt(poiId);
    }

    public enum Handler implements IMessageHandler<POIUpdateMessage, IMessage> {
        INSTANCE;

        @Override
        public IMessage onMessage(POIUpdateMessage message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                BlockPos pos = BlockPos.fromLong(message.pos);
                Chunk chunk = Minecraft.getMinecraft().world.getChunk(pos);
                IPOICapability cap = IPOICapability.get(chunk);
                if(cap != null) {
                    PointOfInterest poi = PointOfInterest.deserialize(message.poiId);
                    cap.setPOI(message.pos, poi);
                    chunk.markDirty();
                }
            });
            return null;
        }
    }
}
