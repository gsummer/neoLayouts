package org.networklibrary.simplelayouts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.server.plugins.Description;
import org.neo4j.server.plugins.Name;
import org.neo4j.server.plugins.Parameter;
import org.neo4j.server.plugins.PluginTarget;
import org.neo4j.server.plugins.ServerPlugin;
import org.neo4j.server.plugins.Source;
import org.neo4j.tooling.GlobalGraphOperations;

public class CircleLayoutExtension extends ServerPlugin {

	public final String LAYOUT_PREFIX = "circ_";
	public final String PREFIX_X  = LAYOUT_PREFIX + "x";
	public final String PREFIX_Y  = LAYOUT_PREFIX + "y";

	public static final double SPACEOFNODE = 60;

	@Name( "circlelayout" )
	@Description( "calculates a circle layout" )
	@PluginTarget( GraphDatabaseService.class )
	public Iterable<Double> circleLayout( @Source GraphDatabaseService graph,
			@Description( "flag to indicate if the layout should be stored in the graph database" )
	@Parameter( name = "saveInGraph", optional = false ) boolean saveInGraph)
	{
		double n = 0;

		List<Node> isolated = new ArrayList<Node>();
		Map<Node,List<Node>> singles = new HashMap<Node,List<Node>>();
		List<Node> normals = new ArrayList<Node>();

		try(Transaction tx = graph.beginTx()){
			for(Node node : GlobalGraphOperations.at(graph).getAllNodes()){
				Set<Node> neighbours = getNeighbours(node);
				int degree = neighbours.size();
				switch(degree){
				case 0:
					isolated.add(node);
					break;
				case 1:
					Node multi = node.getRelationships().iterator().next().getOtherNode(node);
					if(!singles.containsKey(multi)){
						singles.put(multi, new ArrayList<Node>());
					}
					singles.get(multi).add(node);

					break;
				default:

					normals.add(node);
					break;
				}
				++n;
			}
			tx.success();
		}

		int numEntries = (int) (n*3);
		List<Double> result = new ArrayList<Double>(numEntries);

		double r = calculateRadius(normals.size());
		double angle = calculateAngle(normals.size());
		double singleR = r * 1.5;
		int perRow = (int)(2*r / SPACEOFNODE);
		double i = 0;

		try (Transaction tx = graph.beginTx()){
			for(Node node : normals){

				double baseAngle = angle * i;
				double x = r * Math.cos(baseAngle);
				double y = r * Math.sin(baseAngle);

				addToResult(node,x,y,result,saveInGraph);
				
//				result.add(new Long(node.getId()).doubleValue());
//				result.add(x);
//				result.add(y);

				i = i + 1.0;

				if(singles.containsKey(node)){
					double j = 0;
					double flip = 1;
					for(Node single : singles.get(node)){

						double singleAngle = baseAngle + (angle/4.0 * j * flip);

						double sx = singleR * Math.cos(singleAngle);
						double sy = singleR * Math.sin(singleAngle);

//						result.add(new Long(single.getId()).doubleValue());
//						result.add(sx);
//						result.add(sy);
						
						addToResult(single,sx,sy,result,saveInGraph);

						flip *= -1;
						++j;
					}
				}
			}

			// loners
			int j = 0;
			int k = 0;
			double startX = r * Math.cos(3/2*Math.PI);
			double startY = r * 1.75;
			for(Node node : isolated){
				double x = startX + (j*SPACEOFNODE);
				double y = startY + (k*SPACEOFNODE);

//				result.add(new Long(node.getId()).doubleValue());
//				result.add(x);
//				result.add(y);
				addToResult(node,x,y,result,saveInGraph);

				++j;

				if(j > perRow){
					j = 0;
					++k;
				}
			}
			tx.success();
		}

		return result;

	}

	protected double calculateAngle(double n){
		return (2*Math.PI) / n;
	}

	protected double calculateRadius(double n){
		return (n * SPACEOFNODE) / (2*Math.PI);
	}

	protected Set<Node> getNeighbours(Node node){
		Set<Node> neighbours = new HashSet<Node>();

		for(Relationship rel : node.getRelationships()){
			neighbours.add(rel.getOtherNode(node));
		}

		return neighbours;
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
