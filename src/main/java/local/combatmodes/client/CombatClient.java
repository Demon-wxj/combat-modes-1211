package local.combatmodes.client;
import local.combatmodes.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.*;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
public final class CombatClient implements ClientModInitializer {
    private static boolean open;
    public void onInitializeClient(){
        var key=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.combat_modes.menu",InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_F7,"category.combat_modes"));
        ClientPlayNetworking.registerGlobalReceiver(CombatModes.State.ID,(state,ctx)->CombatModes.clientState(state));
        ClientPlayConnectionEvents.DISCONNECT.register((h,c)->{CombatModes.clearClient();AttackIndicatorProfile.restore(c);open=false;});
        ClientTickEvents.END_CLIENT_TICK.register(c->{
            if(c.world!=null){ConfigBridge.apply();ConfigBridge.applyClient();}
            else CombatModes.clearClient();
            AttackIndicatorProfile.tick(c);
            while(key.wasPressed())if(c.player!=null&&c.currentScreen==null)open=true;
            if(open){open=false;c.setScreen(new CombatScreen(c.currentScreen));}
        });
        ClientCommandRegistrationCallback.EVENT.register((d,a)->d.register(literal("combatmenu").executes(ctx->{open=true;return 1;})));
    }
}
