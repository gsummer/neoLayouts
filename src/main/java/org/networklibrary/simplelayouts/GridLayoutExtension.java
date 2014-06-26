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

public class GridLayoutExtension extends ServerPlugin {

	@Name( "gridlayout" )
	@Description( "calculates a grid layout" )
	@PluginTarget( GraphDatabaseService.class )
	public Iterable<Double> gridlayout( @Source GraphDatabaseService graph )
	{
		ExecutionEngine engine = new ExecutionEngine( graph );
		int n = 0;

		int distanceBetweenNodesX = 80; // would normally add the node size here
		int distanceBetweenNodesY = 40;
		try (Transaction tx = graph.beginTx()){
			String query = "match (n) return count(n) as num";
			ResourceIterator<Long> resultIterator = engine.execute( query).columnAs( "num" );
			n = new Long(resultIterator.next()).intValue();
			tx.success();
		}
		List<Double> result = new ArrayList<Double>(n*3);

		int nCols = (int)Math.ceil(Math.sqrt(n));

		int i = 0;
		int currCol = 0;
		int currRow = 0;

		try (Transaction tx = graph.beginTx()){
			for(Node node : GlobalGraphOperations.at(graph).getAllNodes()){
				if(currCol == nCols){
					currCol = 0;
					currRow = currRow + 1;
				}

				double currX = currCol * distanceBetweenNodesX;
				double currY = currRow * distanceBetweenNodesY;

				result.add(i, new Long(node.getId()).doubleValue());
				i=i+1; // go to x
				result.add(i, currX);
				i=i+1;
				result.add(i, currY);
				i=i+1;

				currCol = currCol + 1;
			}
			tx.success();
		}

		return result;

	}

}