package org.osm.query.rdf.blazegraph;

import org.osm.query.rdf.common.uri.OSM;
import org.wikidata.query.rdf.blazegraph.WikibaseInlineUriFactory;

import com.bigdata.rdf.internal.InlineUnsignedIntegerURIHandler;

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
