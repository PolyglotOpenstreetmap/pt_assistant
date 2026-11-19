// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.pt_assistant.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import org.openstreetmap.josm.data.osm.DataSelectionListener;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.gui.dialogs.relation.GenericRelationEditor;
import org.openstreetmap.josm.plugins.pt_assistant.PTAssistantPlugin;
import org.openstreetmap.josm.plugins.pt_assistant.PTAssistantPluginPreferences;
import org.openstreetmap.josm.plugins.pt_assistant.utils.RouteUtils;

/**
 * PT assistant layer manager.
 */
public class PTAssistantLayerManager
    implements DataSelectionListener, PropertyChangeListener {

    public static final PTAssistantLayerManager PTLM = new PTAssistantLayerManager();
    private PTAssistantLayer layer;

    public PTAssistantLayer getLayer() {
        if (layer == null) {
            layer = new PTAssistantLayer();
        }
        return layer;
    }

    public void resetLayer() {
        layer = null;
    }

    /**
     * @return true if the layer exists and the user has set it to invisible
     */
    private boolean isLayerHidden() {
        return layer != null && !layer.isVisible();
    }

    /**
     * Whether the highlight layer shows this relation: public transport routes
     * always, foot, bicycle and horse routes only if the user left that option on.
     *
     * @param relation the relation
     * @return true if it should be highlighted
     */
    static boolean isHighlightable(Relation relation) {
        if (relation == null) {
            return false;
        }
        if (RouteUtils.isVersionTwoPTRoute(relation)) {
            return true;
        }
        return PTAssistantPluginPreferences.HIGHLIGHT_NON_PT_ROUTES.get()
                && (RouteUtils.isBicycleRoute(relation)
                    || RouteUtils.isFootRoute(relation)
                    || RouteUtils.isHorseRoute(relation));
    }

    /**
     * Listens to a selection change
     */
    @Override
    public void selectionChanged(SelectionChangeEvent event) {

        if (isLayerHidden()) {
            return;
        }

        ArrayList<OsmPrimitive> routes = new ArrayList<>();

        for (OsmPrimitive primitive : event.getSelection()) {
            if (primitive.getType().equals(OsmPrimitiveType.RELATION)
                    && isHighlightable((Relation) primitive)) {
                routes.add(primitive);
            }
        }

        if (!routes.isEmpty()) {
            getLayer().setPrimitives(routes);
            PTAssistantPlugin.clearHighlightedRelations();
            for (OsmPrimitive primitive : routes) {
                PTAssistantPlugin.addHighlightedRelation((Relation) primitive);
            }
        }
    }

    /**
     * Listens to a focus change, sets the primitives attribute to the route
     * relation in the top Relation Editor and repaints the map
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        if ("focusedWindow".equals(evt.getPropertyName())
                && evt.getNewValue() != null
                && GenericRelationEditor.class.equals(evt.getNewValue().getClass())) {

            GenericRelationEditor editor = (GenericRelationEditor) evt.getNewValue();
            Relation relation = editor.getRelation();

            if (!isLayerHidden() && isHighlightable(relation)) {
                getLayer().repaint(relation);
            }
        }
    }
}
