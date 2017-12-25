package org.osm.query.rdf.blazegraph.tabular;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jetty.client.HttpClient;
import org.junit.Before;
import org.junit.Test;

import com.bigdata.bop.bindingSet.HashBindingSet;
import com.bigdata.bop.IBindingSet;
import com.bigdata.bop.IConstant;
import com.bigdata.rdf.internal.IV;
import com.bigdata.rdf.model.BigdataValueFactory;
import com.bigdata.rdf.sparql.ast.JoinGroupNode;
import com.bigdata.rdf.sparql.ast.service.ServiceNode;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.wikidata.query.rdf.blazegraph.BigdataValuesHelper.makeVariable;
import static org.wikidata.query.rdf.blazegraph.Matchers.binds;
import static org.wikidata.query.rdf.blazegraph.Matchers.bindsItem;

public class TabularServiceCallUnitTest extends TabularBaseUnitTest {
    private IBindingSet binding;
    private BigdataValueFactory vf;

    @Before
    public void createFixtures() {
//        template = mock(TabularParamParser.class);
        binding = new HashBindingSet();
        vf = store().getValueFactory();
    }


    @Test
    public void testResults() throws Exception {

        JoinGroupNode jgn = new JoinGroupNode();
        addInputParam(jgn, "url", "http://example.com");
        addInputParam(jgn, "firstRowIsHeader", true);

        addOutputParam(jgn, "strVar", "StrColumn", "string");
        addOutputParam(jgn, "intVar", "IntColumn", "integer");
        addOutputParam(jgn, "intVarAsStr", "IntColumn", "string");
        addOutputParam(jgn, "dblVar", "DblColumn", "double");
        addOutputParam(jgn, "uriVar", "UriColumn", "uri");
//        addOutputParam(jgn, "dateVar", "DateColumn", "date:yyyy-MM-dd");

        TabularParamParser params = new TabularParamParser(new ServiceNode(createConstant("test"), jgn));
        TabularServiceCall call = createCall(params);

        String csvContent = "DblColumn,IntColumn,StrColumn,DateColumn,UriColumn\n" +
                "3.14,42,Universe,2000-01-01,http://example.org\n" +
                ".13,12,Test,2017-10-28,https://example.org\n";
        InputStream responseStream = new ByteArrayInputStream(csvContent.getBytes("UTF-8"));

        IBindingSet[] results = toArray(call.parseResponse(responseStream, binding));

        assertThat(results.length, equalTo(2));

        assertThat(results[0], binds("strVar", "Universe"));
        assertThat(results[0], binds("intVarAsStr", "42"));
        assertThat(results[0], bindsItem("uriVar", "http://example.org"));
        assertLiteralEquals(results[0], "intVar", 42);
        assertLiteralEquals(results[0], "dblVar", 3.14);

        assertThat(results[1], binds("strVar", "Test"));
        assertThat(results[1], binds("intVarAsStr", "12"));
        assertThat(results[1], bindsItem("uriVar", "https://example.org"));
        assertLiteralEquals(results[1], "intVar", 12);
        assertLiteralEquals(results[1], "dblVar", .13);
    }

