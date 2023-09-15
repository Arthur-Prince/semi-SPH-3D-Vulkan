package projetos;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import buffers.IndexBuffer;
import buffers.Layout;
import buffers.UniformBuffer;
import buffers.VertexBuffer;
import command.FragmentShader;
import command.VertexShader;
import graph.ModelViewProj;
import interfaces.Destructor;
import interfaces.LoopExecuter;
import pipelines.BasicGraphPipeline;
import pipelines.BasicGraphPipeline.Topology;
import pipelines.Pipeline;
import vulkan.DefaultVulkanSetup;
import vulkan.Device;
import vulkan.FrameBuffer;

public class Triangle implements LoopExecuter, Destructor {

    private DefaultVulkanSetup instance;
    private Device device;
    private FrameBuffer framebuffer;

    private BasicGraphPipeline pipeline;

    VertexBuffer vb;
    IndexBuffer ib;
    UniformBuffer ubo;
    

    VertexShader vertexShader;
    FragmentShader fragmentShader;
    
    Layout verticesLayout;
    List<List<Number>> vertices = new LinkedList<>();
    
    List<List<Number>> indexs = new LinkedList<>();
    
    ModelViewProj mvp;
    
    public Triangle(DefaultVulkanSetup instance,Device device,FrameBuffer framebuffer) {
	this.instance =instance;
	this.device = device;
	this.framebuffer = framebuffer;
    }



    @Override
    public void init() {
	
	vertices = new LinkedList<>();
	indexs = new LinkedList<>();
	verticesLayout = new Layout();

	verticesLayout.pushFloat(2);
	verticesLayout.pushFloat(3);
	// data

	float v[][] = new float[][] { { -0.5f, -0.5f, 1.0f, 0.0f, 0.0f }, { 0.5f, -0.5f, 0.0f, 1.0f, 0.0f },
		{ 0.5f, 0.5f, 0.0f, 0.0f, 1.0f }, { -0.5f, 0.5f, 1.0f, 1.0f, 1.0f },{ -0.5f, -0.5f, 1.0f, 0.0f, 0.0f }, { 0.5f, -0.5f, 0.0f, 1.0f, 0.0f } };

	short ind[][] = new short[][] { { 0, 1, 2 }, { 2, 3, 0 } };

	for (int i = 0; i < ind.length; i++) {
	    List<Number> l = new ArrayList<>();
	    for (int j = 0; j < ind[i].length; j++) {
		l.add(ind[i][j]);
	    }
	    indexs.add(l);
	}
	for (int i = 0; i < v.length; i++) {
	    List<Number> l = new ArrayList<>();
	    for (int j = 0; j < v[i].length; j++) {
		l.add(v[i][j]);
	    }
	    vertices.add(l);
	}


	mvp = new ModelViewProj(framebuffer.getSwapChainExtent().width(),
		framebuffer.getSwapChainExtent().height());
	mvp.gira();

	// buffers

	vb = new VertexBuffer(device, vertices, verticesLayout);
	ib = new IndexBuffer(device, indexs);
	ubo = new UniformBuffer(device, mvp.MVPData(), mvp.getLayout());
	

	// shaders

	vertexShader = new VertexShader("shaders/vertex.vs", device);
	fragmentShader = new FragmentShader("shaders/frag.fs", device);

	
	vertexShader.addUniform(ubo, 0);
	//fragmentShader.addUniform(ubo2, 1);
	vertexShader.setIndex(ib);
	vertexShader.setVertex(vb);

	
	pipeline = new BasicGraphPipeline(device, framebuffer, vertexShader, fragmentShader,Topology.LinhaConsecutivas);

    }

    @Override
    public void execute() {
	
	
	framebuffer.nextFrame();
	
	
	mvp.gira();
	ubo.updateUniformBuffer(mvp.MVPData());
	
	pipeline.submitCommand();
	
	
	framebuffer.apresenta();

    }

    @Override
    public void cleanup() {
	vb.cleanup();
	ib.cleanup();
	ubo.cleanup();
	
	pipeline.cleanup();
	
	framebuffer.cleanup();
	device.cleanup();
	instance.cleanup();

    }

}
