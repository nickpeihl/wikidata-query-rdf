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
     * Uri prefix osm uses for meta fields. Examples: osmmeta:user or osmmeta:version
     */
    public static final String META = ROOT + "/meta/";

    /**
     * Uri prefix osm uses for object tags. Examples: osmtag:name
     */
    public static final String TAG = "https://wiki.openstreetmap.org/wiki/Key:";

    private OSM() {
    }
}
