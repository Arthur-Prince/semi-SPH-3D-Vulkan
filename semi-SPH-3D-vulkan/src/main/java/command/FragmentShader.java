package command;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;

import buffers.UniformBuffer;
import vulkan.Device;

public class FragmentShader extends Shader {
    

    public FragmentShader(String shaderPath, Device device){
	super(shaderPath, VK_SHADER_STAGE_FRAGMENT_BIT, device);
    }
    
    public void addUniform(UniformBuffer uniformBuffer, int bindNmber) {
	super.addLayout(uniformBuffer, bindNmber);
    }
}
