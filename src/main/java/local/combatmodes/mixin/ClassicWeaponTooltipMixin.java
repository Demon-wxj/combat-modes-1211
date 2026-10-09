package local.combatmodes.mixin;

import local.combatmodes.CombatModes;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.function.Consumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ClassicWeaponTooltipMixin {
    @Inject(method="appendAttributeModifierTooltip",at=@At("HEAD"),cancellable=true)
    private void classicWeapon(Consumer<Text> consumer,PlayerEntity player,RegistryEntry<EntityAttribute> attribute,EntityAttributeModifier modifier,CallbackInfo ci){
        if(!CombatModes.legacy()||!attribute.equals(EntityAttributes.GENERIC_ATTACK_DAMAGE)
            ||!modifier.id().equals(Item.BASE_ATTACK_DAMAGE_MODIFIER_ID)||modifier.operation()!=EntityAttributeModifier.Operation.ADD_VALUE)return;
        var stack=(ItemStack)(Object)this;
        double amount=modifier.value();
        // In 1.8 this line is the item's bonus, excluding the player's unarmed one point.
        var enchants=stack.getOrDefault(DataComponentTypes.ENCHANTMENTS,ItemEnchantmentsComponent.DEFAULT);
        for(var enchant:enchants.getEnchantments())if(enchant.matchesKey(Enchantments.SHARPNESS))amount+=1.25*enchants.getLevel(enchant);
        if(amount!=0)consumer.accept(Text.translatable(amount>0?"attribute.modifier.plus.0":"attribute.modifier.take.0",
            AttributeModifiersComponent.DECIMAL_FORMAT.format(Math.abs(amount)),Text.translatable(attribute.value().getTranslationKey()))
            .formatted(amount>0?Formatting.BLUE:Formatting.RED));
        ci.cancel();
    }
}
