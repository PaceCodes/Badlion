package net.badlion.client.util;

import java.util.concurrent.ConcurrentLinkedQueue;
import net.badlion.client.Wrapper;

public class STOCWorker implements Runnable
{
    private ConcurrentLinkedQueue<HashIndex> serverToClientQueue = new ConcurrentLinkedQueue();

    public void addToQueue(HashIndex index)
    {
        this.serverToClientQueue.add(index);
    }

    public void run()
    {
        try
        {
            while (true)
            {
                if (this.serverToClientQueue.size() <= 0)
                {
                    synchronized (this)
                    {
                        this.wait(100L);
                    }
                }
                else if (this.serverToClientQueue.size() > 0)
                {
                    HashIndex hashindex = (HashIndex)this.serverToClientQueue.poll();

                    if (hashindex != null)
                    {
                        Wrapper.getInstance().sendServerToClientComparisonHash(hashindex.getHash(), hashindex.getIndex());
                    }
                    else
                    {
                        System.exit(0);
                    }
                }
            }
        }
        catch (InterruptedException var4)
        {
            System.exit(0);
        }
    }
}
