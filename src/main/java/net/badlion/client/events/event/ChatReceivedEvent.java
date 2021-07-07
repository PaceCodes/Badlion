package net.badlion.client.events.event;

import java.util.List;
import net.badlion.client.events.Event;
import net.badlion.client.events.EventType;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.util.IChatComponent;

public class ChatReceivedEvent extends Event
{
    private IChatComponent component;
    private List<ChatLine> chatLines;

    public ChatReceivedEvent(IChatComponent component, List<ChatLine> chatLines)
    {
        super(EventType.CHAT_RECEIVED);
        this.component = component;
        this.chatLines = chatLines;
    }

    public IChatComponent getComponent()
    {
        return this.component;
    }

    public List<ChatLine> getChatLines()
    {
        return this.chatLines;
    }
}
