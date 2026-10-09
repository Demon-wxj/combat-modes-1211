package local.combatmodes;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Identifier;

/** Arithmetic targets Java 1.8.9, not the former custom no-immunity combo mode. */
public final class ClassicRules {
    private ClassicRules() {}
    public static boolean blocking(LivingEntity entity) {
        return CombatModes.legacy() && entity instanceof PlayerEntity && entity.isUsingItem()
                && entity.getActiveItem().getItem() instanceof SwordItem;
    }
    public static EntityAttributeModifier effectModifier(EntityAttributeModifier current,int amplifier) {
        if(!CombatModes.legacy())return current;
        if(current.id().equals(Identifier.ofVanilla("effect.strength")))
            return new EntityAttributeModifier(current.id(),1.3*(amplifier+1),EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        if(current.id().equals(Identifier.ofVanilla("effect.weakness")))
            return new EntityAttributeModifier(current.id(),-.5*(amplifier+1),EntityAttributeModifier.Operation.ADD_VALUE);
        return current;
    }
    public static void refreshEffects(LivingEntity entity) {
        // Reapply modifiers only: retain remaining duration, amplifier and absorption.
        for(var key:java.util.List.of(StatusEffects.STRENGTH,StatusEffects.WEAKNESS)) {
            var effect=entity.getStatusEffect(key);
            if(effect!=null) {
                key.value().onRemoved(entity.getAttributes());
                key.value().onApplied(entity.getAttributes(),effect.getAmplifier());
            }
        }
    }
    public static int protectionPoints(int level,float coefficient) {
        if(level<=0)return 0;
        // Cap before squaring to prevent malformed command-enchant overflow.
        int safe=Math.min(level,255);
        return (int)Math.floor((6.0+safe*safe)/3.0*coefficient);
    }
}
