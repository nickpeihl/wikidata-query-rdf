package org.osm.query.rdf.blazegraph.tabular;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.osm.query.rdf.blazegraph.tabular.TabularParamParser.OutputVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bigdata.bop.IBindingSet;
import com.bigdata.bop.IConstant;
import com.bigdata.bop.IVariable;
import com.bigdata.rdf.internal.IV;
import com.bigdata.rdf.lexicon.LexiconRelation;
import com.bigdata.rdf.sparql.ast.service.BigdataServiceCall;
import com.bigdata.rdf.sparql.ast.service.IServiceOptions;
import com.bigdata.rdf.sparql.ast.service.MockIVReturningServiceCall;
import com.google.common.base.Charsets;

import cutthecrap.utils.striterators.ICloseableIterator;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Instance of API service call.
 */
@SuppressWarnings({"rawtypes", "unchecked", "checkstyle:classfanoutcomplexity"})
@SuppressFBWarnings(value = "DMC_DUBIOUS_MAP_COLLECTION", justification = "while paramParser could be implemented as a list, the maps makes semantic sense.")
public class TabularServiceCall implements MockIVReturningServiceCall, BigdataServiceCall {
    private static final Logger log = LoggerFactory.getLogger(TabularServiceCall.class);

    /**
     * Query timeout, in seconds.
     * FIXME: make timeout configurable
     */
    private static final int TIMEOUT = 5;
    /**
     * List of input variable bindings.
     */
    private final TabularParamParser paramParser;
    /**
     * HTTP connection.
     */
    private final HttpClient client;
    /**
     * The LexiconRelation for the TripleStore we're working with.
     */
    private final LexiconRelation lexiconRelation;

    /**
     * Call endpoint URL.
     */
//    private final String endpoint;

    TabularServiceCall(TabularParamParser paramParser,
                       HttpClient client,
                       LexiconRelation lexiconRelation
    ) {
        this.paramParser = paramParser;
        this.client = client;
        this.lexiconRelation = lexiconRelation;
    }

    @Override
    public IServiceOptions getServiceOptions() {
        return TabularServiceFactory.SERVICE_OPTIONS;
    }

    @Override
    public ICloseableIterator<IBindingSet> call(IBindingSet[] bindingSets)
            throws Exception {
        return new MultiSearchIterator(bindingSets);
    }

    @Override
    public List<IVariable<IV>> getMockVariables() {
        List<IVariable<IV>> externalVars = new LinkedList<>();
        for (OutputVariable v : paramParser.getOutputVariables()) {
            externalVars.add(v.getVar());
        }
        return externalVars;
    }

    /**
     * Parse tabular response.
     *
     * @param responseStream Response body as stream
     * @param binding        Current binding set.
     * @return Set of resulting bindings, or null if none found.
     * @throws IOException on error
     */
    public Iterator<IBindingSet> parseResponse(InputStream responseStream, IBindingSet binding) throws IOException {
        List<OutputVariable> outputVars = paramParser.getOutputVariables();
        if (outputVars.isEmpty()) {
            return null;
        }

        CSVFormat format = paramParser.getFormat(binding);

        format = paramParser.getCommentMarker(binding, format);
        format = paramParser.getDelimiter(binding, format);
        format = paramParser.getEscape(binding, format);
        format = paramParser.getFirstRowIsHeader(binding, format);
        format = paramParser.getIgnoreSurroundingSpaces(binding, format);
        format = paramParser.getQuote(binding, format);

        boolean useColumnNames = paramParser.getUseColumnNames(binding, format);

        CSVParser parser = format.parse(new InputStreamReader(responseStream, Charsets.UTF_8));
        Map<String, Integer> headerMap = parser.getHeaderMap();
        int[] outputPositions = new int[outputVars.size()];
        int pos = 0;
        for (OutputVariable var : outputVars) {
            if (!useColumnNames) {
                int index = var.getColumnAsIndex();
                if (headerMap != null && index >= headerMap.size()) {
                    throw new IllegalArgumentException(
                            "Column " + index + " does not exist, only " + headerMap.size() + " columns found");
                }
                outputPositions[pos++] = index;
            } else {
                int index = headerMap.getOrDefault(var.getColumn(), -1);
                if (index < 0) {
                    throw new IllegalArgumentException("Column '" + var.getColumn() + "' does not exist");
                }
                outputPositions[pos++] = index;
            }
        }

        return StreamSupport
                .stream(parser.spliterator(), false)
                .map(row -> {
                    IBindingSet res = binding.copy(null);

                    for (int idx = 0; idx < outputPositions.length; idx++) {
                        int columnIdx = outputPositions[idx];
                        if (columnIdx >= row.size()) continue;

                        String value = row.get(columnIdx);
                        IConstant constValue = outputVars.get(idx).parse(value, lexiconRelation.getValueFactory());
                        res.set(outputVars.get(idx).getVar(), constValue);
                    }

                    return res;
                })
                .iterator();
    }

