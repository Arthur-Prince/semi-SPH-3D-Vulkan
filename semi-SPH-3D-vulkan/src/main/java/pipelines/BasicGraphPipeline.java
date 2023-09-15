package pipelines;

import static graph.ShaderSPIRVUtils.compileShaderFile;
import static graph.ShaderSPIRVUtils.ShaderKind.FRAGMENT_SHADER;
import static graph.ShaderSPIRVUtils.ShaderKind.VERTEX_SHADER;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkClearValue;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
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
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkViewport;

import buffers.Buffer;
import command.FragmentShader;
import command.VertexShader;
import graph.Frame;
import graph.ShaderSPIRVUtils.SPIRV;
import vulkan.Device;
import vulkan.FrameBuffer;
import vulkan.Prop;

public class BasicGraphPipeline extends Pipeline {

    private VertexShader vertex;
    private FragmentShader fragment;

    FrameBuffer framebuffer;

    private HashMap<Integer, List<VkCommandBuffer>> commandBuffers;
    
    public enum Topology{
	
	
	Point(VK_PRIMITIVE_TOPOLOGY_POINT_LIST),
	Triangle(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST),
	Line(VK_PRIMITIVE_TOPOLOGY_LINE_LIST),
	LinhaConsecutivas(VK_PRIMITIVE_TOPOLOGY_LINE_STRIP);

	private final int bits;

	Topology (int bits) {
	    this.bits = bits;
	}

	public int getBitValue() {
	    return bits;
	}
    }
    
    

    public BasicGraphPipeline(Device device, FrameBuffer framebuffer, VertexShader vertex, FragmentShader fragment) {
	super(device);
	this.vertex = vertex;
	this.fragment = fragment;
	this.framebuffer = framebuffer;

	shaders.add(vertex);
	shaders.add(fragment);

	createGraphicsPipeline(vertex, fragment, VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST, false);

	createDescriptorPool();
	createDescriptorSets();
	createGraphCommandBuffers(vertex);
	

    }
    
    public BasicGraphPipeline(Device device, FrameBuffer framebuffer, VertexShader vertex, FragmentShader fragment, Topology topology) {
	super(device);
	this.vertex = vertex;
	this.fragment = fragment;
	this.framebuffer = framebuffer;

	shaders.add(vertex);
	shaders.add(fragment);

	createGraphicsPipeline(vertex, fragment, topology.getBitValue(), true);

	createDescriptorPool();
	createDescriptorSets();
	createGraphCommandBuffers(vertex);
	

    }

    public void submitCommand() {
	try (MemoryStack stack = stackPush()) {
	    Frame thisFrame = framebuffer.GetSync();

	    VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
	    submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);

	    submitInfo.waitSemaphoreCount(0);
	    submitInfo.pWaitSemaphores(thisFrame.pImageAvailableSemaphore());
	    submitInfo.pWaitDstStageMask(stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT));

	    submitInfo.pSignalSemaphores(thisFrame.pRenderFinishedSemaphore());

	    submitInfo.pCommandBuffers(stack.pointers(commandBuffers.get(framebuffer.getImageIndex()).get(countSwaps)));

	    if (vkQueueSubmit(device.getGraphicsQueue(), submitInfo, this.sync.fence) != VK_SUCCESS) {
		// vkResetFences(device, thisFrame.pFence());
		throw new RuntimeException("Failed to submit draw command buffer: ");
	    }

