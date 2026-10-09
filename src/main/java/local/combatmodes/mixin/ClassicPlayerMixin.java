package local.combatmodes.mixin;
import local.combatmodes.CombatModes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(PlayerEntity.class)
public abstract class ClassicPlayerMixin {
    @ModifyVariable(method="attack",at=@At("STORE"),ordinal=3)
    private boolean noClassicSweep(boolean original){return !CombatModes.legacy()&&original;}
    @WrapOperation(method="attack",at=@At(value="INVOKE",target="Lnet/minecraft/entity/LivingEntity;takeKnockback(DDD)V",ordinal=0))
    private void sprintAndEnchantmentImpulse(LivingEntity target,double strength,double x,double z,Operation<Void> original) {
        if(!CombatModes.legacy()){original.call(target,strength,x,z);return;}
        // 1.8 adds this impulse AFTER the ordinary hurt knockback; it does not
        // halve velocity a second time or cap the final sprint lift at 0.4.
        target.addVelocity(-x*strength,.1,-z*strength);
    }
}
