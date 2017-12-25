package org.osm.query.rdf.blazegraph.tabular;

import java.util.List;

import org.junit.Test;
import org.osm.query.rdf.blazegraph.tabular.TabularParamParser.OutputVariable;

import com.bigdata.rdf.sparql.ast.JoinGroupNode;
import com.bigdata.rdf.sparql.ast.service.ServiceNode;

import static org.hamcrest.Matchers.equalTo;

public class TabularParamParserUnitTest extends TabularBaseUnitTest {

    @Test
    public void testServiceOutput() throws Exception {
        JoinGroupNode patterns = new JoinGroupNode();
        addInputParam(patterns, "url", "http://example.com");

        addOutputParam(patterns, "strVar", "StrColumn", "string");
        addOutputParam(patterns, "intVar", "IntColumn", "integer");
        addOutputParam(patterns, "intVarAsStr", "IntColumn", "string");
        addOutputParam(patterns, "dblVar", "DblColumn", "double");
//       addOutputParam(pattern"dateVar", ("DateColumn","date:yyyy-MM-dd");
        addOutputParam(patterns, "uriVar", "UriColumn", "uri");

        ServiceNode serviceNode = new ServiceNode(createConstant("test"), patterns);

        List<OutputVariable> outputs = new TabularParamParser(serviceNode).getOutputVariables();

        int idx = 0;

        // Pre-defined variable
        OutputVariable var = outputs.get(idx++);
        assertThat(var.getVarName(), equalTo("strVar"));
        assertThat(var.getColumn(), equalTo("StrColumn"));

        var = outputs.get(idx++);
        assertThat(var.getVarName(), equalTo("intVar"));
        assertThat(var.getColumn(), equalTo("IntColumn"));

        var = outputs.get(idx++);
        assertThat(var.getVarName(), equalTo("intVarAsStr"));
        assertThat(var.getColumn(), equalTo("IntColumn"));

        var = outputs.get(idx++);
        assertThat(var.getVarName(), equalTo("dblVar"));
        assertThat(var.getColumn(), equalTo("DblColumn"));

//        var = outputs.get(idx++);
//        assertThat(var.getVarName(), equalTo("dateVar"));
//        assertThat(var.getColumn(), equalTo("DateColumn"));

        var = outputs.get(idx++);
        assertThat(var.getVarName(), equalTo("uriVar"));
        assertThat(var.getColumn(), equalTo("UriColumn"));

        assertThat(outputs.size(), equalTo(idx));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMissingInputVar() throws Exception {
        JoinGroupNode patterns = new JoinGroupNode();
        ServiceNode serviceNode = new ServiceNode(createConstant("test"), patterns);

        new TabularParamParser(serviceNode);
    }
}
