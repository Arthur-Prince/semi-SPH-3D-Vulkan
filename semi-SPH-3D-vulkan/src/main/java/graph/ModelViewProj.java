package graph;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

import java.util.LinkedList;
import java.util.List;

import org.joml.Matrix4f;

import buffers.Layout;

public class ModelViewProj {
    
    private Matrix4f model;
    private Matrix4f view;
    private Matrix4f proj;
    
    private int width;
    private int height;
    
    private Layout layout;

    public ModelViewProj(int width, int height) {
	model = new Matrix4f();
	view = new Matrix4f();
	proj = new Matrix4f();
	
	
	this.width=width;
	this.height = height;
	
	layout = new Layout();
	
	layout.pushFloat(16);
	
    }

    public ModelViewProj(List<List<Number>> data) {
	model = reformat(data.get(0));
	view = reformat(data.get(1));
	proj = reformat(data.get(2));
    }
    
    
    public List<List<Number>> MVPData(){
	List<List<Number>> rtn = new LinkedList<>();
	rtn.add(format(model));
	rtn.add(format(view));
	rtn.add(format(proj));
	
	return rtn;
    }
    
    public void gira() {
	model = new Matrix4f();
	view = new Matrix4f();
	proj = new Matrix4f();
	model.rotate((float) (glfwGetTime() * Math.toRadians(10)), 0.0f, 1.0f, 0.0f);
	view.lookAt(-2.0f, -2.0f, -2.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f);
	proj.perspective((float) Math.toRadians(45),
		(float) width / (float) height, 0.1f, 10.0f);
	proj.m11(proj.m11() * -1);
    }
    
    public Matrix4f[] getM() {
	Matrix4f m[] = new Matrix4f[3];
	m[0] = model;
	m[1] = view;
	m[2] = proj;
	return m;
    }
    
    private List<Number> format(Matrix4f m){
	List<Number> l = new LinkedList<>();
	for (int i = 0; i < 4; i++) {
	    for (int j = 0; j < 4; j++) {
		l.add(m.get(i, j));
	    }
	}
	
	return l;
    }
    
    private Matrix4f reformat(List<Number> l) {
	Matrix4f m = new Matrix4f();
	for (int i = 0; i < 4; i++) {
	    for (int j = 0; j < 4; j++) {
		m.set(i, j, (Float)l.get(j*4+i));
	    }
	}
	return m;
    }
    
    public Layout getLayout() {
        return layout;
    }
    
    @Override
    public boolean equals(Object o) {
	if(o instanceof ModelViewProj) {
	    ModelViewProj aux = (ModelViewProj)o;
	    
	    if(     this.model.equals(aux.model) &&
		    this.view.equals(aux.view)   &&
		    this.proj.equals(aux.proj)) {
		return true;
	    }
	    
	    return false;
	}
	return this.equals(o);
    }
    
    @Override
    public String toString() {
	String rtn = model+"\n\n"+view+"\n\n"+proj;
	return rtn;
    }

    public Matrix4f getModel() {
        return model;
    }

    public void setModel(Matrix4f model) {
        this.model = model;
    }

    public Matrix4f getView() {
        return view;
    }

    public void setView(Matrix4f view) {
        this.view = view;
    }

    public Matrix4f getProj() {
        return proj;
    }

    public void setProj(Matrix4f proj) {
        this.proj = proj;
    }
    

}
