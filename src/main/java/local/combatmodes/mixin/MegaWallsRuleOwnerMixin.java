package local.combatmodes.mixin;
import local.combatmodes.CombatModes;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Mode component owns shared combat; all MW membership, skills and true damage stay intact. */
@Pseudo
@Mixin(targets="com.megawalls.game.MWLegacyCombat",remap=false)
public abstract class MegaWallsRuleOwnerMixin {
    @Inject(method="active",at=@At("HEAD"),cancellable=true)
    private static void sharedRules(PlayerEntity player,CallbackInfoReturnable<Boolean> cir) {
        if(CombatModes.active())cir.setReturnValue(false);
    }
}
