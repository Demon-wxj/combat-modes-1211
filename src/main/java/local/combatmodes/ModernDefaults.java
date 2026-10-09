package local.combatmodes;

import java.util.*;
import net.minecraft.component.*;
import net.minecraft.component.type.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.*;

/** Capture values before GoldenAgeCombat changes default components at bootstrap. */
public final class ModernDefaults {
    public static final Map<Item,List<AttributeModifiersComponent.Entry>> ATTRIBUTES=new IdentityHashMap<>();
    public static final Map<Item,ToolComponent> TOOLS=new IdentityHashMap<>();
    private static final Map<AttributeModifiersComponent,Map<Item,AttributeModifiersComponent>> MODERN_CACHE=new WeakHashMap<>(),CLASSIC_CACHE=new WeakHashMap<>();
    private static final FoodComponent CLASSIC_APPLE=new FoodComponent.Builder().nutrition(4).saturationModifier(1.2f).alwaysEdible()
        .statusEffect(new StatusEffectInstance(StatusEffects.REGENERATION,600,4),1)
        .statusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION,2400,0),1)
        .statusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE,6000,0),1)
        .statusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE,6000,0),1).build();
    @SuppressWarnings("unchecked")
    public static <T> T component(ItemStack stack,ComponentType<? extends T> type,T original){
        if(CombatModes.legacy()&&type==DataComponentTypes.FOOD&&stack.isOf(Items.ENCHANTED_GOLDEN_APPLE)
            &&stack.getComponentChanges().get(type)==null)return (T)CLASSIC_APPLE;
        if(!CombatModes.active()||(type!=DataComponentTypes.TOOL&&type!=DataComponentTypes.ATTRIBUTE_MODIFIERS)
            ||stack.isEmpty()||stack.getComponentChanges().get(type)!=null)return original;
        if(type==DataComponentTypes.TOOL){
            if(CombatModes.legacy()&&stack.getItem() instanceof HoeItem&&original instanceof ToolComponent tool)
                return (T)new ToolComponent(tool.rules(),tool.defaultMiningSpeed(),0);
            if(TOOLS.containsKey(stack.getItem()))return (T)TOOLS.get(stack.getItem());
        }
        if(type!=DataComponentTypes.ATTRIBUTE_MODIFIERS)return original;
        Item item=stack.getItem();var before=ATTRIBUTES.get(item);
        var raw=original instanceof AttributeModifiersComponent attributes?attributes:AttributeModifiersComponent.DEFAULT;
        // 1.21.1 ArmorItem exposes defaults via getAttributeModifiers(), outside its component map.
        var current=raw.modifiers().isEmpty()?new AttributeModifiersComponent(item.getAttributeModifiers().modifiers(),raw.showInTooltip()):raw;
        if(current.modifiers().isEmpty())return original;
        boolean classic=CombatModes.legacy();
        var cache=classic?CLASSIC_CACHE:MODERN_CACHE;
        synchronized(cache){
            return (T)cache.computeIfAbsent(current,k->new IdentityHashMap<>()).computeIfAbsent(item,k->{
                var entries=new ArrayList<>(current.modifiers());
                if(!classic&&before!=null){
                    entries.removeIf(e->e.matches(EntityAttributes.GENERIC_ATTACK_DAMAGE,Item.BASE_ATTACK_DAMAGE_MODIFIER_ID));
                    before.stream().filter(e->e.matches(EntityAttributes.GENERIC_ATTACK_DAMAGE,Item.BASE_ATTACK_DAMAGE_MODIFIER_ID)).forEach(entries::add);
                }
                if(classic&&Registries.ITEM.getId(item).getNamespace().equals("minecraft")){
                    entries.removeIf(e->e.matches(EntityAttributes.GENERIC_ATTACK_SPEED,Item.BASE_ATTACK_SPEED_MODIFIER_ID));
                    if(item instanceof ToolItem tool&&tool.getMaterial() instanceof ToolMaterials){
                        double bonus=item instanceof HoeItem?0:tool.getMaterial().getAttackDamage()
                            +(item instanceof SwordItem?4:item instanceof AxeItem?3:item instanceof PickaxeItem?2:1);
                        entries.removeIf(e->e.matches(EntityAttributes.GENERIC_ATTACK_DAMAGE,Item.BASE_ATTACK_DAMAGE_MODIFIER_ID));
                        // Keep the zero bonus on hoes: an empty component would fall back to GAC's default modifiers.
                        entries.add(new AttributeModifiersComponent.Entry(EntityAttributes.GENERIC_ATTACK_DAMAGE,
                            new EntityAttributeModifier(Item.BASE_ATTACK_DAMAGE_MODIFIER_ID,bonus,EntityAttributeModifier.Operation.ADD_VALUE),AttributeModifierSlot.MAINHAND));
                    }
                    if(item instanceof ArmorItem)entries.removeIf(e->e.attribute().equals(EntityAttributes.GENERIC_ARMOR_TOUGHNESS)
                        ||e.attribute().equals(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE));
                }
                return new AttributeModifiersComponent(List.copyOf(entries),current.showInTooltip());
            });
        }
    }
    /** Replace equipped modifiers in place; never rewrite stacks, durability, enchantments or potion effects. */
    public static void refreshEquipment(LivingEntity living){
        for(var slot:EquipmentSlot.values()){
            ItemStack stack=living.getEquippedStack(slot);if(stack.isEmpty())continue;
            var removals=new ArrayList<>(stack.getComponents().getOrDefault(DataComponentTypes.ATTRIBUTE_MODIFIERS,AttributeModifiersComponent.DEFAULT).modifiers());
            removals.addAll(stack.getItem().getAttributeModifiers().modifiers());
            removals.addAll(ATTRIBUTES.getOrDefault(stack.getItem(),List.of()));
            for(var entry:removals)if(entry.slot().matches(slot)){
                var attribute=living.getAttributeInstance(entry.attribute());if(attribute!=null)attribute.removeModifier(entry.modifier().id());
            }
            stack.applyAttributeModifiers(slot,(type,modifier)->{
                var attribute=living.getAttributeInstance(type);
                if(attribute!=null){attribute.removeModifier(modifier.id());attribute.addTemporaryModifier(modifier);}
            });
        }
    }
}
