package buffers;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.vkDestroyBuffer;
import static org.lwjgl.vulkan.VK10.vkFreeMemory;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

import java.nio.LongBuffer;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import vulkan.Device;

public class UniformBuffer extends BufferInShader {

    private List<List<Number>> uniforms;
    private int bindLayoutedBuffer;
    private UniformBuffer[] buffern;
    private int trocadorDeBuffer;

    public UniformBuffer(Device device, List<List<Number>> data, Layout layout) {
	super(device);
	buffern = new UniformBuffer[2];
	createUniformBuffer(data, layout);
	this.trocadorDeBuffer = 0;
    }
    public UniformBuffer(UniformBuffer b) {
	super(b);
	buffern = new UniformBuffer[2];
	
	buffern = b.buffern;
	this.trocadorDeBuffer = 0;
    }
    
    private UniformBuffer() {
	super(device);
    }

    public void createUniformBuffer(List<List<Number>> data, Layout layout) {
	try (MemoryStack stack = stackPush()) {
	    for (int i = 0; i < buffern.length; i++) {

		buffern[i] = new UniformBuffer();

		buffern[i].setBufferSize(data.size() * layout.getStride());

		LongBuffer pBuffer = stack.mallocLong(1);
		LongBuffer pBufferMemory = stack.mallocLong(1);

		createBuffer(buffern[i].getBufferSize(), VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
			VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, pBuffer,
			pBufferMemory);
		buffern[i].setBufferPointer(pBuffer.get(0));
		buffern[i].setAllocateMemoryBufferPointer(pBufferMemory.get(0));

		PointerBuffer dataaux = stack.mallocPointer(1);

		vkMapMemory(device.getDevice(), buffern[i].getAllocateMemoryBufferPointer(), 0,
			buffern[i].getBufferSize(), 0, dataaux);
		{
		    memcpy(dataaux.getByteBuffer(0, (int) buffern[i].getBufferSize()), data);

		}
		vkUnmapMemory(device.getDevice(), buffern[i].getAllocateMemoryBufferPointer());

	    }

	    uniforms = data;
	    this.setBufferSize(buffern[0].getBufferSize());
	    this.setBufferPointer(buffern[0].getBufferPointer());
	    this.setAllocateMemoryBufferPointer(buffern[0].getAllocateMemoryBufferPointer());

	}
    }

    public void updateUniformBuffer(List<List<Number>> data) {
	if (uniforms.size() != data.size()) {
	    throw new RuntimeException("Failed to update UniformBuffer");
	}
	trocadorDeBuffer++;
	nextLayout(trocadorDeBuffer);

	try (MemoryStack stack = stackPush()) {
	    PointerBuffer dataaux = stack.mallocPointer(1);

	    vkMapMemory(device.getDevice(), this.getAllocateMemoryBufferPointer(), 0, this.getBufferSize(), 0, dataaux);
	    {
		memcpy(dataaux.getByteBuffer(0, (int) this.getBufferSize()), data);

	    }
	    vkUnmapMemory(device.getDevice(), this.getAllocateMemoryBufferPointer());
	}
    }

    @Override
    public void cleanup() {
	for (int i = 0; i < buffern.length; i++) {
	    vkDestroyBuffer(device.getDevice(), buffern[i].getBufferPointer(), null);
	    vkFreeMemory(device.getDevice(), buffern[i].getAllocateMemoryBufferPointer(), null);
	}

    }

    public int getBindLayoutedBuffer() {
	return bindLayoutedBuffer;
    }

    public void setBindLayoutedBuffer(int bindLayoutedBuffer) {
	this.bindLayoutedBuffer = bindLayoutedBuffer;
    }

    @Override
    public int maxSwaps() {
	// TODO Auto-generated method stub
	return buffern.length;
    }

    @Override
    public int nextLayout(int i) {
	int atual = i % buffern.length;
	this.setBufferSize(buffern[atual].getBufferSize());
	this.setBufferPointer(buffern[atual].getBufferPointer());
	this.setAllocateMemoryBufferPointer(buffern[atual].getAllocateMemoryBufferPointer());
	return bindLayoutedBuffer;
    }
}
