// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.plugins.pt_assistant.PTAssistantPlugin;
import org.openstreetmap.josm.plugins.pt_assistant.PTAssistantPluginPreferences;
import org.openstreetmap.josm.testutils.annotations.BasicPreferences;
import org.openstreetmap.josm.testutils.annotations.Main;

/**
 * Unit tests for which relations the highlight layer reacts to, and that a hidden layer is left alone.
 */
@BasicPreferences
@Main
class PTAssistantLayerManagerTest {

    private DataSet ds;
    private Relation busRoute;
    private Relation bicycleRoute;
    private Relation footRoute;
    private Relation horseRoute;
    private Relation ferryRoute;

    @BeforeEach
    void setUp() {
        PTAssistantLayerManager.PTLM.resetLayer();
        PTAssistantPlugin.clearHighlightedRelations();
        PTAssistantPluginPreferences.HIGHLIGHT_NON_PT_ROUTES.put(true);

        ds = new DataSet();
        busRoute = route(Map.of("type", "route", "route", "bus", "public_transport:version", "2"));
        bicycleRoute = route(Map.of("type", "route", "route", "bicycle"));
        footRoute = route(Map.of("type", "route", "route", "hiking"));
        horseRoute = route(Map.of("type", "route", "route", "horse"));
        ferryRoute = route(Map.of("type", "route", "route", "ferry"));
        ds.addSelectionListener(PTAssistantLayerManager.PTLM);
    }

    @AfterEach
    void tearDown() {
        ds.removeSelectionListener(PTAssistantLayerManager.PTLM);
        PTAssistantPlugin.clearHighlightedRelations();
        PTAssistantLayerManager.PTLM.resetLayer();
    }

    private Relation route(Map<String, String> tags) {
        Relation r = new Relation();
        r.setKeys(tags);
        ds.addPrimitive(r);
        return r;
    }

    @Test
    void publicTransportRoutesAreAlwaysHighlightable() {
        PTAssistantPluginPreferences.HIGHLIGHT_NON_PT_ROUTES.put(true);
        assertTrue(PTAssistantLayerManager.isHighlightable(busRoute));
        PTAssistantPluginPreferences.HIGHLIGHT_NON_PT_ROUTES.put(false);
        assertTrue(PTAssistantLayerManager.isHighlightable(busRoute));
    }

    @Test
    void footBicycleAndHorseRoutesFollowTheOption() {
        PTAssistantPluginPreferences.HIGHLIGHT_NON_PT_ROUTES.put(true);
        assertTrue(PTAssistantLayerManager.isHighlightable(bicycleRoute));
        assertTrue(PTAssistantLayerManager.isHighlightable(footRoute));
        assertTrue(PTAssistantLayerManager.isHighlightable(horseRoute));

        PTAssistantPluginPreferences.HIGHLIGHT_NON_PT_ROUTES.put(false);
        assertFalse(PTAssistantLayerManager.isHighlightable(bicycleRoute));
        assertFalse(PTAssistantLayerManager.isHighlightable(footRoute));
        assertFalse(PTAssistantLayerManager.isHighlightable(horseRoute));
    }

    @Test
    void otherRoutesAndNullAreNeverHighlightable() {
        assertFalse(PTAssistantLayerManager.isHighlightable(ferryRoute));
        assertFalse(PTAssistantLayerManager.isHighlightable(null));
    }

    @Test
    void optionIsOnByDefault() {
        PTAssistantPluginPreferences.HIGHLIGHT_NON_PT_ROUTES.remove();
        assertTrue(PTAssistantPluginPreferences.HIGHLIGHT_NON_PT_ROUTES.get());
    }

    @Test
    void selectingARouteHighlightsItWhileTheLayerIsVisible() {
        PTAssistantLayerManager.PTLM.getLayer().setVisible(true);
        ds.setSelected(busRoute);
        assertEquals(1, PTAssistantPlugin.getHighlightedRelations().size());
        assertTrue(PTAssistantPlugin.getHighlightedRelations().contains(busRoute));
    }

    @Test
    void selectingARouteDoesNothingWhileTheLayerIsHidden() {
        PTAssistantLayerManager.PTLM.getLayer().setVisible(false);
        ds.setSelected(busRoute);
        assertTrue(PTAssistantPlugin.getHighlightedRelations().isEmpty());
    }

    @Test
    void highlightingComesBackWhenTheLayerIsShownAgain() {
        PTAssistantLayer layer = PTAssistantLayerManager.PTLM.getLayer();
        layer.setVisible(false);
        ds.setSelected(busRoute);
        assertTrue(PTAssistantPlugin.getHighlightedRelations().isEmpty());

        layer.setVisible(true);
        ds.setSelected(footRoute);
        assertEquals(1, PTAssistantPlugin.getHighlightedRelations().size());
    }

    @Test
    void bicycleRouteIsNotHighlightedWhenTheOptionIsOff() {
        PTAssistantLayerManager.PTLM.getLayer().setVisible(true);
        PTAssistantPluginPreferences.HIGHLIGHT_NON_PT_ROUTES.put(false);
        ds.setSelected(bicycleRoute);
        assertTrue(PTAssistantPlugin.getHighlightedRelations().isEmpty());

        ds.setSelected(busRoute);
        assertEquals(1, PTAssistantPlugin.getHighlightedRelations().size());
    }
}
