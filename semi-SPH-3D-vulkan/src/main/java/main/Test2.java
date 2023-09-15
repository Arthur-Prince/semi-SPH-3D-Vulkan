package main;

import vulkan.Device;
import vulkan.FrameBuffer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import buffers.Buffer;
import buffers.Layout;
import buffers.VertexBuffer;
import command.ComputeShader;
import command.FragmentShader;
import command.VertexShader;
import projetos.Particulas;
import projetos.SPH;
import projetos.Triangle;
import buffers.ShaderStorageBuffer;
import buffers.UniformBuffer;
import vulkan.DefaultVulkanSetup;

public class Test2 {
    

    public static void main(String[] args) {
	DefaultVulkanSetup v = new DefaultVulkanSetup();
	Device device = new Device(v);
	
	//compute layout
	
	FrameBuffer s = new FrameBuffer(device,v);
	
//	List<List<Number>> particles = new LinkedList<>();
//	Layout particleLayout = new Layout();
//	
//	particleLayout.pushFloat(3); // posicao
//	particleLayout.pushFloat(3); // velocidade
//	particleLayout.pushFloat(1); // massa
//	
//	//matrix de 100x100x100 = 1 milhao de particulas
//	for (int i = 0; i < 100; i++) {
//	    for (int j = 0; j < 100; j++) {
//		for (int j2 = 0; j2 < 100; j2++) {
//		    List<Number> particula = new ArrayList<>(7);
//		    particula.add(0,(float)i);
//		    particula.add(1,(float)j);
//		    particula.add(2,(float)j2);
//		    
//		    particula.add(3,(float)0.0);
//		    particula.add(4,(float)0.0);
//		    particula.add(5,(float)0.0);
//		    
//		    particula.add(6,(float)0.0);
//		    
//		    particles.add(particula);
//		}
//	    }
//	}
//	
//	List<List<Number>> cor = new LinkedList<>();
//	Layout uniformLayout = new Layout();
//	
//	List<Number> rgb = new ArrayList<>(3);
//	    rgb.add(0,(float)0.5);
//	    rgb.add(1,(float)0.5);
//	    rgb.add(2,(float)1.0);
//	
//	    cor.add(rgb);
//	    
//	    uniformLayout.pushFloat(3);
//	    
//	
//	UniformBuffer uniform = new UniformBuffer(device,cor,uniformLayout);
//	
//	uniform.setBindLayoutedBuffer(0);
//	
//	ShaderStorageBuffer ssbo1 = new ShaderStorageBuffer(device, particles,particleLayout,
//		ShaderStorageBuffer.Usage.COMPUTE_TO_VERTEX);
//	
//	ShaderStorageBuffer ssbo2 = new ShaderStorageBuffer(device, particles,particleLayout,
//		ShaderStorageBuffer.Usage.COMPUTE_TO_VERTEX);
//	
//	ssbo1.addNextOrderSwapLayout(1);
//	ssbo1.addNextOrderSwapLayout(2);
//	
//	ssbo2.addNextOrderSwapLayout(2);
//	ssbo2.addNextOrderSwapLayout(1);
//	
//	ComputeShader computeShader = new ComputeShader("shaders/comp.txt", device);
//	
//	computeShader.addLayout(uniform,0);
//	computeShader.addLayout(ssbo1,1);
//	computeShader.addLayout(ssbo2,2);
//
//	
//	
//	VertexShader vertexShader = new VertexShader("shaders/compVert.txt", device);
//	
//	FragmentShader fragShader = new FragmentShader("shaders/compFrag.txt", device);
	
	
	

	
	//Triangle t = new Triangle(v, device, s);
	
	//Particulas t = new Particulas(v, device, s);
	
	SPH t = new SPH(v, device, s);

	v.run(t);
	t.cleanup();
//	uniform.cleanup();
//	ssbo1.cleanup();
//	ssbo2.cleanup();
//	computeShader.cleanup();
//	vertexShader.cleanup();
//	fragShader.cleanup();
//	s.cleanup();
//	device.cleanup();
//	v.cleanup();

    }

}
