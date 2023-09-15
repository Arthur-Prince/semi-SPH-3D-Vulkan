package buffers;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT;
import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY;
import static org.lwjgl.vulkan.VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_HEAP_DEVICE_LOCAL_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT;
import static org.lwjgl.vulkan.VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT;
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;
import static org.lwjgl.vulkan.VK10.VK_SHARING_MODE_EXCLUSIVE;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO;
import static org.lwjgl.vulkan.VK10.VK_SUCCESS;
import static org.lwjgl.vulkan.VK10.vkAllocateCommandBuffers;
import static org.lwjgl.vulkan.VK10.vkAllocateMemory;
import static org.lwjgl.vulkan.VK10.vkBeginCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkBindBufferMemory;
import static org.lwjgl.vulkan.VK10.vkCmdCopyBuffer;
import static org.lwjgl.vulkan.VK10.vkCreateBuffer;
import static org.lwjgl.vulkan.VK10.vkDestroyBuffer;
import static org.lwjgl.vulkan.VK10.vkEndCommandBuffer;
import static org.lwjgl.vulkan.VK10.vkFreeCommandBuffers;
import static org.lwjgl.vulkan.VK10.vkFreeMemory;
import static org.lwjgl.vulkan.VK10.vkGetBufferMemoryRequirements;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceMemoryProperties;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkQueueSubmit;
import static org.lwjgl.vulkan.VK10.vkQueueWaitIdle;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkBufferCopy;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkMemoryAllocateInfo;
import org.lwjgl.vulkan.VkMemoryRequirements;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceMemoryProperties;
import org.lwjgl.vulkan.VkSubmitInfo;

import buffers.Layout.Elements;
import interfaces.Destructor;
import vulkan.Device;

public class Buffer implements Destructor {

//    private int capacity;
//    private Layout layout;
//    private Shader shader;

    private long bufferPointer;
    private long allocateMemoryBufferPointer;
    private int bufferSize;
    
    private Layout layout;
    private int NumElements;


    // o q precisa
    static Device device;

    public static void setDevice(Device devic) {
	device = devic;
    }

    public Buffer(Device devic) {
	device = devic;
    }

    Buffer(Buffer b) {
	this.bufferPointer = b.getBufferPointer();
	this.allocateMemoryBufferPointer = b.getAllocateMemoryBufferPointer();
	this.bufferSize = b.getBufferSize();
	this.layout = b.getLayout();
	this.NumElements = b.getNumElements();
    }

    void createBuffer(long size, int usage, int properties, LongBuffer pBuffer, LongBuffer pBufferMemory) {

	try (MemoryStack stack = stackPush()) {

	    VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.calloc(stack);
	    bufferInfo.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO);
	    bufferInfo.size(size);
	    bufferInfo.usage(usage);
	    bufferInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);

	    if (vkCreateBuffer(device.getDevice(), bufferInfo, null, pBuffer) != VK_SUCCESS) {
		throw new RuntimeException("Failed to create vertex buffer");
	    }

	    VkMemoryRequirements memRequirements = VkMemoryRequirements.malloc(stack);
	    vkGetBufferMemoryRequirements(device.getDevice(), pBuffer.get(0), memRequirements);

	    VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.calloc(stack);
	    allocInfo.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
	    allocInfo.allocationSize(memRequirements.size());
	    allocInfo.memoryTypeIndex(device.findMemoryType(stack, memRequirements.memoryTypeBits(), properties));

	    if (vkAllocateMemory(device.getDevice(), allocInfo, null, pBufferMemory) != VK_SUCCESS) {
		throw new RuntimeException("Failed to allocate vertex buffer memory");
	    }

