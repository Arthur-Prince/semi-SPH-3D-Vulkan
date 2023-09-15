package buffers;

import vulkan.Device;

public abstract class BufferInShader extends Buffer {
    
    BufferInShader(Device device){
	super(device);
    }
    
    BufferInShader(Buffer b){
	super(b);
    }

    
    public abstract int maxSwaps();

    public abstract int nextLayout(int i);
}
