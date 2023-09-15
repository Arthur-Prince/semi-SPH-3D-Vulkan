package command;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_VERTEX_BIT;

import buffers.Buffer;
import buffers.IndexBuffer;
import buffers.UniformBuffer;
import buffers.VertexBuffer;
import vulkan.Device;

public class VertexShader extends Shader {
    
    private VertexBuffer vertex;
    private IndexBuffer index;

    
    public VertexShader(String shaderPath, Device device){
	super(shaderPath, VK_SHADER_STAGE_VERTEX_BIT, device);
    }
    
    public VertexShader(String shaderPath, Device device, VertexBuffer vertex, IndexBuffer index){
	super(shaderPath, VK_SHADER_STAGE_VERTEX_BIT, device);
	this.vertex = vertex;
	this.index = index;
    }
    
    public VertexShader(String shaderPath, Device device, VertexBuffer vertex){
	super(shaderPath, VK_SHADER_STAGE_VERTEX_BIT, device);
	this.vertex = vertex;
	this.index = null;
    }
    
    public void addUniform(UniformBuffer uniformBuffer, int bindNmber) {
	super.addLayout(uniformBuffer, bindNmber);
    }

    public VertexBuffer getVertex() {
	return vertex;
    }

    public void setVertex(VertexBuffer vertex) {
	this.vertex = vertex;
    }

    public IndexBuffer getIndex() {
	return index;
    }

    public void setIndex(IndexBuffer index) {
	this.index = index;
    }

}
