package local.combatmodes.mixin;
import local.combatmodes.CombatModes;
import net.minecraft.item.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ItemStack.class)
public abstract class ClassicUseMixin {
    @Inject(method="use",at=@At("HEAD"),cancellable=true)
    private void noModernHand(World world,PlayerEntity player,Hand hand,CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack stack=(ItemStack)(Object)this;
        if(CombatModes.legacy()&&(hand==Hand.OFF_HAND||stack.isOf(Items.SHIELD)))cir.setReturnValue(TypedActionResult.fail(stack));
    }
    @Inject(method="useOnBlock",at=@At("HEAD"),cancellable=true)
    private void noOffhandBlock(ItemUsageContext context,CallbackInfoReturnable<ActionResult> cir) {
        if(CombatModes.legacy()&&context.getHand()==Hand.OFF_HAND)cir.setReturnValue(ActionResult.FAIL);
    }
    @Inject(method="useOnEntity",at=@At("HEAD"),cancellable=true)
    private void noOffhandEntity(PlayerEntity user,LivingEntity target,Hand hand,CallbackInfoReturnable<ActionResult> cir) {
        if(CombatModes.legacy()&&hand==Hand.OFF_HAND)cir.setReturnValue(ActionResult.FAIL);
    }
}
