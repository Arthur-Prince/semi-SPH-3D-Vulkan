package pipelines;

import static graph.ShaderSPIRVUtils.compileShaderFile;
import static graph.ShaderSPIRVUtils.ShaderKind.COMPUTE_SHADER;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_BIND_POINT_COMPUTE;
import static org.lwjgl.vulkan.VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_COMPUTE_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMPUTE_PIPELINE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateCommandBuffers;
import static org.lwjgl.vulkan.VK10.vkBeginCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkCmdBindDescriptorSets;
import static org.lwjgl.vulkan.VK10.vkCmdBindPipeline;
import static org.lwjgl.vulkan.VK10.vkCmdDispatch;
import static org.lwjgl.vulkan.VK10.vkCreateComputePipelines;
import static org.lwjgl.vulkan.VK10.vkCreatePipelineLayout;
import static org.lwjgl.vulkan.VK10.vkDestroyShaderModule;
import static org.lwjgl.vulkan.VK10.vkEndCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkQueueSubmit;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkComputePipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;

import command.ComputeShader;
import graph.Frame;
import graph.ShaderSPIRVUtils.SPIRV;
import vulkan.Device;
import vulkan.FrameBuffer;

public class ComputePipeline extends Pipeline {

    ComputeShader computeShader;

    List<VkCommandBuffer> commandBuffers;
    
    private int[] workGroup;

    public ComputePipeline(Device device, ComputeShader computeShader) {
	super(device);
	this.computeShader = computeShader;

	shaders.add(computeShader);
	this.workGroup = new int[] {1,1,1};

	createComputePipeline(computeShader);
	
	createDescriptorPool();
	createDescriptorSets();
	createComputeCommandBuffers();
	
	

    }
    
    public ComputePipeline(Device device, ComputeShader computeShader,int workGroupX, int workGroupY, int workGroupZ) {
	super(device);
	this.computeShader = computeShader;

	shaders.add(computeShader);
	this.workGroup = new int[] {workGroupX,workGroupY,workGroupZ};

	createComputePipeline(computeShader);
	
	createDescriptorPool();
	createDescriptorSets();
	createComputeCommandBuffers();
	
	

    }
    
    public void submitCommand(Pipeline anterior) {
	try (MemoryStack stack = stackPush()) {

	    VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
	    submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);
	    
	    //LongBuffer semaphores = stack.longs(VK_SUCCESS);

	    submitInfo.pWaitSemaphores(anterior.getSync().pRenderFinishedSemaphore());
	    submitInfo.pSignalSemaphores(sync.pRenderFinishedSemaphore());

	    submitInfo.pCommandBuffers(stack.pointers(commandBuffers.get(countSwaps)));

	    if (vkQueueSubmit(device.getGraphicsQueue(), submitInfo, this.sync.fence) != VK_SUCCESS) {
		// vkResetFences(device, thisFrame.pFence());
		throw new RuntimeException("Failed to submit compute command buffer: ");
	    }

	    countSwaps = (countSwaps + 1) % numSwaps;
	}
    }

    void createComputePipeline(ComputeShader computeShader) {
	try (MemoryStack stack = stackPush()) {
	    SPIRV compShaderSPIRV = compileShaderFile(computeShader.getShaderPath(), COMPUTE_SHADER);

	    long compShaderModule = createShaderModule(compShaderSPIRV.bytecode());

	    ByteBuffer entryPoint = stack.UTF8("main");

	    VkPipelineShaderStageCreateInfo.Buffer computeShaderStageInfo = VkPipelineShaderStageCreateInfo.calloc(1,
		    stack);

	    computeShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
	    computeShaderStageInfo.stage(VK_SHADER_STAGE_COMPUTE_BIT);
	    computeShaderStageInfo.module(compShaderModule);
	    computeShaderStageInfo.pName(entryPoint);

	    setupPipelineDescriptorSetLayout();

	    VkComputePipelineCreateInfo.Buffer pipelineInfo = VkComputePipelineCreateInfo.calloc(1, stack);
	    pipelineInfo.sType(VK_STRUCTURE_TYPE_COMPUTE_PIPELINE_CREATE_INFO);
	    pipelineInfo.layout(pipelineLayout);
	    pipelineInfo.stage(computeShaderStageInfo.get(0));

	    LongBuffer pComputePipeline = stack.mallocLong(1);

	    if (vkCreateComputePipelines(device.getDevice(), VK_NULL_HANDLE, pipelineInfo, null,
		    pComputePipeline) != VK_SUCCESS) {
		throw new RuntimeException("Failed to create graphics pipeline");
	    }

	    pipeline = pComputePipeline.get(0);

	    vkDestroyShaderModule(device.getDevice(), compShaderModule, null);

	    compShaderSPIRV.free();

	}
    }

    private void createComputeCommandBuffers() {
	final int commandBuffersCount = numSwaps;

	commandBuffers = new ArrayList<>(commandBuffersCount);
	try (MemoryStack stack = stackPush()) {

	    VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack);
	    allocInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
	    allocInfo.commandPool(device.getCommandPool());
	    allocInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY);
	    allocInfo.commandBufferCount(commandBuffersCount);

	    PointerBuffer pCommandBuffers = stack.mallocPointer(commandBuffersCount);

	    if (vkAllocateCommandBuffers(device.getDevice(), allocInfo, pCommandBuffers) != VK_SUCCESS) {
		throw new RuntimeException("Failed to allocate command buffers");
	    }

	    for (int i = 0; i < commandBuffersCount; i++) {
		commandBuffers.add(new VkCommandBuffer(pCommandBuffers.get(i), device.getDevice()));
	    }

	    for (int i = 0; i < numSwaps; i++) {
		VkCommandBuffer commandBuffer = commandBuffers.get(i);

		VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack);
		beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
		if (vkBeginCommandBuffer(commandBuffer, beginInfo) != VK_SUCCESS) {
		    throw new RuntimeException("Failed to begin recording command buffer");
		}

		vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_COMPUTE, pipeline);

		vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_COMPUTE, pipelineLayout, 0,
			stack.longs(descriptorSets.get(i)), null);
		vkCmdDispatch(commandBuffer, this.workGroup[0], this.workGroup[1], this.workGroup[2]);

		if (vkEndCommandBuffer(commandBuffer) != VK_SUCCESS) {
		    throw new RuntimeException("Failed to record command buffer");
		}
	    }
	}
    }

    public int[] getWorkGroup() {
	return workGroup;
    }

    public void setWorkGroup(int x, int y, int z) {
	this.workGroup[0]=x;
	this.workGroup[1]=y;
	this.workGroup[2]=z;
    }

    

}
