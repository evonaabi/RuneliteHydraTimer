package com.evonaabi;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.Locale;
import javax.inject.Inject;

import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;

public class HydraTimerOverlay extends Overlay
{
    private static final NumberFormat TIME_LEFT_FORMATTER = DecimalFormat.getInstance(Locale.US);

    static
    {
        ((DecimalFormat)TIME_LEFT_FORMATTER).applyPattern("#0");
    }

    private final Client client;
    private final HydraTimerConfig config;
    private final HydraTimerPlugin plugin;

    @Inject
    private HydraTimerOverlay(Client client, HydraTimerConfig config, HydraTimerPlugin plugin)
    {
        this.client = client;
        this.config = config;
        this.plugin = plugin;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.MED);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        WorldPoint ventPoint = new WorldPoint(0 ,0, 0);
        Tile[][][] tiles = client.getScene().getTiles();
        boolean found = false;
        if (tiles != null) {
            for (int x = 0; x < tiles.length; ++x) {
                if (tiles[x] != null) {
                    for (int y = 0; y < tiles[x].length; ++y) {
                        if (tiles[x][y] != null) {
                            for (int z = 0; z < tiles[x][y].length; ++z) {
                                if (tiles[x][y][z] != null) {
                                    GameObject[] gobj = tiles[x][y][z].getGameObjects();
                                    for (int g = 0; g < gobj.length; ++g) {
                                        if (gobj[g] != null) {
                                            ObjectComposition objectDefinition = client.getObjectDefinition(gobj[g].getId());
                                            if (objectDefinition != null) {
                                                String name = objectDefinition.getName();
                                                if (name != null && name.equalsIgnoreCase("Chemical vent (red)")) {
                                                    ventPoint = tiles[x][y][z].getWorldLocation();
                                                    found = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                                if (found)
                                    break;
                            }
                        }
                        if (found)
                            break;
                    }
                }
                if (found)
                    break;
            }
        }
        renderVentTimer(new WorldPoint(ventPoint.getX() - 17, ventPoint.getY() - 5, ventPoint.getPlane()), graphics);
        renderVentTimer(new WorldPoint(ventPoint.getX() - 17, ventPoint.getY() - 4, ventPoint.getPlane()), graphics);
        renderVentTimer(new WorldPoint(ventPoint.getX() - 18, ventPoint.getY() - 5, ventPoint.getPlane()), graphics);
        renderVentTimer(new WorldPoint(ventPoint.getX() - 18, ventPoint.getY() - 4, ventPoint.getPlane()), graphics);

        return null;
    }

    private void renderVentTimer(final WorldPoint drawPoint, final Graphics2D graphics)
    {
        if (drawPoint == null || graphics == null)
            return;

        final LocalPoint lp = LocalPoint.fromWorld(client, drawPoint.getX(), drawPoint.getY());

        if (lp == null)
        {
            return;
        }

        final LocalPoint centerLp = new LocalPoint(
                lp.getX(),
                lp.getY());

        final Instant now = Instant.now();
        final double ventTick = 8 - (client.getTickCount() % 8);
        final String ventTickStr = TIME_LEFT_FORMATTER.format(ventTick);

        final int textWidth = graphics.getFontMetrics().stringWidth(ventTickStr);
        final int textHeight = graphics.getFontMetrics().getAscent();

        final Point canvasPoint = Perspective
                .localToCanvas(client, centerLp, 0);

        if (canvasPoint != null)
        {
            final Point canvasCenterPoint = new Point(
                    canvasPoint.getX() - textWidth / 2,
                    canvasPoint.getY() + textHeight / 2);

            Color color = ventTick == 1? Color.GREEN : Color.RED;
            if (ventTick == 2)
                color = Color.ORANGE;

            OverlayUtil.renderTextLocation(graphics, canvasCenterPoint, ventTickStr, color);
        }
    }
}