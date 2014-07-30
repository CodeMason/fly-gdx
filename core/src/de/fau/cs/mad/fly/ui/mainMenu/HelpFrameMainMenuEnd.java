package de.fau.cs.mad.fly.ui.mainMenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import de.fau.cs.mad.fly.I18n;
import de.fau.cs.mad.fly.ui.HelpFrame;

/**
 * Frame to describe what to do with the play button.
 * 
 * @author Lukas Hahmann
 * 
 */
public class HelpFrameMainMenuEnd extends HelpFrame {
    
    private final TextureRegion arrowDown;
    
    /**
     * Create the content of the frame, a describing text and an arrow.
     * @param skin
     */
    public HelpFrameMainMenuEnd(Skin skin) {
        super.setupBatchAndStage();
        
        LabelStyle labelStyle = skin.get("black", LabelStyle.class);
        Label helpToPlay = new Label(I18n.t("helpEnd"), labelStyle);
        
        this.arrowDown = skin.getRegion("helpArrowDown");
        
        stage.addActor(helpToPlay);
        helpToPlay.setPosition(900, 300);
        
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }
    
    @Override
    public void render() {
        batch.begin();
        batch.draw(arrowDown, 3800 / scalingFactor, 350 / scalingFactor, 0, 0, arrowDown.getRegionWidth() / scalingFactor, arrowDown.getRegionHeight() / scalingFactor, 1f, 1f, 90);
        batch.end();
        stage.draw();
    }
    
}