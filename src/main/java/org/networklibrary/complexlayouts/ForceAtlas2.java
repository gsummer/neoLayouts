package org.networklibrary.complexlayouts;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.plugins.Description;
import org.neo4j.server.plugins.Name;
import org.neo4j.server.plugins.PluginTarget;
import org.neo4j.server.plugins.ServerPlugin;
import org.neo4j.server.plugins.Source;

public class ForceAtlas2 extends ServerPlugin {
	@Name( "forceatlas2" )
	@Description( "calculates the force atlas 2 layout" )
	@PluginTarget( GraphDatabaseService.class )
	public Iterable<Double> forceatlas2( @Source GraphDatabaseService graph )
	{
		return null;
	}
}
