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
import org.neo4j.server.plugins.PluginTarget;
import org.neo4j.server.plugins.ServerPlugin;
import org.neo4j.server.plugins.Source;
import org.neo4j.tooling.GlobalGraphOperations;
import org.networklibrary.complexlayouts.forceatlas2.ForceAtlas2;
import org.networklibrary.complexlayouts.forceatlas2.ForceAtlas2LayoutData;
import org.networklibrary.complexlayouts.forceatlas2.LayoutDataRegistry;
import org.networklibrary.simplelayouts.CircleLayoutExtension;
import org.networklibrary.simplelayouts.GridLayoutExtension;

public class ForceAtlas2LayoutExtension extends ServerPlugin {
	@Name( "forceatlas2" )
	@Description( "calculates the force atlas 2 layout" )
	@PluginTarget( GraphDatabaseService.class )
	public Iterable<Double> forceatlas2( @Source GraphDatabaseService graph )
	{
		int numNodes = 0;

		try (Transaction tx = graph.beginTx()){

			numNodes = (int)Iterables.count(GlobalGraphOperations.at(graph).getAllNodes());
			System.out.println("num nodes: "+ numNodes);
			tx.success();
		}

		List<Double> result = new ArrayList<Double>(numNodes*3);
		try (Transaction tx = graph.beginTx()){

			LayoutDataRegistry registry = LayoutDataRegistry.getInstance();
			
			primeRegistry(graph);
			
			ForceAtlas2 forceAtlas2 = new ForceAtlas2(graph);

			System.out.println("init algo");
			forceAtlas2.initAlgo();

			System.out.println("go algo");
			for(int i = 0; i < 10000; ++i){
				forceAtlas2.goAlgo();
			}

			System.out.println("end algo");
			forceAtlas2.endAlgo();

			System.out.println("result prep");

			for(Node n : GlobalGraphOperations.at(graph).getAllNodes()){
				ForceAtlas2LayoutData nLayout = registry.getLayoutData(n);
				result.add(new Long(n.getId()).doubleValue());
				result.add(nLayout.x());
				result.add(nLayout.y());
			}
			tx.success();
		}

		return result;
	}
	
	protected void primeRegistry(GraphDatabaseService graph){
		LayoutDataRegistry registry = LayoutDataRegistry.getInstance();
		
		GridLayoutExtension ext = new GridLayoutExtension();
		Iterable<Double> res = ext.gridlayout(graph);
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
}
