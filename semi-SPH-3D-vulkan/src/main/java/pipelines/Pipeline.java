package pipelines;

import static graph.ShaderSPIRVUtils.compileShaderFile;
import static graph.ShaderSPIRVUtils.ShaderKind.COMPUTE_SHADER;
import static graph.ShaderSPIRVUtils.ShaderKind.FRAGMENT_SHADER;
import static graph.ShaderSPIRVUtils.ShaderKind.VERTEX_SHADER;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.vulkan.VK10.VK_BLEND_FACTOR_SRC_ALPHA;
import static org.lwjgl.vulkan.VK10.VK_BLEND_FACTOR_ZERO;
import static org.lwjgl.vulkan.VK10.VK_BLEND_OP_ADD;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_A_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_B_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_G_BIT;
import static org.lwjgl.vulkan.VK10.VK_COLOR_COMPONENT_R_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY;
import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_BACK_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_FENCE_CREATE_SIGNALED_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_FRONT_FACE_COUNTER_CLOCKWISE;
import static org.lwjgl.vulkan.VK10.VK_INDEX_TYPE_UINT32;
import static org.lwjgl.vulkan.VK10.VK_INDEX_TYPE_UINT16;
import static org.lwjgl.vulkan.VK10.VK_LOGIC_OP_COPY;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_GRAPHICS;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
import static org.lwjgl.vulkan.VK10.VK_POLYGON_MODE_FILL;
import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_COMPUTE_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMPUTE_PIPELINE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUBPASS_CONTENTS_INLINE;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;
import static org.lwjgl.vulkan.VK10.vkAllocateCommandBuffers;
import static org.lwjgl.vulkan.VK10.vkAllocateDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkBeginCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBeginRenderPass;
import static org.lwjgl.vulkan.VK10.vkCmdBindDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkCmdBindIndexBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBindPipeline;
import static org.lwjgl.vulkan.VK10.vkCmdBindVertexBuffers;
import static org.lwjgl.vulkan.VK10.vkCmdDrawIndexed;
import static org.lwjgl.vulkan.VK10.vkCmdDraw;
import static org.lwjgl.vulkan.VK10.vkCmdEndRenderPass;
import static org.lwjgl.vulkan.VK10.vkCreateComputePipelines;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorPool;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorSetLayout;
import static org.lwjgl.vulkan.VK10.vkCreateFence;
import static org.lwjgl.vulkan.VK10.vkCreateGraphicsPipelines;
import static org.lwjgl.vulkan.VK10.vkCreatePipelineLayout;
import static org.lwjgl.vulkan.VK10.vkCreateSemaphore;
import static org.lwjgl.vulkan.VK10.vkCreateShaderModule;
import static org.lwjgl.vulkan.VK10.vkDestroyDescriptorPool;
import static org.lwjgl.vulkan.VK10.vkDestroyDescriptorSetLayout;
import static org.lwjgl.vulkan.VK10.vkDestroyPipeline;
import static org.lwjgl.vulkan.VK10.vkDestroyPipelineLayout;
import static org.lwjgl.vulkan.VK10.vkDestroyShaderModule;
import static org.lwjgl.vulkan.VK10.vkEndCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkQueueSubmit;
import static org.lwjgl.vulkan.VK10.vkResetFences;
import static org.lwjgl.vulkan.VK10.vkUpdateDescriptorSets;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkComputePipelineCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkOffset2D;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkPipelineViewportStateCreateInfo;
import org.lwjgl.vulkan.VkRect2D;
import org.lwjgl.vulkan.VkRenderPassBeginInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import org.lwjgl.vulkan.VkViewport;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import buffers.Buffer;
import buffers.Layout.Elements;
import buffers.VertexBuffer;
import command.ComputeShader;
import command.FragmentShader;
import command.Shader;
import command.VertexShader;
import graph.Frame;
import graph.ShaderSPIRVUtils.SPIRV;
import interfaces.Destructor;
import vulkan.Device;
import vulkan.FrameBuffer;
import vulkan.Prop;

