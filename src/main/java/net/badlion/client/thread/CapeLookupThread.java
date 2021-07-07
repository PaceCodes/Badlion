package net.badlion.client.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import net.badlion.client.Wrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;

public class CapeLookupThread extends Thread
{
    private BlockingQueue<UUID> queue = new LinkedBlockingDeque();

    public CapeLookupThread()
    {
        super("CapeLookupThread");
    }

    public void addPlayer(UUID uuid)
    {
        this.queue.add(uuid);
    }

    public void run()
    {
        int i = -1;

        while (true)
        {
            while (Wrapper.getInstance().wasAntiLauncherSet())
            {
                if (this.queue.size() > 0 && i == -1)
                {
                    i = 5;
                }

                if (this.queue.size() > 0 && i == 0)
                {
                    List<UUID> list = new ArrayList();
                    this.queue.drainTo(list);

                    try
                    {
                        LogManager.getLogger().info("Checking players: [" + StringUtils.join((Iterable)list, ", ") + "]");
                        String s = Wrapper.getInstance().checkCapesBulk(Wrapper.getInstance().getGsonNonPretty().toJson((Object)(new CapeLookupThread.BulkCheckRequest(list))));

                        if (s.contains("error"))
                        {
                            this.queue.addAll(list);
                            LogManager.getLogger().info("Error checking players.");
                            i = -1;
                            continue;
                        }

                        CapeLookupThread.BulkCheckResponse capelookupthread$bulkcheckresponse = (CapeLookupThread.BulkCheckResponse)Wrapper.getInstance().getGsonNonPretty().fromJson(s, CapeLookupThread.BulkCheckResponse.class);
                        int j = 0;

                        for (UUID uuid : list)
                        {
                            Wrapper.getInstance().getCapeManager().getUserToCape().put(uuid, capelookupthread$bulkcheckresponse.getCapes().get(j));
                            ++j;
                        }

                        i = -1;
                    }
                    catch (UnsatisfiedLinkError unsatisfiedlinkerror)
                    {
                        LogManager.getLogger().info("Error checking capes: " + unsatisfiedlinkerror.getMessage());
                    }
                }

                try
                {
                    if (i > 0)
                    {
                        --i;
                    }

                    Thread.sleep(1000L);
                }
                catch (InterruptedException interruptedexception1)
                {
                    interruptedexception1.printStackTrace();
                }
            }

            try
            {
                Thread.sleep(1000L);
            }
            catch (InterruptedException interruptedexception)
            {
                interruptedexception.printStackTrace();
            }
        }
    }

    public class BulkCheckRequest
    {
        private List<UUID> uuids;

        public BulkCheckRequest(List<UUID> uuids)
        {
            this.uuids = uuids;
        }
    }

    public class BulkCheckResponse
    {
        private List<Integer> capes;

        public BulkCheckResponse(List<Integer> capes)
        {
            this.capes = capes;
        }

        public List<Integer> getCapes()
        {
            return this.capes;
        }
    }
}
