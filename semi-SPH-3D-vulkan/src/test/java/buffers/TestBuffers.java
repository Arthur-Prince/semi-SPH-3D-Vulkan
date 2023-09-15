package buffers;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import buffers.*;
import vulkan.DefaultVulkanSetup;
import vulkan.Device;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

public class TestBuffers {

    private static DefaultVulkanSetup instance;
    private static Device device;

    private Layout layout;
    private List<List<Number>> verticesList;
    private Buffer buffer;

    @BeforeClass
    public static void setup() {
	instance = new DefaultVulkanSetup();
	device = new Device(instance);

    }

    @Test
    public void testVertex() {
	int numVertices = 10;
	layout = new Layout();
	layout.pushFloat(4);
	layout.pushFloat(4);
	layout.pushFloat(3);

	float x = (float) 0.5;

	verticesList = new ArrayList<>();

	for (int i = 0; i < numVertices; i++) {
	    
	    List<Number> vertice = new ArrayList<>();
	    for (int j = 0; j < layout.getNumAtrib(); j++) {
		x += 0.1;
		vertice.add(x);
	    }
	    verticesList.add(vertice);
	}

	buffer = new VertexBuffer(device,verticesList, layout);

	List<List<Number>> retorno = buffer.getBufferWithLayout(layout);

	boolean test = true;
	for (int i = 0; i < retorno.size(); i++) {
	    for (int j = 0; j < retorno.get(i).size(); j++) {
		if (!retorno.get(i).get(j).equals(verticesList.get(i).get(j)) ) {
		    
		    test = false;
		    break;

		}

	    }
	    if (!test)
		break;
	}
	assertTrue(test);

    }
    
    @Test
    public void testUniform() {
	int numVertices = 20;
	layout = new Layout();
	layout.pushFloat(4);
	layout.pushFloat(4);

	float x = (float) 0.5;

	verticesList = new ArrayList<>();

	for (int i = 0; i < numVertices; i++) {
	    List<Number> vertice = new ArrayList<>();
	    for (int j = 0; j < layout.getNumAtrib(); j++) {
		x += 0.1;
		vertice.add(x);
	    }
	    verticesList.add(vertice);
	}

	UniformBuffer auxbuffer = new UniformBuffer(device, verticesList, layout);
	
	auxbuffer.updateUniformBuffer(verticesList);
	
	buffer = auxbuffer;

	List<List<Number>> retorno = buffer.getBufferWithLayout(layout);

	boolean test = true;
	for (int i = 0; i < retorno.size(); i++) {
	    for (int j = 0; j < retorno.get(i).size(); j++) {
		if (!retorno.get(i).get(j).equals(verticesList.get(i).get(j)) ) {
		    test = false;
		    break;

		}

	    }
	    if (!test)
		break;
	}
	assertTrue(test);

    }
    
    @Test
    public void testSSBO() {
	int numVertices = 200;
	layout = new Layout();
	layout.pushFloat(4);
	layout.pushFloat(4);

	float x = (float) 0.5;

	verticesList = new ArrayList<>();

	for (int i = 0; i < numVertices; i++) {
	    List<Number> vertice = new ArrayList<>();
	    for (int j = 0; j < layout.getNumAtrib(); j++) {
		x += 0.1;
		vertice.add(x);
	    }
	    verticesList.add(vertice);
	}

	buffer= new ShaderStorageBuffer(device, verticesList, layout, ShaderStorageBuffer.Usage.COMPUTE_TO_COMPUTE);



	List<List<Number>> retorno = buffer.getBufferWithLayout(layout);

	boolean test = true;
	for (int i = 0; i < retorno.size(); i++) {
	    for (int j = 0; j < retorno.get(i).size(); j++) {
		if (!retorno.get(i).get(j).equals(verticesList.get(i).get(j)) ) {
		    test = false;
		    break;

		}

	    }
	    if (!test)
		break;
	}
	assertTrue(test);

    }
    
    @Test
    public void testIndexBuffer() {
	int numVertices = 200;
	layout = new Layout();
	layout.pushInteger(3);


	int x = 1;

	verticesList = new ArrayList<>();

	for (int i = 0; i < numVertices; i++) {
	    List<Number> vertice = new ArrayList<>();
	    for (int j = 0; j < layout.getNumAtrib(); j++) {
		x += 2;
		vertice.add(x);
	    }
	    verticesList.add(vertice);
	}

	buffer= new IndexBuffer(device, verticesList, layout);



	List<List<Number>> retorno = buffer.getBufferWithLayout(layout);

	boolean test = true;
	for (int i = 0; i < retorno.size(); i++) {
	    for (int j = 0; j < retorno.get(i).size(); j++) {
		if (!retorno.get(i).get(j).equals(verticesList.get(i).get(j)) ) {
		    test = false;
		    break;

		}

	    }
	    if (!test)
		break;
	}
	assertTrue(test);

    }

    @After
    public void cleanBuffer() {
	buffer.cleanup();
    }

    @AfterClass
    public static void cleanVulkan() {
	device.cleanup();
	instance.cleanup();
    }

}
