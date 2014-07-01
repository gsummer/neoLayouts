package org.networklibrary;

import java.util.Iterator;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.networklibrary.complexlayouts.ForceAtlas2LayoutExtension;
import org.networklibrary.simplelayouts.CircleLayoutExtension;
import org.networklibrary.simplelayouts.GridLayoutExtension;

public class App {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String testing = 	"/Users/gsu/random/cynetlibsync/neo4j-community-2.1.1-2/data/graph.db";
//		String testing = 	"/home/gsu/random/neoanalyzer/testing/data/graph.db";


		GraphDatabaseService g = new GraphDatabaseFactory().newEmbeddedDatabase(testing);
	
		System.out.println("is available: " + g.isAvailable(10));

		CircleLayoutExtension circle = new CircleLayoutExtension();
		GridLayoutExtension grid = new GridLayoutExtension();
		ForceAtlas2LayoutExtension forceAtlas2 = new ForceAtlas2LayoutExtension();

		long start = System.currentTimeMillis();
		Iterable<Double> resCircle = circle.circleLayout(g,true);
//		Iterable<Double> resForce = forceAtlas2.forceatlas2(g,100,false,false);
		Iterable<Double> resGrid = grid.gridlayout(g, true);
		long end = System.currentTimeMillis();
		
	
		Iterator<Double> it = resCircle.iterator();
		
		while(it.hasNext()){
			long id = it.next().longValue();
			double x = it.next();
			double y = it.next();
			
			System.out.println("circle node id = " + id + " x = " + x + "\ty = "+y);
		}
		
		it = resGrid.iterator();
//	 	it = resForce.iterator();
		while(it.hasNext()){
			long id = it.next().longValue();
			double x = it.next();
			double y = it.next();
			
			System.out.println("force node id = " + id + " x = " + x + "\ty = "+y);
		}
		
		System.out.println("duration: " + (end - start));
	}

}
