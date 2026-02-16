package ore.forge.game.screens.ItemCreator.VisualScripting;

public interface LinkBehavior<E> {

    void onLink(ScriptingNode<E> root, ScriptingNode<E> toAdd);

    void onUnlink(ScriptingNode<E> root, ScriptingNode<E> toRemove);

}