    /**
     * A chunk of calls to resolve labels.
     */
    private class MultiSearchIterator implements ICloseableIterator<IBindingSet> {
        /**
         * Binding sets being resolved in this chunk.
         */
        private final IBindingSet[] bindingSets;
        /**
         * Has this chunk been closed?
         */
        private boolean closed;
        /**
         * Index of the next binding set to handle when next is next called.
         */
        private int i;
        /**
         * Current search result.
         */
        private Iterator<IBindingSet> searchResult;

        MultiSearchIterator(IBindingSet[] bindingSets) {
            this.bindingSets = bindingSets;
            searchResult = doNextSearch();
        }

        @Override
        public boolean hasNext() {
            if (closed) {
                return false;
            }

            if (searchResult == null) {
                return false;
            }

            if (searchResult.hasNext()) {
                return true;
            }

            searchResult = doNextSearch();
            if (searchResult == null) {
                return false;
            } else {
                return searchResult.hasNext();
            }
        }

        /**
         * Produce next search results iterator. Skips over empty results.
         *
         * @return Result iterator or null if no more results.
         */
        private Iterator<IBindingSet> doNextSearch() {
            // Just in case, double check
            if (closed || bindingSets == null || i >= bindingSets.length) {
                searchResult = null;
                return null;
            }
            Iterator<IBindingSet> result;
            do {
                IBindingSet binding = bindingSets[i++];
                result = doSearchFromBinding(binding);
            } while (result != null && !result.hasNext() && i < bindingSets.length);
            if (result != null && result.hasNext()) {
                return result;
            } else {
                return null;
            }
        }

        /**
         * Execute search for one specific binding set.
         *
         * @param binding
         * @return Search results iterator.
         * @throws TimeoutException
         * @throws InterruptedException
         */
        private Iterator<IBindingSet> doSearchFromBinding(IBindingSet binding) {

            final Request req = client.newRequest(paramParser.getUrl(binding));
            req.method(HttpMethod.GET);
            final Response response;
            InputStreamResponseListener listener = new InputStreamResponseListener();
            try {
                req.send(listener);
                response = listener.get(TIMEOUT, TimeUnit.SECONDS);
            } catch (ExecutionException | TimeoutException | InterruptedException e) {
                throw new RuntimeException("TABULAR request failed", e);
            }
            if (response.getStatus() != HttpStatus.OK_200) {
                throw new RuntimeException("Bad response status: " + response.getStatus());
            }

            try {
                return parseResponse(listener.getInputStream(), binding);
            } catch (IOException e) {
                throw new RuntimeException("Failed to parse response", e);
            }
        }

        @Override
        public IBindingSet next() {
            if (closed || searchResult == null) {
                return null;
            }

            if (searchResult.hasNext()) {
                return searchResult.next();
            }

            searchResult = doNextSearch();
            if (searchResult == null || !searchResult.hasNext()) {
                return null;
            } else {
                return searchResult.next();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() {
            closed = true;
        }
    }
}
