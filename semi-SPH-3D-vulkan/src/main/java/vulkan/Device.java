package vulkan;

import static vulkan.Prop.*;
import static vulkan.QueueFamilyType.*;
import static java.util.stream.Collectors.toSet;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR;
import static org.lwjgl.vulkan.KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateCommandPool;
import static org.lwjgl.vulkan.VK10.vkCreateDevice;
import static org.lwjgl.vulkan.VK10.vkDestroyCommandPool;
import static org.lwjgl.vulkan.VK10.vkDestroyDevice;
import static org.lwjgl.vulkan.VK10.vkEnumerateDeviceExtensionProperties;
import static org.lwjgl.vulkan.VK10.vkEnumeratePhysicalDevices;
import static org.lwjgl.vulkan.VK10.vkGetDeviceQueue;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceMemoryProperties;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkDeviceCreateInfo;
import org.lwjgl.vulkan.VkDeviceQueueCreateInfo;
import org.lwjgl.vulkan.VkExtensionProperties;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkQueue;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

import interfaces.Destructor;

public class Device implements Destructor {

    private VkInstance instance;

    private VkPhysicalDevice physicalDevice;
    private VkDevice device;

    private VkQueue graphicsQueue;
    private VkQueue presentQueue;

    private long commandPool;

    public Device(DefaultVulkanSetup d) {
	this.instance = d.getInstance();
	pickPhysicalDevice();
	createLogicalDevice();
	createCommandPool();

    }

    public Device(VkInstance instance, VkPhysicalDevice physicalDevice, VkDevice device, VkQueue graphicsQueue,
	    VkQueue presentQueue, long commandPool) {
	this.instance=instance;
	this.physicalDevice=physicalDevice;
	this.device=device;
	this.graphicsQueue=graphicsQueue;
	this.presentQueue=presentQueue;
	this.commandPool=commandPool;

    }

    private void pickPhysicalDevice() {

	try (MemoryStack stack = stackPush()) {

	    IntBuffer deviceCount = stack.ints(0);

	    vkEnumeratePhysicalDevices(instance, deviceCount, null);

	    if (deviceCount.get(0) == 0) {
		throw new RuntimeException("Failed to find GPUs with Vulkan support");
	    }

	    PointerBuffer ppPhysicalDevices = stack.mallocPointer(deviceCount.get(0));

	    vkEnumeratePhysicalDevices(instance, deviceCount, ppPhysicalDevices);

	    for (int i = 0; i < ppPhysicalDevices.capacity(); i++) {

		VkPhysicalDevice device = new VkPhysicalDevice(ppPhysicalDevices.get(i), instance);

		if (isDeviceSuitable(device)) {
		    physicalDevice = device;
		    return;
		}
	    }

	    throw new RuntimeException("Failed to find a suitable GPU");
	}
    }

    private void createLogicalDevice() {

	try (MemoryStack stack = stackPush()) {

	    QueueFamilyIndices indices = QueueFamilyIndices.findQueueFamilies(physicalDevice);

	    int[] uniqueQueueFamilies = indices.unique();

	    VkDeviceQueueCreateInfo.Buffer queueCreateInfos = VkDeviceQueueCreateInfo.calloc(uniqueQueueFamilies.length,
		    stack);

	    for (int i = 0; i < uniqueQueueFamilies.length; i++) {
		VkDeviceQueueCreateInfo queueCreateInfo = queueCreateInfos.get(i);
		queueCreateInfo.sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO);
		queueCreateInfo.queueFamilyIndex(uniqueQueueFamilies[i]);
		queueCreateInfo.pQueuePriorities(stack.floats(1.0f));
	    }

	    VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.calloc(stack);

	    VkDeviceCreateInfo createInfo = VkDeviceCreateInfo.calloc(stack);

	    createInfo.sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO);
	    createInfo.pQueueCreateInfos(queueCreateInfos);
	    // queueCreateInfoCount is automatically set

	    createInfo.pEnabledFeatures(deviceFeatures);

	    createInfo.ppEnabledExtensionNames(asPointerBuffer(stack, DEVICE_EXTENSIONS));

	    if (ENABLE_VALIDATION_LAYERS) {
		createInfo.ppEnabledLayerNames(asPointerBuffer(stack, VALIDATION_LAYERS));
	    }

