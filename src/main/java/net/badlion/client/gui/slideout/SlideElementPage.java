package net.badlion.client.gui.slideout;

import java.util.ArrayList;
import java.util.List;

public class SlideElementPage
{
    private List<RenderElement> elementList = new ArrayList();

    public void addElement(RenderElement e)
    {
        this.elementList.add(e);
    }

    public RenderElement getElement(int index)
    {
        return (RenderElement)this.elementList.get(index);
    }

    public List<RenderElement> getElementList()
    {
        return this.elementList;
    }
}
