package de.fau.cs.mad.fly.levels.medium;

import java.util.List;

import de.fau.cs.mad.fly.I18n;
import de.fau.cs.mad.fly.features.ICollisionListener;
import de.fau.cs.mad.fly.features.IFeatureInit;
import de.fau.cs.mad.fly.features.overlay.InfoOverlay;
import de.fau.cs.mad.fly.features.upgrades.types.Collectible;
import de.fau.cs.mad.fly.game.GameController;
import de.fau.cs.mad.fly.game.GameControllerBuilder;
import de.fau.cs.mad.fly.game.GameObject;
import de.fau.cs.mad.fly.levels.ILevel;

/**
 * Level script file for the points tutorial level.
 * 
 * @author Tobi
 * 
 */
public class PointsTutorialLevel implements ILevel, IFeatureInit, ICollisionListener {
    
    @Override
    public void create(GameControllerBuilder builder) {
        builder.addFeatureToLists(this);
    }
    
    @Override
    public void init(GameController game) {
        InfoOverlay.getInstance().setOverlay(I18n.tLevel("tutorial.points"), 5);
    }
    
    @Override
    public void onCollision(GameObject g1, GameObject g2) {
        if (!(g2 instanceof Collectible)) {
            return;
        }
        List<GameObject> objects = GameController.getInstance().getLevel().components;
        for (GameObject object : objects) {
            if (object.getId().equals("IndicatorArrow")) {
                object.hide();
                return;
            }
        }
    }
}