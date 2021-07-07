package net.badlion.client.mods.render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.swing.JOptionPane;
import net.badlion.client.Wrapper;
import net.badlion.client.events.Event;
import net.badlion.client.events.EventType;
import net.badlion.client.events.event.MotionUpdate;
import net.badlion.client.events.event.RenderGame;
import net.badlion.client.gui.BadlionFontRenderer.FontType;
import net.badlion.client.gui.slideout.Dropdown;
import net.badlion.client.gui.slideout.Label;
import net.badlion.client.gui.slideout.ModPreviewRenderer;
import net.badlion.client.gui.slideout.Padding;
import net.badlion.client.gui.slideout.SlidePage;
import net.badlion.client.gui.slideout.SlideoutGUI;
import net.badlion.client.gui.slideout.elements.ColorPicker;
import net.badlion.client.mods.ModProfile;
import net.badlion.client.mods.misc.SlideoutAccess;
import net.badlion.client.mods.render.color.ModColor;
import net.badlion.client.mods.render.gui.BoxedCoord;
import net.badlion.client.util.ImageDimension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Timer;
import org.lwjgl.opengl.GL11;

public class ArmorStatus
  extends RenderMod
{
  private ArmorMode armorMode = ArmorMode.BOTH;
  private ModOrientation armorOrientation = ModOrientation.HORIZONTAL;
  private ModColor backgroundColor = new ModColor(1140850688);
  private transient Dropdown duraDropdown;
  private transient Dropdown orientationDropdown;
  
  public void reset()
  {
    this.armorMode = ArmorMode.BOTH;
    this.armorOrientation = ModOrientation.HORIZONTAL;
    this.defaultSizeX = 100;
    this.defaultSizeY = 24;
    this.backgroundColor.setColor(1140850688);
    super.reset();
  }
  
  public void createCogMenu()
  {
    SlideoutGUI slideoutgui = Wrapper.getInstance().getActiveModProfile().getSlideoutAccess().getSlideoutInstance();
    this.slideCogMenu = new SlidePage(getName() + "_cog", slideoutgui.getSlideoutWidth(), slideoutgui.getSlideoutHeight());
    this.slideCogMenu.addElement(new Padding(slideoutgui.getSlideoutWidth() - 25, 6));
    this.slideCogMenu.addElement(new Label(getName(), -1, 16, FontType.TITLE, false));
    this.slideCogMenu.addElement(new Padding(slideoutgui.getSlideoutWidth() - 25, 10));
    this.slideCogMenu.addElement(new ModPreviewRenderer(this, 0, 0));
    this.slideCogMenu.addElement(new Label("Settings", -7894388, 12, FontType.TITLE, true));
    this.slideCogMenu.addElement(this.duraDropdown = new Dropdown(new String[] { "Durability", "Bar", "Both" }, this.armorMode.equals(ArmorMode.BAR) ? 1 : this.armorMode.equals(ArmorMode.DURA) ? 0 : 2, 0.19D));
    this.slideCogMenu.addElement(this.orientationDropdown = new Dropdown(new String[] { "Vertical", "Horizontal" }, this.armorOrientation.equals(ModOrientation.HORIZONTAL) ? 1 : 0, 0.19D));
    this.slideCogMenu.addElement(new ColorPicker("Background Color", this.backgroundColor, 0.13D, true));
    if (this.armorOrientation.equals(ModOrientation.VERTICAL))
    {
      this.defaultSizeX = 100;
      this.defaultSizeY = 24;
    }
    else
    {
      this.defaultSizeX = 40;
      this.defaultSizeY = 83;
    }
    super.createCogMenu();
  }
  
  public ArmorStatus()
  {
    super("ArmorStatus", 65328, 92, 100, 25);
    this.defaultSizeX = 100;
    this.defaultSizeY = 24;
    this.iconDimension = new ImageDimension(104, 88);
    this.defaultTopLeftBox = new BoxedCoord(8, 30, 0.6833333333333333D, 0.8D);
    this.defaultCenterBox = new BoxedCoord(10, 31, 0.7666666666666667D, 0.6540540540540519D);
    this.defaultBottomRightBox = new BoxedCoord(12, 32, 0.85D, 0.5333333333333333D);
  }
  
  public void init()
  {
    registerEvent(EventType.RENDER_GAME);
    registerEvent(EventType.MOTION_UPDATE);
    setFontOffset(0.008D);
    super.init();
  }
  
  public void onEvent(Event event)
  {
    if ((event instanceof MotionUpdate))
    {
      if (!Wrapper.getInstance().checkVerify())
      {
        JOptionPane.showMessageDialog(null, "Please use the Badlion launcher!  If you are seeing this error constantly, please fully restart the Badlion Client and launcher.", "Badlion Client", 0);
        System.exit(0);
      }
      this.armorMode = (this.duraDropdown.getValue().equals("Bar") ? ArmorMode.BAR : this.duraDropdown.getValue().equals("Durability") ? ArmorMode.DURA : ArmorMode.BOTH);
      ModOrientation modorientation = ModOrientation.valueOf(this.orientationDropdown.getValue().toUpperCase(Locale.US));
      if (!modorientation.equals(this.armorOrientation))
      {
        this.armorOrientation = modorientation;
        int i = this.defaultSizeX;
        int j = this.defaultSizeY;
        if (this.armorOrientation.equals(ModOrientation.HORIZONTAL))
        {
          this.defaultSizeX = 100;
          this.defaultSizeY = 24;
        }
        else
        {
          this.defaultSizeX = 40;
          this.defaultSizeY = 83;
        }
        this.sizeX = ((int)(this.sizeX / i * this.defaultSizeX));
        this.sizeY = ((int)(this.sizeY / j * this.defaultSizeY));
        Wrapper.getInstance().getActiveModProfile().getSlideoutAccess().getSlideoutInstance().initPages();
      }
      this.backgroundColor.tickColor();
    }
    if (((event instanceof RenderGame)) && (isEnabled()))
    {
      RenderGame rendergame = (RenderGame)event;
      List<ItemStack> list = (List<ItemStack>) (Wrapper.getInstance().getActiveModProfile().getModConfigurator().isInEditingMode() ? Arrays.asList(new ItemStack[] { new ItemStack(Item.getItemById(313)), new ItemStack(Item.getItemById(312)), new ItemStack(Item.getItemById(311)), new ItemStack(Item.getItemById(310)) }) : Arrays.asList((Object[])this.gameInstance.thePlayer.inventory.armorInventory.clone()));
      Collections.reverse(list);
      ArrayList arraylist = new ArrayList(list);
      if (Wrapper.getInstance().getActiveModProfile().getModConfigurator().isInEditingMode()) {
        arraylist.add(new ItemStack(Item.getItemById(276)));
      } else if (this.gameInstance.thePlayer.getHeldItem() != null) {
        arraylist.add(this.gameInstance.thePlayer.getHeldItem().copy());
      }
      int j1 = 0;
      for (Object itemstack : arraylist) {
        if (itemstack != null) {
          j1++;
        }
      }
      if (j1 == 0) {
        return;
      }
      int k1 = 0;
      int l1 = 1;
      int k = 0;
      beginRender();
      int l = this.defaultSizeX - 1;
      if (this.armorMode == ArmorMode.BAR) {
        l = (int)(l * 0.5D);
      }
      if (this.armorOrientation == ModOrientation.HORIZONTAL) {
        l = j1 * 20;
      }
      if ((this.armorOrientation == ModOrientation.VERTICAL) && (this.armorMode == ArmorMode.BAR) && (getX() + 40 > Minecraft.getMinecraft().displayWidth / 2)) {
        l1 += 20;
      }
      Gui.drawRect(l1 - 1, 0, l1 + l, this.armorMode == ArmorMode.BAR ? this.defaultSizeY - 6 : this.armorOrientation == ModOrientation.VERTICAL ? 16 * j1 + 3 : this.defaultSizeY, this.backgroundColor.getColorInt());
      if ((this.armorOrientation == ModOrientation.VERTICAL) && (getX() + 40 > Minecraft.getMinecraft().displayWidth / 2)) {
        l1 = 22;
      }
      for (final ItemStack item : list) {
          if (item != null) {
              switch (this.armorOrientation) {
          case VERTICAL: 
            l1 += k1;
            break;
          case HORIZONTAL: 
            k += k1;
          }
          GlStateManager.enableRescaleNormal();
          GlStateManager.enableBlend();
          GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
          RenderHelper.enableGUIStandardItemLighting();
          float f = item.animationsToGo - this.gameInstance.getTimer().renderPartialTicks;
          if (f > 0.0F)
          {
            GlStateManager.pushMatrix();
            float f1 = 1.0F + f / 5.0F;
            GlStateManager.translate(l1 + 8, k + 12, 0.0F);
            GlStateManager.scale(1.0F / f1, (f1 + 1.0F) / 2.0F, 1.0F);
            GlStateManager.translate(-(l1 + 8), -(k + 12), 0.0F);
          }
          this.gameInstance.getRenderItem().renderItemAndEffectIntoGUI(item, l1, k);
          if (f > 0.0F) {
            GlStateManager.popMatrix();
          }
          if (this.armorMode != ArmorMode.DURA) {
            renderItemOverLayIntoGuiForced(this.gameInstance.fontRendererObj, item, l1, k, null);
          }
          RenderHelper.disableStandardItemLighting();
          GlStateManager.disableRescaleNormal();
          GlStateManager.disableBlend();
          if (item.getMaxDamage() > 0)
          {
            int i2 = 0;
            double d0 = 1.0D - (item.getItemDamage() / (item.getMaxDamage()));
            if (d0 > 0.0D) {
              i2 = -65536;
            }
            if (d0 >= 0.1D) {
              i2 = -52480;
            }
            if (d0 >= 0.4D) {
              i2 = 65280;
            }
            if (d0 >= 0.6D) {
              i2 = -1;
            }
            if (this.armorMode != ArmorMode.BAR)
            {
              int i1 = rendergame.getGameRenderer().getFontRenderer().getStringWidth(String.valueOf(item.getMaxDamage() - (item.getItemDamage())));
              double d1 = 0;
              if (this.armorOrientation == ModOrientation.VERTICAL)
              {
                double d11 = 0.9D;
                GL11.glScaled(d11, d11, d11);
                if (getX() + 40 > Minecraft.getMinecraft().displayWidth / 2) {
                  rendergame.getGameRenderer().drawString(this.gameInstance.fontRendererObj, String.valueOf(item.getMaxDamage() - item.getItemDamage()), (int)((l1 - 18 + (22 - i1) / 2) * (1.0D / d11)), (int)(1.0D + k * (1.0D / d11)) + 5, i2);
                } else {
                  rendergame.getGameRenderer().drawString(this.gameInstance.fontRendererObj, String.valueOf(item.getMaxDamage() - item.getItemDamage()), (int)((l1 + 18 + (22 - i1) / 2) * (1.0D / d11)), (int)(1.0D + k * (1.0D / d11)) + 5, i2);
                }
              }
              else
              {
                d1 = 0.8D;
                GL11.glScaled(d1, d1, d1);
                rendergame.getGameRenderer().drawString(this.gameInstance.fontRendererObj, String.valueOf(item.getMaxDamage() - item.getItemDamage()), (int)((l1 + (18.0D - i1 * d1) / 2.0D) * (1.0D / d1)), (int)(1.0D + k * (1.0D / d1)) + 20, i2);
              }
              GL11.glScaled(1.0D / d1, 1.0D / d1, 1.0D / d1);
            }
          }
          switch (this.armorOrientation)
          {
          case VERTICAL: 
            k1 = 20;
            break;
          case HORIZONTAL: 
            k1 = 16;
          }
        }
      }
      endRender();
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }
    super.onEvent(event);
  }
  
  public void renderItemOverLayIntoGuiForced(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, String text)
  {
    if (stack != null)
    {
      if ((stack.stackSize != 1) || (text != null))
      {
        String s = text == null ? String.valueOf(stack.stackSize) : text;
        if ((text == null) && (stack.stackSize < 1)) {
          s = EnumChatFormatting.RED + String.valueOf(stack.stackSize);
        }
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableBlend();
        fr.drawStringWithShadow(s, xPosition + 19 - 2 - fr.getStringWidth(s), yPosition + 6 + 3, 16777215);
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
      }
      if (stack.getMaxDamage() != 0)
      {
        int j = (int)Math.round(13.0D - stack.getItemDamage() * 13.0D / stack.getMaxDamage());
        int i = (int)Math.round(255.0D - stack.getItemDamage() * 255.0D / stack.getMaxDamage());
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        draw(worldrenderer, xPosition + 2, yPosition + 13, 13, 2, 0, 0, 0, 255);
        draw(worldrenderer, xPosition + 2, yPosition + 13, 12, 1, (255 - i) / 4, 64, 0, 255);
        draw(worldrenderer, xPosition + 2, yPosition + 13, j, 1, 255 - i, i, 0, 255);
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
      }
    }
  }
  
  private void draw(WorldRenderer renderer, int x, int y, int width, int height, int red, int green, int blue, int alpha)
  {
    renderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
    renderer.pos(x + 0, y + 0, 0.0D).color(red, green, blue, alpha).endVertex();
    renderer.pos(x + 0, y + height, 0.0D).color(red, green, blue, alpha).endVertex();
    renderer.pos(x + width, y + height, 0.0D).color(red, green, blue, alpha).endVertex();
    renderer.pos(x + width, y + 0, 0.0D).color(red, green, blue, alpha).endVertex();
    Tessellator.getInstance().draw();
  }
  
  public static enum ArmorMode
  {
    DURA,  BAR,  BOTH;
  }
}
