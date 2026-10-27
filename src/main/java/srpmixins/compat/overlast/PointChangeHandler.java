package srpmixins.compat.overlast;

import com.dhanantry.scapeandrunparasites.world.SRPSaveData;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import srpmixins.network.PacketHandler;
import srpmixins.util.customphasemechanics.SRPSaveDataInterface;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Sends the change of the evolution points a player is shown in the OverLast HUD.
 * Switching to different points (other dimension, player phases, chunk phase regions) only resets the tracked value.
 */
public class PointChangeHandler {
    private static final Map<EntityPlayer, TrackedPoints> trackedPoints = new WeakHashMap<>();

    public static void init() {
        PacketHandler.NETWORK.registerMessage(SRPMixinsPacketPointChange.Handler.class, SRPMixinsPacketPointChange.class, PacketHandler.POINT_CHANGE, Side.CLIENT);
        MinecraftForge.EVENT_BUS.register(PointChangeHandler.class);
    }

    //done like this to not show massive changes on chunk phases when crossing chunk borders
    private static class TrackedPoints {
        private final SRPSaveData data;
        private final int dimension;
        private int points;

        private TrackedPoints(SRPSaveData data, int dimension, int points) {
            this.data = data;
            this.dimension = dimension;
            this.points = points;
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof EntityPlayerMP)) return;
        EntityPlayer player = event.player;

        SRPSaveData data = SRPSaveDataInterface.get(player.world, player, null);
        int dimension = player.world.provider.getDimension();
        int points = data.getTotalKills(dimension);

        TrackedPoints tracked = trackedPoints.get(player);
        if (tracked == null || tracked.data != data || tracked.dimension != dimension) {
            trackedPoints.put(player, new TrackedPoints(data, dimension, points));
            return;
        }

        if (points != tracked.points) {
            PacketHandler.NETWORK.sendTo(new SRPMixinsPacketPointChange(points - tracked.points), (EntityPlayerMP) player);
            tracked.points = points;
        }
    }

    /**
     * Client-side list of recent evolution point changes, rendered as rising and fading numbers above the OverLast phase circle.
     */
    @SideOnly(Side.CLIENT)
    public static class PointChangePopups {
        private static final long LIFETIME_MS = 500;
        private static final int RISE = 18;
        private static final int MAX_POPUPS = 15;

        private static final Deque<Popup> popups = new ArrayDeque<>();

        private static class Popup {
            private final String text;
            private final int color;
            private final long start;

            private Popup(int change, long start) {
                this.text = (change > 0 ? "+" : "-") + format(Math.abs(change));
                this.color = change > 0 ? 0xFFFFFF : 0x55FF55;
                this.start = start;
            }
        }

        private static String format(int points) {
            if (points >= 10000000) return points / 1000000 + "m";
            if (points >= 10000) return points / 1000 + "k";
            return String.valueOf(points);
        }

        public static void add(int change) {
            if (change == 0) return;
            popups.addLast(new Popup(change, Minecraft.getSystemTime()));
            if (popups.size() > MAX_POPUPS) popups.pollFirst();
        }

        /**
         * @param circleTop popups spawn above this and rise upwards
         * @param circleBottom popups spawn below this and move downwards if there is not enough room above the circle
         */
        public static void render(FontRenderer fontRenderer, int centerX, int circleTop, int circleBottom) {
            long now = Minecraft.getSystemTime();
            while (!popups.isEmpty() && now - popups.peekFirst().start >= LIFETIME_MS) popups.pollFirst();
            if (popups.isEmpty()) return;

            boolean downwards = circleTop < fontRenderer.FONT_HEIGHT + RISE;
            int startY = downwards ? circleBottom + 1 : circleTop - fontRenderer.FONT_HEIGHT - 1;

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            //Oldest first so newer popups are drawn on top
            for (Popup popup : popups) {
                float progress = (now - popup.start) / (float) LIFETIME_MS;
                int offset = (int) (progress * RISE);

                //FontRenderer treats alpha below 4 as fully opaque
                int alpha = (int) (255 * Math.min(1F, 2F * (1F - progress)));
                if (alpha < 4) continue;

                int y = downwards ? startY + offset : startY - offset;
                fontRenderer.drawStringWithShadow(popup.text, centerX - fontRenderer.getStringWidth(popup.text) / 2F, y, alpha << 24 | popup.color);
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    public static class SRPMixinsPacketPointChange implements IMessage {
        private int change;

        public SRPMixinsPacketPointChange() {}

        public SRPMixinsPacketPointChange(int change) {
            this.change = change;
        }

        @Override
        public void fromBytes(ByteBuf byteBuf) {
            this.change = byteBuf.readInt();
        }

        @Override
        public void toBytes(ByteBuf byteBuf) {
            byteBuf.writeInt(this.change);
        }

        @SideOnly(Side.CLIENT)
        public static class Handler implements IMessageHandler<SRPMixinsPacketPointChange, IMessage> {
            @Override
            public IMessage onMessage(SRPMixinsPacketPointChange message, MessageContext ctx) {
                Minecraft.getMinecraft().addScheduledTask(() -> PointChangePopups.add(message.change));
                return null;
            }
        }
    }
}
