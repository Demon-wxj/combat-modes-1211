package local.combatmodes.mixin;
import local.combatmodes.CombatModes;
import net.minecraft.item.*;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.text.Text;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PotionItem.class)
public abstract class ClassicPotionTooltipMixin {
    @Inject(method="appendTooltip",at=@At("HEAD"),cancellable=true)
    private void duration(ItemStack stack,Item.TooltipContext context,List<Text> lines,TooltipType type,CallbackInfo ci) {
        if(!CombatModes.legacy()||!stack.isOf(Items.SPLASH_POTION))return;
        var potion=stack.get(DataComponentTypes.POTION_CONTENTS);
        if(potion==null||potion.potion().isEmpty()||!potion.customEffects().isEmpty())return;
        potion.buildTooltip(lines::add,.75f,context.getUpdateTickRate());ci.cancel();
    }
}
