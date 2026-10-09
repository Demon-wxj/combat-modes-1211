package local.combatmodes.mixin;
import local.combatmodes.CombatModes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=MiningToolItem.class,priority=1100)
public class ClassicHoeMixin {
    @Inject(method="postDamageEntity",at=@At("HEAD"),cancellable=true)
    private void classicHoeDurability(ItemStack stack,LivingEntity target,LivingEntity attacker,CallbackInfo ci){
        // ItemHoe in 1.8 inherited Item's no-op hitEntity instead of ItemTool's two-point cost.
        if(CombatModes.legacy()&&stack.getItem() instanceof HoeItem)ci.cancel();
    }
}