	    PointerBuffer pDevice = stack.pointers(VK_NULL_HANDLE);

	    if (vkCreateDevice(physicalDevice, createInfo, null, pDevice) != VK_SUCCESS) {
		throw new RuntimeException("Failed to create logical device");
	    }

	    device = new VkDevice(pDevice.get(0), physicalDevice, createInfo);

	    PointerBuffer pQueue = stack.pointers(VK_NULL_HANDLE);

	    vkGetDeviceQueue(device, indices.getGraphicsFamily(), 0, pQueue);
	    graphicsQueue = new VkQueue(pQueue.get(0), device);

	    vkGetDeviceQueue(device, indices.getPresentFamily(), 0, pQueue);
	    presentQueue = new VkQueue(pQueue.get(0), device);
	}
    }

    private PointerBuffer asPointerBuffer(MemoryStack stack, Collection<String> collection) {

	PointerBuffer buffer = stack.mallocPointer(collection.size());

	collection.stream().map(stack::UTF8).forEach(buffer::put);

	return buffer.rewind();
    }

    private boolean isDeviceSuitable(VkPhysicalDevice device) {

	QueueFamilyIndices indices = QueueFamilyIndices.findQueueFamilies(device);

	boolean extensionsSupported = checkDeviceExtensionSupport(device);
	boolean swapChainAdequate = false;

	if (extensionsSupported) {
	    try (MemoryStack stack = stackPush()) {
		SwapChainSupportDetails swapChainSupport = SwapChainSupportDetails.querySwapChainSupport(device, stack);
		swapChainAdequate = swapChainSupport.swapChainAdequate();
	    }
	}

	return indices.isComplete() && extensionsSupported && swapChainAdequate;
    }

    private boolean checkDeviceExtensionSupport(VkPhysicalDevice device) {

	try (MemoryStack stack = stackPush()) {

	    IntBuffer extensionCount = stack.ints(0);

	    vkEnumerateDeviceExtensionProperties(device, (String) null, extensionCount, null);

	    VkExtensionProperties.Buffer availableExtensions = VkExtensionProperties.malloc(extensionCount.get(0),
		    stack);

	    vkEnumerateDeviceExtensionProperties(device, (String) null, extensionCount, availableExtensions);

	    return availableExtensions.stream().map(VkExtensionProperties::extensionNameString).collect(toSet())
		    .containsAll(DEVICE_EXTENSIONS);
	}
    }

    private void createCommandPool() {

	try (MemoryStack stack = stackPush()) {

	    QueueFamilyIndices queueFamilyIndices = QueueFamilyIndices.findQueueFamilies(physicalDevice);

	    VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.calloc(stack);
	    poolInfo.sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO);
	    poolInfo.queueFamilyIndex(queueFamilyIndices.getGraphicsFamily());

	    LongBuffer pCommandPool = stack.mallocLong(1);

	    if (vkCreateCommandPool(device, poolInfo, null, pCommandPool) != VK_SUCCESS) {
		throw new RuntimeException("Failed to create command pool");
	    }

	    setCommandPool(pCommandPool.get(0));
	}
    }

    public int findMemoryType(MemoryStack stack, int typeFilter, int properties) {

	VkPhysicalDeviceMemoryProperties memProperties = VkPhysicalDeviceMemoryProperties.malloc(stack);
	vkGetPhysicalDeviceMemoryProperties(physicalDevice, memProperties);

	for (int i = 0; i < memProperties.memoryTypeCount(); i++) {
	    if ((typeFilter & (1 << i)) != 0
		    && (memProperties.memoryTypes(i).propertyFlags() & properties) == properties) {
		return i;
	    }
	}

	throw new RuntimeException("Failed to find suitable memory type");
    }

    public VkDevice getDevice() {
	return device;
    }

    public VkPhysicalDevice getPhysicalDevice() {
	return physicalDevice;
    }

    public VkQueue getGraphicsQueue() {
	return graphicsQueue;
    }

    public VkQueue getPresentQueue() {
	return presentQueue;
    }

    public long getCommandPool() {
	return commandPool;
    }

    public void setCommandPool(long commandPool) {
	this.commandPool = commandPool;
    }

    @Override
    public void cleanup() {
	vkDestroyCommandPool(device, commandPool, null);
	vkDestroyDevice(device, null);

    }

}
