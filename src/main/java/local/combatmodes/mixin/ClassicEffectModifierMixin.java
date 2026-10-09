package local.combatmodes.mixin;
import local.combatmodes.ClassicRules;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets="net.minecraft.entity.effect.StatusEffect$EffectAttributeModifierCreator")
public abstract class ClassicEffectModifierMixin {
    @ModifyReturnValue(method="createAttributeModifier",at=@At("RETURN"))
    private EntityAttributeModifier classicPotion(EntityAttributeModifier original,int amplifier) {
        return ClassicRules.effectModifier(original,amplifier);
    }
}
