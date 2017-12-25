package org.osm.query.rdf.blazegraph.tabular;

import static java.util.Objects.requireNonNull;

import java.util.Set;

import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.URI;
import org.osm.query.rdf.common.uri.Sophox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.query.rdf.common.uri.Ontology;

import com.bigdata.bop.IVariable;
import com.bigdata.rdf.sparql.ast.eval.AbstractServiceFactory;
import com.bigdata.rdf.sparql.ast.eval.ServiceParams;
import com.bigdata.rdf.sparql.ast.service.BigdataNativeServiceOptions;
import com.bigdata.rdf.sparql.ast.service.BigdataServiceCall;
import com.bigdata.rdf.sparql.ast.service.IServiceOptions;
import com.bigdata.rdf.sparql.ast.service.ServiceCallCreateParams;
import com.bigdata.rdf.sparql.ast.service.ServiceNode;
import com.bigdata.rdf.sparql.ast.service.ServiceRegistry;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Service factory for loading any external data in a tabular format.
 * Service call looks like:
 * <p>
 * SELECT * WHERE {
 *   SERVICE wikibase:tabular {
 *     # Input params
 *     bd:serviceParam wikibase:url "https://example.com/.../TSLA.csv" .
 *     bd:serviceParam wikibase:firstRowIsHeader true .
 *     # Output params
 *     ?date   tabular:Date 'string' .
 *     ?volume tabular:Volume 'double' .
 *   }
 * }
 * <p>
 * }
 */
@SuppressWarnings("checkstyle:classfanoutcomplexity")
@SuppressFBWarnings(value = "FCCD_FIND_CLASS_CIRCULAR_DEPENDENCY", justification = "low priority to fix")
public class TabularServiceFactory extends AbstractServiceFactory {
    private static final Logger log = LoggerFactory.getLogger(TabularServiceFactory.class);

    /**
     * Options configuring this service as a native Blazegraph service.
     */
    public static final BigdataNativeServiceOptions SERVICE_OPTIONS = new BigdataNativeServiceOptions();
    /**
     * The URI service key.
     */
    public static final URI SERVICE_KEY = new URIImpl(Ontology.NAMESPACE + "tabular");
    /**
     * Endpoint hostname parameter name.
     */
    public static final URI ENDPOINT_KEY = new URIImpl(Ontology.NAMESPACE + "endpoint");
    /**
     * Namespace for the input parameters.
     */
    public static final String PARAM_NAMESPACE = Ontology.NAMESPACE;
    /**
     * Namespace for the column name parameters.
     */
    public static final String COLUMN_NAMESPACE = Sophox.NAMESPACE + "column:";

    public TabularServiceFactory() {
    }

    @Override
    public IServiceOptions getServiceOptions() {
        return SERVICE_OPTIONS;
    }

    /**
     * Register the service so it is recognized by Blazegraph.
     */
    public static void register() {
        ServiceRegistry reg = ServiceRegistry.getInstance();
        reg.add(SERVICE_KEY, new TabularServiceFactory());
        reg.addWhitelistURL(SERVICE_KEY.toString());
    }

    @Override
    public BigdataServiceCall create(ServiceCallCreateParams params, final ServiceParams serviceParams) {
        ServiceNode serviceNode = params.getServiceNode();

        requireNonNull(serviceNode, "Missing service node?");

        return new TabularServiceCall(
                new TabularParamParser(serviceNode),
                params.getClientConnectionManager(),
                params.getTripleStore().getLexiconRelation()
        );
    }

    @Override
    public Set<IVariable<?>> getRequiredBound(final ServiceNode serviceNode) {
        return new TabularParamParser(serviceNode).getRequiredBound();
    }

    /**
     * Create predicate parameter URI from name.
     *
     * @param name
     * @return
     */
    public static URI paramNameToURI(String name) {
        return new URIImpl(PARAM_NAMESPACE + name);
    }

    /**
     * Create predicate parameter URI from name.
     *
     * @param name
     * @return
     */
    public static URI columnNameToURI(String name) {
        return new URIImpl(COLUMN_NAMESPACE + name);
    }
}
