// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.ptl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.testutils.annotations.BasicPreferences;
import org.openstreetmap.josm.testutils.annotations.Projection;

/**
 * Unit tests for {@link DistanceBetweenStops}.
 */
@BasicPreferences
@Projection
class DistanceBetweenStopsTest {

    private static Node stop(DataSet ds, String name, double lat) {
        final Node n = new Node(new LatLon(lat, 0));
        n.setKeys(Map.of("name", name));
        ds.addPrimitive(n);
        return n;
    }

    private static Relation route(DataSet ds) {
        final Relation r = new Relation();
        r.setKeys(Map.of("type", "route", "route", "bus", "public_transport:version", "2"));
        ds.addPrimitive(r);
        return r;
    }

    @Test
    void stopsWithoutAnyWayDoNotFail() {
        final DataSet ds = new DataSet();
        final Relation route = route(ds);
        route.addMember(new RelationMember("stop", stop(ds, "Alpha", 0)));
        route.addMember(new RelationMember("stop", stop(ds, "Beta", 0.001)));

        // No distance can be measured, so the average has nothing to divide by.
        final String text = assertDoesNotThrow(() -> DistanceBetweenStops.calculateDistanceBetweenStops(route));
        assertTrue(text.contains("Alpha"));
        assertTrue(text.contains("Beta"));
    }

    @Test
    void routeWithoutStopsDoesNotFail() {
        final DataSet ds = new DataSet();
        final Relation route = route(ds);
        assertDoesNotThrow(() -> DistanceBetweenStops.calculateDistanceBetweenStops(route));
    }

    @Test
    void stopsOnAWayGetADistance() {
        final DataSet ds = new DataSet();
        final Node a = stop(ds, "Alpha", 0);
        final Node b = stop(ds, "Beta", 0.001);
        final Way way = new Way();
        way.addNode(a);
        way.addNode(b);
        ds.addPrimitive(way);
        final Relation route = route(ds);
        route.addMember(new RelationMember("stop", a));
        route.addMember(new RelationMember("stop", b));
        route.addMember(new RelationMember("", way));

        final String text = DistanceBetweenStops.calculateDistanceBetweenStops(route);
        assertTrue(text.contains("Alpha"));
        assertTrue(text.contains("Beta"));
        assertFalse(text.contains("NaN"));
    }
}