	    vkBindBufferMemory(device.getDevice(), pBuffer.get(0), pBufferMemory.get(0), 0);
	}
    }

    void memcpy(ByteBuffer buffer, List<List<Number>> data) {
	for (List<Number> list : data) {
	    for (Number n : list) {

		if (n instanceof Integer) {
		    buffer.putInt(n.intValue());
		} else if (n instanceof Double) {
		    buffer.putDouble(n.doubleValue());
		} else if (n instanceof Float) {
		    buffer.putFloat(n.floatValue());
		} else if (n instanceof Short) {
		    buffer.putShort(n.shortValue());
		}else if (n instanceof Long) {
		    buffer.putLong(n.longValue());
		}else {
		    throw new RuntimeException("tipo(float,int...) de buffer não suportado");
		}
		
	    }
	}
    }

    void copyBuffer(long srcBuffer, long dstBuffer, long size) {

	try (MemoryStack stack = stackPush()) {

	    VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack);
	    allocInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
	    allocInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY);
	    allocInfo.commandPool(device.getCommandPool());
	    allocInfo.commandBufferCount(1);

	    PointerBuffer pCommandBuffer = stack.mallocPointer(1);
	    vkAllocateCommandBuffers(device.getDevice(), allocInfo, pCommandBuffer);
	    VkCommandBuffer commandBuffer = new VkCommandBuffer(pCommandBuffer.get(0), device.getDevice());

	    VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack);
	    beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
	    beginInfo.flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);

	    vkBeginCommandBuffer(commandBuffer, beginInfo);
	    {
		VkBufferCopy.Buffer copyRegion = VkBufferCopy.calloc(1, stack);
		copyRegion.size(size);
		vkCmdCopyBuffer(commandBuffer, srcBuffer, dstBuffer, copyRegion);
	    }
	    vkEndCommandBuffer(commandBuffer);

	    VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
	    submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);
	    submitInfo.pCommandBuffers(pCommandBuffer);

	    if (vkQueueSubmit(device.getGraphicsQueue(), submitInfo, VK_NULL_HANDLE) != VK_SUCCESS) {
		throw new RuntimeException("Failed to submit copy command buffer");
	    }

	    vkQueueWaitIdle(device.getGraphicsQueue());

	    vkFreeCommandBuffers(device.getDevice(), device.getCommandPool(), pCommandBuffer);
	}
    }

    ByteBuffer getBufferBackToCPU() {
	ByteBuffer data = null;
	try (MemoryStack stack = stackPush()) {

	    LongBuffer pBuffer = stack.mallocLong(1);
	    LongBuffer pBufferMemory = stack.mallocLong(1);
	    createBuffer(this.bufferSize, VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
		    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, pBuffer, pBufferMemory);

	    long stagingBuffer = pBuffer.get(0);
	    long stagingBufferMemory = pBufferMemory.get(0);

	    copyBuffer(bufferPointer, stagingBuffer, bufferSize);

	    PointerBuffer pData = stack.mallocPointer(1);
	    vkMapMemory(device.getDevice(), stagingBufferMemory, 0, bufferSize, 0, pData);
	    {
		ByteBuffer mappedByteBuffer = pData.getByteBuffer(0, (int) bufferSize);
		data = ByteBuffer.allocateDirect(mappedByteBuffer.capacity()); // Create a new ByteBuffer with the same
									       // capacity
		data.put(mappedByteBuffer); // Copy the data from mappedByteBuffer to data
		data.flip(); // Prepare the buffer for reading
	    }
	    vkUnmapMemory(device.getDevice(), stagingBufferMemory);

	    vkDestroyBuffer(device.getDevice(), stagingBuffer, null);
	    vkFreeMemory(device.getDevice(), stagingBufferMemory, null);
	}

	return data;
    }

    void createUsageBuffer(List<List<Number>> data, Layout layout, int usage) {
	this.layout = layout;
	NumElements = data.size();

	try (MemoryStack stack = stackPush()) {

	    this.bufferSize = data.size() * layout.getStride();

	    LongBuffer pBuffer = stack.mallocLong(1);
	    LongBuffer pBufferMemory = stack.mallocLong(1);
	    createBuffer(bufferSize, VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
		    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, pBuffer, pBufferMemory);

	    long stagingBuffer = pBuffer.get(0);
	    long stagingBufferMemory = pBufferMemory.get(0);

	    PointerBuffer dataaux = stack.mallocPointer(1);

	    vkMapMemory(device.getDevice(), stagingBufferMemory, 0, bufferSize, 0, dataaux);
	    {
		memcpy(dataaux.getByteBuffer(0, (int) bufferSize), data);
	    }
	    vkUnmapMemory(device.getDevice(), stagingBufferMemory);

	    createBuffer(bufferSize, usage, VK_MEMORY_HEAP_DEVICE_LOCAL_BIT, pBuffer, pBufferMemory);

	    this.bufferPointer = pBuffer.get(0);
	    this.allocateMemoryBufferPointer = pBufferMemory.get(0);

	    copyBuffer(stagingBuffer, bufferPointer, bufferSize);

	    vkDestroyBuffer(device.getDevice(), stagingBuffer, null);
	    vkFreeMemory(device.getDevice(), stagingBufferMemory, null);
	}

    }

    public List<List<Number>> getBufferWithLayout(Layout layout) {

	List<List<Number>> vertexBuffer = new ArrayList<>();

	ByteBuffer buffer = getBufferBackToCPU();
	buffer.order(ByteOrder.LITTLE_ENDIAN);

	List<Elements> e = layout.getDescriptors();

	while (buffer.remaining() >= layout.getStride()) {
	    List<Number> vertice = new ArrayList<>();
	    int size = 0;
	    int i = 0;
	    

	    while (size < layout.getStride()) {
		for (int j = 0; j < e.get(i).getCount(); j++) {
		    vertice.add(e.get(i).get(buffer));
		    size += e.get(i).getSize();
		}
		i++;
	    }

	    vertexBuffer.add(vertice);
	}

	return vertexBuffer;
    }
    
    public long getBufferPointer() {
        return bufferPointer;
    }

    public void setBufferPointer(long bufferPointer) {
        this.bufferPointer = bufferPointer;
    }

    public long getAllocateMemoryBufferPointer() {
        return allocateMemoryBufferPointer;
    }

    public void setAllocateMemoryBufferPointer(long allocateMemoryBufferPointer) {
        this.allocateMemoryBufferPointer = allocateMemoryBufferPointer;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public Layout getLayout() {
        return layout;
    }

    public void setLayout(Layout layout) {
        this.layout = layout;
    }

    @Override
    public void cleanup() {
	vkDestroyBuffer(device.getDevice(), bufferPointer, null);
	vkFreeMemory(device.getDevice(), allocateMemoryBufferPointer, null);

    }

    public int getNumElements() {
	return NumElements;
    }

    public void setNumElements(int numElements) {
	NumElements = numElements;
    }

}
