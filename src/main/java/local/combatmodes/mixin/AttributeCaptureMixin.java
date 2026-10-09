package local.combatmodes.mixin;
import fuzs.goldenagecombat.handler.AttackAttributeHandler;
import local.combatmodes.ModernDefaults;
import net.minecraft.item.Item;
import net.minecraft.component.*;
import net.minecraft.component.type.AttributeModifiersComponent;
import java.util.*;
import java.util.function.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=AttackAttributeHandler.class,remap=false)
public class AttributeCaptureMixin {
    @Inject(method="onComputeItemAttributeModifiers",at=@At("HEAD"))
    private static void capture(Item item,List<AttributeModifiersComponent.Entry> entries,CallbackInfo ci){ModernDefaults.ATTRIBUTES.put(item,List.copyOf(entries));}
    @ModifyVariable(method="onFinalizeItemComponents",at=@At("HEAD"),argsOnly=true)
    private static Consumer<Function<ComponentMap,ComponentChanges>> captureTool(Consumer<Function<ComponentMap,ComponentChanges>> consumer,Item item){
        return transform->consumer.accept(components->{
            ModernDefaults.TOOLS.put(item,components.get(DataComponentTypes.TOOL));
            return transform.apply(components);
        });
    }
}
