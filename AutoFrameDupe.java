package com.matt.forgehax.mods;

import com.matt.forgehax.Globals;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Comparator;

/**
 * Created on 2/8/2022 by Windowsxp4972
 */
@RegisterMod
public class AutoFrameDupe extends ToggleMod {

  public final Setting<Integer> delay =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("delay")
          .description("delay in ticks")
          .defaultTo(10)
          .min(0)
          .max(20)
          .build();

  public AutoFrameDupe() {
    super(Category.EXPLOIT, "AutoFrameDupe", false, "Shit Servers Dupe");
  }

  private boolean isSending = false;
  private Entity entity;

  private int ticks = 0;

  @Override
  public void onEnabled (){
    isSending = false;
    ticks = 0;

  }
  @Override
  public void onDisabled (){
    ticks = 0;
    Globals.MC.player.inventory.currentItem = isSending? 0:1;
  }

  private boolean isValidTileEntity(Entity entity) { return (entity instanceof EntityItemFrame) && Globals.MC.player.getDistance(entity)<4f; }

  @SubscribeEvent
  public void onTick(LocalPlayerUpdateEvent event) {
    ticks++;
    if(ticks >= delay.get())
      return;
    entity = Globals.MC.world.loadedEntityList.stream()
        .filter(loadedEntity -> isValidTileEntity(loadedEntity))
        .min(Comparator.comparing(loadedEntity -> Globals.MC.player.getDistance(loadedEntity.getPosition().getX(), loadedEntity.getPosition().getY(), loadedEntity.getPosition().getZ())))
        .orElse(null);
    EntityItemFrame itemFrame = (EntityItemFrame)entity;
    if(entity == null) {
      toggle();
      return;
    }
    if(Globals.MC.player.getHeldItemMainhand() == null || Globals.MC.player.getHeldItemMainhand().getItem() == Items.AIR)
      return;

    if(isSending && (itemFrame.getDisplayedItem() == null || itemFrame.getDisplayedItem().getItem() == Items.AIR))
      isSending = false;
    Globals.MC.player.connection.sendPacket(isSending?new CPacketUseEntity(entity):new CPacketUseEntity(entity, EnumHand.MAIN_HAND));
    Globals.MC.player.connection.sendPacket(isSending?new CPacketHeldItemChange(0):new CPacketHeldItemChange(1));
    isSending = !isSending;
    ticks = 0;

  }

}
