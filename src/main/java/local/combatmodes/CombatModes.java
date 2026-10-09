package local.combatmodes;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.*;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import java.nio.file.*;
import java.util.*;
import static net.minecraft.server.command.CommandManager.literal;

public final class CombatModes implements ModInitializer {
    private static volatile MinecraftServer localServer;
    private static volatile boolean serverLegacy=true,clientActive,clientLegacy;
    public static volatile boolean clientCanControl;
    private static final Map<UUID,Integer> lastRequest=new HashMap<>();
    private static final Map<UUID,Boolean> permissions=new HashMap<>();
    public static boolean active(){return localServer!=null||clientActive;}
    public static boolean legacy(){return active()&&(localServer!=null?serverLegacy:clientLegacy);}
    public static boolean modern(){return active()&&!legacy();}
    public static String label(){return !active()?"主机未启用战斗切换":legacy()?"1.8 经典战斗":"1.21.1 现代战斗";}
    public record Select(boolean legacy) implements CustomPayload {
        public static final Id<Select> ID=new Id<>(Identifier.of("combat_modes","select_v3"));
        public static final PacketCodec<RegistryByteBuf,Select> CODEC=PacketCodec.of((p,b)->b.writeBoolean(p.legacy),b->new Select(b.readBoolean()));
        public Id<Select> getId(){return ID;}
    }
    public record State(boolean legacy,boolean canControl) implements CustomPayload {
        public static final Id<State> ID=new Id<>(Identifier.of("combat_modes","state_v3"));
        public static final PacketCodec<RegistryByteBuf,State> CODEC=PacketCodec.of((p,b)->{b.writeBoolean(p.legacy);b.writeBoolean(p.canControl);},b->new State(b.readBoolean(),b.readBoolean()));
        public Id<State> getId(){return ID;}
    }
    public static boolean canControl(ServerPlayerEntity player){
        MinecraftServer s=player.getServer();
        return s!=null;
    }
    private static boolean canControl(ServerCommandSource source){
        return true;
    }
    private static Path file(MinecraftServer s){return s.getSavePath(WorldSavePath.ROOT).resolve("combat-modes.properties");}
    private static boolean load(MinecraftServer s){
        Path p=file(s);if(!Files.exists(p))return true;
        try(var in=Files.newBufferedReader(p)){var props=new Properties();props.load(in);String mode=props.getProperty("mode","legacy");
            if(!mode.equals("legacy")&&!mode.equals("modern"))throw new IllegalArgumentException("Invalid mode "+mode);
            return mode.equals("legacy");
        }catch(Exception e){throw new IllegalStateException("Cannot load "+p,e);}
    }
    private static void save(MinecraftServer s,boolean legacy)throws java.io.IOException{
        Path p=file(s),temp=p.resolveSibling(p.getFileName()+".tmp");
        Files.writeString(temp,"# Combat rules for this world\nmode="+(legacy?"legacy":"modern")+"\n");
        try{Files.move(temp,p,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
        catch(AtomicMoveNotSupportedException e){Files.move(temp,p,StandardCopyOption.REPLACE_EXISTING);}
    }
    public static void clientState(State state){clientLegacy=state.legacy;clientCanControl=state.canControl;clientActive=true;ConfigBridge.apply();ConfigBridge.applyClient();}
    public static void clearClient(){clientActive=false;clientCanControl=false;if(localServer==null)ConfigBridge.restoreAll();}
    public static void sync(ServerPlayerEntity p){boolean allowed=canControl(p);permissions.put(p.getUuid(),allowed);if(ServerPlayNetworking.canSend(p,State.ID))ServerPlayNetworking.send(p,new State(serverLegacy,allowed));}
    public static void setMode(MinecraftServer s,boolean next){
        if(s!=localServer)throw new IllegalArgumentException("Wrong server");
        try{save(s,next);}catch(Exception e){throw new IllegalStateException("Cannot save combat mode",e);}
        serverLegacy=next;ConfigBridge.apply();
        // The stacks have not changed identity: refresh every equipped slot under the new profile.
        for(var w:s.getWorlds())for(var entity:w.iterateEntities())if(entity instanceof LivingEntity living){
            ClassicRules.refreshEffects(living);
            ModernDefaults.refreshEquipment(living);
            if(!next&&living.getActiveItem().getItem() instanceof SwordItem)living.stopUsingItem();
            if(next&&living.isUsingItem()&&(living.getActiveHand()==Hand.OFF_HAND||living.getActiveItem().isOf(Items.SHIELD)))living.stopUsingItem();
        }
        for(var p:s.getPlayerManager().getPlayerList()){p.resetLastAttackedTicks();sync(p);p.sendMessage(Text.literal("已切换："+label()),false);}
    }
    @Override public void onInitialize(){
        PayloadTypeRegistry.playC2S().register(Select.ID,Select.CODEC);
        PayloadTypeRegistry.playS2C().register(State.ID,State.CODEC);
        ServerLifecycleEvents.SERVER_STARTED.register(s->{serverLegacy=load(s);localServer=s;ConfigBridge.apply();for(var w:s.getWorlds())for(var e:w.iterateEntities())if(e instanceof LivingEntity living){ClassicRules.refreshEffects(living);ModernDefaults.refreshEquipment(living);}});
        ServerLifecycleEvents.SERVER_STOPPED.register(s->{localServer=null;clientActive=false;lastRequest.clear();permissions.clear();ConfigBridge.restoreAll();});
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_LOAD.register((entity,world)->{if(entity instanceof LivingEntity living&&active()){ClassicRules.refreshEffects(living);ModernDefaults.refreshEquipment(living);}});
        ServerTickEvents.START_SERVER_TICK.register(s->{ConfigBridge.apply();if(s.getTicks()%20==0)for(var p:s.getPlayerManager().getPlayerList())if(!Objects.equals(permissions.get(p.getUuid()),canControl(p)))sync(p);});
        ServerPlayConnectionEvents.JOIN.register((h,sender,s)->{
            if(!ServerPlayNetworking.canSend(h.player,State.ID)){h.disconnect(Text.literal("此房间需要安装战斗模式切换 Mod 和所需依赖。"));return;}
            sync(h.player);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((h,s)->{lastRequest.remove(h.player.getUuid());permissions.remove(h.player.getUuid());});
        ServerPlayNetworking.registerGlobalReceiver(Select.ID,(packet,ctx)->{
            var p=ctx.player();
            int now=ctx.server().getTicks();Integer last=lastRequest.get(p.getUuid());
            if(last!=null&&now-last<10)return;lastRequest.put(p.getUuid(),now);
            if(!canControl(p)){sync(p);return;}
            if(serverLegacy!=packet.legacy)setMode(ctx.server(),packet.legacy);else sync(p);
        });
        CommandRegistrationCallback.EVENT.register((d,a,e)->d.register(literal("combatmode")
            .executes(ctx->{ctx.getSource().sendFeedback(()->Text.literal("当前："+label()),false);return 1;})
            .then(literal("legacy").requires(CombatModes::canControl).executes(ctx->{setMode(ctx.getSource().getServer(),true);return 1;}))
            .then(literal("modern").requires(CombatModes::canControl).executes(ctx->{setMode(ctx.getSource().getServer(),false);return 1;}))));
    }
}
