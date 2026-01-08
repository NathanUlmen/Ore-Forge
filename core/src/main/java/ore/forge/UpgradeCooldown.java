package ore.forge;

import ore.forge.Strategies.TimeUpdatable;

public class UpgradeCooldown implements TimeUpdatable {
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
    public void update(float delta) {
        if (cooldown.update(delta)) {
            ore.removeUpgradeCooldown(tag);
            TimerUpdater.unregister(this);
        }
    }

}
