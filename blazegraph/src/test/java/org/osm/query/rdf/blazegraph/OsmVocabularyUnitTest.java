package org.osm.query.rdf.blazegraph;

import com.bigdata.rdf.internal.impl.TermId;
import com.bigdata.rdf.internal.impl.uri.VocabURIByteIV;
import com.bigdata.rdf.model.BigdataStatement;
import org.junit.Test;
import org.osm.query.rdf.common.uri.CommonRoles;
import org.osm.query.rdf.common.uri.CommonTags;
import org.osm.query.rdf.common.uri.MetaFields;
import org.osm.query.rdf.common.uri.OSM;

import static org.hamcrest.Matchers.instanceOf;

public class OsmVocabularyUnitTest extends AbstractRandomizedBlazegraphTestBase {
    @Test
    public void nodeIsByte() {
        BigdataStatement statement = roundTrip(OSM.NODE, OSM.TAG + "tag", OSM.NODE);
        assertThat(statement.getSubject().getIV(), instanceOf(VocabURIByteIV.class));
        assertThat(statement.getObject().getIV(), instanceOf(VocabURIByteIV.class));
    }

    @Test
    public void wayIsByte() {
        BigdataStatement statement = roundTrip(OSM.WAY, OSM.TAG + "tag", OSM.WAY);
        assertThat(statement.getSubject().getIV(), instanceOf(VocabURIByteIV.class));
        assertThat(statement.getObject().getIV(), instanceOf(VocabURIByteIV.class));
    }

    @Test
    public void relIsByte() {
        BigdataStatement statement = roundTrip(OSM.REL, OSM.TAG + "tag", OSM.REL);
        assertThat(statement.getSubject().getIV(), instanceOf(VocabURIByteIV.class));
        assertThat(statement.getObject().getIV(), instanceOf(VocabURIByteIV.class));
    }

    @Test
    public void roleIsByte() {
        BigdataStatement statement = roundTrip(OSM.ROLE, OSM.TAG + "tag", OSM.ROLE);
        assertThat(statement.getSubject().getIV(), instanceOf(VocabURIByteIV.class));
        assertThat(statement.getObject().getIV(), instanceOf(VocabURIByteIV.class));
    }

    @Test
    public void commonTagIsByte() {
        BigdataStatement statement = roundTrip(OSM.NODE, "osmtag:" + CommonTags.VALUES[0], OSM.NODE);
        assertThat(statement.getPredicate().getIV(), instanceOf(VocabURIByteIV.class));
    }

    @Test
    public void uncommonTagIsNotByte() {
        BigdataStatement statement = roundTrip(OSM.NODE, "osmtag:abcdef0987", OSM.NODE);
        assertThat(statement.getPredicate().getIV(), instanceOf(TermId.class));
    }

    @Test
    public void MetaFieldIsByte() {
        BigdataStatement statement = roundTrip(OSM.NODE, "osmmeta:" + MetaFields.VALUES[0], OSM.NODE);
        assertThat(statement.getPredicate().getIV(), instanceOf(VocabURIByteIV.class));
    }

    @Test
    public void commonRoleIsByte() {
        BigdataStatement statement = roundTrip(OSM.NODE, "osmrole:" + CommonRoles.VALUES[0], OSM.NODE);
        assertThat(statement.getPredicate().getIV(), instanceOf(VocabURIByteIV.class));
    }

    @Test
    public void uncommonRoleIsNotByte() {
        BigdataStatement statement = roundTrip(OSM.NODE, "osmrole:abcdef0987", OSM.NODE);
        assertThat(statement.getPredicate().getIV(), instanceOf(TermId.class));
    }
}
