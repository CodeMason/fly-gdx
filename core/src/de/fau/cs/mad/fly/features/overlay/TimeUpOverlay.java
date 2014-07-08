package de.fau.cs.mad.fly.features.overlay;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

import de.fau.cs.mad.fly.Fly;
import de.fau.cs.mad.fly.I18n;
import de.fau.cs.mad.fly.game.TimeIsUpListener;
import de.fau.cs.mad.fly.ui.UI;

public class TimeUpOverlay implements TimeIsUpListener {
    
    private Fly game;
    private Skin skin;
    private Stage stage;
    
    public TimeUpOverlay(Fly game, Stage stage) {
        this.game = game;
        this.skin = game.getSkin();
        this.stage = stage;
    }
    
    @Override
    public void timeIsUp() {
        Table outerTable = new Table();
        outerTable.setFillParent(true);
        
        TextButtonStyle textButtonStyle = skin.get(UI.Buttons.STYLE, TextButtonStyle.class);
        TextButton continueButton = new TextButton(I18n.t("back.to.menu"), textButtonStyle);
        
        final Table messageTable = new Table();
        NinePatchDrawable background = new NinePatchDrawable(skin.get("grey-progress-bar", NinePatch.class));
        messageTable.setBackground(background);
        
        Label infoLabel = new Label(I18n.t("level.time.up"), skin);
        messageTable.add(infoLabel);
        
        messageTable.row().expand();
        messageTable.add(continueButton).pad(10f);
        
        outerTable.add(messageTable).center();
        continueButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.getGameController().endGame();
                game.setMainMenuScreen();
            }
        });
        stage.addActor(outerTable);
        
    }
    
}