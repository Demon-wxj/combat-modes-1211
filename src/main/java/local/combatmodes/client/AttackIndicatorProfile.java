package local.combatmodes.client;
import local.combatmodes.CombatModes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.AttackIndicator;

/** Runtime presentation; never writes the user's options file. */
public final class AttackIndicatorProfile {
    private static AttackIndicator previous,modernChoice,applied;
    public static void tick(MinecraftClient client) {
        if(client.world==null||!CombatModes.active()){restore(client);return;}
        var option=client.options.getAttackIndicator();
        if(previous==null){previous=option.getValue();modernChoice=previous==AttackIndicator.OFF?AttackIndicator.CROSSHAIR:previous;}
        if(CombatModes.modern()&&option.getValue()!=applied&&option.getValue()!=AttackIndicator.OFF)modernChoice=option.getValue();
        applied=CombatModes.legacy()?AttackIndicator.OFF:modernChoice;
        if(option.getValue()!=applied)option.setValue(applied);
    }
    public static void restore(MinecraftClient client) {
        if(previous!=null&&client.options.getAttackIndicator().getValue()==applied)client.options.getAttackIndicator().setValue(previous);
        previous=modernChoice=applied=null;
    }
}
