package local.combatmodes;

import fuzs.goldenagecombat.GoldenAgeCombat;
import fuzs.goldenagecombat.config.CommonConfig;
import fuzs.goldenagecombat.config.ServerConfig;
import fuzs.swordblockingmechanics.SwordBlockingMechanics;
import java.lang.reflect.Field;
import java.util.*;

/** Runtime overrides only: original third-party configuration files are never saved. */
public final class ConfigBridge {
    private static final Map<Object,Map<Field,Object>> originals = new IdentityHashMap<>();
    private static Object emptySounds, emptyParticles;
    private static Object appliedServer,appliedCommon,appliedSword;
    private static int appliedMode=-1;
    private static void set(Object config,String name,Object value) {
        try {
            Field f=config.getClass().getField(name);
            originals.computeIfAbsent(config,k->new HashMap<>()).computeIfAbsent(f,k->{try{return k.get(config);}catch(Exception e){throw new IllegalStateException(e);}});
            f.set(config,value);
        } catch (ReflectiveOperationException e) { throw new IllegalStateException("Incompatible combat component: "+name,e); }
    }
    private static void restore(Object config) {
        var values=originals.get(config);
        if(values!=null)values.forEach((f,v)->{try{f.set(config,v);}catch(Exception e){throw new IllegalStateException(e);}});
    }
    public static synchronized void apply() {
        if(!GoldenAgeCombat.CONFIG.getHolder(ServerConfig.class).isAvailable())return;
        var server=GoldenAgeCombat.CONFIG.get(ServerConfig.class);
        var common=GoldenAgeCombat.CONFIG.get(CommonConfig.class);
        var sword=SwordBlockingMechanics.CONFIG.get(fuzs.swordblockingmechanics.config.ServerConfig.class);
        int mode=!CombatModes.active()?0:CombatModes.legacy()?1:2;
        // Integrated servers share these objects with the render thread. Do not
        // briefly restore legacy defaults every client tick during live combat.
        if(appliedMode==mode&&appliedServer==server&&appliedCommon==common&&appliedSword==sword)return;
        restore(server);restore(common);restore(sword);
        appliedServer=server;appliedCommon=common;appliedSword=sword;appliedMode=mode;
        if(!CombatModes.active())return;
        if(CombatModes.legacy()) {
            set(server,"removeAttackCooldown",true);
            set(server,"requireSweepingEdge",true);
            set(server,"upwardsKnockback",false); // ClassicRules owns the entire velocity formula.
            set(server,"sprintAttacks",false);
            set(server,"criticalHitsWhileSprinting",true);
            set(server,"inflateHitboxes",false);
            set(server,"legacyFoodMechanics",true);
            set(server,"weakAttacksKnockBackPlayers",true);
            set(server,"fishingRodKnockback",true);
            set(server,"fishingRodLaunch",true);
            set(server,"fishingRodSlowerBreaking",true);
            set(server,"goldenAppleEffects",false); // Component profile also handles the last apple in a stack.
            set(server,"interactWhileUsing",false); // Our real-click handler also supports entity block-hits.
            set(common,"oldAttackDamage",true);
            set(common,"noItemDurabilityPenalty",false); // 1.8 tools cost two durability per hit, swords cost two per block.
            set(sword,"allowBlockingAndParrying",true);
            set(sword,"parryWindow",0);
            set(sword,"knockbackReduction",0.0);
            set(sword,"prioritizeOffHand",false);
            set(sword,"requireBothHands",false);
            set(sword,"requiredAttackStrength",0.0);
            set(sword,"protectionArc",360.0);
            return;
        }
        for(Field f:ServerConfig.class.getFields())if(f.getType()==boolean.class)set(server,f.getName(),false);
        if(emptySounds==null) {
            try {
                var empty=new ServerConfig();
                for(String n:List.of("canceledAttackSoundsRaw","canceledParticlesRaw")){
                    Field f=ServerConfig.class.getDeclaredField(n);f.setAccessible(true);f.set(empty,List.of());
                }
                empty.afterConfigReload();emptySounds=empty.canceledAttackSounds;emptyParticles=empty.canceledParticles;
            }catch(Exception e){throw new IllegalStateException(e);}
        }
        set(server,"canceledAttackSounds",emptySounds);set(server,"canceledParticles",emptyParticles);
        set(common,"oldAttackDamage",false);set(common,"noItemDurabilityPenalty",false);
        set(sword,"allowBlockingAndParrying",false);
    }
    public static synchronized void applyClient() {
        if(!GoldenAgeCombat.CONFIG.getHolder(fuzs.goldenagecombat.config.ClientConfig.class).isAvailable())return;
        var c=GoldenAgeCombat.CONFIG.get(fuzs.goldenagecombat.config.ClientConfig.class);restore(c);
        if(CombatModes.legacy()){set(c,"attributesStyle",fuzs.goldenagecombat.config.ClientConfig.AttributesStyle.LEGACY);return;}
        if(!CombatModes.modern())return;
        set(c,"interactAnimations",false);set(c,"noReequipWhenUsing",false);
        set(c,"attributesStyle",fuzs.goldenagecombat.config.ClientConfig.AttributesStyle.MODERN);
    }
    public static synchronized void restoreAll(){for(Object o:originals.keySet())restore(o);originals.clear();appliedServer=appliedCommon=appliedSword=null;appliedMode=-1;}
}
