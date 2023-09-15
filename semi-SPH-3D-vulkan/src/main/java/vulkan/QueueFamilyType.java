package vulkan;

import static org.lwjgl.vulkan.VK10.VK_QUEUE_COMPUTE_BIT;
import static org.lwjgl.vulkan.VK10.VK_QUEUE_GRAPHICS_BIT;

public class QueueFamilyType {
    enum queueType {
	GRAPHIC_QUEUE, COMPUTE_QUEUE, GRAPHIC_COMPUTE_QUEUE
    }

    private Integer queueFamily;
    private queueType queueType;

    public QueueFamilyType(queueType queueType) {
	this.queueType = queueType;
    }

    boolean isQueueFamily(int value) {
	switch (queueType) {

	case GRAPHIC_QUEUE:
	    if ((VK_QUEUE_GRAPHICS_BIT & value) != 0) {
		return true;
	    }
	    
	case COMPUTE_QUEUE:
	    if ((VK_QUEUE_COMPUTE_BIT & value) != 0) {
		return true;
	    }

	    
	case GRAPHIC_COMPUTE_QUEUE:
	    if ((VK_QUEUE_GRAPHICS_BIT & value) != 0 && (VK_QUEUE_COMPUTE_BIT & value) != 0) {
		return true;
	    }

	    

	default:
	    return false;
	}
    }
    
    public Integer getQueueFamily() {
        return queueFamily;
    }

    public void setQueueFamily(Integer queueFamily) {
        this.queueFamily = queueFamily;
    }

    public queueType getQueueType() {
        return queueType;
    }

    public void setQueueType(queueType queueType) {
        this.queueType = queueType;
    }

}
