package vulkan;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.system.MemoryStack.stackGet;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_PRESENT_MODE_FIFO_KHR;
import static org.lwjgl.vulkan.KHRSurface.VK_PRESENT_MODE_MAILBOX_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.VK_SUBOPTIMAL_KHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkAcquireNextImageKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkCreateSwapchainKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkDestroySwapchainKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkGetSwapchainImagesKHR;
import static org.lwjgl.vulkan.KHRSwapchain.vkQueuePresentKHR;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_COLOR_ATTACHMENT_READ_BIT;
import static org.lwjgl.vulkan.VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_LOAD_OP_CLEAR;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE;
import static org.lwjgl.vulkan.VK10.VK_ATTACHMENT_STORE_OP_STORE;
import static org.lwjgl.vulkan.VK10.VK_COMPONENT_SWIZZLE_IDENTITY;
import static org.lwjgl.vulkan.VK10.VK_FENCE_CREATE_SIGNALED_BIT;
import static org.lwjgl.vulkan.VK10.VK_FORMAT_B8G8R8_UNORM;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_ASPECT_COLOR_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_UNDEFINED;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_VIEW_TYPE_2D;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_CONCURRENT;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUBPASS_EXTERNAL;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateFence;
import static org.lwjgl.vulkan.VK10.vkCreateFramebuffer;
import static org.lwjgl.vulkan.VK10.vkCreateImageView;
import static org.lwjgl.vulkan.VK10.vkCreateRenderPass;
import static org.lwjgl.vulkan.VK10.vkCreateSemaphore;
import static org.lwjgl.vulkan.VK10.vkDestroyImageView;
import static org.lwjgl.vulkan.VK10.vkDestroyRenderPass;
import static org.lwjgl.vulkan.VK10.vkQueueSubmit;
import static org.lwjgl.vulkan.VK10.vkResetFences;
import static org.lwjgl.vulkan.VK10.vkWaitForFences;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkAttachmentDescription;
import org.lwjgl.vulkan.VkAttachmentReference;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;
import org.lwjgl.vulkan.VkImageViewCreateInfo;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkRenderPassCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSubpassDependency;
import org.lwjgl.vulkan.VkSubpassDescription;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import graph.Frame;
import interfaces.Destructor;

public class FrameBuffer implements Destructor {

    private static final int MAX_FRAMES_IN_FLIGHT = 2;

//    class Frame {
//
//	private final long imageAvailableSemaphore;
//	private final long renderFinishedSemaphore;
//	private final long fence;
//
//	public Frame(long imageAvailableSemaphore, long renderFinishedSemaphore, long fence) {
//	    this.imageAvailableSemaphore = imageAvailableSemaphore;
//	    this.renderFinishedSemaphore = renderFinishedSemaphore;
//	    this.fence = fence;
//	}
//    }

    Device device;
    DefaultVulkanSetup vulkan;

    private long swapChain; // ponteiro para a lista de todas as imagens do swap chain
    private List<Long> swapChainImages; // todas as imagens no swap chain

    private int swapChainImageFormat; // RGBA 8 bits
    private VkExtent2D swapChainExtent; // Largura e altura (imagem 2d)

    private List<Long> swapChainImageViews;
    private List<Long> swapChainFramebuffers;

    private long renderPass;

    private List<Frame> inFlightFrames;
    private Map<Integer, Frame> imagesInFlight;

    private int currentFrame;
    private int imageIndex;
    private int vkResult;

    public FrameBuffer(Device device, DefaultVulkanSetup vulkan) {
	this.vulkan = vulkan;
	this.device = device;
	createSwapChain();
	createImageViews();
	createRenderPass();
	createFramebuffers();
	createSyncObjects();

	currentFrame = -1;
    }

    public FrameBuffer(Device device, DefaultVulkanSetup vulkan, long swapChain, List<Long> swapChainImages,
	    int swapChainImageFormat, VkExtent2D swapChainExtent, List<Long> swapChainImageViews,
	    List<Long> swapChainFramebuffers, long renderPass, List<Frame> inFlightFrames,
	    Map<Integer, Frame> imagesInFlight) {
	this.device = device;
	this.vulkan = vulkan;
	this.swapChain = swapChain;
	this.swapChainImages = swapChainImages;
	this.swapChainImageFormat = swapChainImageFormat;
	this.swapChainExtent = swapChainExtent;
	this.swapChainImageViews = swapChainImageViews;
	this.swapChainFramebuffers = swapChainFramebuffers;
	this.renderPass = renderPass;
	this.inFlightFrames = inFlightFrames;
	this.imagesInFlight = imagesInFlight;
	currentFrame = -1;

    }

