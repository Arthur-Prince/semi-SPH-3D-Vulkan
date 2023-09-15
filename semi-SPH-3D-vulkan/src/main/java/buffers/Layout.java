package buffers;

import static org.lwjgl.vulkan.VK10.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.vulkan.VkVertexInputAttributeDescription;

public class Layout {

    private List<Elements> descriptors;
    private int stride;
    private int quantidade;
    private int numAtrib;
    
    public Layout(){
	descriptors= new ArrayList<>();
	stride = 0;
	numAtrib=0;
		
    }

    public abstract class Elements {
	private int format;
	private int count;
	private int size;
	
	abstract Number get(ByteBuffer byteBuffer);

	public int getFormat() {
	    return format;
	}

	public void setFormat(int format) {
	    this.format = format;
	}

	public int getCount() {
	    return count;
	}

	public void setCount(int count) {
	    this.count = count;
	}

	public int getSize() {
	    return size;
	}

	public void setSize(int size) {
	    this.size = size;
	}
	
	

    }
    

    public void pushInteger(int count) {
	int format = 0;

	switch (count) {
	case 1:
	    format = VK_FORMAT_R32_SINT;
	    break;
	case 2:
	    format = VK_FORMAT_R32G32_SINT;
	    break;
	case 3:
	    format = VK_FORMAT_R32G32B32_SINT;
	    break;
	case 4:
	    format = VK_FORMAT_R32G32B32A32_SINT;
	    break;

	}

	Elements e = new Elements(){

	    @Override
	    Number get(ByteBuffer byteBuffer) {
		return byteBuffer.getInt();
	    }
	    
	};
	e.format = format;
	e.count = count;
	e.size = Integer.BYTES;
	this.numAtrib += count;
	this.stride += e.count * e.size;
	this.descriptors.add(e);
	
    }
    
    public void pushShort(short count) {
	int format = 0;

	switch (count) {
	case 1:
	    format = VK_FORMAT_R16_UINT;
	    break;
	case 2:
	    format = VK_FORMAT_R16G16_UINT;
	    break;
	case 3:
	    format = VK_FORMAT_R16G16B16_UINT;
	    break;
	case 4:
	    format = VK_FORMAT_R16G16B16A16_UINT;
	    break;

	}

	Elements e = new Elements(){

	    @Override
	    Number get(ByteBuffer byteBuffer) {
		return byteBuffer.getShort();
	    }
	    
	};
	e.format = format;
	e.count = count;
	e.size = Short.BYTES;
	this.numAtrib += count;
	this.stride += e.count * e.size;
	this.descriptors.add(e);
	
    }

    public void pushDouble(int count) {
	int format = 0;

	switch (count) {
	case 1:
	    format = VK_FORMAT_R64_SFLOAT;
	    break;
	case 2:
	    format = VK_FORMAT_R64G64_SFLOAT;
	    break;
	case 3:
	    format = VK_FORMAT_R64G64B64_SFLOAT;
	    break;
	case 4:
	    format = VK_FORMAT_R64G64B64A64_SFLOAT;
	    break;

	}
	Elements e = new Elements(){

	    @Override
	    Number get(ByteBuffer byteBuffer) {
		return byteBuffer.getDouble();
	    }
	    
	};
	e.format = format;
	e.count = count;
	e.size = Double.BYTES;
	this.numAtrib += count;
	this.stride += e.count * e.size;
	this.descriptors.add(e);
    }

    public void pushFloat(int count) {
	int format = 0;

	switch (count) {
	case 1:
	    format = VK_FORMAT_R32_SFLOAT;
	    break;
	case 2:
	    format = VK_FORMAT_R32G32_SFLOAT;
	    break;
	case 3:
	    format = VK_FORMAT_R32G32B32_SFLOAT;
	    break;
	case 4:
	    format = VK_FORMAT_R32G32B32A32_SFLOAT;
	    break;

	}
	Elements e = new Elements(){

	    @Override
	    Number get(ByteBuffer byteBuffer) {
		
		return byteBuffer.getFloat();
	    }
	    
	};
	e.format = format;
	e.count = count;
	e.size = Float.BYTES;
	this.numAtrib += count;
	this.stride += e.count * e.size;
	this.descriptors.add(e);
    }
    
    public List<Elements> getDescriptors() {
        return descriptors;
    }

    public void setDescriptors(List<Elements> descriptors) {
        this.descriptors = descriptors;
    }

    public int getStride() {
        return stride;
    }

    public void setStride(int stride) {
        this.stride = stride;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public int getNumAtrib() {
	return numAtrib;
    }

    public void setNumAtrib(int numAtrib) {
	this.numAtrib = numAtrib;
    }

}
