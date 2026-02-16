package ore.forge.game.screens;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.TimeUtils;
import ore.forge.game.event.Events.GameEvent;
import ore.forge.game.event.Events.OreDroppedGameEvent;
import ore.forge.game.event.Events.OreSoldGameEvent;
import ore.forge.FontColors;
import ore.forge.game.ui.UIHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;


/**
 * @author Nathan Ulmen
 * The event logger will register to the eventManager and whenever an event occurs the logger will log it in the bottom right corner of the screen.
 * It will contain a scrollbox which constains a table of labels.
 * Labels will be culled after X events.
 * Events should be color coded based on event.
 * EventLogger should have functionality which allow you to turn on and off notifications for specific events
 * without having to restart the game.
 */
public class EventLogger extends WidgetGroup {
    private static final Logger log = LoggerFactory.getLogger(EventLogger.class);
    private final SimpleDateFormat simpleDateFormat;
    private final HashSet<Class<?>> disabledEvents;
    private final Table logTable;
    private final ScrollPane scrollPane;
    private final static int MAX_EVENTS = 1_000;
    private final Label.LabelStyle labelStyle;
    private boolean autoScroll = true;

    public EventLogger() {
        disabledEvents = new HashSet<>();
        disabledEvents.add(OreDroppedGameEvent.class);
        disabledEvents.add(OreSoldGameEvent.class);
        simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        BitmapFont font2 = UIHelper.generateFont(24);
        labelStyle = new Label.LabelStyle(font2, Color.WHITE);
        font2.getData().markupEnabled = true;
//        font2.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        logTable = new Table();

        logTable.setSize(IRHelper.getWidth(0.3f), IRHelper.getHeight(0.2f));
//        logTable.setDebug(true);


        scrollPane = new ScrollPane(logTable);
        scrollPane.setScrollingDisabled(true, false);
//        scrollPane.setDebug(true);

        scrollPane.setSize(IRHelper.getWidth(0.3f), IRHelper.getHeight(0.2f));
        scrollPane.addListener(new InputListener() {
            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                float scrollY = scrollPane.getScrollY();
                float maxScrollY = scrollPane.getMaxY();
                if (scrollY < maxScrollY) {
                    autoScroll = false;
                } else if (scrollY >= maxScrollY) {
                    autoScroll = true;
                }
                return true;
            }
        });

        this.addActor(scrollPane);
    }


    //TODO:Update this so that it removes events, make sure it isn't super jittery.
    public void logEvent(GameEvent<?> event) {
        if (disabledEvents.contains(event.getClass())) {
            return;
        }

        if (logTable.getChildren().size > MAX_EVENTS) {
            logTable.removeActor(logTable.getChildren().first());
            logTable.layout();
        }
        var label = createLabel(event);
        logTable.add(label).bottom().left().fillX().expandX().row();
        label.addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(0.45f)));


        if (autoScroll && scrollPane.getScrollY() > 0) {
            scrollPane.scrollTo(0, 0, 0, 0);
        }
        scrollPane.updateVisualScroll();
    }

    private Label createLabel(GameEvent<?> event) {
        var label = new Label(formatText(event), labelStyle);
        label.getStyle().font.getData().setScale(0.5f);
        return label;
    }

    private String formatText(GameEvent<?> event) {
        String info = event.getBriefInfo();
        String timestamp = simpleDateFormat.format(new Date(TimeUtils.millis()));
        timestamp = FontColors.highlightString("[" + timestamp + "]", FontColors.PALE_GOLDEN_ROD);
        return timestamp + " - " + FontColors.highlightString("[" + event.eventName() + "]", event.getColor()) + " - " + info;
    }

    public void disableEventLogging(Class<?> eventClass) {
        disabledEvents.add(eventClass);
    }

    public void enableEventLogging(Class<?> eventClass) {
        disabledEvents.remove(eventClass);
    }

}
