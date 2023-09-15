package vulkan;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR;
import static org.lwjgl.vulkan.VK10.VK_FALSE;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_COMPUTE_BIT;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_GRAPHICS_BIT;
import static org.lwjgl.vulkan.VK10.VK_TRUE;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceQueueFamilyProperties;

import java.nio.IntBuffer;
import java.util.stream.IntStream;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

class QueueFamilyIndices {

    // We use Integer to use null as the empty value
    static long surface;
    Integer graphicsFamily;
    Integer presentFamily;

    boolean isComplete() {
	return graphicsFamily != null && presentFamily != null;
    }

    public int[] unique() {
	return IntStream.of(graphicsFamily, presentFamily).distinct().toArray();
    }

    public int[] array() {
	return new int[] { graphicsFamily, presentFamily };
    }

    static QueueFamilyIndices findQueueFamilies(VkPhysicalDevice device) {

	QueueFamilyIndices indices = new QueueFamilyIndices();

	try (MemoryStack stack = stackPush()) {

	    IntBuffer queueFamilyCount = stack.ints(0);

	    vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, null);

	    VkQueueFamilyProperties.Buffer queueFamilies = VkQueueFamilyProperties.malloc(queueFamilyCount.get(0),
		    stack);

	    vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, queueFamilies);

	    IntBuffer presentSupport = stack.ints(VK_FALSE);

	    for (int i = 0; i < queueFamilies.capacity() || !indices.isComplete(); i++) {

		if ((queueFamilies.get(i).queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0
			&& (queueFamilies.get(i).queueFlags() & VK_QUEUE_COMPUTE_BIT) != 0) {
		    indices.graphicsFamily = i;
		}

		vkGetPhysicalDeviceSurfaceSupportKHR(device, i, surface, presentSupport);

		if (presentSupport.get(0) == VK_TRUE) {
		    indices.presentFamily = i;
		}
	    }

	    return indices;
	}
    }

    public Integer getGraphicsFamily() {
	return graphicsFamily;
    }

    public void setGraphicsFamily(Integer graphicsFamily) {
	this.graphicsFamily = graphicsFamily;
    }

    public Integer getPresentFamily() {
	return presentFamily;
    }

    public void setPresentFamily(Integer presentFamily) {
	this.presentFamily = presentFamily;
    }

    public static long getSurface() {
	return surface;
    }

    static void setSurface(long surface) {
	QueueFamilyIndices.surface = surface;
    }

}