    public void test(List<VkCommandBuffer> commandBuffer) {
	try (MemoryStack stack = stackPush()) {
	    currentFrame = (currentFrame + 1) % MAX_FRAMES_IN_FLIGHT;
	    
	    
	    Frame thisFrame = inFlightFrames.get(currentFrame);

	    vkWaitForFences(device.getDevice(), stackGet().longs(thisFrame.fence), true, Prop.UINT64_MAX);
	    IntBuffer pImageIndex = stack.mallocInt(1);

	    int vkResult = vkAcquireNextImageKHR(device.getDevice(), swapChain, Prop.UINT64_MAX,
		    thisFrame.imageAvailableSemaphore, VK_NULL_HANDLE, pImageIndex);

	    if (vkResult == VK_ERROR_OUT_OF_DATE_KHR) {
		// recreateSwapChain();
		return;
	    } else if (vkResult != VK_SUCCESS) {
		throw new RuntimeException("Cannot get image");
	    }

	    imageIndex = pImageIndex.get(0);

	    if (imagesInFlight.containsKey(imageIndex)) {
		vkWaitForFences(device.getDevice(), imagesInFlight.get(imageIndex).fence, true, Prop.UINT64_MAX);
	    }

	    VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
	    submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);

	    submitInfo.waitSemaphoreCount(1);
	    submitInfo.pWaitSemaphores(stackGet().longs(thisFrame.imageAvailableSemaphore));
	    submitInfo.pWaitDstStageMask(stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT));

	    submitInfo.pSignalSemaphores(stackGet().longs(thisFrame.renderFinishedSemaphore));
	    
	    System.out.println(imageIndex);

	    submitInfo.pCommandBuffers(stack.pointers(commandBuffer.get(imageIndex)));

	    vkResetFences(device.getDevice(), thisFrame.fence);

	    if ((vkResult = vkQueueSubmit(device.getGraphicsQueue(), submitInfo, thisFrame.fence)) != VK_SUCCESS) {
		vkResetFences(device.getDevice(), thisFrame.fence);
		throw new RuntimeException("Failed to submit draw command buffer: " + vkResult);
	    }

	    imagesInFlight.put(imageIndex, thisFrame);