public class Pipeline implements Destructor {

    Device device;

    private long descriptorSetLayout;
    long pipelineLayout;
    long pipeline;

    public List<Shader> shaders;
    int numAtrib;
    int numSwaps;

    private long descriptorPool;

    List<Long> descriptorSets;
    List<VkCommandBuffer> commandBuffers;

    Frame sync;

    int countSwaps;

    public Pipeline(Device device, FrameBuffer framebuffer, long descriptorSetLayout, long pipelineLayout,
	    long pipeline, long descriptorPool, List<Long> descriptorSets, List<VkCommandBuffer> commandBuffers) {
	this.device = device;
	this.shaders = new LinkedList<>();
	this.descriptorSetLayout = descriptorSetLayout;
	this.pipelineLayout = pipelineLayout;
	this.pipeline = pipeline;
	this.descriptorPool = descriptorPool;
	this.descriptorSets = descriptorSets;
	this.commandBuffers = commandBuffers;

	countSwaps = 0;

	this.numSwaps = 5;
	this.numAtrib = 1;

    }

    Pipeline(Device device) {
	this.device = device;
	this.shaders = new LinkedList<>();
	createSyncObjects();
	countSwaps = 0;

	this.numSwaps = 1;

    }

    Pipeline() {

    }


    public void submitCommand(int indexImag) {
	try (MemoryStack stack = stackPush()) {

	    VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
	    submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);

	    submitInfo.waitSemaphoreCount(0);
	    // submitInfo.pWaitSemaphores(thisFrame.pImageAvailableSemaphore());
	    submitInfo.pWaitDstStageMask(stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT));

	    // submitInfo.pSignalSemaphores(thisFrame.pRenderFinishedSemaphore());

	    submitInfo.pCommandBuffers(stack.pointers(commandBuffers.get(countSwaps + indexImag)));

	    if (vkQueueSubmit(device.getGraphicsQueue(), submitInfo, VK_NULL_HANDLE) != VK_SUCCESS) {
		// vkResetFences(device, thisFrame.pFence());
		throw new RuntimeException("Failed to submit draw command buffer: ");
	    }

