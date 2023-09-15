package vulkan;

import static java.util.stream.Collectors.toSet;
import static org.lwjgl.system.Configuration.DEBUG;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class Prop {

    public static final int UINT32_MAX = 0xFFFFFFFF;
    public static final long UINT64_MAX = 0xFFFFFFFFFFFFFFFFL;

    public static final int MAX_FRAMES_IN_FLIGHT = 2;

    public static final boolean ENABLE_VALIDATION_LAYERS = DEBUG.get(true);

    public static final Set<String> VALIDATION_LAYERS;
    static {
	if (ENABLE_VALIDATION_LAYERS) {
	    VALIDATION_LAYERS = new HashSet<>();
	    VALIDATION_LAYERS.add("VK_LAYER_KHRONOS_validation");
	} else {
	    // We are not going to use it, so we don't create it
	    VALIDATION_LAYERS = null;
	}
    }

    public static final Set<String> DEVICE_EXTENSIONS = Stream.of(VK_KHR_SWAPCHAIN_EXTENSION_NAME).collect(toSet());
    
    public static int mdc(int a, int b) {
        if (b == 0) {
            return a;
        } else {
            return mdc(b, a % b);
        }
    }
    public static int mmc(int a, int b) {
        return (a * b) / mdc(a, b);
    }
}
