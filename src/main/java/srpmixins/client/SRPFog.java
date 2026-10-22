package srpmixins.client;

import net.minecraft.client.Minecraft;

import java.lang.ref.WeakReference;

public class SRPFog {
    /**
     * Client-side holder of the current SRP fog density (smoothed via {@link Transition}), reset per server connection instead of per world load.
     */
    public static final class State {
        private static WeakReference<Object> connection = new WeakReference<>(null);
        private static Transition transition = new Transition();

        private State() {
        }

        private static void checkConnection() {
            Object current = Minecraft.getMinecraft().getConnection();
            if (current == null || connection.get() != current) {
                connection = new WeakReference<>(current);
                transition = new Transition();
            }
        }

        public static void receive(float density) {
            checkConnection();
            transition.receive(density, System.nanoTime());
        }

        public static float density() {
            checkConnection();
            return transition.sample(System.nanoTime());
        }
    }

    /**
     * Math only: eases the displayed density towards the last density received from the server (exponential, ~0.15s time constant),
     * and converts density into a fog color blend weight.
     */
    public static final class Transition {
        private float value;
        private float target;
        private long sampledAt;
        private boolean initialized;

        public void receive(float density, long now) {
            sample(now);
            target = Float.isFinite(density) ? Math.max(0.0F, density) : 0.0F;
        }

        public float sample(long now) {
            if (initialized) {
                double elapsed = Math.max(0.0, (now - sampledAt) * 1.0e-9);
                value += (target - value) * (float) -Math.expm1(-elapsed / 0.15);
                if (target == 0.0F && value < 1.0e-7F) value = 0.0F;
            }
            sampledAt = now;
            initialized = true;
            return value;
        }

        public static float colorWeight(float density, float maximum) {
            if (!Float.isFinite(maximum) || maximum <= 0.0F) return 0.0F;
            float weight = Math.max(0.0F, Math.min(1.0F, density / maximum / 0.2F));
            return weight * weight * (3.0F - 2.0F * weight);
        }
    }
}
