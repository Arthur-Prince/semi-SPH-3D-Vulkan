package vulkan;

import static vulkan.Prop.*;
import static java.util.stream.Collectors.toSet;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWVulkan.glfwCreateWindowSurface;
import static org.lwjgl.glfw.GLFWVulkan.glfwGetRequiredInstanceExtensions;
import static org.lwjgl.system.Configuration.DEBUG;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME;
import static org.lwjgl.vulkan.EXTDebugUtils.VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT;
import static org.lwjgl.vulkan.EXTDebugUtils.vkCreateDebugUtilsMessengerEXT;
import static org.lwjgl.vulkan.EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT;
import static org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.HashSet;
import java.util.Set;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkAllocationCallbacks;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.lwjgl.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.lwjgl.vulkan.VkLayerProperties;

import interfaces.Destructor;
import interfaces.LoopExecuter;


public class DefaultVulkanSetup implements Destructor {

    private static int WIDTH = 800;
    private static int HEIGHT = 600;


    private static int debugCallback(int messageSeverity, int messageType, long pCallbackData, long pUserData) {

	VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);

	System.err.println("Validation layer: " + callbackData.pMessageString());

	return VK_FALSE;
    }

    private static int createDebugUtilsMessengerEXT(VkInstance instance, VkDebugUtilsMessengerCreateInfoEXT createInfo,
	    VkAllocationCallbacks allocationCallbacks, LongBuffer pDebugMessenger) {

	if (vkGetInstanceProcAddr(instance, "vkCreateDebugUtilsMessengerEXT") != NULL) {
	    return vkCreateDebugUtilsMessengerEXT(instance, createInfo, allocationCallbacks, pDebugMessenger);
	}

	return VK_ERROR_EXTENSION_NOT_PRESENT;
    }

    private static void destroyDebugUtilsMessengerEXT(VkInstance instance, long debugMessenger,
	    VkAllocationCallbacks allocationCallbacks) {

	if (vkGetInstanceProcAddr(instance, "vkDestroyDebugUtilsMessengerEXT") != NULL) {
	    vkDestroyDebugUtilsMessengerEXT(instance, debugMessenger, allocationCallbacks);
	}

    }


    private long window;

    private VkInstance instance;
    private long debugMessenger;
    private long surface;

    boolean framebufferResize;

    // contrutores para algumas inivicualizacoes
    public DefaultVulkanSetup(int width, int height) {
	WIDTH = width;
	HEIGHT = height;
	initWindow();
	createInstance();
	setupDebugMessenger();
	createSurface();

    }
    
    public DefaultVulkanSetup(VkInstance instance,long window,long surface,long debugMessenger) {
	this.window=window;
	this.instance= instance;
	this.surface = surface;
	this.debugMessenger = debugMessenger;
    }

    public DefaultVulkanSetup() {
	initWindow();
	createInstance();
	setupDebugMessenger();
	createSurface();
    }

    public void initWindow() {

	if (!glfwInit()) {
	    throw new RuntimeException("Cannot initialize GLFW");
	}

	glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);

	String title = "FPS " + 0;

	window = glfwCreateWindow(WIDTH, HEIGHT, title, NULL, NULL);

	if (window == NULL) {
	    throw new RuntimeException("Cannot create window");
	}

	glfwSetFramebufferSizeCallback(window, this::framebufferResizeCallback);
    }

    private void framebufferResizeCallback(long window, int width, int height) {

	framebufferResize = true;
    }

    public void run(LoopExecuter run) {
	long fps = 0;
	int count = 0;
	long ant = System.currentTimeMillis();
	
	
	run.init();
	
	while (!glfwWindowShouldClose(window)) {

	    glfwPollEvents();
	    
	    run.execute();

	    long dps = System.currentTimeMillis();
	    if (dps - ant > 1000) {
		String f = "FPS " + fps;
		glfwSetWindowTitle(window, f);
		ant = dps;
		fps = 0;

	    }
	    fps++;
	    count++;
	}
    }

    private void createInstance() {

//      if(ENABLE_VALIDATION_LAYERS && !checkValidationLayerSupport()) {
//          throw new RuntimeException("Validation requested but not supported");
//      }

	try (MemoryStack stack = stackPush()) {

	    // Use calloc to initialize the structs with 0s. Otherwise, the program can
	    // crash due to random values

	    VkApplicationInfo appInfo = VkApplicationInfo.calloc(stack);

	    appInfo.sType(VK_STRUCTURE_TYPE_APPLICATION_INFO);
	    appInfo.pApplicationName(stack.UTF8Safe("Hello Triangle"));
	    appInfo.applicationVersion(VK_MAKE_VERSION(1, 0, 0));
	    appInfo.pEngineName(stack.UTF8Safe("No Engine"));
	    appInfo.engineVersion(VK_MAKE_VERSION(1, 0, 0));
	    appInfo.apiVersion(VK_API_VERSION_1_0);

	    VkInstanceCreateInfo createInfo = VkInstanceCreateInfo.calloc(stack);

	    createInfo.sType(VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO);
	    createInfo.pApplicationInfo(appInfo);
	    // enabledExtensionCount is implicitly set when you call ppEnabledExtensionNames
	    createInfo.ppEnabledExtensionNames(getRequiredExtensions(stack));

	    if (ENABLE_VALIDATION_LAYERS) {

		// createInfo.ppEnabledLayerNames(asPointerBuffer(stack, VALIDATION_LAYERS));

		VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack);
		populateDebugMessengerCreateInfo(debugCreateInfo);
		createInfo.pNext(debugCreateInfo.address());
	    }

	    // We need to retrieve the pointer of the created instance
	    PointerBuffer instancePtr = stack.mallocPointer(1);

	    if (vkCreateInstance(createInfo, null, instancePtr) != VK_SUCCESS) {
		throw new RuntimeException("Failed to create instance");
	    }

	    instance = new VkInstance(instancePtr.get(0), createInfo);
	}
    }

    private void populateDebugMessengerCreateInfo(VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo) {
	debugCreateInfo.sType(VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT);
	debugCreateInfo.messageSeverity(VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT
		| VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT);
	debugCreateInfo.messageType(VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT
		| VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT | VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT);
	debugCreateInfo.pfnUserCallback(DefaultVulkanSetup::debugCallback);
    }

    private void setupDebugMessenger() {

	if (!ENABLE_VALIDATION_LAYERS) {
	    return;
	}

	try (MemoryStack stack = stackPush()) {

	    VkDebugUtilsMessengerCreateInfoEXT createInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack);

	    populateDebugMessengerCreateInfo(createInfo);

	    LongBuffer pDebugMessenger = stack.longs(VK_NULL_HANDLE);

	    if (createDebugUtilsMessengerEXT(instance, createInfo, null, pDebugMessenger) != VK_SUCCESS) {
		throw new RuntimeException("Failed to set up debug messenger");
	    }

	    debugMessenger = pDebugMessenger.get(0);
	}
    }

    private void createSurface() {

	try (MemoryStack stack = stackPush()) {

	    LongBuffer pSurface = stack.longs(VK_NULL_HANDLE);

	    if (glfwCreateWindowSurface(instance, window, null, pSurface) != VK_SUCCESS) {
		throw new RuntimeException("Failed to create window surface");
	    }

	    surface = pSurface.get(0);
	    QueueFamilyIndices.setSurface(surface);
	}
    }

    private PointerBuffer getRequiredExtensions(MemoryStack stack) {

	PointerBuffer glfwExtensions = glfwGetRequiredInstanceExtensions();

	if (ENABLE_VALIDATION_LAYERS) {

	    PointerBuffer extensions = stack.mallocPointer(glfwExtensions.capacity() + 1);

	    extensions.put(glfwExtensions);
	    extensions.put(stack.UTF8(VK_EXT_DEBUG_UTILS_EXTENSION_NAME));

	    // Rewind the buffer before returning it to reset its position back to 0
	    return extensions.rewind();
	}

	return glfwExtensions;
    }

    private boolean checkValidationLayerSupport() {

	try (MemoryStack stack = stackPush()) {

	    IntBuffer layerCount = stack.ints(0);

	    vkEnumerateInstanceLayerProperties(layerCount, null);

	    VkLayerProperties.Buffer availableLayers = VkLayerProperties.malloc(layerCount.get(0), stack);

	    vkEnumerateInstanceLayerProperties(layerCount, availableLayers);

	    Set<String> availableLayerNames = availableLayers.stream().map(VkLayerProperties::layerNameString)
		    .collect(toSet());

	    return availableLayerNames.containsAll(VALIDATION_LAYERS);
	}
    }

    public VkInstance getInstance() {
	return instance;
    }

    @Override
    public void cleanup() {

	if (ENABLE_VALIDATION_LAYERS) {
	    destroyDebugUtilsMessengerEXT(instance, debugMessenger, null);
	}

	vkDestroySurfaceKHR(instance, surface, null);

	vkDestroyInstance(instance, null);

	glfwDestroyWindow(window);

	glfwTerminate();

    }

    public long getWindow() {
        return window;
    }

    public void setWindow(long window) {
        this.window = window;
    }

    public long getSurface() {
        return surface;
    }

    public void setSurface(long surface) {
        this.surface = surface;
    }

}
