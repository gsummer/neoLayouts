package org.networklibrary.complexlayouts;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.plugins.Description;
import org.neo4j.server.plugins.Name;
import org.neo4j.server.plugins.PluginTarget;
import org.neo4j.server.plugins.ServerPlugin;
import org.neo4j.server.plugins.Source;

public class OpenOrdLayout extends ServerPlugin {
	@Name( "openord" )
	@Description( "calculates the openord layout" )
	@PluginTarget( GraphDatabaseService.class )
	public Iterable<Double> openord( @Source GraphDatabaseService graph )
	{
		return null;
	}
}
