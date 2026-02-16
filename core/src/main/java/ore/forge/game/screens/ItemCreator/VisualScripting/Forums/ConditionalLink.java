package ore.forge.game.screens.ItemCreator.VisualScripting.Forums;

import com.badlogic.gdx.Gdx;
import ore.forge.game.screens.ItemCreator.VisualScripting.LinkBehavior;
import ore.forge.game.screens.ItemCreator.VisualScripting.ScriptingNode;

public class ConditionalLink<E> implements LinkBehavior<E> {

    @Override
    public void onLink(ScriptingNode<E> root, ScriptingNode<E> toAdd) {
        System.out.println("Firing Link");
        if (root.getChildNodes().isEmpty()) {
            Gdx.app.log("Conditional Link", "Linked True Branch");
            root.addChild(toAdd, "trueBranch");
        } else if (root.getChildNodes().size() == 1){
            Gdx.app.log("Conditional Link", "Linked False Branch");
            root.addChild(toAdd, "falseBranch");
        } else {
            Gdx.app.log("Conditional Link", "Unable to link, true and false branches full.");
        }
    }

    @Override
    public void onUnlink(ScriptingNode<E> root, ScriptingNode<E> toRemove) {
        root.removeChild(toRemove);
    }

}
