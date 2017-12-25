package org.osm.query.rdf.blazegraph.tabular;

import org.wikidata.query.rdf.blazegraph.AbstractRandomizedBlazegraphTestBase;

import com.bigdata.rdf.sparql.ast.DummyConstantNode;
import com.bigdata.rdf.sparql.ast.JoinGroupNode;
import com.bigdata.rdf.sparql.ast.StatementPatternNode;
import com.bigdata.rdf.sparql.ast.VarNode;
import com.bigdata.rdf.store.BD;

import static org.osm.query.rdf.blazegraph.tabular.TabularServiceFactory.columnNameToURI;
import static org.osm.query.rdf.blazegraph.tabular.TabularServiceFactory.paramNameToURI;

public class TabularBaseUnitTest extends AbstractRandomizedBlazegraphTestBase {
    public void addInputParam(JoinGroupNode jgn, String name, Boolean value) {
        jgn.addArg(new StatementPatternNode(
                createURI(BD.SERVICE_PARAM),
                createURI(paramNameToURI(name)),
                new DummyConstantNode(store().getLexiconRelation().getValueFactory().createLiteral(value))));
    }

    public void addInputParam(JoinGroupNode jgn, String name, String value) {
        jgn.addArg(new StatementPatternNode(
                createURI(BD.SERVICE_PARAM),
                createURI(paramNameToURI(name)),
                createConstant(value)));
    }

    public void addOutputParam(JoinGroupNode jgn, String varName, String columnName, String type) {
        jgn.addArg(new StatementPatternNode(
                new VarNode(varName),
                createURI(columnNameToURI(columnName)),
                createConstant(type)
        ));
    }
}