	    // countSwaps = (countSwaps + framebuffer.getSwapChainFramebuffers().size()) %
	    // numSwaps;
	}
    }

    public void setupPipelineDescriptorSetLayout() {
	try (MemoryStack stack = stackPush()) {
	    int bufferLayoutSize = 0;

	    for (int i = 0; i < shaders.size(); i++) {

		bufferLayoutSize += shaders.get(i).getbuffersLayoutSize();
	    }

	    VkDescriptorSetLayoutBinding.Buffer layoutBindings = VkDescriptorSetLayoutBinding.calloc(bufferLayoutSize,
		    stack);

	    int atual = 0;
	    for (int i = 0; i < shaders.size(); i++) {
		atual = shaders.get(i).createDescriptorSetLayout(layoutBindings, stack, atual);
	    }

	    VkDescriptorSetLayoutCreateInfo layoutInfo = VkDescriptorSetLayoutCreateInfo.calloc(stack);

	    layoutInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);

	    layoutInfo.pBindings(layoutBindings);

	    LongBuffer pDescriptorSetLayout = stack.mallocLong(1);

	    if (vkCreateDescriptorSetLayout(device.getDevice(), layoutInfo, null, pDescriptorSetLayout) != VK_SUCCESS) {
		throw new RuntimeException("Failed to create descriptor set layout");
	    }

	    descriptorSetLayout = pDescriptorSetLayout.get(0);

	    VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack);
	    pipelineLayoutInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
	    pipelineLayoutInfo.pSetLayouts(stack.longs(descriptorSetLayout));

	    LongBuffer pPipelineLayout = stack.longs(VK_NULL_HANDLE);

	    if (vkCreatePipelineLayout(device.getDevice(), pipelineLayoutInfo, null, pPipelineLayout) != VK_SUCCESS) {
		throw new RuntimeException("Failed to create pipeline layout");
	    }

	    pipelineLayout = pPipelineLayout.get(0);

	}
    }

    public void createDescriptorPool() {

	int numSSBO = 0;
	int numUniforms = 0;

	for (int i = 0; i < shaders.size(); i++) {
	    numUniforms += shaders.get(i).getNumUniforms();
	    numSSBO += shaders.get(i).getNumSSBO();
	    this.numSwaps = Prop.mmc(this.numSwaps, shaders.get(i).getNumSwapsLayouts());
	}
	this.numAtrib = numSSBO + numUniforms;

	int numDescritores = 0;
	if (numSSBO > 0)
	    numDescritores++;
	if (numUniforms > 0)
	    numDescritores++;

	try (MemoryStack stack = stackPush()) {
	    VkDescriptorPoolSize.Buffer poolSizes = VkDescriptorPoolSize.calloc(numDescritores, stack);
	    int position = 0;
	    if (numUniforms > 0) {
		System.out.println("uni " + numUniforms + " swap " + numSwaps);
		VkDescriptorPoolSize poolSize = poolSizes.get(position);
		poolSize.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
		poolSize.descriptorCount(numUniforms * numSwaps);
		position++;
	    }
	    if (numSSBO > 0) {
		System.out.println("SSBO " + numSSBO + " swap " + numSwaps);
		VkDescriptorPoolSize poolSize1 = poolSizes.get(position);
		poolSize1.type(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER);
		poolSize1.descriptorCount(numSSBO * numSwaps);
		position++;
	    }

	    VkDescriptorPoolCreateInfo poolInfo = VkDescriptorPoolCreateInfo.calloc(stack);
	    poolInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO);
	    poolInfo.pPoolSizes(poolSizes);
	    poolInfo.maxSets(numSwaps);

	    LongBuffer pDescriptorPool = stack.mallocLong(1);

	    if (vkCreateDescriptorPool(device.getDevice(), poolInfo, null, pDescriptorPool) != VK_SUCCESS) {
		throw new RuntimeException("Failed to create descriptor pool");
	    }

	    descriptorPool = pDescriptorPool.get(0);
	}
    }

    public void createDescriptorSets() {

	try (MemoryStack stack = stackPush()) {

	    LongBuffer layouts = stack.mallocLong(numSwaps);

	    for (int i = 0; i < layouts.capacity(); i++) {
		layouts.put(i, descriptorSetLayout);
	    }

	    VkDescriptorSetAllocateInfo allocInfo = VkDescriptorSetAllocateInfo.calloc(stack);
	    allocInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO);
	    allocInfo.descriptorPool(descriptorPool);
	    allocInfo.pSetLayouts(layouts);

	    LongBuffer pDescriptorSets = stack.mallocLong(numSwaps);

	    if (vkAllocateDescriptorSets(device.getDevice(), allocInfo, pDescriptorSets) != VK_SUCCESS) {
		throw new RuntimeException("Failed to allocate descriptor sets");
	    }

	    descriptorSets = new ArrayList<>();
	    for (int i = 0; i < numSwaps; i++) {

		VkWriteDescriptorSet.Buffer descriptorWrite = VkWriteDescriptorSet.calloc(numAtrib, stack);

		long descriptorSet = pDescriptorSets.get(i);

		int next = 0;
		for (int j = 0; j < shaders.size(); j++) {

		    next = shaders.get(j).setupDescriptorSet(descriptorWrite, stack, descriptorSet, next, i);

		}

		vkUpdateDescriptorSets(device.getDevice(), descriptorWrite, null);

		descriptorSets.add(descriptorSet);

	    }

	}
    }

    private void createSyncObjects() {

	try (MemoryStack stack = stackPush()) {

	    VkSemaphoreCreateInfo semaphoreInfo = VkSemaphoreCreateInfo.calloc(stack);
	    semaphoreInfo.sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);

	    VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.calloc(stack);
	    fenceInfo.sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
	    fenceInfo.flags(VK_FENCE_CREATE_SIGNALED_BIT);

	    LongBuffer pImageAvailableSemaphore = stack.mallocLong(1);
	    LongBuffer pRenderFinishedSemaphore = stack.mallocLong(1);
	    LongBuffer pFence = stack.mallocLong(1);

	    if (vkCreateSemaphore(device.getDevice(), semaphoreInfo, null, pImageAvailableSemaphore) != VK_SUCCESS
		    || vkCreateSemaphore(device.getDevice(), semaphoreInfo, null,
			    pRenderFinishedSemaphore) != VK_SUCCESS
		    || vkCreateFence(device.getDevice(), fenceInfo, null, pFence) != VK_SUCCESS) {

		throw new RuntimeException("Failed to create synchronization objects for the pipeline");
	    }

	    sync = new Frame(pImageAvailableSemaphore.get(0), pRenderFinishedSemaphore.get(0), pFence.get(0));

	}
    }

    long createShaderModule(ByteBuffer spirvCode) {

	try (MemoryStack stack = stackPush()) {

	    VkShaderModuleCreateInfo createInfo = VkShaderModuleCreateInfo.calloc(stack);

	    createInfo.sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO);
	    createInfo.pCode(spirvCode);

	    LongBuffer pShaderModule = stack.mallocLong(1);

	    if (vkCreateShaderModule(device.getDevice(), createInfo, null, pShaderModule) != VK_SUCCESS) {
		throw new RuntimeException("Failed to create shader module");
	    }

	    return pShaderModule.get(0);
	}
    }

    VkVertexInputBindingDescription.Buffer getBindingDescription(MemoryStack stack, VertexBuffer vertexs) {

	VkVertexInputBindingDescription.Buffer bindingDescription = VkVertexInputBindingDescription.calloc(1, stack);

	bindingDescription.binding(0);
	bindingDescription.stride(vertexs.getLayout().getStride());
	bindingDescription.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

	return bindingDescription;
    }

    VkVertexInputAttributeDescription.Buffer getAttributeDescriptions(MemoryStack stack, VertexBuffer vertexBuffer) {

	List<Elements> e = vertexBuffer.getLayout().getDescriptors();

	VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription
		.calloc(e.size());
	int offset = 0;
	for (int i = 0; i < e.size(); i++) {
	    VkVertexInputAttributeDescription posDescription = attributeDescriptions.get(i);
	    posDescription.binding(0);
	    posDescription.location(i);
	    posDescription.format(e.get(i).getFormat());
	    posDescription.offset(offset);
	    offset += e.get(i).getSize() * e.get(i).getCount();
	}

	return attributeDescriptions.rewind();
    }

    public List<VkCommandBuffer> getCommandBuffer() {
	return commandBuffers;
    }

    public Frame getSync() {
	return sync;
    }
    public long getDescriptorSetLayout() {
	return descriptorSetLayout;
    }

    public void setDescriptorSetLayout(long descriptorSetLayout) {
	this.descriptorSetLayout = descriptorSetLayout;
    }

    public long getDescriptorPool() {
	return descriptorPool;
    }

    public void setDescriptorPool(long descriptorPool) {
	this.descriptorPool = descriptorPool;
    }

    public List<Long> getDescriptorSets() {
	return descriptorSets;
    }

    public void setDescriptorSets(List<Long> descriptorSets) {
	this.descriptorSets = descriptorSets;
    }

    public List<VkCommandBuffer> getCommandBuffers() {
	return commandBuffers;
    }

    public void setCommandBuffers(List<VkCommandBuffer> commandBuffers) {
	this.commandBuffers = commandBuffers;
    }

    @Override
    public void cleanup() {

	vkDestroyDescriptorSetLayout(device.getDevice(), descriptorSetLayout, null);

	vkDestroyDescriptorPool(device.getDevice(), descriptorPool, null);

	vkDestroyPipeline(device.getDevice(), pipeline, null);

	vkDestroyPipelineLayout(device.getDevice(), pipelineLayout, null);
    }

}
