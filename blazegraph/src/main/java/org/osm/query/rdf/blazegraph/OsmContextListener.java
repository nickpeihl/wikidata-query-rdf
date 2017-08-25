package org.osm.query.rdf.blazegraph;

import com.bigdata.rdf.sail.sparql.PrefixDeclProcessor;
import org.osm.query.rdf.common.uri.OSM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.query.rdf.blazegraph.WikibaseContextListener;

import javax.servlet.ServletContextEvent;
import java.util.Map;

/**
 * Context listener to enact configurations we need on initialization.
 */
@SuppressWarnings({"checkstyle:classfanoutcomplexity", "rawtypes"})
public class OsmContextListener extends WikibaseContextListener {

    private static final Logger log = LoggerFactory.getLogger(OsmContextListener.class);

    /**
     * Initializes BG service setup to allow whitelisted services.
     * Also add additional custom services and functions.
     */
    public static void initializeServices() {
        addPrefixes();

        log.info("OSM services initialized.");
    }

    /**
     * Add standard prefixes to the system.
     */
    private static void addPrefixes() {
        final Map<String, String> defaultDecls = PrefixDeclProcessor.defaultDecls;
        defaultDecls.put("osmroot", OSM.ROOT);
        defaultDecls.put("osmnode", OSM.NODE);
        defaultDecls.put("osmway", OSM.WAY);
        defaultDecls.put("osmrel", OSM.REL);
        defaultDecls.put("osmtag", OSM.TAG);
        defaultDecls.put("osmmeta", OSM.META);

        // legacy
        defaultDecls.put("osmt", OSM.TAG);
        defaultDecls.put("osmm", OSM.META);
    }

    @Override
    public void contextInitialized(final ServletContextEvent e) {
        super.contextInitialized(e);
        initializeServices();
    }

}
