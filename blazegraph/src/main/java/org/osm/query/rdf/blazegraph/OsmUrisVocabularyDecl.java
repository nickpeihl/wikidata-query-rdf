package org.osm.query.rdf.blazegraph;

import org.osm.query.rdf.common.uri.OSM;

import com.bigdata.rdf.vocab.BaseVocabularyDecl;

/**
 * Vocabulary containing the URIs from
 * {@linkplain org.wikidata.query.rdf.common.uri.Ontology} that are imported
 * into Blazegraph.
 */
public class OsmUrisVocabularyDecl extends BaseVocabularyDecl {

    public OsmUrisVocabularyDecl() {
        super(
                OSM.NODE,
                OSM.WAY,
                OSM.REL
        );
    }
}
