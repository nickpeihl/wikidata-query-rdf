package org.osm.query.rdf.blazegraph;

import org.junit.Test;
import org.osm.query.rdf.common.uri.CommonTags;
import org.osm.query.rdf.common.uri.MetaFields;
import org.osm.query.rdf.common.uri.OSM;

import com.bigdata.rdf.internal.impl.TermId;
import com.bigdata.rdf.internal.impl.uri.VocabURIByteIV;
import com.bigdata.rdf.model.BigdataStatement;

import static org.hamcrest.Matchers.instanceOf;

public class OsmVocabularyUnitTest extends OsmBlazegraphTestBase {
    @Test
    public void nodeIsByte() {
        BigdataStatement statement = roundTrip(OSM.NODE, OSM.NODE, OSM.NODE);
        assertThat(statement.getSubject().getIV(), instanceOf(VocabURIByteIV.class));
        assertThat(statement.getPredicate().getIV(), instanceOf(VocabURIByteIV.class));
        assertThat(statement.getObject().getIV(), instanceOf(VocabURIByteIV.class));
    }

    @Test
    public void wayIsByte() {
        BigdataStatement statement = roundTrip(OSM.WAY, OSM.WAY, OSM.WAY);
        assertThat(statement.getSubject().getIV(), instanceOf(VocabURIByteIV.class));
        assertThat(statement.getPredicate().getIV(), instanceOf(VocabURIByteIV.class));
        assertThat(statement.getObject().getIV(), instanceOf(VocabURIByteIV.class));
    }

    @Test
    public void relIsByte() {
        BigdataStatement statement = roundTrip(OSM.REL, OSM.REL, OSM.REL);
        assertThat(statement.getSubject().getIV(), instanceOf(VocabURIByteIV.class));
        assertThat(statement.getPredicate().getIV(), instanceOf(VocabURIByteIV.class));
        assertThat(statement.getObject().getIV(), instanceOf(VocabURIByteIV.class));
    }

    @Test
    public void commonTagIsByte() {
        BigdataStatement statement = roundTrip(OSM.NODE, "osmt:" + CommonTags.VALUES[0], OSM.NODE);
        assertThat(statement.getPredicate().getIV(), instanceOf(VocabURIByteIV.class));
    }

    @Test
    public void uncommonTagIsNotByte() {
        BigdataStatement statement = roundTrip(OSM.NODE, "osmt:abcdef0987", OSM.NODE);
        assertThat(statement.getPredicate().getIV(), instanceOf(TermId.class));
    }

    @Test
    public void MetaFieldIsByte() {
        BigdataStatement statement = roundTrip(OSM.NODE, "osmm:" + MetaFields.VALUES[0], OSM.NODE);
        assertThat(statement.getPredicate().getIV(), instanceOf(VocabURIByteIV.class));
    }
}
