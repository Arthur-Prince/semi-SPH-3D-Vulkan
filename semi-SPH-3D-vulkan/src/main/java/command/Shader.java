package command;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_SAMPLED_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_STORAGE_BIT;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_COMPUTE_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkCreateDescriptorSetLayout;
import static org.lwjgl.vulkan.VK10.vkDestroyDescriptorSetLayout;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import buffers.Buffer;
import buffers.BufferInShader;
import buffers.ShaderStorageBuffer;
import buffers.UniformBuffer;
import interfaces.Destructor;
import vulkan.Device;
import vulkan.Prop;

public class Shader {

    private Device device;

    private String shaderPath;
    private List<DescriptorParam> buffersLayouts;
    private int stage;

    

    private int numSwapsLayouts;

    private int numSSBO;
    private int numUniforms;

    Shader(String shaderPath, int stage, Device device) {
	this.buffersLayouts = new LinkedList<>();
	this.shaderPath = shaderPath;
	this.stage = stage;
	this.device = device;

	numSwapsLayouts = 1;

	numUniforms = 0;
	numSSBO = 0;
    }

    private class DescriptorParam {
	int type;
	int bind;
	BufferInShader bufferLayout;

	DescriptorParam(int bind, BufferInShader buffer, int type) {
	    this.bind = bind;
	    this.bufferLayout = buffer;
	    this.type = type;
	}

    }

    public void addLayout(BufferInShader buffer, int bindNumber) {
	int type = 0;

	if (buffer instanceof UniformBuffer) {
	    ((UniformBuffer) buffer).setBindLayoutedBuffer(bindNumber);
	    numUniforms++;
	    type = VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;

	} else if (buffer instanceof ShaderStorageBuffer) {
	    if(buffer.maxSwaps()==0)
		((ShaderStorageBuffer) buffer).addNextOrderSwapLayout(bindNumber);
	    numSSBO++;
	    type = VK_DESCRIPTOR_TYPE_STORAGE_BUFFER;
	}
	//o bind do ssbo esta errado
	buffersLayouts.add(new DescriptorParam(bindNumber, buffer, type));
	numSwapsLayouts = Prop.mmc(numSwapsLayouts, buffer.maxSwaps());
    }

    public int createDescriptorSetLayout(VkDescriptorSetLayoutBinding.Buffer layoutBindings, MemoryStack stack, int next) {
	

	    for (int i = 0; i < buffersLayouts.size(); i++) {
		VkDescriptorSetLayoutBinding layouts = layoutBindings.get(i+next);
		DescriptorParam p = buffersLayouts.get(i);
		layouts.binding(p.bind);
		layouts.descriptorCount(1);
		layouts.descriptorType(p.type);
		layouts.pImmutableSamplers(null);
		layouts.stageFlags(this.stage);
	    }
	    return next+buffersLayouts.size();
	
    }

    public int setupDescriptorSet(VkWriteDescriptorSet.Buffer descriptorWrites, MemoryStack stack, long dstSet, int next, int numSwap) {
	
	    for (int j = 0; j < buffersLayouts.size(); j++) {
		VkWriteDescriptorSet descriptorWrite = descriptorWrites.get(next+j);
		DescriptorParam p = buffersLayouts.get(j);

		VkDescriptorBufferInfo.Buffer bufferInfo = VkDescriptorBufferInfo.calloc(1, stack);
		bufferInfo.offset(0);
		bufferInfo.range(p.bufferLayout.getBufferSize());
		

		descriptorWrite.dstSet(dstSet);
		descriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
		descriptorWrite.dstBinding(p.bufferLayout.nextLayout(numSwap));
		descriptorWrite.dstArrayElement(0);
		descriptorWrite.descriptorType(p.type);
		descriptorWrite.descriptorCount(1);
		descriptorWrite.pBufferInfo(bufferInfo);
		bufferInfo.buffer(p.bufferLayout.getBufferPointer());
		
	    }
	    return next+buffersLayouts.size();
	   
    }


    public String getShaderPath() {
	return shaderPath;
    }

    public void setShaderPath(String shaderPath) {
	this.shaderPath = shaderPath;
    }

    public int getNumSwapsLayouts() {
        return numSwapsLayouts;
    }

    public int getNumSSBO() {
        return numSSBO;
    }

    public int getNumUniforms() {
        return numUniforms;
    }
    
    public int getbuffersLayoutSize() {
	return this.buffersLayouts.size();
    }
    

}
