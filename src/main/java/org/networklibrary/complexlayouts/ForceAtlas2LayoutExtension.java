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



	public static final int DEFAULT_NUMITERATIONS = 100;

	public static final String LAYOUT_PREFIX = "fa2_";
	public static final String PREFIX_X  = LAYOUT_PREFIX + "x";
	public static final String PREFIX_Y  = LAYOUT_PREFIX + "y";
	public static final String PREFIX_odx = LAYOUT_PREFIX + "odx";
	public static final String PREFIX_ody = LAYOUT_PREFIX + "ody";
	public static final String PREFIX_dx = LAYOUT_PREFIX + "dx";
	public static final String PREFIX_dy = LAYOUT_PREFIX + "dx";
	public static final String PREFIX_mass = LAYOUT_PREFIX + "mass";
	public static final String PREFIX_size = LAYOUT_PREFIX + "size";

	@Name( "forceatlas2" )
	@Description( "calculates the force atlas 2 layout" )
	@PluginTarget( GraphDatabaseService.class )
	public Iterable<Double> forceatlas2( @Source GraphDatabaseService graph,
			@Description( "number of iterations for the force atlas 2 to compute. defaults to 1000" )
			@Parameter( name = "depth", optional = true ) Integer numIterations,
			@Description( "flag to indicate if the statistics should be stored in the graph database" )
			@Parameter( name = "saveInGraph", optional = false ) boolean saveInGraph,
			@Description( "indicate if the algorithm should restart or use coordinates saved in the graph (if available)" )
			@Parameter( name = "pickup", optional = false ) boolean pickup,
			
			//behaviour
			@Parameter( name = "dissuadeHubs", optional = false ) boolean dissuadeHubs,
			@Parameter( name = "linLogMode", optional = false ) boolean linLogMode,
			@Parameter( name = "preventOverlap", optional = false ) boolean preventOverlap,
			@Parameter( name = "edgeWeightInfluence", optional = false ) Double edgeWeightInfluence,
			
			// tuning
			@Parameter( name = "scaling", optional = false ) Double scaling,
			@Parameter( name = "strongGravityMode", optional = false ) boolean strongGravityMode,
			@Parameter( name = "gravity", optional = false ) Double gravity,
			
			// performance
			@Parameter( name = "tolerance", optional = false ) Double tolerance,
			@Parameter( name = "approxRepulsion", optional = false ) boolean approxRepulsion,
			@Parameter( name = "approx", optional = false ) Double approx)
	{
		int numNodes = 0;
		// algorithm defaults to min(4, max threads -1)
		int numThreads = -1;
		
		if(edgeWeightInfluence == null){
			edgeWeightInfluence = 1.0;
		}
		
		if(scaling == null){
			scaling = 10.0;
		}
		
		if(gravity == null){
			gravity = 1.0;
		}
		
		if(tolerance == null){
			tolerance = 0.1;
		}
		
		if(approx == null){
			approx = 1.2;
		}

		if(numIterations == null || numIterations <= 0){
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

			ForceAtlas2 forceAtlas2 = new ForceAtlas2(graph,numThreads);

			System.out.println("init algo");
			forceAtlas2.initAlgo();

			forceAtlas2.setOutboundAttractionDistribution(dissuadeHubs);
			forceAtlas2.setLinLogMode(linLogMode);
			forceAtlas2.setAdjustSizes(preventOverlap);
			forceAtlas2.setEdgeWeightInfluence(edgeWeightInfluence);

			forceAtlas2.setScalingRatio(scaling);
			forceAtlas2.setStrongGravityMode(strongGravityMode);
			forceAtlas2.setGravity(gravity);

			forceAtlas2.setJitterTolerance(tolerance);
			forceAtlas2.setBarnesHutOptimize(approxRepulsion);
			forceAtlas2.setBarnesHutTheta(approx);


			System.out.println("go algo");
			for(int i = 0; i < numIterations; ++i){
				forceAtlas2.goAlgo();
			}

			System.out.println("end algo");
			forceAtlas2.endAlgo();

			System.out.println("result prep");

			for(Node n : GlobalGraphOperations.at(graph).getAllNodes()){
				ForceAtlas2LayoutData nLayout = registry.getLayoutData(n);

				addToResult(n, nLayout, result, saveInGraph);
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

			nLayout.old_dx = (Double)n.getProperty(PREFIX_odx, 0.0); 
			nLayout.old_dy = (Double)n.getProperty(PREFIX_ody, 0.0);
			nLayout.dx = (Double)n.getProperty(PREFIX_dx, 0.0);
			nLayout.dy = (Double)n.getProperty(PREFIX_dy, 0.0);
			nLayout.mass = (Double)n.getProperty(PREFIX_mass, 1+n.getDegree());
			nLayout.size = (Double)n.getProperty(PREFIX_size, 0.0);

			nLayout.x = (Double)n.getProperty(PREFIX_X, 0.0);
			nLayout.y = (Double)n.getProperty(PREFIX_Y, 0.0);
		}
	}

	protected void addToResult(Node node, ForceAtlas2LayoutData data, List<Double> result, boolean saveInGraph){
		result.add(new Long(node.getId()).doubleValue());
		result.add(data.x);
		result.add(data.y);

		if(saveInGraph){
			node.setProperty(PREFIX_odx, data.old_dx);
			node.setProperty(PREFIX_ody, data.old_dy);
			node.setProperty(PREFIX_dx, data.dx);
			node.setProperty(PREFIX_dy, data.dy);
			node.setProperty(PREFIX_mass, data.mass);
			node.setProperty(PREFIX_size, data.size);

			node.setProperty(PREFIX_X, data.x);
			node.setProperty(PREFIX_Y, data.y);
		}
	}
}
