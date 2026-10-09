package local.combatmodes.mixin;
import local.combatmodes.CombatModes;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(MinecraftClient.class)
public abstract class ClassicInputMixin {
    @Shadow protected abstract boolean doAttack();
    @Inject(method="handleInputEvents",at=@At("HEAD"))
    private void realBlockHitClicks(CallbackInfo ci) {
        var client=(MinecraftClient)(Object)this;
        if(CombatModes.legacy()&&client.player!=null&&client.currentScreen==null&&client.player.isUsingItem())
            while(client.options.attackKey.wasPressed())doAttack();
    }
}
