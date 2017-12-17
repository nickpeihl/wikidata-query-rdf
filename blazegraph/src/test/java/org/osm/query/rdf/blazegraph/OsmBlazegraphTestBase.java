package org.osm.query.rdf.blazegraph;

import java.util.Map;
import java.util.Properties;

import org.openrdf.model.Value;
import org.osm.query.rdf.common.uri.OSM;
import org.wikidata.query.rdf.blazegraph.AbstractRandomizedBlazegraphTestBase;

import com.bigdata.rdf.sail.sparql.PrefixDeclProcessor;

/**
 * Base class for OSM-related tests.
 */
public class OsmBlazegraphTestBase extends AbstractRandomizedBlazegraphTestBase {

    /*
     * Initialize the Wikibase services including shutting off remote SERVICE
     * calls and turning on label service calls.
     */
    static {
        final Map<String, String> defaultDecls = PrefixDeclProcessor.defaultDecls;
        defaultDecls.put("osmroot", OSM.ROOT);
        defaultDecls.put("osmnode", OSM.NODE);
        defaultDecls.put("osmway", OSM.WAY);
        defaultDecls.put("osmrel", OSM.REL);
        defaultDecls.put("osmt", OSM.TAG);
        defaultDecls.put("osmm", OSM.META);
        defaultDecls.put("pageviews", OSM.PAGEVIEWS);
    }

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
            s = s.replaceFirst("^osmt:", OSM.TAG);
            s = s.replaceFirst("^osmm:", OSM.META);
            s = s.replaceFirst("^pageviews:", OSM.PAGEVIEWS);

            o = s;
        }
        return super.convert(o);
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
