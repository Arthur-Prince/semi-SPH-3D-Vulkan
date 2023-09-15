package vulkan;

import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR;

import java.nio.IntBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

public class SwapChainSupportDetails {

    VkSurfaceCapabilitiesKHR capabilities;
    VkSurfaceFormatKHR.Buffer formats;
    IntBuffer presentModes;

    boolean swapChainAdequate() {
	return formats.hasRemaining() && presentModes.hasRemaining();
    }

    static SwapChainSupportDetails querySwapChainSupport(VkPhysicalDevice device, MemoryStack stack) {

	SwapChainSupportDetails details = new SwapChainSupportDetails();

	long surface = QueueFamilyIndices.getSurface();

	details.capabilities = VkSurfaceCapabilitiesKHR.malloc(stack);
	vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device, surface, details.capabilities);

	IntBuffer count = stack.ints(0);

	vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, count, null);

	if (count.get(0) != 0) {
	    details.formats = VkSurfaceFormatKHR.malloc(count.get(0), stack);
	    vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, count, details.formats);
	}

	vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, count, null);

	if (count.get(0) != 0) {
	    details.presentModes = stack.mallocInt(count.get(0));
	    vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, count, details.presentModes);
	}

	return details;
    }
}
