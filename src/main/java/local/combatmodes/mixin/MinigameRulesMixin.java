package local.combatmodes.mixin;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
/** Late compatibility pass, after Mega Walls has merged its fixed legacy-rule callbacks. */
@Mixin(value={LivingEntity.class,PlayerEntity.class,HungerManager.class,Item.class},priority=400)
public abstract class MinigameRulesMixin {}
