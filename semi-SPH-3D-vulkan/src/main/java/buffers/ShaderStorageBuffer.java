package buffers;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_SAMPLED_BIT;
import static org.lwjgl.vulkan.VK10.VK_IMAGE_USAGE_STORAGE_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.vkDestroyBuffer;
import static org.lwjgl.vulkan.VK10.vkFreeMemory;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

import java.nio.LongBuffer;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import vulkan.Device;

public class ShaderStorageBuffer extends BufferInShader {

    public enum Usage {
	COMPUTE_TO_COMPUTE(VK_BUFFER_USAGE_STORAGE_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT),
	COMPUTE_TO_VERTEX(VK_BUFFER_USAGE_VERTEX_BUFFER_BIT | VK_BUFFER_USAGE_STORAGE_BUFFER_BIT
		| VK_BUFFER_USAGE_TRANSFER_DST_BIT),
	COMPUTE_TO_SAMPLER(VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_STORAGE_BIT);

	private final int bits;

	Usage(int bits) {
	    this.bits = bits;
	}

	public int getBitValue() {
	    return bits;
	}
    }

    private List<Integer> orderSwapLayout;

    public ShaderStorageBuffer(Device device, List<List<Number>> data, Layout layout, Usage usage) {
	super(device);
	super.createUsageBuffer(data, layout, usage.getBitValue());
	this.orderSwapLayout = new LinkedList<>();
    }
    
    public ShaderStorageBuffer(Buffer b) {
	super(b);
	this.orderSwapLayout = new LinkedList<>();
    }
    
    public void addNextOrderSwapLayout(int nextLayout) {
	this.orderSwapLayout.add(nextLayout);
    }

    public int getOrderSwapLayout(int i) {
        return orderSwapLayout.get(i);
    }

    public void setOrderSwapLayout(List<Integer> orderSwapLayout) {
        this.orderSwapLayout = orderSwapLayout;
    }

    @Override
    public int maxSwaps() {
	// TODO Auto-generated method stub
	return orderSwapLayout.size();
    }

    @Override
    public int nextLayout(int i) {
	// TODO Auto-generated method stub
	return orderSwapLayout.get(i%orderSwapLayout.size());
    }

    
}
