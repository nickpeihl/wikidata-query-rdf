package org.osm.query.rdf.blazegraph.tabular;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.osm.query.rdf.blazegraph.tabular.TabularServiceFactory.columnNameToURI;
import static org.osm.query.rdf.blazegraph.tabular.TabularServiceFactory.paramNameToURI;
import static org.wikidata.query.rdf.blazegraph.BigdataValuesHelper.makeConstant;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.URI;

import com.bigdata.bop.IBindingSet;
import com.bigdata.bop.IConstant;
import com.bigdata.bop.IVariable;
import com.bigdata.bop.IVariableOrConstant;
import com.bigdata.rdf.internal.IV;
import com.bigdata.rdf.model.BigdataValueFactory;
import com.bigdata.rdf.sparql.ast.eval.ServiceParams;
import com.bigdata.rdf.sparql.ast.GraphPatternGroup;
import com.bigdata.rdf.sparql.ast.IGroupMemberNode;
import com.bigdata.rdf.sparql.ast.service.ServiceNode;
import com.bigdata.rdf.sparql.ast.StatementPatternNode;
import com.bigdata.rdf.sparql.ast.TermNode;
import com.bigdata.rdf.store.BD;

/**
 * This class represents tabular service parameters.
 */
public class TabularParamParser {

    // Overpass QL: Parse the "[timeout:25]" into a number (if exists)
    private static final Pattern timeoutPattern = Pattern.compile("\\[timeout:(\\d+)]", Pattern.CASE_INSENSITIVE);

    private final Set<IVariable<?>> requiredBound = new HashSet<>(4);
    private final List<OutputVariable> outputVariables;
    private final IVariableOrConstant url;
    private final IVariableOrConstant otQuery;

    private final IVariableOrConstant format;
    private final IVariableOrConstant commentMarker;
    private final IVariableOrConstant delimiter;
    private final IVariableOrConstant escape;
    private final IVariableOrConstant firstRowIsHeader;
    private final IVariableOrConstant ignoreSurroundingSpaces;
    private final IVariableOrConstant quote;
    private final IVariableOrConstant useColumnNames;

    private long requestTimeout = 30;

    /**
     * Parse list of bindings from input and output params to specific variables or constants.
     */
    public TabularParamParser(ServiceNode serviceNode) {
        ServiceParams serviceParams = serviceParamsFromNode(serviceNode);

        url = parseInputParam("url", serviceParams);
        otQuery = parseInputParam("otQuery", serviceParams);
        if ((url == null) == (otQuery == null)) {
            throw new IllegalArgumentException("Parameter wikibase:url or wikibase:otQuery must be bound, but not both");
        }

        format = parseInputParam("format", serviceParams);
        firstRowIsHeader = parseInputParam("firstRowIsHeader", serviceParams);
        ignoreSurroundingSpaces = parseInputParam("ignoreSurroundingSpaces", serviceParams);
        commentMarker = parseInputParam("commentMarker", serviceParams);
        delimiter = parseInputParam("delimiter", serviceParams);
        escape = parseInputParam("escape", serviceParams);
        quote = parseInputParam("quote", serviceParams);
        useColumnNames = parseInputParam("useColumnNames", serviceParams);

        outputVariables = getOutputVars(serviceNode);
    }

    /**
     * Get service params from Service Node.
     * FIXME: copypaste from MWApiServiceFactory.java
     * FIXME: copypaste from ServiceParams.java, should be integrated there
     *
     * @param serviceNode
     * @return
     */
    public static ServiceParams serviceParamsFromNode(final ServiceNode serviceNode) {
        requireNonNull(serviceNode, "Service node is null?");

        final GraphPatternGroup<IGroupMemberNode> group = serviceNode.getGraphPattern();
        requireNonNull(serviceNode, "Group node is null?");

        final ServiceParams serviceParams = new ServiceParams();
        final Iterator<IGroupMemberNode> it = group.iterator();

        while (it.hasNext()) {
            final IGroupMemberNode node = it.next();

            if (node instanceof StatementPatternNode) {
                final StatementPatternNode sp = (StatementPatternNode) node;
                final TermNode s = sp.s();

                if (s.isConstant() && BD.SERVICE_PARAM.equals(s.getValue())) {
                    if (sp.p().isVariable()) {
                        throw new RuntimeException("not a valid service param triple pattern, "
                                + "predicate must be constant: " + sp);
                    }

                    final URI param = (URI) sp.p().getValue();
                    serviceParams.add(param, sp.o());
                }
            }
        }

        return serviceParams;
    }

