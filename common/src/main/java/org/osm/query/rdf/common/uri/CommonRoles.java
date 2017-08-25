package org.osm.query.rdf.common.uri;

/**
 * A list of relation roles that occure on more than a 100k relations.
 */
public final class CommonRoles {

    /**
     * List of values. Order is important.
     */
    public static final String[] VALUES = new String[]{
        "",
        "_",
        "outer",
        "inner",
        "house",
        "forward",
        "stop",
        "platform",
        "to",
        "from",
        "via",
        "street",
        "backward",
        "main_stream",
        "admin_centre",
        "subarea",
    };

    private CommonRoles() {
    }
}
