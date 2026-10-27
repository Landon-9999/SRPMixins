package srpmixins.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import srpmixins.SRPMixins;

public class PacketHandler {
    public static SimpleNetworkWrapper NETWORK;

    //Fixed packet ids as some packets are only registered with certain mods or configs. 0 stays unused
    public static final int POINT_CHANGE = 1;

    public static void init() {
        NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(SRPMixins.MODID);
    }
}
