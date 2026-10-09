package local.combatmodes.mixin;
import local.combatmodes.CombatModes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Both modes are now provided by the host; retain all other arena/start validation. */
@Pseudo
@Mixin(targets={"com.megawalls.minigame.ClassicDuel","com.megawalls.minigame.PotDuel"},remap=false)
public abstract class MinigameSettingsMixin {
    @Inject(method="legacySettings",at=@At("HEAD"),cancellable=true)
    private static void selectedHostRules(CallbackInfoReturnable<Boolean> cir){if(CombatModes.active())cir.setReturnValue(true);}
}
