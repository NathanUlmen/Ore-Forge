package ore.forge;

import ore.forge.Strategies.Updatable;

public class UpgradeCooldown implements Updatable {
    private final CoolDown cooldown;
    private final UpgradeTag tag;
    private final Ore ore;

    public UpgradeCooldown(float cooldown, Ore ore,  UpgradeTag tag) {
        this.cooldown = new CoolDown(cooldown);
        this.ore = ore;
        this.tag = tag;
        TimerUpdater.register(this);
    }

    @Override
    public void update(float delta, GameState state) {
        if (cooldown.update(delta)) {
            ore.removeUpgradeCooldown(tag);
            TimerUpdater.unregister(this);
        }
    }

}
