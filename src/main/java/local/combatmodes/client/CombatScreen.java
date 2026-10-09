package local.combatmodes.client;
import local.combatmodes.CombatModes;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
public final class CombatScreen extends Screen {
    private final Screen parent;
    private ButtonWidget legacy,modern;
    public CombatScreen(Screen parent){super(Text.literal("战斗模式"));this.parent=parent;}
    protected void init(){
        int x=width/2-110,y=height/2;
        legacy=addDrawableChild(ButtonWidget.builder(Text.literal("1.8 经典战斗"),b->choose(true)).dimensions(x,y,220,20).build());
        modern=addDrawableChild(ButtonWidget.builder(Text.literal("1.21.1 现代战斗"),b->choose(false)).dimensions(x,y+26,220,20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("返回"),b->close()).dimensions(x,y+58,220,20).build());
    }
    private void choose(boolean mode){if(CombatModes.clientCanControl&&ClientPlayNetworking.canSend(CombatModes.Select.ID))ClientPlayNetworking.send(new CombatModes.Select(mode));}
    public void render(DrawContext ctx,int mx,int my,float delta){
        boolean allowed=CombatModes.clientCanControl&&ClientPlayNetworking.canSend(CombatModes.Select.ID);
        legacy.active=allowed&&!CombatModes.legacy();modern.active=allowed&&!CombatModes.modern();
        super.render(ctx,mx,my,delta);
        ctx.drawCenteredTextWithShadow(textRenderer,title,width/2,height/2-82,0xffffff);
        ctx.drawCenteredTextWithShadow(textRenderer,Text.literal("当前："+CombatModes.label()),width/2,height/2-59,0xffd477);
        ctx.drawCenteredTextWithShadow(textRenderer,Text.literal("经典：旧伤害、护甲与击退；现代：攻击蓄力"),width/2,height/2-40,0xbbbbbb);
        ctx.drawCenteredTextWithShadow(textRenderer,Text.literal(allowed?"全房间生效":"未连接到游戏"),width/2,height/2-22,0xbbbbbb);
    }
    public void close(){client.setScreen(parent);}
    public boolean shouldPause(){return false;}
}
