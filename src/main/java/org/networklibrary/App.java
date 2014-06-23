package org.networklibrary;

import java.util.Iterator;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.networklibrary.complexlayouts.ForceAtlas2LayoutExtension;
import org.networklibrary.simplelayouts.CircleLayoutExtension;

public class App {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String testing = 	"/Users/gsu/random/cynetlibsync/neo4j-community-2.1.1-2/data/graph.db";

		GraphDatabaseService g = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(
				testing )
				.setConfig( GraphDatabaseSettings.read_only, "true" )
				.newGraphDatabase();
	
		System.out.println("is available: " + g.isAvailable(10));

		CircleLayoutExtension circle = new CircleLayoutExtension();
		ForceAtlas2LayoutExtension forceAtlas2 = new ForceAtlas2LayoutExtension();

		long start = System.currentTimeMillis();
		Iterable<Double> resCircle = circle.circleLayout(g);
		Iterable<Double> resForce = forceAtlas2.forceatlas2(g);
		long end = System.currentTimeMillis();
		
		int i = 0;
		
		Iterator<Double> it = resCircle.iterator();
		
		while(it.hasNext()){
			long id = it.next().longValue();
			double x = it.next();
			double y = it.next();
			
			System.out.println("circle node id = " + id + " x = " + x + "\ty = "+y);
		}
		
		it = resForce.iterator();
		while(it.hasNext()){
			long id = it.next().longValue();
			double x = it.next();
			double y = it.next();
			
			System.out.println("force node id = " + id + " x = " + x + "\ty = "+y);
		}
		
		System.out.println("duration: " + (end - start));
	}

}
