package net.badlion.client.manager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import net.badlion.client.Wrapper;
import net.badlion.client.events.event.RenderGame;
import net.badlion.client.mods.ModProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.Sys;
import org.lwjgl.input.Mouse;

public class ModProfileManager
{
    private static final String NEW_PROFILE_NAME_PREFIX = "New Profile ";
    private static final String CLONED_PROFILE_NAME_PREFIX = "Cloned Profile ";
    private final File modProfilesFolder;
    private ModProfile activeModProfile;
    private List<ModProfile> modProfiles = new ArrayList();
    private Map<String, ModProfile> modProfileNames = new HashMap();
    private AccountManager accountManager = new AccountManager();

    public ModProfileManager()
    {
        this.modProfilesFolder = new File(Minecraft.getMinecraft().mcDataDir, "BLClient-Mod-Profiles-1.8/");
    }

    public void loadModProfiles() throws Throwable
    {
        if (!this.modProfilesFolder.exists())
        {
            this.modProfilesFolder.mkdir();
        }

        File[] afile = this.modProfilesFolder.listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.toLowerCase().endsWith(".json");
            }
        });

        if (afile != null)
        {
            for (File file1 : afile)
            {
                try
                {
                    FileReader filereader = new FileReader(file1);
                    Throwable throwable = null;

                    try
                    {
                        ModProfile modprofile = (ModProfile)Wrapper.getInstance().getGson().fromJson((Reader)filereader, ModProfile.class);

                        if (modprofile.getVersion() != null && modprofile.getVersion().intValue() == 2)
                        {
                            modprofile.setProfileName(file1.getName().substring(0, file1.getName().length() - 5));

                            if (this.modProfileNames.containsKey(modprofile.getProfileName()))
                            {
                                LogManager.getLogger().error("Duplicate Badlion Client Mod Profile detected, not loading duplicate: " + file1.getName());
                            }
                            else
                            {
                                this.modProfiles.add(modprofile);
                                this.modProfileNames.put(modprofile.getProfileName(), modprofile);

                                if (modprofile.isActive())
                                {
                                    this.activateModProfile(modprofile);
                                }
                            }
                        }
                        else
                        {
                            file1.renameTo(new File(this.modProfilesFolder, file1.getName() + ".OUTDATED"));
                        }
                    }
                    catch (Throwable throwable2)
                    {
                        throwable = throwable2;
                        throw throwable2;
                    }
                    finally
                    {
                        if (filereader != null)
                        {
                            if (throwable != null)
                            {
                                try
                                {
                                    filereader.close();
                                }
                                catch (Throwable throwable1)
                                {
                                    throwable.addSuppressed(throwable1);
                                }
                            }
                            else
                            {
                                filereader.close();
                            }
                        }
                    }
                }
                catch (Exception exception)
                {
                    LogManager.getLogger().error("Failed to load Badlion Client Mod Profile: " + file1.getName());
                    LogManager.getLogger().catching(exception);
                }
            }
        }

        if (this.modProfiles.isEmpty())
        {
            this.createDefaultModProfile();
        }
        else if (this.activeModProfile == null)
        {
            this.activateModProfile((ModProfile)this.modProfiles.get(0));
        }

        Collections.sort(this.modProfiles, new Comparator<ModProfile>()
        {
            public int compare(ModProfile o1, ModProfile o2)
            {
                return Integer.valueOf(o1.getSortIndex()).compareTo(Integer.valueOf(o2.getSortIndex()));
            }
        });
    }

    private void createDefaultModProfile()
    {
        ModProfile modprofile = new ModProfile("Default");
        this.activateModProfile(modprofile);
        this.modProfiles.add(modprofile);
        this.modProfileNames.put(modprofile.getProfileName(), modprofile);
    }

    public void saveActiveModProfile()
    {
        this.saveModProfile(this.activeModProfile);
    }

    private void saveModProfile(ModProfile modProfile)
    {
        File file1 = new File(this.modProfilesFolder, modProfile.getProfileName() + ".json");

        try
        {
            FileWriter filewriter = new FileWriter(file1);
            Throwable throwable = null;

            try
            {
                filewriter.write(Wrapper.getInstance().getGson().toJson((Object)modProfile));
            }
            catch (Throwable throwable2)
            {
                throwable = throwable2;
                try {
					throw throwable2;
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            finally
            {
                if (filewriter != null)
                {
                    if (throwable != null)
                    {
                        try
                        {
                            filewriter.close();
                        }
                        catch (Throwable throwable1)
                        {
                            throwable.addSuppressed(throwable1);
                        }
                    }
                    else
                    {
                        filewriter.close();
                    }
                }
            }
        }
        catch (Exception exception)
        {
            LogManager.getLogger().error("Failed to save Badlion Client Mod Profile: " + file1.getName());
            LogManager.getLogger().catching(exception);
        }
    }

    public void activateModProfile(ModProfile modProfile)
    {
        synchronized (this)
        {
            int i = Mouse.getX();
            int j = Mouse.getY();

            if (this.activeModProfile != null)
            {
                this.activeModProfile.deactivate();
                this.saveModProfile(this.activeModProfile);
            }

            this.activeModProfile = modProfile;
            modProfile.activate();
            Mouse.setCursorPosition(i, j);
            this.saveModProfile(modProfile);
        }
    }

    public void createNewModProfile()
    {
        String s;

        for (int i = 1; this.modProfileNames.containsKey(s = "New Profile " + i); ++i)
        {
            ;
        }

        ModProfile modprofile = new ModProfile(s);
        this.saveModProfile(modprofile);
        this.modProfiles.add(modprofile);
        this.modProfileNames.put(modprofile.getProfileName(), modprofile);
        this.activateModProfile(modprofile);
    }

    public void cloneActiveModProfile()
    {
        String s;

        for (int i = 1; this.modProfileNames.containsKey(s = "Cloned Profile " + i); ++i)
        {
            ;
        }

        File file1 = new File(this.modProfilesFolder, this.activeModProfile.getProfileName() + ".json");

        try
        {
            FileReader filereader = new FileReader(file1);
            Throwable throwable = null;

            try
            {
                ModProfile modprofile = (ModProfile)Wrapper.getInstance().getGson().fromJson((Reader)filereader, ModProfile.class);

                if (modprofile != null)
                {
                    modprofile.setProfileName(s);
                    modprofile.setSortIndex(this.modProfiles.size());
                    this.saveModProfile(modprofile);
                    this.modProfiles.add(modprofile);
                    this.modProfileNames.put(modprofile.getProfileName(), modprofile);
                }

                this.activateModProfile(modprofile);
            }
            catch (Throwable throwable2)
            {
                throwable = throwable2;
                try {
					throw throwable2;
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            finally
            {
                if (filereader != null)
                {
                    if (throwable != null)
                    {
                        try
                        {
                            filereader.close();
                        }
                        catch (Throwable throwable1)
                        {
                            throwable.addSuppressed(throwable1);
                        }
                    }
                    else
                    {
                        filereader.close();
                    }
                }
            }
        }
        catch (Exception exception)
        {
            LogManager.getLogger().error("Failed to clone Badlion Client Mod Profile: " + this.activeModProfile.getProfileName());
            LogManager.getLogger().catching(exception);
        }
    }

    public void renameModProfile(ModProfile modProfile, String name)
    {
        if (!name.equals(modProfile.getProfileName()))
        {
            (new File(this.modProfilesFolder, modProfile.getProfileName() + ".json")).delete();
            modProfile.setProfileName(name);
            this.saveModProfile(modProfile);
        }
    }

    public void deleteActiveModProfile()
    {
        final ModProfile modprofile = this.activeModProfile;
        this.modProfiles.remove(modprofile);
        this.modProfileNames.remove(modprofile.getProfileName());

        if (this.modProfiles.isEmpty())
        {
            (new Timer()).schedule(new TimerTask()
            {
                public void run()
                {
                    if (!ModProfileManager.this.modProfileNames.containsKey(modprofile.getProfileName()))
                    {
                        (new File(ModProfileManager.this.modProfilesFolder, modprofile.getProfileName() + ".json")).delete();
                    }
                }
            }, 2000L);
            this.createDefaultModProfile();
        }
        else
        {
            for (int i = modprofile.getSortIndex(); i < this.modProfiles.size(); ++i)
            {
                ModProfile modprofile1 = (ModProfile)this.modProfiles.get(i);
                modprofile1.setSortIndex(modprofile1.getSortIndex() - 1);
                this.saveModProfile(modprofile1);
            }

            int j = modprofile.getSortIndex();

            if (j >= this.modProfiles.size())
            {
                --j;
            }

            this.activateModProfile((ModProfile)this.modProfiles.get(j));
            (new Timer()).schedule(new TimerTask()
            {
                public void run()
                {
                    if (!ModProfileManager.this.modProfileNames.containsKey(modprofile.getProfileName()))
                    {
                        (new File(ModProfileManager.this.modProfilesFolder, modprofile.getProfileName() + ".json")).delete();
                    }
                }
            }, 1000L);
        }
    }

    public void moveActiveModProfileUp()
    {
        ModProfile modprofile = Wrapper.getInstance().getActiveModProfile();

        if (modprofile.getSortIndex() != 0)
        {
            ModProfile modprofile1 = (ModProfile)Wrapper.getInstance().getModProfileManager().getModProfiles().get(modprofile.getSortIndex() - 1);
            modprofile.setSortIndex(modprofile.getSortIndex() - 1);
            modprofile1.setSortIndex(modprofile1.getSortIndex() + 1);
            this.saveActiveModProfile();
            this.saveModProfile(modprofile1);
            Collections.sort(this.modProfiles, new Comparator<ModProfile>()
            {
                public int compare(ModProfile o1, ModProfile o2)
                {
                    return Integer.valueOf(o1.getSortIndex()).compareTo(Integer.valueOf(o2.getSortIndex()));
                }
            });
        }
    }

    public void moveActiveModProfileDown()
    {
        ModProfile modprofile = Wrapper.getInstance().getActiveModProfile();

        if (modprofile.getSortIndex() != Wrapper.getInstance().getModProfileManager().getModProfiles().size() - 1)
        {
            ModProfile modprofile1 = (ModProfile)Wrapper.getInstance().getModProfileManager().getModProfiles().get(modprofile.getSortIndex() + 1);
            modprofile.setSortIndex(modprofile.getSortIndex() + 1);
            modprofile1.setSortIndex(modprofile1.getSortIndex() - 1);
            this.saveActiveModProfile();
            this.saveModProfile(modprofile1);
            Collections.sort(this.modProfiles, new Comparator<ModProfile>()
            {
                public int compare(ModProfile o1, ModProfile o2)
                {
                    return Integer.valueOf(o1.getSortIndex()).compareTo(Integer.valueOf(o2.getSortIndex()));
                }
            });
        }
    }

    public void openModProfilesSystemFolder()
    {
        String s = this.modProfilesFolder.getAbsolutePath();

        if (Util.getOSType() == Util.EnumOS.OSX)
        {
            try
            {
                Runtime.getRuntime().exec(new String[] {"/usr/bin/open", s});
                return;
            }
            catch (IOException ioexception1)
            {
                LogManager.getLogger().error((String)"Couldn\'t open file", (Throwable)ioexception1);
            }
        }
        else if (Util.getOSType() == Util.EnumOS.WINDOWS)
        {
            String s1 = String.format("cmd.exe /C start \"Open file\" \"%s\"", new Object[] {s});

            try
            {
                Runtime.getRuntime().exec(s1);
                return;
            }
            catch (IOException ioexception)
            {
                LogManager.getLogger().error((String)"Couldn\'t open file", (Throwable)ioexception);
            }
        }

        boolean flag = false;

        try
        {
            Class<?> oclass = Class.forName("java.awt.Desktop");
            Object object = oclass.getMethod("getDesktop", new Class[0]).invoke((Object)null, new Object[0]);
            oclass.getMethod("browse", new Class[] {URI.class}).invoke(object, new Object[] {this.modProfilesFolder.toURI()});
        }
        catch (Throwable throwable)
        {
            LogManager.getLogger().error("Couldn\'t open link", throwable);
            flag = true;
        }

        if (flag)
        {
            LogManager.getLogger().info("Opening via system class!");
            Sys.openURL("file://" + s);
        }
    }

    public ModProfile getActiveModProfile()
    {
        return this.activeModProfile;
    }

    public List<ModProfile> getModProfiles()
    {
        return this.modProfiles;
    }

    public AccountManager getAccountManager()
    {
        return this.accountManager;
    }

    public static void callRenderGame(GuiIngame guiIngame)
    {
        RenderGame rendergame = new RenderGame(guiIngame);
        Wrapper.getInstance().getActiveModProfile().passEvent(rendergame);
    }
}
