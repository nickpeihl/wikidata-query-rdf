package org.osm.query.rdf.blazegraph;

import com.bigdata.rdf.vocab.BaseVocabularyDecl;
import org.osm.query.rdf.common.uri.CommonTags;
import org.osm.query.rdf.common.uri.MetaFields;
import org.osm.query.rdf.common.uri.OSM;

import java.util.LinkedList;
import java.util.List;

/**
 * Vocabulary containing the URIs from
 * {@linkplain org.wikidata.query.rdf.common.uri.Ontology} that are imported
 * into Blazegraph.
 */
public class OsmCommonTagsVocabularyDecl extends BaseVocabularyDecl {

    /**
     * Get the list of URIs we will import.
     * @return
     */
    private static List<String> getUriList() {
        List<String> uriList = new LinkedList<String>();
        for (String v: MetaFields.VALUES) {
            uriList.add(OSM.META + v);
        }
        for (String v: CommonTags.VALUES) {
            uriList.add(OSM.TAG + v);
        }
        return uriList;
    }

    public OsmCommonTagsVocabularyDecl() {
        super(getUriList().toArray());
    }
}