    private void assertLiteralEquals(IBindingSet result, String varName, Object expectedValue) {
        IV iv = ((IConstant<IV>) result.get(makeVariable(varName))).get();
        Object val = iv.getInlineValue();
        assertEquals(val, expectedValue);
    }

/*
    @Test
    public void testFixedParams() throws Exception {
        Map<String, String> fixedMap = ImmutableMap.of("test1", "val1", "test2",
                "val2");
        when(template.getFixedParams()).thenReturn(fixedMap);

        Map<String, String> params = createCall().getRequestParams(binding);
        assertThat(params.entrySet(), equalTo(fixedMap.entrySet()));
    }

    @Test
    public void testInputParams() throws Exception {
        Map<String, IVariableOrConstant> inputVars = new HashMap<>();
        inputVars.put("const", makeConstant(vf, "val1"));
        inputVars.put("var", makeVariable("boundVar"));
        inputVars.put("varDefault", null);
        inputVars.put("emptyDefault", null);

        binding.set(makeVariable("boundVar"), makeConstant(vf, "boundValue"));

        when(template.getInputDefault("varDefault")).thenReturn("defaultValue");
        when(template.getInputDefault("emptyDefault")).thenReturn("");

        Map<String, String> params = createCall(inputVars).getRequestParams(binding);
        assertThat(params, hasEntry("const", "val1"));
        assertThat(params, hasEntry("varDefault", "defaultValue"));
        assertThat(params, hasEntry("var", "boundValue"));
        assertThat(params, not(hasKey("emptyDefault")));
    }

    @Test
    public void testInputParamsUnboundDefault() throws Exception {
        // Variable declared as bound but isn't actually bound - fallback to default
        Map<String, IVariableOrConstant> inputVars = new HashMap<>();
        inputVars.put("var", makeVariable("boundVar"));
        when(template.getInputDefault("var")).thenReturn("defaultValue");
        Map<String, String> params = createCall(inputVars).getRequestParams(binding);
        assertThat(params, hasEntry("var", "defaultValue"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInputParamsUnbound() throws Exception {
        // Variable declared as bound but isn't actually bound
        Map<String, IVariableOrConstant> inputVars = new HashMap<>();
        when(template.isRequiredParameter("var")).thenReturn(true);
        inputVars.put("var", makeVariable("boundVar"));
        Map<String, String> params = createCall(inputVars).getRequestParams(binding);
    }

    @Test
    public void testInputParamsUnboundNotRequired() throws Exception {
        // Variable declared as bound but isn't actually bound
        // If it's not required it's OK
        Map<String, IVariableOrConstant> inputVars = new HashMap<>();
        when(template.isRequiredParameter("var")).thenReturn(false);
        inputVars.put("var", makeVariable("boundVar"));
        Map<String, String> params = createCall(inputVars).getRequestParams(binding);
        assertFalse(params.containsKey("var"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInputParamsMissing() throws Exception {
        // Variable declared but has no binding
        Map<String, IVariableOrConstant> inputVars = new HashMap<>();
        when(template.isRequiredParameter("var")).thenReturn(true);
        inputVars.put("var", null);
        Map<String, String> params = createCall(inputVars).getRequestParams(binding);
    }

    @Test
    public void testEmptyVars() throws Exception {
        List<OutputVariable> outputVars = Lists.newArrayList();
        InputStream responseStream = new ByteArrayInputStream("not even xml".getBytes("UTF-8"));
        Iterator<IBindingSet> results = createCall(outputVars).parseResponse(responseStream, binding);
        assertNull(results);
    }

    @Test
    public void testEmptyResult() throws Exception {
        List<OutputVariable> outputVars = ImmutableList
                .of(new OutputVariable(makeVariable("var"), "@test"));
        InputStream responseStream = new ByteArrayInputStream("<result></result>".getBytes("UTF-8"));
        when(template.getItemsPath()).thenReturn("/api/result");
        Iterator<IBindingSet> results = createCall(outputVars).parseResponse(responseStream, binding);
        assertNull(results);
    }


    @Test(expected = SAXException.class)
    public void testResultsBadXML() throws Exception {
        List<OutputVariable> outputVars = ImmutableList
                .of(new OutputVariable(makeVariable("var"), "@test"));
        InputStream responseStream = new ByteArrayInputStream("Fatal error: I am a teapot".getBytes("UTF-8"));
        when(template.getItemsPath()).thenReturn("/api/result");
        IBindingSet[] results = toArray(createCall(outputVars).parseResponse(responseStream, binding));
    }

    @Test
    public void testResultsMissingVar() throws Exception {
        List<OutputVariable> outputVars = ImmutableList.of(
                new OutputVariable(makeVariable("var"), "@name"),
                new OutputVariable(makeVariable("data"), "text()"),
                new OutputVariable(makeVariable("header"), "/api/header/@value"));
        when(template.getItemsPath()).thenReturn("/api/result");
        InputStream responseStream = new ByteArrayInputStream(
                "<api><header value=\"heading\"></header><result name=\"result1\">datadata</result><result>we need moar data</result></api>"
                        .getBytes("UTF-8"));

        IBindingSet[] results = toArray(createCall(outputVars).parseResponse(responseStream, binding));
        assertThat(results.length, equalTo(2));
        assertThat(results[0], binds("var", "result1"));
        assertThat(results[0], binds("data", "datadata"));
        assertThat(results[0], binds("header", "heading"));
        assertThat(results[1], notBinds("var"));
        assertThat(results[1], binds("data", "we need moar data"));
        assertThat(results[1], binds("header", "heading"));
    }
*/

    private IBindingSet[] toArray(Iterator<IBindingSet> iterator) {
        if (iterator == null) return null;
        List<IBindingSet> result = new ArrayList<>();
        iterator.forEachRemaining(result::add);
        return result.toArray(new IBindingSet[result.size()]);
    }


    private TabularServiceCall createCall(TabularParamParser paramParser) throws Exception {
        HttpClient mockClient = mock(HttpClient.class);
        return new TabularServiceCall(paramParser, mockClient, store().getLexiconRelation());
    }

}
