package local.combatmodes.compat;
import java.util.*;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.*;

/** Do not replace vanilla armor code or disable game membership/security checks.
 * Let the known fixed legacy-rule callbacks yield to the selected combat profile.
 * MixinMerged identifies their source, so unrelated injection methods are never patched.
 */
public final class CompatibilityPlugin implements IMixinConfigPlugin {
    private static final Set<String> SOURCES=Set.of("com.megawalls.mixin.DuelArmorMixin","com.megawalls.mixin.DuelHungerMixin","com.megawalls.mixin.DuelSwordUseMixin","com.megawalls.mixin.MWLegacyHungerMixin");
    public void onLoad(String p){} public String getRefMapperConfig(){return null;}
    public boolean shouldApplyMixin(String t,String m){return true;}
    public void acceptTargets(Set<String>a,Set<String>b){} public List<String>getMixins(){return null;}
    public void preApply(String t,ClassNode n,String m,IMixinInfo i){}
    public void postApply(String target,ClassNode node,String mixin,IMixinInfo info){
        if(!mixin.endsWith(".MinigameRulesMixin"))return;
        for(MethodNode method:node.methods){
            if(method.visibleAnnotations==null||!method.desc.endsWith(")V"))continue;
            boolean known=false;
            for(AnnotationNode a:method.visibleAnnotations)if(a.desc.equals("Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;"))
                for(int j=0;j<a.values.size()-1;j+=2)if(a.values.get(j).equals("mixin")){
                    Object owner=a.values.get(j+1);
                    if(SOURCES.contains(owner))known=true;
                    if((owner.equals("steak.mapperplugin.mixin.PlayerEntityMixin")||owner.equals("steak.mapperplugin.mixin.LivingEntityMixin"))&&method.name.endsWith("$onApplyDamage"))known=true;
                }
            if(!known)continue;
            InsnList guard=new InsnList();LabelNode oldRules=new LabelNode();
            guard.add(new MethodInsnNode(Opcodes.INVOKESTATIC,"local/combatmodes/CombatModes","active","()Z",false));
            guard.add(new JumpInsnNode(Opcodes.IFEQ,oldRules));guard.add(new InsnNode(Opcodes.RETURN));guard.add(oldRules);
            method.instructions.insert(guard);
            System.out.println("COMBAT_COMPAT guarded "+target+" :: "+method.name);
        }
    }
}
