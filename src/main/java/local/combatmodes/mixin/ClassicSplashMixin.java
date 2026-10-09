package local.combatmodes.mixin;
import local.combatmodes.CombatModes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import java.util.ArrayList;

@Mixin(PotionEntity.class)
public abstract class ClassicSplashMixin {
    @ModifyVariable(method="applySplashPotion",at=@At("HEAD"),argsOnly=true)
    private Iterable<StatusEffectInstance> classicDuration(Iterable<StatusEffectInstance> effects) {
        if(!CombatModes.legacy())return effects;
        var potion=((PotionEntity)(Object)this).getStack().getOrDefault(DataComponentTypes.POTION_CONTENTS,PotionContentsComponent.DEFAULT);
        // Explicit custom effects include class potions and map items; preserve their chosen duration.
        if(!potion.customEffects().isEmpty()||potion.potion().isEmpty())return effects;
        var converted=new ArrayList<StatusEffectInstance>();
        for(var effect:effects) {
            if(effect.getEffectType().value().isInstant()||effect.isInfinite())converted.add(effect);
            else converted.add(new StatusEffectInstance(effect.getEffectType(),(int)(effect.getDuration()*.75+.5),effect.getAmplifier(),effect.isAmbient(),effect.shouldShowParticles(),effect.shouldShowIcon()));
        }
        return converted;
    }
}
