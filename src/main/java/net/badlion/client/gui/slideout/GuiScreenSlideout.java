package net.badlion.client.gui.slideout;

import net.badlion.client.Wrapper;
import net.badlion.client.events.event.GUIClickMouse;
import net.badlion.client.events.event.GUIKeyPress;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiScreenSlideout extends GuiScreen
{
    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button)
    {
    }

    /**
     * Called when the mouse is clicked. Args : mouseX, mouseY, clickedButton
     */
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        Wrapper.getInstance().getActiveModProfile().passEvent(new GUIClickMouse(mouseButton));
    }

    /**
     * Fired when a key is typed (except F11 which toggles full screen). This is the equivalent of
     * KeyListener.keyTyped(KeyEvent e). Args : character (character on the key), keyCode (lwjgl Keyboard key code)
     */
    protected void keyTyped(char typedChar, int keyCode)
    {
        if (Wrapper.getInstance().getActiveModProfile().getSlideoutAccess().getSlideoutInstance().isOpen())
        {
            Wrapper.getInstance().getActiveModProfile().passEvent(new GUIKeyPress(typedChar, keyCode));
        }
    }

    /**
     * Returns true if this GUI should pause the game when it is displayed in single-player
     */
    public boolean doesGuiPauseGame()
    {
        return false;
    }
}