	    countSwaps = (countSwaps + 1) % numSwaps;
	}
    }
    
    public void submitCommand(Pipeline anterior) {
	try (MemoryStack stack = stackPush()) {
	    Frame thisFrame = framebuffer.GetSync();

	    VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
	    submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);
	    
	    LongBuffer semaphores = stack.callocLong(2);
	    semaphores.put(thisFrame.pImageAvailableSemaphore());
	    semaphores.put(anterior.getSync().pRenderFinishedSemaphore());

	    submitInfo.waitSemaphoreCount(0);
	    submitInfo.pWaitSemaphores(semaphores);
	    submitInfo.pWaitDstStageMask(stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT));
	    submitInfo.pSignalSemaphores(thisFrame.pRenderFinishedSemaphore());

	    submitInfo.pCommandBuffers(stack.pointers(commandBuffers.get(framebuffer.getImageIndex()).get(countSwaps)));

	    if (vkQueueSubmit(device.getGraphicsQueue(), submitInfo, this.sync.fence) != VK_SUCCESS) {
		// vkResetFences(device, thisFrame.pFence());
		throw new RuntimeException("Failed to submit draw command buffer: ");
	    }

	    countSwaps = (countSwaps + 1) % numSwaps;
	}
    }

    public void createGraphicsPipeline(VertexShader vertex, FragmentShader fragment, int primitiveTopology,
	    boolean blending) {

	// this.numSwaps = Prop.mmc(numSwaps,
	// framebuffer.getSwapChainFramebuffers().size());

	try (MemoryStack stack = stackPush()) {

	    // Let's compile the GLSL shaders into SPIR-V at runtime using the shaderc
	    // library
	    // Check ShaderSPIRVUtils class to see how it can be done
	    SPIRV vertShaderSPIRV = compileShaderFile(vertex.getShaderPath(), VERTEX_SHADER);
	    SPIRV fragShaderSPIRV = compileShaderFile(fragment.getShaderPath(), FRAGMENT_SHADER);

	    long vertShaderModule = createShaderModule(vertShaderSPIRV.bytecode());
	    long fragShaderModule = createShaderModule(fragShaderSPIRV.bytecode());

	    ByteBuffer entryPoint = stack.UTF8("main");

	    VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.calloc(2, stack);

	    VkPipelineShaderStageCreateInfo vertShaderStageInfo = shaderStages.get(0);

	    vertShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
	    vertShaderStageInfo.stage(VK_SHADER_STAGE_VERTEX_BIT);
	    vertShaderStageInfo.module(vertShaderModule);
	    vertShaderStageInfo.pName(entryPoint);

	    VkPipelineShaderStageCreateInfo fragShaderStageInfo = shaderStages.get(1);

	    fragShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
	    fragShaderStageInfo.stage(VK_SHADER_STAGE_FRAGMENT_BIT);
	    fragShaderStageInfo.module(fragShaderModule);
	    fragShaderStageInfo.pName(entryPoint);

	    // ===> VERTEX STAGE <===

	    VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.calloc(stack);
	    vertexInputInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
	    vertexInputInfo.pVertexBindingDescriptions(getBindingDescription(stack, vertex.getVertex()));
	    vertexInputInfo.pVertexAttributeDescriptions(getAttributeDescriptions(stack, vertex.getVertex()));

	    // ===> ASSEMBLY STAGE <===

	    VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo.calloc(stack);
	    inputAssembly.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
	    // pode mudar aqui
	    inputAssembly.topology(primitiveTopology);
	    inputAssembly.primitiveRestartEnable(false);

	    // ===> VIEWPORT & SCISSOR

	    VkViewport.Buffer viewport = VkViewport.calloc(1, stack);
	    viewport.x(0.0f);
	    viewport.y(0.0f);
	    viewport.width(framebuffer.getSwapChainExtent().width());
	    viewport.height(framebuffer.getSwapChainExtent().height());
	    viewport.minDepth(0.0f);
	    viewport.maxDepth(1.0f);

	    VkRect2D.Buffer scissor = VkRect2D.calloc(1, stack);
	    scissor.offset(VkOffset2D.calloc(stack).set(0, 0));
	    scissor.extent(framebuffer.getSwapChainExtent());

	    VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.calloc(stack);
	    viewportState.sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO);
	    viewportState.pViewports(viewport);
	    viewportState.pScissors(scissor);

	    // ===> RASTERIZATION STAGE <===

	    VkPipelineRasterizationStateCreateInfo rasterizer = VkPipelineRasterizationStateCreateInfo.calloc(stack);
	    rasterizer.sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO);
	    rasterizer.depthClampEnable(false);
	    rasterizer.rasterizerDiscardEnable(false);
	    rasterizer.polygonMode(VK_POLYGON_MODE_FILL);
	    rasterizer.lineWidth(1.0f);
	    rasterizer.cullMode(VK_CULL_MODE_BACK_BIT);
	    rasterizer.frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE);
	    rasterizer.depthBiasEnable(false);

	    // ===> MULTISAMPLING <===

	    VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.calloc(stack);
	    multisampling.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
	    multisampling.sampleShadingEnable(false);
	    multisampling.rasterizationSamples(VK_SAMPLE_COUNT_1_BIT);

	    // ===> COLOR BLENDING <===

	    VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachment = VkPipelineColorBlendAttachmentState
		    .calloc(1, stack);
	    colorBlendAttachment.colorWriteMask(VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT
		    | VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT);

	    colorBlendAttachment.colorBlendOp(VK_BLEND_OP_ADD);
	    colorBlendAttachment.srcColorBlendFactor(VK_BLEND_FACTOR_SRC_ALPHA);
	    colorBlendAttachment.dstColorBlendFactor(VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA);
	    colorBlendAttachment.alphaBlendOp(VK_BLEND_OP_ADD);
	    colorBlendAttachment.srcAlphaBlendFactor(VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA);
	    colorBlendAttachment.dstAlphaBlendFactor(VK_BLEND_FACTOR_ZERO);

	    colorBlendAttachment.blendEnable(blending);

	    VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.calloc(stack);
	    colorBlending.sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
	    colorBlending.logicOpEnable(false);
	    colorBlending.logicOp(VK_LOGIC_OP_COPY);
	    colorBlending.pAttachments(colorBlendAttachment);
	    colorBlending.blendConstants(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));

	    // ===> PIPELINE LAYOUT CREATION <===

	    setupPipelineDescriptorSetLayout();

	    VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.calloc(1, stack);
	    pipelineInfo.sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO);
	    pipelineInfo.pStages(shaderStages);
	    pipelineInfo.pVertexInputState(vertexInputInfo);
	    pipelineInfo.pInputAssemblyState(inputAssembly);
	    pipelineInfo.pViewportState(viewportState);
	    pipelineInfo.pRasterizationState(rasterizer);
	    pipelineInfo.pMultisampleState(multisampling);
	    pipelineInfo.pColorBlendState(colorBlending);
	    pipelineInfo.layout(pipelineLayout);
	    pipelineInfo.renderPass(framebuffer.getRenderPass());
	    pipelineInfo.subpass(0);
	    pipelineInfo.basePipelineHandle(VK_NULL_HANDLE);
	    pipelineInfo.basePipelineIndex(-1);

	    LongBuffer pGraphicsPipeline = stack.mallocLong(1);

	    if (vkCreateGraphicsPipelines(device.getDevice(), VK_NULL_HANDLE, pipelineInfo, null,
		    pGraphicsPipeline) != VK_SUCCESS) {
		throw new RuntimeException("Failed to create graphics pipeline");
	    }

	    pipeline = pGraphicsPipeline.get(0);

	    // ===> RELEASE RESOURCES <===

	    vkDestroyShaderModule(device.getDevice(), vertShaderModule, null);
	    vkDestroyShaderModule(device.getDevice(), fragShaderModule, null);

	    vertShaderSPIRV.free();
	    fragShaderSPIRV.free();
	}
    }

    public void createGraphCommandBuffers(VertexShader vertex) {

	final int commandBuffersCount = Prop.mmc(numSwaps, framebuffer.getSwapChainFramebuffers().size());

	commandBuffers = new HashMap<>();

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

	    VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack);
	    beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);

	    VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.calloc(stack);
	    renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);

	    renderPassInfo.renderPass(framebuffer.getRenderPass());

	    VkRect2D renderArea = VkRect2D.calloc(stack);
	    renderArea.offset(VkOffset2D.calloc(stack).set(0, 0));
	    renderArea.extent(framebuffer.getSwapChainExtent());
	    renderPassInfo.renderArea(renderArea);

	    VkClearValue.Buffer clearValues = VkClearValue.calloc(1, stack);
	    clearValues.color().float32(stack.floats(0.0f, 0.0f, 0.0f, 1.0f));
	    renderPassInfo.pClearValues(clearValues);

	    int numFramebuffer = framebuffer.getSwapChainFramebuffers().size();
	    for (int i = 0; i < numFramebuffer; i++) {
		List<VkCommandBuffer> l = new ArrayList<>(numSwaps);
		
		for (int j = 0; j < numSwaps; j++) {
		    VkCommandBuffer commandBuffer = new VkCommandBuffer(pCommandBuffers.get(j * numFramebuffer + i),
			    device.getDevice());
		    
		    l.add(commandBuffer);

		    if (vkBeginCommandBuffer(commandBuffer, beginInfo) != VK_SUCCESS) {
			throw new RuntimeException("Failed to begin recording command buffer");
		    }

		    renderPassInfo.framebuffer(framebuffer.getSwapChainFramebuffers().get(i));

		    vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
		    {
			vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline);

			LongBuffer vertexBuffers = stack.longs(vertex.getVertex().getBufferPointer());
			LongBuffer offsets = stack.longs(0);
			vkCmdBindVertexBuffers(commandBuffer, 0, vertexBuffers, offsets);

			vkCmdBindDescriptorSets(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipelineLayout, 0,
				stack.longs(descriptorSets.get(j)), null);

			if (vertex.getIndex() != null) {
			    vkCmdBindIndexBuffer(commandBuffer, vertex.getIndex().getBufferPointer(), 0,
				    VK_INDEX_TYPE_UINT16);

			    vkCmdDrawIndexed(commandBuffer,
				    vertex.getIndex().getNumElements() * vertex.getIndex().getLayout().getNumAtrib(), 1,
				    0, 0, 0);
			} else {
			    vkCmdDraw(commandBuffer, vertex.getVertex().getNumElements(), 1, 0, 0);
			}

			// vkCmdDraw(commandBuffer, 3, 1, 0, 0);

		    }
		    vkCmdEndRenderPass(commandBuffer);

		    if (vkEndCommandBuffer(commandBuffer) != VK_SUCCESS) {
			throw new RuntimeException("Failed to record command buffer");
		    }

		}
		commandBuffers.put(i, l);
	    }

	    

	}
    }
}
