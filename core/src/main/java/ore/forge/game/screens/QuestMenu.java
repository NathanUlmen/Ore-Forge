package ore.forge.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import ore.forge.game.event.Events.QuestStepCompletedGameEvent;
import ore.forge.game.event.GameEventListener;
import ore.forge.game.quest.Quest;
import ore.forge.game.quest.QuestManager;
import ore.forge.game.quest.QuestStatus;
import ore.forge.game.screens.Widgets.QuestWidget;
import ore.forge.game.ui.ButtonType;
import ore.forge.game.ui.UIHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class QuestMenu extends Table implements GameEventListener<QuestStepCompletedGameEvent> {
    private final ArrayList<QuestWidget> questWidgets;
    private final HashMap<String, QuestWidget> lookup;
    private QuestStatus filterType;
    private final Table widgetTable;

    /*
     * Sort Quests by Name, Completed Progress, Reward Type?
     * */
    public QuestMenu(QuestManager questManager) {
        questWidgets = new ArrayList<>(questManager.getAllQuests().size());
        lookup = new HashMap<>(questManager.getAllQuests().size());

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.fontColor = Color.BLACK;
        textButtonStyle.up = UIHelper.getRoundFull();
        textButtonStyle.down = UIHelper.getRoundFull();
        textButtonStyle.checked = UIHelper.getRoundFull();
        textButtonStyle.over = UIHelper.getRoundFull();
        textButtonStyle.font = UIHelper.generateFont(10);
        TextButton completed = new TextButton("Completed", textButtonStyle);
        TextButton inProgress = new TextButton("In Progress", textButtonStyle);
        TextButton locked = new TextButton("Locked", textButtonStyle);
        TextButton all = new TextButton("Unlocked", textButtonStyle);

        this.addListener(completed, () -> {
            changeFilter(QuestStatus.COMPLETED);
        });

        this.addListener(inProgress, () -> {
            changeFilter(QuestStatus.IN_PROGRESS);
        });

        this.addListener(locked, () -> {
            changeFilter(QuestStatus.LOCKED);
        });

        this.addListener(all, () -> {
            changeFilter(null);
        });

        /*
         * Completed
         * Locked
         * Active
         * */
        widgetTable = new Table();
        widgetTable.setBackground(UIHelper.getButton(ButtonType.ROUND_FULL_128));
        widgetTable.setColor(Color.ORANGE);


        int count = 0;
        for (Quest quest : questManager.getAllQuests().values()) {
            var questIcon = new QuestWidget(quest);
            lookup.put(quest.getId(), questIcon);
            questWidgets.add(questIcon);

            widgetTable.add(questIcon).top().left().align(Align.topLeft).grow().uniform().pad(2.75f);
            if (++count % 2 == 0) {
                widgetTable.row().top().left();
            }
        }

//        widgetTable.setSize(IRHelper.getWidth(0.8f), IRHelper.getHeight(0.7f));

        widgetTable.setDebug(true);
//        this.setDebug(true);

        this.pad(2.75f);
        var scrollPane = new ScrollPane(widgetTable);
//        this.add(widgetTable).expand().fill().top().left().align(Align.topLeft);
        this.add(scrollPane).top().left().fill().expand();
        this.background(UIHelper.getButton(ButtonType.ROUND_BOLD_128));
        this.setColor(Color.BLACK);
        this.setSize(IRHelper.getWidth(0.9f), IRHelper.getHeight(0.90f));

        this.setPosition(IRHelper.getWidth(0.075f), IRHelper.getHeight(0.075f));

    }

    private void addListener(TextButton button, Runnable clickedLogic) {
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                clickedLogic.run();
            }
        });
    }

    private void changeFilter(QuestStatus status) {
        if (this.filterType != status) {
            this.filterType = status;
            CompletableFuture.supplyAsync(() -> {
                ArrayList<QuestWidget> widgetsToAdd = new ArrayList<>(questWidgets);
                for (QuestWidget widget : questWidgets) {
                    if (this.filterType == null || widget.getStatus() == this.filterType) {
                        widgetsToAdd.add(widget);
                    }
                }
                return widgetsToAdd;
            }).thenAccept(widgetsToAdd -> {
                Gdx.app.postRunnable(() -> {
                    addIcons(widgetsToAdd);
                });
            });

        }
    }

    private void updateIcons() {

    }

    public void addIcons(ArrayList<QuestWidget> icons) {
        widgetTable.clear();
        int count = 0;
        for (QuestWidget widget : icons) {
            widgetTable.add(widget).top().left().align(Align.topLeft).colspan(1);
            if (++count % 2 == 0) {
                widgetTable.row();
            }
        }
    }

    private void foo() {
        for (QuestWidget questWidget : questWidgets) {
            if (questWidget.getStatus() == filterType) {
                //add icon to current table.
            }
        }
    }

    @Override
    public void handle(QuestStepCompletedGameEvent event) {

    }

    @Override
    public Class<?> getEventType() {
        return QuestStepCompletedGameEvent.class;
    }

    public void hide() {
        this.setVisible(false);
    }

    public void show() {
        this.setVisible(true);
    }
}
