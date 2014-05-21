package org.networklibrary.simplelayouts;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.server.plugins.Description;
import org.neo4j.server.plugins.Name;
import org.neo4j.server.plugins.PluginTarget;
import org.neo4j.server.plugins.ServerPlugin;
import org.neo4j.server.plugins.Source;
import org.neo4j.tooling.GlobalGraphOperations;

public class CircleLayoutExtension extends ServerPlugin {

	@Name( "circlelayout" )
	@Description( "calculates a circle layout" )
	@PluginTarget( GraphDatabaseService.class )
	public Iterable<Double> circleLayout( @Source GraphDatabaseService graph )
	{
		ExecutionEngine engine = new ExecutionEngine( graph );
		double n = 0;

		try (Transaction tx = graph.beginTx()){
			String query = "match (n) return count(n) as num";
			ResourceIterator<Long> resultIterator = engine.execute( query).columnAs( "num" );
			n = new Long(resultIterator.next()).doubleValue();
		}
		int numEntries = (int) (n*3);
		List<Double> result = new ArrayList<Double>(numEntries);

		double spaceOfNode = 60;
		
		double r = (n * spaceOfNode) / (2*Math.PI);
		double angle = (2*Math.PI) /  n;
		
		double i = 0;
		try (Transaction tx = graph.beginTx()){
			for(Node node : GlobalGraphOperations.at(graph).getAllNodes()){
				result.add(new Long(node.getId()).doubleValue());
				double x = r * Math.cos(angle * i);
				double y = r * Math.sin(angle * i);
				result.add(x);
				result.add(y);
				i = i + 1.0;
			}
		}

		return result;

	}

}
