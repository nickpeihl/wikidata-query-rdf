package org.osm.query.rdf.blazegraph;

import org.openrdf.model.Value;
import org.osm.query.rdf.common.uri.OSM;

import java.util.Properties;


/**
 * Base class for OSM-related tests.
 */
public class AbstractRandomizedBlazegraphTestBase extends org.wikidata.query.rdf.blazegraph.AbstractRandomizedBlazegraphTestBase {

    /**
     * Convert any object into an RDF value.
     */
    protected Value convert(Object o) {
        if (o instanceof String) {
            String s = (String) o;
            s = s.replaceFirst("^osmroot:", OSM.ROOT);
            s = s.replaceFirst("^osmnode:", OSM.NODE);
            s = s.replaceFirst("^osmway:", OSM.WAY);
            s = s.replaceFirst("^osmrel:", OSM.REL);
            s = s.replaceFirst("^osmtag:", OSM.TAG);
            s = s.replaceFirst("^osmmeta:", OSM.META);
            s = s.replaceFirst("^osmrole:", OSM.ROLE);
            o = s;
        }
        return super.convert(o);
    }

    /*
     * Initialize the Osm services including shutting off remote SERVICE
     * calls and turning on label service calls.
     */
    static {
        OsmContextListener.initializeServices();
    }

    @Override
    protected Properties initStoreProperties(Properties properties) {
        properties = super.initStoreProperties(properties);
        properties.setProperty("com.bigdata.rdf.store.AbstractTripleStore.vocabularyClass",
                OsmVocabulary.VOCABULARY_CLASS.getName());
        properties.setProperty("com.bigdata.rdf.store.AbstractTripleStore.inlineURIFactory",
                OsmInlineUriFactory.class.getName());
        return properties;
    }
}
