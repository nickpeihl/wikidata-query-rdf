package org.osm.query.rdf.blazegraph;

import org.wikidata.query.rdf.blazegraph.WikibaseVocabulary;

/**
 * Versioned vocabulary classes for wikibase. All classes need a namespace and a
 * default constructor or Blazegraph blows up on them.
 */
public class OsmVocabulary {

    /**
     * Current vocabulary class.
     */
    public static final Class VOCABULARY_CLASS = V002.class;

    protected OsmVocabulary() {
        // prevents calls from subclass
        throw new UnsupportedOperationException();
    }

    /*
     * Vocabulary classes
     */

    /**
     * Vocabulary class for BG 2x.
     * Inherits Wikidata vocabulary.
     */
    public static class V001 extends WikibaseVocabulary.V002 {
        public V001() {
        }

        public V001(String namespace) {
            super(namespace);
        }

        @Override
        protected void addValues() {
            addDecl(new OsmUrisVocabularyDecl());
            addDecl(new OsmCommonTagsVocabularyDecl());
            super.addValues();
        }
    }

    /**
     * Vocabulary class for BG 2x.
     * Inherits Wikidata vocabulary.
     */
    public static class V002 extends WikibaseVocabulary.V003 {
        public V002() {
        }

        public V002(String namespace) {
            super(namespace);
        }

        @Override
        protected void addValues() {
            addDecl(new OsmUrisVocabularyDecl());
            addDecl(new OsmCommonTagsVocabularyDecl());
            super.addValues();
        }
    }

}
