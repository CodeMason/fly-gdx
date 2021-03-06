package de.fau.cs.mad.fly.features.upgrades;

import java.util.ArrayList;
import java.util.List;

import de.fau.cs.mad.fly.I18n;
import de.fau.cs.mad.fly.features.IFeatureFinish;
import de.fau.cs.mad.fly.features.IFeatureInit;
import de.fau.cs.mad.fly.features.IFeatureUpdate;
import de.fau.cs.mad.fly.features.game.CollectibleObjects;
import de.fau.cs.mad.fly.features.overlay.InfoOverlay;
import de.fau.cs.mad.fly.features.upgrades.types.Collectible;
import de.fau.cs.mad.fly.features.upgrades.types.SpeedUpgradeEffect;
import de.fau.cs.mad.fly.features.upgrades.types.TemporarySpeedUpgrade;
import de.fau.cs.mad.fly.game.GameController;
import de.fau.cs.mad.fly.player.IPlane;
import de.fau.cs.mad.fly.sound.AudioManager;

/**
 * Used to display and handle instant speed upgrades.
 * <p>
 * Do not mix instant speed upgrades with a linear speed upgrade.
 * 
 * @author Lukas Hahmann <lukas.hahmann@gmail.com>
 * 
 */
public class TemporarySpeedUpgradeHandler extends CollectibleObjects implements IFeatureInit, IFeatureUpdate, IFeatureFinish {
    
    /**
     * The plane which speed should be changed after a speed upgrade was
     * collected.
     */
    private IPlane plane;
    
    private boolean upgradesActive;
    
    private List<SpeedUpgradeEffect> upgrades;
    
    /**
     * Creates a new {@link TemporarySpeedUpgradeHandler}.
     */
    public TemporarySpeedUpgradeHandler() {
        super(TemporarySpeedUpgrade.TYPE);
        upgradesActive = false;
        upgrades = new ArrayList<SpeedUpgradeEffect>();
    }
    
    @Override
    public void init(GameController game) {
        plane = game.getPlayer().getPlane();
    }
    
    @Override
    protected void handleCollecting(Collectible c) {
        if (c instanceof TemporarySpeedUpgrade) {
            TemporarySpeedUpgrade upgrade = (TemporarySpeedUpgrade) c;
            upgrades.add(upgrade.getEffect());
            c.dispose();
            
            // show info message
            StringBuilder builder = new StringBuilder();
            builder.append(I18n.t("speedUpgradeCollected"));
            builder.append("\n");
            builder.append(I18n.t("bonus"));
            builder.append(" ");
            builder.append(I18n.floatToString(upgrade.getEffect().getMaxSpeedupFactor() * 100f));
            builder.append(" %");
            InfoOverlay.getInstance().setOverlay(builder.toString(), 3);
            
            // start playing sound if not yet playing
			GameController.getInstance().getAudioManager().play(AudioManager.Sounds.PICKUP);
			upgradesActive = true;
        }
        
    }
    
    @Override
    public void update(float delta) {
        int size = upgrades.size();
        if (size > 0) {
            float speedUpFactor = SpeedUpgradeEffect.NO_SPEEDUP;
            SpeedUpgradeEffect upgrade;
            for (int i = size - 1; i >= 0; i--) {
                upgrade = upgrades.get(i);
                upgrade.update(delta * 1000f);
                if (upgrade.isActive()) {
                    speedUpFactor *= upgrade.getCurrentSpeedupFactor();
                } else {
                    upgrades.remove(i);
                }
            }
            plane.setCurrentSpeed(plane.getBaseSpeed() * speedUpFactor);
        } else if (upgradesActive) {
            upgradesActive = false;
            plane.setCurrentSpeed(plane.getBaseSpeed());
        }
    }

    @Override
    public void finish() {
    }
}