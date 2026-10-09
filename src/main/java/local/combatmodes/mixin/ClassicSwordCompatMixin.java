package local.combatmodes.mixin;
import local.combatmodes.CombatModes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.util.*;
import fuzs.puzzleslib.api.event.v1.core.EventResultHolder;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Pseudo
@Mixin(targets="fuzs.swordblockingmechanics.handler.SwordBlockingHandler",remap=false)
public abstract class ClassicSwordCompatMixin {
    @Inject(method="onUseItem",at=@At("HEAD"),cancellable=true)
    private static void mainHandOnly(PlayerEntity player,World world,Hand hand,CallbackInfoReturnable<EventResultHolder<ActionResult>> cir) {
        if(CombatModes.legacy()&&hand==Hand.OFF_HAND)cir.setReturnValue(EventResultHolder.interrupt(ActionResult.FAIL));
    }
    @Inject(method="isActiveItemStackBlocking",at=@At("HEAD"),cancellable=true)
    private static void reductionOwner(PlayerEntity player,CallbackInfoReturnable<Boolean> cir) {
        if(!player.getWorld().isClient&&CombatModes.legacy())cir.setReturnValue(false);
    }
}
