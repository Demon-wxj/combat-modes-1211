package local.combatmodes.mixin;
import local.combatmodes.ModernDefaults;
import net.minecraft.item.ItemStack;
import net.minecraft.component.*;
import org.spongepowered.asm.mixin.*;

@Mixin(ItemStack.class)
public abstract class StackComponentsMixin implements ComponentHolder {
    @Shadow public abstract ComponentMap getComponents();
    // Override ComponentHolder's default lookup; never rewrite or serialize item components.
    @Override public <T> T get(ComponentType<? extends T> type){
        return ModernDefaults.component((ItemStack)(Object)this,type,getComponents().get(type));
    }
    @Override public <T> T getOrDefault(ComponentType<? extends T> type,T fallback){
        T value=get(type);return value==null?fallback:value;
    }
}
