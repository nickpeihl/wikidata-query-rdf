package org.osm.query.rdf.common.uri;

/**
 * Uris osm uses that are relative to the osm instance.
 */
public final class OSM {

    /**
     * The root of the osm uris.
     */
    public static final String ROOT = "https://www.openstreetmap.org";

    /**
     * Uri prefix osm uses for nodes.
     */
    public static final String NODE = ROOT + "/node/";

    /**
     * Uri prefix osm uses for ways.
     */
    public static final String WAY = ROOT + "/way/";

    /**
     * Uri prefix osm uses for relations.
     */
    public static final String REL = ROOT + "/relation/";

    /**
     * Uri prefix osm uses for meta fields. Examples: osmm:user or osmm:version
     */
    public static final String META = ROOT + "/meta/";

    /**
     * Uri prefix osm uses for object tags. Examples: osmt:name
     */
    public static final String TAG = "https://wiki.openstreetmap.org/wiki/Key:";

    /**
     * Predicate Uri to store how often a page has been viewed.
     * (the value is usually relative to the pageviews of other pages, not in of itself)
     */
    public static final String PAGEVIEWS = "https://dumps.wikimedia.org/other/pageviews/";

    private OSM() {
    }
}
