package pipelines;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import buffers.IndexBuffer;
import buffers.Layout;
import buffers.UniformBuffer;
import buffers.VertexBuffer;
import command.FragmentShader;
import command.VertexShader;
import graph.ModelViewProj;
import vulkan.DefaultVulkanSetup;
import vulkan.Device;
import vulkan.FrameBuffer;

public class TestPipeline {
    
    private static DefaultVulkanSetup instance;
    private static Device device;
    private static FrameBuffer framebuffer;
    
    private Pipeline pipeline;
    
    @BeforeClass
    public static void setup() {
	instance = new DefaultVulkanSetup();
	device = new Device(instance);
	framebuffer = new FrameBuffer(device, instance);

    }
    
//    @Test
//    public void testBasicGraphPipeline() {
//	List<List<Number>> vertices = new LinkedList<>();
//	List<List<Number>> indexs = new LinkedList<>();
//	Layout verticesLayout = new Layout();
//	
//	
//	verticesLayout.pushFloat(2);
//	verticesLayout.pushFloat(3);
//	// data
//	
//	float v[][] = new float[][]{{-0.5f, -0.5f,	1.0f, 0.0f, 0.0f},
//	    				{0.5f, -0.5f,	0.0f, 1.0f, 0.0f},
//					{0.5f, 0.5f,	0.0f, 0.0f, 1.0f},
//					{-0.5f, 0.5f,	1.0f, 1.0f, 1.0f}};
//				
//	int ind[][] = new int[][] {{0, 1, 2},
//	    			   {2, 3, 0}};
//	    			   
//	for (int i = 0; i < ind.length; i++) {
//	    List<Number> l = new ArrayList<>();
//	    for (int j = 0; j < ind[i].length; j++) {
//		l.add(ind[i][j]);
//	    }
//	    indexs.add(l);
//	}
//	for (int i = 0; i < v.length; i++) {
//	    List<Number> l = new ArrayList<>();
//	    for (int j = 0; j < v[i].length; j++) {
//		l.add(v[i][j]);
//	    }
//	    vertices.add(l);
//	}
//	
//	ModelViewProj mvp = new ModelViewProj(framebuffer.getSwapChainExtent().width(), framebuffer.getSwapChainExtent().height());
//	mvp.gira();
//	
//	// buffers
//	
//	VertexBuffer vb = new VertexBuffer(device, vertices , verticesLayout);
//	IndexBuffer ib = new IndexBuffer(device, indexs);
//	UniformBuffer ubo = new UniformBuffer(device, mvp.MVPData(), mvp.getLayout());
//	
//	// shaders
//	
//	VertexShader vertexShader = new VertexShader("shaders/vertex.vs", device);
//	FragmentShader fragmentShader = new FragmentShader("shaders/frag.fs", device);
//	
//	vertexShader.addUniform(ubo, 0);
//	vertexShader.setIndex(ib);
//	vertexShader.setVertex(vb);
//	
//	pipeline = new BasicGraphPipeline(device, framebuffer, vertexShader, fragmentShader);
//	
//	//instance.run();
//	
//	
//	vb.cleanup();
//	ib.cleanup();
//	ubo.cleanup();
//	
//	
//	
//    }
    
//    @Test
//    public void testComputePipeline() {
//	
//    }
//    
//    @Test
//    public void testComputeGraphPipeline() {
//	
//    }
    
    @After
    public void cleanBuffer() {
	pipeline.cleanup();
    }
    
    @AfterClass
    public static void cleanVulkan() {
	framebuffer.cleanup();
	device.cleanup();
	instance.cleanup();
    }

}
