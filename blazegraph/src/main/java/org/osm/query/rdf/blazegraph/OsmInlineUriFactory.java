package org.osm.query.rdf.blazegraph;

import com.bigdata.rdf.internal.InlineUnsignedIntegerURIHandler;
import org.osm.query.rdf.common.uri.OSM;
import org.wikidata.query.rdf.blazegraph.WikibaseInlineUriFactory;

/**
 * Factory building InlineURIHandlers for OSM.
 */
public class OsmInlineUriFactory extends WikibaseInlineUriFactory {
    public OsmInlineUriFactory() {
        super();

        addHandler(new InlineUnsignedIntegerURIHandler(OSM.NODE));
        addHandler(new InlineUnsignedIntegerURIHandler(OSM.WAY));
        addHandler(new InlineUnsignedIntegerURIHandler(OSM.REL));
    }
}
