package projetos;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import buffers.IndexBuffer;
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
import pipelines.Pipeline;
import pipelines.BasicGraphPipeline.Topology;
import pipelines.BasicGraphPipeline;
import pipelines.ComputePipeline;
import vulkan.DefaultVulkanSetup;
import vulkan.Device;
import vulkan.FrameBuffer;

public class Particulas implements Destructor, LoopExecuter {

    private DefaultVulkanSetup instance;
    private Device device;
    private FrameBuffer framebuffer;

    private BasicGraphPipeline graphicPipeline;
    private ComputePipeline ComputePipeline;

    ShaderStorageBuffer SSBO1;
    ShaderStorageBuffer SSBO2;
    UniformBuffer ubo;
    UniformBuffer ubov;

    ComputeShader computeShader;
    VertexShader vertexShader;
    FragmentShader fragmentShader;

    Layout verticesLayout;
    List<List<Number>> vertices;

    ModelViewProj mvp;

    public Particulas(DefaultVulkanSetup instance, Device device, FrameBuffer framebuffer) {
	this.instance = instance;
	this.device = device;
	this.framebuffer = framebuffer;
    }

    @Override
    public void init() {
	vertices = new LinkedList<>();
	verticesLayout = new Layout();

	verticesLayout.pushFloat(2);// pos
	verticesLayout.pushFloat(2);// vel
	verticesLayout.pushFloat(4);// cor

//	 float v[][] = new float[][] { { 0.0f, -1.0f, 0.0f, 0.001f, 1.0f, 0.0f, 0.0f }};

//	float v[][] = new float[][] { { -0.5f, -0.5f, 0.0f, 0.001f, 1.0f, 0.0f, 0.0f, 0.0f },
//		{ 0.5f, -0.5f, 0.0f, 0.001f, 0.0f, 1.0f, 0.0f, 0.0f }, 
//		{ 0.5f, 0.5f, 0.0f, 0.001f, 0.0f, 0.0f, 1.0f, 0.0f },
//		{ -0.5f, 0.5f, 0.0f, 0.001f, 1.0f, 1.0f, 1.0f, 0.0f }, { -0.5f, -0.5f, 0.0f, 0.001f, 1.0f, 0.0f, 0.0f, 0.0f },
//		{ 0.5f, -0.5f, 0.0f, 0.001f, 0.0f, 1.0f, 0.0f, 0.0f } };

	for (int i = 0; i < 10; i++) {
	    List<Number> l = new ArrayList<>();

	    for (int j = 0; j < verticesLayout.getNumAtrib(); j++) {
		float n;
		if (j == 2) {
		    n = 0.5f;
		} else {
		    n = (float) (Math.random() - 0.5) * 2;
		}

		l.add(n);
	    }
	    vertices.add(l);
	}

	Layout lubo = new Layout();
	lubo.pushFloat(1);// deltaTime

	List<List<Number>> dubo = new LinkedList<>();
	List<Number> aux = new ArrayList<>();
	aux.add(1.0f);
	dubo.add(aux);

	mvp = new ModelViewProj(framebuffer.getSwapChainExtent().width(), framebuffer.getSwapChainExtent().height());
	mvp.gira();

	computeShader = new ComputeShader("shaders/comp.txt", device);
	vertexShader = new VertexShader("shaders/compVert.txt", device);
	fragmentShader = new FragmentShader("shaders/compFrag.txt", device);

	ubo = new UniformBuffer(device, dubo, lubo);
	SSBO1 = new ShaderStorageBuffer(device, vertices, verticesLayout, ShaderStorageBuffer.Usage.COMPUTE_TO_VERTEX);
	SSBO2 = new ShaderStorageBuffer(device, vertices, verticesLayout, ShaderStorageBuffer.Usage.COMPUTE_TO_VERTEX);

	SSBO1.addNextOrderSwapLayout(1);
	SSBO1.addNextOrderSwapLayout(2);

	SSBO2.addNextOrderSwapLayout(2);
	SSBO2.addNextOrderSwapLayout(1);

	computeShader.addLayout(ubo, 0);
	computeShader.addLayout(SSBO1, 1);
	computeShader.addLayout(SSBO2, 2);

	VertexBuffer vb1 = new VertexBuffer(SSBO1);
//	VertexBuffer vb2 = new VertexBuffer(SSBO2);
//	VertexBuffer vb1 = new VertexBuffer(device, vertices, verticesLayout);
	ubov = new UniformBuffer(device, mvp.MVPData(), mvp.getLayout());

	vertexShader.setVertex(vb1);
	vertexShader.addLayout(ubov, 0);

	ComputePipeline = new ComputePipeline(device, computeShader);
	graphicPipeline = new BasicGraphPipeline(device, framebuffer, vertexShader, fragmentShader, Topology.Point);

    }

    @Override
    public void execute() {
//	try {
//	    Thread.sleep(1000);
//	} catch (InterruptedException e) {
//	    // TODO Auto-generated catch block
//	    e.printStackTrace();
//	}

	List<List<Number>> dubo = new LinkedList<>();
	List<Number> aux = new ArrayList<>();
	aux.add(0.001f);
	dubo.add(aux);

	ubo.updateUniformBuffer(dubo);

	mvp.gira();
	ubov.updateUniformBuffer(mvp.MVPData());

	ComputePipeline.submitCommand(ComputePipeline);

	framebuffer.nextFrame();

	graphicPipeline.submitCommand();

	framebuffer.apresenta();

//	List<List<Number>> s1 = SSBO1.getBufferWithLayout(verticesLayout);
//	List<List<Number>> s2 = SSBO2.getBufferWithLayout(verticesLayout);
//	int k = 0;
//	for (int i = 0; i < s1.size(); i++) {
//	    boolean conta = true;
//	    for (int j = 0; j < s1.get(i).size(); j++) {
//		if (!vertices.get(i).get(j).equals(s1.get(i).get(j))) {
//		    conta = false;
//		}
////		System.out.print(((float)s1.get(i).get(j))+" ");
//	    }
//	    //System.out.println();
//	    if (conta) {
//		k++;
////		System.out.print(i + " ");
////		System.out.println();
//	    }
//	     
//
//	}
//	System.out.println();
//	System.out.println("...............");
//	System.out.println(k);
	//vertices = s1;

    }

    @Override
    public void cleanup() {

	SSBO1.cleanup();
	SSBO2.cleanup();
	ubo.cleanup();
	ubov.cleanup();
	graphicPipeline.cleanup();
	ComputePipeline.cleanup();

	framebuffer.cleanup();
	device.cleanup();
	instance.cleanup();

    }

    public String floatToBinaryString(float value) {
	int intValue = Float.floatToIntBits(value);
	byte[] bytes = new byte[4];
	StringBuilder binaryString = new StringBuilder();

	for (int i = 0; i < 4; i++) {
	    bytes[i] = (byte) ((intValue >> (i * 8)) & 0xFF);
	    binaryString.append(String.format("%8s", Integer.toBinaryString(bytes[i] & 0xFF)).replace(' ', '0'))
		    .append(" ");
	}

	return binaryString.toString();
    }
    
    

}
