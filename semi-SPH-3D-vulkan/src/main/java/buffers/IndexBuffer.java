package buffers;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;

import java.util.LinkedList;
import java.util.List;

import vulkan.Device;

public class IndexBuffer extends Buffer {


    public IndexBuffer(Device device, List<List<Number>> data, Layout layout) {
	super(device);

	createIndexBuffer(data, layout);
    }
    
    public IndexBuffer(Device device, List<List<Number>> data) {
	super(device);
	Layout layout = new Layout();
	layout.pushShort((short)3);
	createIndexBuffer(data, layout);
    }


    public void createIndexBuffer(List<List<Number>> data, Layout layout) {
	super.createUsageBuffer(data, layout,
		VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_INDEX_BUFFER_BIT);

    }


}
