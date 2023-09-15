package command;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_COMPUTE_BIT;

import buffers.ShaderStorageBuffer;
import buffers.UniformBuffer;
import vulkan.Device;

public class ComputeShader extends Shader {

    public ComputeShader(String shaderPath, Device device){
	super(shaderPath, VK_SHADER_STAGE_COMPUTE_BIT, device);
    }
    
    public void addUniform(UniformBuffer uniformBuffer, int bindNmber) {
	super.addLayout(uniformBuffer, bindNmber);
    }
    
    public void SSBO(ShaderStorageBuffer SSBO, int bindNmber) {
	super.addLayout(SSBO, bindNmber);
    }

    
}
