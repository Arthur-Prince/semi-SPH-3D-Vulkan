package projetos;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import buffers.Layout;
import buffers.ShaderStorageBuffer;
import buffers.UniformBuffer;
import buffers.VertexBuffer;
import command.ComputeShader;
import command.FragmentShader;
import command.VertexShader;
import graph.ModelViewProj;
import interfaces.Destructor;
import interfaces.LoopExecuter;
import pipelines.BasicGraphPipeline;
import pipelines.ComputePipeline;
import pipelines.BasicGraphPipeline.Topology;
import vulkan.DefaultVulkanSetup;
import vulkan.Device;
import vulkan.FrameBuffer;

public class SPH implements Destructor, LoopExecuter {

    private DefaultVulkanSetup instance;
    private Device device;
    private FrameBuffer framebuffer;

    private BasicGraphPipeline graphicPipeline;
    private ComputePipeline computePipelineReintegration;
    private ComputePipeline computePipelineSimulation;

    ShaderStorageBuffer SSBO1;
    ShaderStorageBuffer SSBO2;
    UniformBuffer size;
    UniformBuffer time;
    UniformBuffer uMVP;

    ComputeShader computeShaderReintegration;
    ComputeShader computeShaderSimulation;
    VertexShader vertexShader;
    FragmentShader fragmentShader;

    Layout verticesLayout;
    List<List<Number>> vertices;

    ModelViewProj mvp;
    
    long atual;
    long ant;
    
    float count;

    public SPH(DefaultVulkanSetup instance, Device device, FrameBuffer framebuffer) {
	this.instance = instance;
	this.device = device;
	this.framebuffer = framebuffer;
    }

    @Override
    public void init() {

	vertices = new LinkedList<>();
	verticesLayout = new Layout();

	verticesLayout.pushFloat(3); // pos
	verticesLayout.pushFloat(1); // mass
	verticesLayout.pushFloat(3); // vel
	verticesLayout.pushFloat(1); // extra
	verticesLayout.pushFloat(4); // cor

	int[] pos = new int[] { 110, 110, 24 };
	
	List<Number> usize = new ArrayList<Number>();
	for (int i = 0; i < pos.length; i++) {
	    usize.add(pos[i]);
	}
	Layout layoutSize = new Layout();
	layoutSize.pushInteger(3);
	List<List<Number>> lSize = new LinkedList<>();
	lSize.add(usize);

	for (int x = 0; x < pos[0]; x++) {
	    for (int y = 0; y < pos[1]; y++) {
		for (int z = 0; z < pos[2]; z++) {
		    List<Number> v = new ArrayList<>();
		    // position
		    v.add((float) 0);
		    v.add((float) 0);
		    v.add((float) 0);
		    // resto
		    for (int i = 3; i < verticesLayout.getNumAtrib(); i++) {
			v.add(0.0f);
		    }
		    vertices.add(v);
		}
	    }
	}

	Layout ltime = new Layout();
	ltime.pushFloat(1);// deltaTime

	List<List<Number>> dtime = new LinkedList<>();
	List<Number> aux = new ArrayList<>();
	aux.add(20.0f);
	dtime.add(aux);

	mvp = new ModelViewProj(framebuffer.getSwapChainExtent().width(), framebuffer.getSwapChainExtent().height());
	mvp.gira();

	computeShaderReintegration = new ComputeShader("shaders/reintegration.txt", device);
	computeShaderSimulation = new ComputeShader("shaders/simulation.txt", device);
	vertexShader = new VertexShader("shaders/SPHvertex.txt", device);
	fragmentShader = new FragmentShader("shaders/SPHfragment.txt", device);

	size = new UniformBuffer(device, lSize, layoutSize);
	time = new UniformBuffer(device, dtime, ltime);
	uMVP = new UniformBuffer(device, mvp.MVPData(), mvp.getLayout());
	SSBO1 = new ShaderStorageBuffer(device, vertices, verticesLayout, ShaderStorageBuffer.Usage.COMPUTE_TO_VERTEX);
	SSBO2 = new ShaderStorageBuffer(device, vertices, verticesLayout, ShaderStorageBuffer.Usage.COMPUTE_TO_COMPUTE);

	computeShaderReintegration.addLayout(SSBO1, 0);
	computeShaderReintegration.addLayout(SSBO2, 1);
	computeShaderReintegration.addLayout(size, 2);
	computeShaderReintegration.addLayout(time, 3);

	computeShaderSimulation.addLayout(new ShaderStorageBuffer(SSBO1), 1);
	computeShaderSimulation.addLayout(new ShaderStorageBuffer(SSBO2), 0);
	computeShaderSimulation.addLayout(size, 2);
	computeShaderSimulation.addLayout(time, 3);

	vertexShader.setVertex(new VertexBuffer(SSBO1));
	vertexShader.addLayout(uMVP, 0);
	vertexShader.addLayout(size, 2);

	computePipelineReintegration = new ComputePipeline(device, computeShaderReintegration,10,10,3);
	computePipelineSimulation = new ComputePipeline(device, computeShaderSimulation,10,10,3);
	graphicPipeline = new BasicGraphPipeline(device, framebuffer, vertexShader, fragmentShader,
		Topology.Point);
	
	atual = System.currentTimeMillis();
	ant = atual;
	count = 0;
	

    }

