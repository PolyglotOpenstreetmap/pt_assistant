// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.ptl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.data.osm.Relation;

/**
 * Unit tests for {@link PublicTransportLayer}.
 */
class PublicTransportLayerTest {

    private static Relation route(Map<String, String> tags) {
        final Relation r = new Relation();
        r.setKeys(tags);
        return r;
    }

    @Test
    void routeWithoutColourIsGreen() {
        assertEquals(Color.GREEN, PublicTransportLayer.routeColor(route(Map.of("type", "route"))));
    }

    @Test
    void routeUsesItsColourTag() {
        assertEquals(new Color(0xFF, 0x00, 0x00),
                PublicTransportLayer.routeColor(route(Map.of("type", "route", "colour", "#ff0000"))));
    }

    @Test
    void unreadableColourFallsBackToGreen() {
        assertEquals(Color.GREEN,
                PublicTransportLayer.routeColor(route(Map.of("type", "route", "colour", "not a colour"))));
    }
}
