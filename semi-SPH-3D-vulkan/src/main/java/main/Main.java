package main;

import graph.Window;
import projetos.SPH;
import vulkan.DefaultVulkanSetup;
import vulkan.Device;
import vulkan.FrameBuffer;

public class Main {

    public static void main(String[] args) {

	DefaultVulkanSetup v = new DefaultVulkanSetup();
	Device device = new Device(v);

	FrameBuffer s = new FrameBuffer(device, v);

	// Triangle t = new Triangle(v, device, s);

	// Particulas t = new Particulas(v, device, s);

	SPH t = new SPH(v, device, s);

	v.run(t);
	t.cleanup();
    }
}
