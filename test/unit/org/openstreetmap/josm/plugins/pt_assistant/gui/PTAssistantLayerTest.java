// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.gui;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.image.BufferedImage;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.testutils.annotations.BasicPreferences;
import org.openstreetmap.josm.testutils.annotations.Main;

/**
 * Unit tests for {@link PTAssistantLayer}: a hidden layer must not draw or record anything.
 */
@BasicPreferences
@Main
class PTAssistantLayerTest {

    private PTAssistantLayer layer;
    private Relation route;

    @BeforeEach
    void setUp() {
        layer = new PTAssistantLayer();
        route = new Relation();
        route.setKeys(Map.of("type", "route", "route", "bicycle"));
    }

    @AfterEach
    void tearDown() {
        MainApplication.getLayerManager().removeLayer(layer);
    }

    @Test
    void repaintOfAHiddenLayerRecordsNothing() {
        layer.setVisible(false);
        assertDoesNotThrow(() -> layer.repaint(route));
        assertNull(layer.getModeOfTravel());
    }

    @Test
    void repaintOfAVisibleLayerRecordsTheModeOfTravel() {
        layer.setVisible(true);
        assertDoesNotThrow(() -> layer.repaint(route));
        assertEquals("bicycle", layer.getModeOfTravel());
    }

    @Test
    void paintOfAHiddenLayerDrawsNothing() {
        layer.setVisible(false);
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        // A hidden layer returns before touching the map view, so a null view is never dereferenced.
        assertDoesNotThrow(() -> layer.paint(image.createGraphics(), null, null));
    }
}