	    VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc(stack);
	    presentInfo.sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);

	    presentInfo.pWaitSemaphores(stackGet().longs(thisFrame.renderFinishedSemaphore));

	    presentInfo.swapchainCount(1);
	    presentInfo.pSwapchains(stack.longs(swapChain));

	    presentInfo.pImageIndices(pImageIndex);

	    vkResult = vkQueuePresentKHR(device.getPresentQueue(), presentInfo);

	    if (vkResult == VK_ERROR_OUT_OF_DATE_KHR || vkResult == VK_SUBOPTIMAL_KHR) {
		// framebufferResize = false;
		// recreateSwapChain();
	    } else if (vkResult != VK_SUCCESS) {
		throw new RuntimeException("Failed to present swap chain image");
	    }
	}
    }

    public void nextFrame() {
	try (MemoryStack stack = stackPush()) {
	    currentFrame = (currentFrame + 1) % MAX_FRAMES_IN_FLIGHT;

	    Frame thisFrame = inFlightFrames.get(currentFrame);

	    vkWaitForFences(device.getDevice(), stackGet().longs(thisFrame.fence), true, Prop.UINT64_MAX);
	    IntBuffer pImageIndex = stack.mallocInt(1);

	    int vkResult = vkAcquireNextImageKHR(device.getDevice(), swapChain, Prop.UINT64_MAX,
		    thisFrame.imageAvailableSemaphore, VK_NULL_HANDLE, pImageIndex);

	    if (vkResult == VK_ERROR_OUT_OF_DATE_KHR) {
		// recreateSwapChain();
		return ;
	    } else if (vkResult != VK_SUCCESS) {
		throw new RuntimeException("Cannot get image");
	    }

	    imageIndex = pImageIndex.get(0);

	    if (imagesInFlight.containsKey(imageIndex)) {
		vkWaitForFences(device.getDevice(), imagesInFlight.get(imageIndex).fence, true, Prop.UINT64_MAX);
	    }

	    
	}
    }

    public void apresenta() {
	try (MemoryStack stack = stackPush()) {
	    
	    Frame thisFrame = inFlightFrames.get(currentFrame);
	    IntBuffer pImageIndex = stack.ints(imageIndex);
	    
	    imagesInFlight.put(imageIndex, thisFrame);
	    
	    VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc(stack);
	    presentInfo.sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);

	    presentInfo.pWaitSemaphores(stackGet().longs(thisFrame.renderFinishedSemaphore));

	    presentInfo.swapchainCount(1);
	    presentInfo.pSwapchains(stack.longs(swapChain));

	    presentInfo.pImageIndices(pImageIndex);

	    vkResult = vkQueuePresentKHR(device.getPresentQueue(), presentInfo);

	    if (vkResult == VK_ERROR_OUT_OF_DATE_KHR || vkResult == VK_SUBOPTIMAL_KHR) {
		// framebufferResize = false;
		// recreateSwapChain();
	    } else if (vkResult != VK_SUCCESS) {
		throw new RuntimeException("Failed to present swap chain image");
	    }
	}
    }

    private void createFramebuffers() {

	swapChainFramebuffers = new ArrayList<>(swapChainImageViews.size());

	try (MemoryStack stack = stackPush()) {

	    LongBuffer attachments = stack.mallocLong(1);
	    LongBuffer pFramebuffer = stack.mallocLong(1);

	    // Lets allocate the create info struct once and just update the pAttachments
	    // field each iteration
	    VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.calloc(stack);
	    framebufferInfo.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
	    framebufferInfo.renderPass(renderPass);
	    framebufferInfo.width(swapChainExtent.width());
	    framebufferInfo.height(swapChainExtent.height());
	    framebufferInfo.layers(1);

	    for (long imageView : swapChainImageViews) {

		attachments.put(0, imageView);

		framebufferInfo.pAttachments(attachments);

		if (vkCreateFramebuffer(device.getDevice(), framebufferInfo, null, pFramebuffer) != VK_SUCCESS) {
		    throw new RuntimeException("Failed to create framebuffer");
		}

		swapChainFramebuffers.add(pFramebuffer.get(0));
	    }
	}
    }

    private void createSwapChain() {

	try (MemoryStack stack = stackPush()) {

	    SwapChainSupportDetails swapChainSupport = SwapChainSupportDetails
		    .querySwapChainSupport(device.getPhysicalDevice(), stack);

	    VkSurfaceFormatKHR surfaceFormat = chooseSwapSurfaceFormat(swapChainSupport.formats);
	    int presentMode = chooseSwapPresentMode(swapChainSupport.presentModes);
	    VkExtent2D extent = chooseSwapExtent(stack, swapChainSupport.capabilities);

	    IntBuffer imageCount = stack.ints(swapChainSupport.capabilities.minImageCount() + 1);

	    if (swapChainSupport.capabilities.maxImageCount() > 0
		    && imageCount.get(0) > swapChainSupport.capabilities.maxImageCount()) {
		imageCount.put(0, swapChainSupport.capabilities.maxImageCount());
	    }

	    VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.calloc(stack);

	    createInfo.sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR);
	    createInfo.surface(vulkan.getSurface());

	    // Image settings
	    createInfo.minImageCount(imageCount.get(0));
	    createInfo.imageFormat(surfaceFormat.format());
	    createInfo.imageColorSpace(surfaceFormat.colorSpace());
	    createInfo.imageExtent(extent);
	    createInfo.imageArrayLayers(1);
	    createInfo.imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);

	    QueueFamilyIndices indices = QueueFamilyIndices.findQueueFamilies(device.getPhysicalDevice());

	    if (!indices.graphicsFamily.equals(indices.presentFamily)) {
		createInfo.imageSharingMode(VK_SHARING_MODE_CONCURRENT);
		createInfo.pQueueFamilyIndices(stack.ints(indices.graphicsFamily, indices.presentFamily));
	    } else {
		createInfo.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE);
	    }

	    createInfo.preTransform(swapChainSupport.capabilities.currentTransform());
	    createInfo.compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
	    createInfo.presentMode(presentMode);
	    createInfo.clipped(true);

	    createInfo.oldSwapchain(VK_NULL_HANDLE);

	    LongBuffer pSwapChain = stack.longs(VK_NULL_HANDLE);

	    if (vkCreateSwapchainKHR(device.getDevice(), createInfo, null, pSwapChain) != VK_SUCCESS) {
		throw new RuntimeException("Failed to create swap chain");
	    }

	    swapChain = pSwapChain.get(0);

	    vkGetSwapchainImagesKHR(device.getDevice(), swapChain, imageCount, null);

	    LongBuffer pSwapchainImages = stack.mallocLong(imageCount.get(0));

	    vkGetSwapchainImagesKHR(device.getDevice(), swapChain, imageCount, pSwapchainImages);

	    swapChainImages = new ArrayList<>(imageCount.get(0));

	    for (int i = 0; i < pSwapchainImages.capacity(); i++) {
		swapChainImages.add(pSwapchainImages.get(i));
	    }

	    swapChainImageFormat = surfaceFormat.format();
	    swapChainExtent = VkExtent2D.create().set(extent);
	}
    }

    private void createImageViews() {

	swapChainImageViews = new ArrayList<>(swapChainImages.size());

	try (MemoryStack stack = stackPush()) {

	    LongBuffer pImageView = stack.mallocLong(1);

	    for (long swapChainImage : swapChainImages) {

		VkImageViewCreateInfo createInfo = VkImageViewCreateInfo.calloc(stack);

		createInfo.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
		createInfo.image(swapChainImage);
		createInfo.viewType(VK_IMAGE_VIEW_TYPE_2D);
		createInfo.format(swapChainImageFormat);

		createInfo.components().r(VK_COMPONENT_SWIZZLE_IDENTITY);
		createInfo.components().g(VK_COMPONENT_SWIZZLE_IDENTITY);
		createInfo.components().b(VK_COMPONENT_SWIZZLE_IDENTITY);
		createInfo.components().a(VK_COMPONENT_SWIZZLE_IDENTITY);

		createInfo.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
		createInfo.subresourceRange().baseMipLevel(0);
		createInfo.subresourceRange().levelCount(1);
		createInfo.subresourceRange().baseArrayLayer(0);
		createInfo.subresourceRange().layerCount(1);

		if (vkCreateImageView(device.getDevice(), createInfo, null, pImageView) != VK_SUCCESS) {
		    throw new RuntimeException("Failed to create image views");
		}

		swapChainImageViews.add(pImageView.get(0));
	    }

	}
    }

    private void createRenderPass() {

	try (MemoryStack stack = stackPush()) {

	    VkAttachmentDescription.Buffer colorAttachment = VkAttachmentDescription.calloc(1, stack);
	    colorAttachment.format(swapChainImageFormat);
	    colorAttachment.samples(VK_SAMPLE_COUNT_1_BIT);
	    colorAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
	    colorAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
	    colorAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
	    colorAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
	    colorAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
	    colorAttachment.finalLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);

	    VkAttachmentReference.Buffer colorAttachmentRef = VkAttachmentReference.calloc(1, stack);
	    colorAttachmentRef.attachment(0);
	    colorAttachmentRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

	    VkSubpassDescription.Buffer subpass = VkSubpassDescription.calloc(1, stack);
	    subpass.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS);
	    subpass.colorAttachmentCount(1);
	    subpass.pColorAttachments(colorAttachmentRef);

	    VkSubpassDependency.Buffer dependency = VkSubpassDependency.calloc(1, stack);
	    dependency.srcSubpass(VK_SUBPASS_EXTERNAL);
	    dependency.dstSubpass(0);
	    dependency.srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
	    dependency.srcAccessMask(0);
	    dependency.dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
	    dependency.dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);

	    VkRenderPassCreateInfo renderPassInfo = VkRenderPassCreateInfo.calloc(stack);
	    renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
	    renderPassInfo.pAttachments(colorAttachment);
	    renderPassInfo.pSubpasses(subpass);
	    renderPassInfo.pDependencies(dependency);

	    LongBuffer pRenderPass = stack.mallocLong(1);

	    if (vkCreateRenderPass(device.getDevice(), renderPassInfo, null, pRenderPass) != VK_SUCCESS) {
		throw new RuntimeException("Failed to create render pass");
	    }

	    renderPass = pRenderPass.get(0);
	}
    }

    private void createSyncObjects() {

	inFlightFrames = new ArrayList<>(MAX_FRAMES_IN_FLIGHT);
	imagesInFlight = new HashMap<>(swapChainImages.size());

	try (MemoryStack stack = stackPush()) {

	    VkSemaphoreCreateInfo semaphoreInfo = VkSemaphoreCreateInfo.callocStack(stack);
	    semaphoreInfo.sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);

	    VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.callocStack(stack);
	    fenceInfo.sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
	    fenceInfo.flags(VK_FENCE_CREATE_SIGNALED_BIT);

	    LongBuffer pImageAvailableSemaphore = stack.mallocLong(1);
	    LongBuffer pRenderFinishedSemaphore = stack.mallocLong(1);
	    LongBuffer pFence = stack.mallocLong(1);

	    for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {

		if (vkCreateSemaphore(device.getDevice(), semaphoreInfo, null, pImageAvailableSemaphore) != VK_SUCCESS
			|| vkCreateSemaphore(device.getDevice(), semaphoreInfo, null,
				pRenderFinishedSemaphore) != VK_SUCCESS
			|| vkCreateFence(device.getDevice(), fenceInfo, null, pFence) != VK_SUCCESS) {

		    throw new RuntimeException("Failed to create synchronization objects for the frame " + i);
		}

		inFlightFrames.add(
			new Frame(pImageAvailableSemaphore.get(0), pRenderFinishedSemaphore.get(0), pFence.get(0)));
	    }

	}
    }

    private VkSurfaceFormatKHR chooseSwapSurfaceFormat(VkSurfaceFormatKHR.Buffer availableFormats) {
	return availableFormats.stream().filter(availableFormat -> availableFormat.format() == VK_FORMAT_B8G8R8_UNORM)
		.filter(availableFormat -> availableFormat.colorSpace() == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR).findAny()
		.orElse(availableFormats.get(0));
    }

    private int chooseSwapPresentMode(IntBuffer availablePresentModes) {

	for (int i = 0; i < availablePresentModes.capacity(); i++) {
	    if (availablePresentModes.get(i) == VK_PRESENT_MODE_MAILBOX_KHR) {
		return availablePresentModes.get(i);
	    }
	}

	return VK_PRESENT_MODE_FIFO_KHR;
    }

    private VkExtent2D chooseSwapExtent(MemoryStack stack, VkSurfaceCapabilitiesKHR capabilities) {

	if (capabilities.currentExtent().width() != Prop.UINT32_MAX) {
	    return capabilities.currentExtent();
	}

	IntBuffer width = stackGet().ints(0);
	IntBuffer height = stackGet().ints(0);

	glfwGetFramebufferSize(vulkan.getWindow(), width, height);

	VkExtent2D actualExtent = VkExtent2D.malloc(stack).set(width.get(0), height.get(0));

	VkExtent2D minExtent = capabilities.minImageExtent();
	VkExtent2D maxExtent = capabilities.maxImageExtent();

	actualExtent.width(clamp(minExtent.width(), maxExtent.width(), actualExtent.width()));
	actualExtent.height(clamp(minExtent.height(), maxExtent.height(), actualExtent.height()));

	return actualExtent;
    }

    private int clamp(int min, int max, int value) {
	return Math.max(min, Math.min(max, value));
    }
    
    public Frame GetSync() {
	return inFlightFrames.get(currentFrame);
    }

    public int getImageIndex() {
        return imageIndex;
    }

    public void setImageIndex(int imageIndex) {
        this.imageIndex = imageIndex;
    }

    public long getSwapChain() {
	return swapChain;
    }

    public void setSwapChain(long swapChain) {
	this.swapChain = swapChain;
    }

    public List<Long> getSwapChainImages() {
	return swapChainImages;
    }

    public void setSwapChainImages(List<Long> swapChainImages) {
	this.swapChainImages = swapChainImages;
    }

    public int getSwapChainImageFormat() {
	return swapChainImageFormat;
    }

    public void setSwapChainImageFormat(int swapChainImageFormat) {
	this.swapChainImageFormat = swapChainImageFormat;
    }

    public VkExtent2D getSwapChainExtent() {
	return swapChainExtent;
    }

    public void setSwapChainExtent(VkExtent2D swapChainExtent) {
	this.swapChainExtent = swapChainExtent;
    }

    public List<Long> getSwapChainImageViews() {
	return swapChainImageViews;
    }

    public void setSwapChainImageViews(List<Long> swapChainImageViews) {
	this.swapChainImageViews = swapChainImageViews;
    }

    public List<Long> getSwapChainFramebuffers() {
	return swapChainFramebuffers;
    }

    public void setSwapChainFramebuffers(List<Long> swapChainFramebuffers) {
	this.swapChainFramebuffers = swapChainFramebuffers;
    }

    public long getRenderPass() {
	return renderPass;
    }

    public void setRenderPass(long renderPass) {
	this.renderPass = renderPass;
    }
    public List<Frame> getInFlightFrames() {
        return inFlightFrames;
    }

    public void setInFlightFrames(List<Frame> inFlightFrames) {
        this.inFlightFrames = inFlightFrames;
    }

    public Map<Integer, Frame> getImagesInFlight() {
        return imagesInFlight;
    }

    public void setImagesInFlight(Map<Integer, Frame> imagesInFlight) {
        this.imagesInFlight = imagesInFlight;
    }

    @Override
    public void cleanup() {
	vkDestroyRenderPass(device.getDevice(), renderPass, null);

	swapChainImageViews.forEach(imageView -> vkDestroyImageView(device.getDevice(), imageView, null));

	vkDestroySwapchainKHR(device.getDevice(), swapChain, null);

    }

}