    @Override
    public void execute() {
	
//	List<List<Number>> s1 = SSBO1.getBufferWithLayout(verticesLayout);
//	List<List<Number>> s2 = SSBO2.getBufferWithLayout(verticesLayout);
//	int k = 0;
//	for (int i = 0; i < s1.size(); i++) {
//	    for (int j = 0; j < s1.get(i).size(); j++) {
//
//		System.out.print(((float) s1.get(i).get(j)) + " ");
//	    }
//	    System.out.println();
//
//	    for (int j = 0; j < s1.get(i).size(); j++) {
//
//		System.out.print(((float) s2.get(i).get(j)) + " ");
//	    }
//	    System.out.println();
//
//	}
//	Layout layoutSize = new Layout();
//	layoutSize.pushInteger(3);
//	List<List<Number>> usize = size.getBufferWithLayout(layoutSize);
//	for (int i = 0; i < usize.size(); i++) {
//	    for (int j = 0; j < usize.get(i).size(); j++) {
//		System.out.print(usize.get(i).get(j)+" ");
//	    }
//	    System.out.println();
//	}
//	
//	Layout layoutTime = new Layout();
//	layoutTime.pushFloat(1);
//	List<List<Number>> uTime = time.getBufferWithLayout(layoutTime);
//	for (int i = 0; i < uTime.size(); i++) {
//	    for (int j = 0; j < uTime.get(i).size(); j++) {
//		System.out.print(uTime.get(i).get(j)+" ");
//	    }
//	    System.out.println();
//	}
//	
//	System.out.println("------------------------------------");
//	
//	try {
//	    Thread.sleep(1000);
//	} catch (InterruptedException e) {
//	    // TODO Auto-generated catch block
//	    e.printStackTrace();
//	}

	atual = System.currentTimeMillis();
	float deltaTime = (atual-ant)* 0.001f;
	ant = atual;
	count++;
	
	List<List<Number>> dtime = new LinkedList<>();
	List<Number> aux = new ArrayList<>();
	aux.add(deltaTime);
	dtime.add(aux);
	

	time.updateUniformBuffer(dtime);

	mvp.gira();
	uMVP.updateUniformBuffer(mvp.MVPData());
	
	computePipelineReintegration.submitCommand(computePipelineSimulation);
	
	computePipelineSimulation.submitCommand(computePipelineReintegration);


	framebuffer.nextFrame();

	graphicPipeline.submitCommand();

	framebuffer.apresenta();


	

    }

    @Override
    public void cleanup() {

	SSBO1.cleanup();
	SSBO2.cleanup();
	time.cleanup();
	size.cleanup();
	//uMVP.cleanup();

	graphicPipeline.cleanup();
	computePipelineReintegration.cleanup();
	computePipelineSimulation.cleanup();

	framebuffer.cleanup();
	device.cleanup();
	instance.cleanup();

    }

}
