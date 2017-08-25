package org.osm.query.rdf.common.uri;

/**
 * A list of known meta fields.
 */
public final class MetaFields {

    private MetaFields() {
    }

    /**
     * List of values. Order is important.
     */
    public static final String[] VALUES = new String[]{
        "type",
        "has",
        "version",
        "changeset",
        "timestamp",
        "user",
        "badkey",
        "isClosed",
        "loc",
        "loc:error",
    };
}