    /**
     * Add input var from the service params to the map.
     *
     * @param varName Parameter name
     */
    private IVariableOrConstant parseInputParam(String varName, ServiceParams sp) {
        TermNode var = sp.get(paramNameToURI(varName), null);
        if (var == null) {
            return null;
        }

        if (var.isVariable()) {
            requiredBound.add((IVariable) var.getValueExpression());
        } else if (!var.isConstant()) {
            throw new IllegalArgumentException("Parameter " + varName + " must be constant or variable");
        }

        return var.getValueExpression();
    }

    /**
     * Create map of output variables from template and service params.
     */
    private static List<OutputVariable> getOutputVars(final ServiceNode serviceNode) {
        List<OutputVariable> vars = new ArrayList<>();

        final GraphPatternGroup<IGroupMemberNode> group = serviceNode.getGraphPattern();
        requireNonNull(serviceNode, "Group node is null?");
        String columnPrefix = columnNameToURI("").stringValue();

        group.iterator().forEachRemaining(node -> {
            // ?variable tabular:Close 'double'
            if (node instanceof StatementPatternNode) {
                final StatementPatternNode sp = (StatementPatternNode) node;

                if (sp.s().isVariable() && sp.p().isConstant() && sp.o().isConstant()) {
                    String fmt = sp.o().getValueExpression().get().stringValue();
                    IVariable var = (IVariable) sp.s().getValueExpression();
                    IV column = sp.p().getValueExpression().get();
                    String columnName = column.stringValue().substring(columnPrefix.length());
                    vars.add(new OutputVariable(var, columnName, fmt));
                }
            }
        });

        return vars;
    }

    private static String parse(IVariableOrConstant var, IBindingSet binding) {
        if (var == null) {
            return null;
        }
        IV iv = (IV) var.get(binding);
        if (iv == null) {
            return null;
        }
        return iv.stringValue();
    }

    public String getUrl(IBindingSet binding) {
        boolean useOT = otQuery != null;
        String str = parse(useOT ? otQuery : url, binding);
        if (str == null) {
            throw new IllegalArgumentException("Parameter wikibase:" + (useOT ? "otQuery" : "url") + " is not set");
        }
        try {
            if (useOT) {
                updateRequestTimeout(str);

                // FIXME: should be configurable parameter
                str = "http://overpass-api.de/api/interpreter?data=" + URLEncoder.encode(str, UTF_8.name());
            }
            URL url = new URL(str);
            if (!"https".equals(url.getProtocol()) && !"http".equals(url.getProtocol())) {
                throw new IllegalArgumentException("URL must use https: or http: protocols");
            }
            str = url.toExternalForm();
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Bad url parameter '" + str + "'", e);
        }
        return str;
    }

    public CSVFormat getFormat(IBindingSet binding) {
        String str = parse(format, binding);
        if (str == null) {
            return otQuery != null ? CSVFormat.TDF : CSVFormat.DEFAULT;
        }
        switch (str.toUpperCase(Locale.ROOT)) {
            case "DEFAULT":
                return CSVFormat.DEFAULT;
            case "RFC4180":
                return CSVFormat.RFC4180;
            case "EXCEL":
                return CSVFormat.EXCEL;
            case "TDF":
                return CSVFormat.TDF;
            case "MYSQL":
                return CSVFormat.MYSQL;
            default:
                throw new IllegalArgumentException("Unknown format name '" + str + "'");
        }
    }

    public CSVFormat getFirstRowIsHeader(IBindingSet binding, CSVFormat csvFormat) {
        return parseBoolean(binding, csvFormat, firstRowIsHeader, "firstRowIsHeader",
                otQuery != null ? true : csvFormat.getSkipHeaderRecord(),
                (fmt, val) -> val ? fmt.withHeader().withSkipHeaderRecord(true) : fmt.withSkipHeaderRecord(false));
    }

    public CSVFormat getIgnoreSurroundingSpaces(IBindingSet binding, CSVFormat csvFormat) {
        return parseBoolean(binding, csvFormat, ignoreSurroundingSpaces, "ignoreSurroundingSpaces",
                null, CSVFormat::withIgnoreSurroundingSpaces);
    }

    public CSVFormat getCommentMarker(IBindingSet binding, CSVFormat csvFormat) {
        return parseChar(binding, csvFormat, commentMarker, "commentMarker", CSVFormat::withCommentMarker);
    }

    public CSVFormat getDelimiter(IBindingSet binding, CSVFormat csvFormat) {
        return parseChar(binding, csvFormat, delimiter, "delimiter", CSVFormat::withDelimiter);
    }

    public CSVFormat getEscape(IBindingSet binding, CSVFormat csvFormat) {
        return parseChar(binding, csvFormat, escape, "escape", CSVFormat::withEscape);
    }

    public CSVFormat getQuote(IBindingSet binding, CSVFormat csvFormat) {
        return parseChar(binding, csvFormat, quote, "quote", CSVFormat::withQuote);
    }

