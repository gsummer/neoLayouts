package org.networklibrary.complexlayouts.forceatlas2;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.Node;

public class LayoutDataRegistry {

	protected static LayoutDataRegistry instance = null;
	
	public static LayoutDataRegistry getInstance() {
		if(instance == null){
			setupInstance();
		}
		
		return instance;
	}
	
	protected static void setupInstance(){
		instance = new LayoutDataRegistry();
	}
	
	protected Map<Node, LayoutData> registry = null;
	
	protected LayoutDataRegistry() {
		registry = new HashMap<Node, LayoutData> ();
	}
	
	public <T extends LayoutData> T getLayoutData(Node n){
		return (T) registry.get(n);
	}
		
	public <T extends LayoutData> T setLayoutData(Node n, LayoutData ld){
		registry.put(n, ld);
		return  (T) ld;
	}
	
}
