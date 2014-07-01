package org.networklibrary.complexlayouts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.Iterables;
import org.neo4j.server.plugins.Description;
import org.neo4j.server.plugins.Name;
import org.neo4j.server.plugins.Parameter;
import org.neo4j.server.plugins.PluginTarget;
import org.neo4j.server.plugins.ServerPlugin;
import org.neo4j.server.plugins.Source;
import org.neo4j.tooling.GlobalGraphOperations;
import org.networklibrary.complexlayouts.forceatlas2.ForceAtlas2;
import org.networklibrary.complexlayouts.forceatlas2.ForceAtlas2LayoutData;
import org.networklibrary.complexlayouts.forceatlas2.LayoutDataRegistry;
import org.networklibrary.simplelayouts.GridLayoutExtension;

public class ForceAtlas2LayoutExtension extends ServerPlugin {
	
	public final int DEFAULT_NUMITERATIONS = 100;
	public final String LAYOUT_PREFIX = "fa2_";
	public final String PREFIX_X  = LAYOUT_PREFIX + "x";
	public final String PREFIX_Y  = LAYOUT_PREFIX + "y";
	
	@Name( "forceatlas2" )
	@Description( "calculates the force atlas 2 layout" )
	@PluginTarget( GraphDatabaseService.class )
	public Iterable<Double> forceatlas2( @Source GraphDatabaseService graph,
			@Description( "number of iterations for the force atlas 2 to compute. defaults to 1000" )
    		@Parameter( name = "depth", optional = true ) Integer numIterations,
    		@Description( "flag to indicate if the statistics should be stored in the graph database" )
			@Parameter( name = "saveInGraph", optional = false ) boolean saveInGraph,
			@Description( "indicate if the algorithm should restart or use coordinates saved in the graph (if available)" )
			@Parameter( name = "saveInGraph", optional = false ) boolean pickup)
	{
		int numNodes = 0;

		if(numIterations == null || numIterations == 0){
			numIterations = DEFAULT_NUMITERATIONS;
		}
		
		try (Transaction tx = graph.beginTx()){

			numNodes = (int)Iterables.count(GlobalGraphOperations.at(graph).getAllNodes());
			System.out.println("num nodes: "+ numNodes);
			tx.success();
		}

		List<Double> result = new ArrayList<Double>(numNodes*3);
		try (Transaction tx = graph.beginTx()){

			LayoutDataRegistry registry = LayoutDataRegistry.getInstance();
			if(pickup)
				primeRegistryByGraph(graph);
			else 
				primeRegistryByGrid(graph);
			
			ForceAtlas2 forceAtlas2 = new ForceAtlas2(graph);

			System.out.println("init algo");
			forceAtlas2.initAlgo();

			System.out.println("go algo");
			for(int i = 0; i < numIterations; ++i){
				forceAtlas2.goAlgo();
			}

			System.out.println("end algo");
			forceAtlas2.endAlgo();

			System.out.println("result prep");

			for(Node n : GlobalGraphOperations.at(graph).getAllNodes()){
				ForceAtlas2LayoutData nLayout = registry.getLayoutData(n);
//				result.add(new Long(n.getId()).doubleValue());
//				result.add(nLayout.x());
//				result.add(nLayout.y());
				
				addToResult(n, nLayout.x(), nLayout.y(), result, saveInGraph);
			}
			tx.success();
		}

		return result;
	}
	
	protected void primeRegistryByGrid(GraphDatabaseService graph){
		LayoutDataRegistry registry = LayoutDataRegistry.getInstance();
		
		GridLayoutExtension ext = new GridLayoutExtension();
		Iterable<Double> res = ext.gridlayout(graph,false);
		Iterator<Double> it = res.iterator();
		
		while(it.hasNext()){
			long id = it.next().longValue();
			double x = it.next();
			double y = it.next();
			
			Node n = graph.getNodeById(id);
			ForceAtlas2LayoutData nLayout = registry.setLayoutData(n, new ForceAtlas2LayoutData());
			nLayout.mass = 1 + n.getDegree();
			nLayout.old_dx = 0;
			nLayout.old_dy = 0;
			nLayout.dx = 0;
			nLayout.dy = 0;
			nLayout.x = x;
			nLayout.y = y;
		}
	}
	
	protected void primeRegistryByGraph(GraphDatabaseService graph){
		LayoutDataRegistry registry = LayoutDataRegistry.getInstance();
		
		for(Node n : GlobalGraphOperations.at(graph).getAllNodes()){
			ForceAtlas2LayoutData nLayout = registry.setLayoutData(n, new ForceAtlas2LayoutData());
			nLayout.mass = 1 + n.getDegree();
			// those should all be saved shouldn't they?
			nLayout.old_dx = 0; 
			nLayout.old_dy = 0;
			nLayout.dx = 0;
			nLayout.dy = 0;
			
			nLayout.x = (Double)n.getProperty(PREFIX_X, 0.0);
			nLayout.y = (Double)n.getProperty(PREFIX_Y, 0.0);
		}
	}
	
	protected void addToResult(Node node, double x, double y, List<Double> result, boolean saveInGraph){
		result.add(new Long(node.getId()).doubleValue());
		result.add(x);
		result.add(y);
		
		if(saveInGraph){
			node.setProperty(PREFIX_X, x);
			node.setProperty(PREFIX_Y, y);
		}
	}
}
