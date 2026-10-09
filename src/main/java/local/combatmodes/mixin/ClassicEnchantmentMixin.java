package local.combatmodes.mixin;
import local.combatmodes.*;
import net.minecraft.enchantment.*;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public abstract class ClassicEnchantmentMixin {
    @Inject(method="getDamage",at=@At("HEAD"),cancellable=true)
    private static void damage(ServerWorld world,ItemStack stack,Entity target,DamageSource source,float base,CallbackInfoReturnable<Float> cir) {
        if(!CombatModes.legacy())return;
        MutableFloat value=new MutableFloat(base);
        EnchantmentHelper.forEachEnchantment(stack,(entry,level)->{
            if(entry.matchesKey(Enchantments.SHARPNESS))value.add(1.25f*level);
            else entry.value().modifyDamage(world,level,stack,target,source,value);
        });
        cir.setReturnValue(value.floatValue());
    }
    @Inject(method="getProtectionAmount",at=@At("HEAD"),cancellable=true)
    private static void protection(ServerWorld world,LivingEntity wearer,DamageSource source,CallbackInfoReturnable<Float> cir) {
        if(!CombatModes.legacy())return;
        int[] points={0};
        MutableFloat custom=new MutableFloat(0);
        for(ItemStack stack:wearer.getArmorItems())EnchantmentHelper.forEachEnchantment(stack,(entry,level)->{
            float factor=0;
            if(entry.matchesKey(Enchantments.PROTECTION))factor=.75f;
            else if(entry.matchesKey(Enchantments.FIRE_PROTECTION))factor=source.isIn(DamageTypeTags.IS_FIRE)?1.25f:0;
            else if(entry.matchesKey(Enchantments.FEATHER_FALLING))factor=source.isIn(DamageTypeTags.IS_FALL)?2.5f:0;
            else if(entry.matchesKey(Enchantments.BLAST_PROTECTION))factor=source.isIn(DamageTypeTags.IS_EXPLOSION)?1.5f:0;
            else if(entry.matchesKey(Enchantments.PROJECTILE_PROTECTION))factor=source.isIn(DamageTypeTags.IS_PROJECTILE)?1.5f:0;
            else {entry.value().modifyDamageProtection(world,level,stack,wearer,source,custom);return;}
            points[0]+=ClassicRules.protectionPoints(level,factor);
        });
        int sum=Math.clamp(points[0],0,25);
        int rolled=((sum+1)>>1)+wearer.getRandom().nextInt((sum>>1)+1);
        cir.setReturnValue(Math.min(20,rolled+custom.floatValue()));
    }
}