    private CSVFormat parseBoolean(IBindingSet binding, CSVFormat csvFormat,
                                   IVariableOrConstant field, String fieldName, Boolean defaultValue,
                                   BiFunction<CSVFormat, Boolean, CSVFormat> setter) {
        Boolean newValue;
        String str = parse(field, binding);
        if (str == null) {
            if (defaultValue == null) {
                // If null, return unchanged
                return csvFormat;
            }
            newValue = defaultValue;
        } else if ("true".equals(str)) {
            newValue = Boolean.TRUE;
        } else if ("false".equals(str)) {
            newValue = Boolean.FALSE;
        } else {
            throw new IllegalArgumentException("Unknown " + fieldName + " value '" + str + "', must be boolean");
        }
        return setter.apply(csvFormat, newValue);
    }

    private CSVFormat parseChar(IBindingSet binding, CSVFormat csvFormat,
                                IVariableOrConstant field, String fieldName,
                                BiFunction<CSVFormat, Character, CSVFormat> setter) {
        String str = parse(field, binding);
        if (str == null) {
            return csvFormat;  // return unchanged
        } else if (str.length() == 1) {
            return setter.apply(csvFormat, str.charAt(0));
        } else {
            throw new IllegalArgumentException("Unknown " + fieldName + " value '" + str + "', must be a single char");
        }
    }

    public boolean getUseColumnNames(IBindingSet binding, CSVFormat csvFormat) {
        String str = parse(useColumnNames, binding);
        if (str == null) {
            return csvFormat.getSkipHeaderRecord();
        }
        switch (str) {
            case "true":
                if (!csvFormat.getSkipHeaderRecord()) {
                    throw new IllegalArgumentException("firstRowIsHeader=true is required for useColumnByName");
                }
                return true;
            case "false":
                return false;
            default:
                throw new IllegalArgumentException("Unknown useColumnNames value '" + str + "'");
        }
    }

    public List<OutputVariable> getOutputVariables() {
        return outputVariables;
    }

    public Set<IVariable<?>> getRequiredBound() {
        return requiredBound;
    }

    private void updateRequestTimeout(String otQuery) {
        // Process Overpass [timeout:nn] pattern
        Matcher matcher = timeoutPattern.matcher(otQuery);
        if (matcher.find()) {
            int timeout = Integer.parseInt(matcher.group(1));
            if (timeout > 0 && timeout < 180) {
                this.requestTimeout = timeout + 10;
            }
        }
    }

    public long getRequestTimeout() {
        return requestTimeout;
    }

    /**
     * Variable in the output of the API.
     */
    public static class OutputVariable {

        /**
         * Original Blazegraph var.
         */
        private final IVariable var;
        /**
         * Column name.
         */
        private final String column;
        /**
         * Function to convert a string into an output constant value.
         */
        private final BiFunction<String, BigdataValueFactory, IConstant> parser;


        OutputVariable(IVariable var, String column, String fmt) {
            this.var = var;
            this.column = column;
            String[] formatParts = fmt.split(":", 2);

            switch (formatParts[0]) {
                case "string":
                    this.parser = (str, vf) -> makeConstant(vf, str);
                    break;
                case "double":
                    this.parser = (str, vf) -> makeConstant(vf, Double.parseDouble(str));
                    break;
                case "integer":
                    this.parser = (str, vf) -> makeConstant(vf, Integer.parseInt(str));
                    break;
//                case "date":
//                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern(formatParts[1]);
//                    this.parser = (str, vf) -> {
//                        String dt = ZonedDateTime.parse(str, dtf).format(DateTimeFormatter.ISO_DATE_TIME);
//                        return makeConstant(vf, dt, XMLSchema.DATETIME);
//                    };
//                    break;
                case "uri":
                case "url":
                    this.parser = (str, vf) -> makeConstant(vf, new URIImpl(str));
                    break;
                default:
                    throw new IllegalArgumentException("Output variable type '" + formatParts[0] + "' is unknown");
            }
        }

        /**
         * Get associated variable.
         */
        public IVariable getVar() {
            return var;
        }

        /**
         * Get column for this variable.
         */
        public String getColumn() {
            return column;
        }

        public int getColumnAsIndex() {
            int columnIndex;
            try {
                columnIndex = Integer.parseInt(column);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Column index '" + column + "' is not a valid integer", ex);
            }
            if (columnIndex < 0) {
                throw new IllegalArgumentException("Column index must be a non-negative integer");
            }
            return columnIndex;
        }

        /**
         * Get associated variable name.
         */
        public String getVarName() {
            return var.getName();
        }

        /**
         * Get associated variable name.
         */
        public IConstant parse(String value, BigdataValueFactory valueFactory) {
            return parser.apply(value, valueFactory);
        }

        @Override
        public String toString() {
            return getColumn() + " → ?" + getVarName();
        }
    }

}
