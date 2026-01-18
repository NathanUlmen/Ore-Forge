package ore.forge.QuestComponents;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import ore.forge.EventSystem.EventManager;
import ore.forge.EventSystem.Events.QuestCompletedGameEvent;
import ore.forge.EventSystem.GameEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class QuestManager implements GameEventListener<QuestCompletedGameEvent> {
    private final HashMap<String, Quest> allQuests;
    private ArrayList<Quest> completedQuests, lockedQuests;
    private final EventManager eventManager = EventManager.getSingleton();

    public QuestManager() {
        JsonReader reader = new JsonReader();
        JsonValue allQuestData = reader.parse(Gdx.files.local("Player/Quests.json"));
        allQuests = new HashMap<>();
        lockedQuests = new ArrayList<>();
        completedQuests = new ArrayList<>();

        for (JsonValue questData : allQuestData) {
            Quest quest = new Quest(questData);
            allQuests.put(quest.getId(), quest);
            handleQuest(quest);
        }
    }

    public void activateQuest(String questId) {
        var quest = allQuests.get(questId);
        assert quest != null && quest.getStatus() == QuestStatus.LOCKED;
        quest.start();
    }

    private void handleQuest(Quest quest) {
        switch (quest.getStatus()) {
            case COMPLETED -> completedQuests.add(quest);
            case LOCKED -> lockedQuests.add(quest);
            case IN_PROGRESS -> quest.initialize();
        }
    }

    public Quest getQuest(String questId) {
        return allQuests.get(questId);
    }

    @Override
    public void handle(QuestCompletedGameEvent event) {
        var finishedQuest = event.getSubject();
        assert finishedQuest.getStatus() == QuestStatus.COMPLETED;
        completedQuests.add(finishedQuest);
    }

    @Override
    public Class<?> getEventType() {
        return QuestCompletedGameEvent.class;
    }

    public HashMap<String, Quest> getAllQuests() {
        return allQuests;
    }

}




