package local.combatmodes.mixin;

import local.combatmodes.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.*;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(value=LivingEntity.class,priority=900)
public abstract class ClassicLivingMixin {
    @Shadow protected abstract void damageArmor(DamageSource source,float amount);
    @Inject(method="applyArmorToDamage",at=@At("HEAD"),cancellable=true)
    private void classicArmor(DamageSource source,float amount,CallbackInfoReturnable<Float> cir) {
        if(!CombatModes.legacy())return;
        var entity=(LivingEntity)(Object)this;
        boolean armored=!source.isIn(DamageTypeTags.BYPASSES_ARMOR)||source.isOf(DamageTypes.LAVA)||source.isOf(DamageTypes.IN_FIRE);
        if(armored) {
            if(amount>0&&ClassicRules.blocking(entity))amount=(amount+1)*.5f;
            damageArmor(source,amount);
            amount*=1-Math.clamp(entity.getArmor(),0,20)*.04f;
        }
        cir.setReturnValue(amount);
    }
    @Inject(method="blockedByShield",at=@At("HEAD"),cancellable=true)
    private void classicSword(DamageSource source,CallbackInfoReturnable<Boolean> cir) {
        if(ClassicRules.blocking((LivingEntity)(Object)this))cir.setReturnValue(false);
    }
    @Inject(method="takeKnockback",at=@At("HEAD"),cancellable=true)
    private void classicKnockback(double strength,double x,double z,CallbackInfo ci) {
        if(!CombatModes.legacy())return;
        var entity=(LivingEntity)(Object)this;
        double length=Math.sqrt(x*x+z*z);
        if(length>1.0E-8&&strength>0&&entity.getRandom().nextDouble()>=entity.getAttributeValue(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE)) {
            Vec3d v=entity.getVelocity();
            entity.setVelocity(v.x*.5-x/length*strength,Math.min(.4,v.y*.5+strength),v.z*.5-z/length*strength);
            entity.velocityDirty=true;
        }
        ci.cancel();
    }
}
