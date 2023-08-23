package kombee.fmhelper;

import net.minecraft.world.World;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.SaveHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Mod(modid = FMHelper.MODID, name = FMHelper.NAME, version = FMHelper.VERSION)
public class FMHelper
{
    public static final String MODID = "fmhelper";
    public static final String NAME = "Fancy Menu Helper";
    public static final String VERSION = "1.0";
    private static File configDir;
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (event.getSide().isClient()) {
            configDir = new File(event.getModConfigurationDirectory(), "fmhelper");
        } else {
            configDir = new File(new File("."), "config/fmhelper");
        }

        if (!configDir.exists()) {
            if (!configDir.mkdir()) {
                FMLLog.severe("Failed to create the 'fmhelper' folder.");
            }
        }

        File configFile = new File(configDir, "statshelper.json");
        if (!configFile.exists()) {
            try {
                JsonObject defaultData = new JsonObject();
                defaultData.addProperty("path", "");
                FileWriter writer = new FileWriter(configFile);
                writer.write(defaultData.toString());
                writer.close();
            } catch (IOException e) {
                FMLLog.severe("Failed to create 'statshelper.json': %s", e.getMessage());
            }
        }
    }
    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    }
    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {

        if (event.player.world.isRemote) {
            return;
        }

        String worldName = getExactWorldFolderName(event.player.world);

        if (worldName != null) {
            String playerUUID = event.player.getUniqueID().toString();

            File configFile = new File(configDir, "statshelper.json");
            if (configFile.exists()) {
                try {
                    JsonObject configData = new JsonObject();
                    configData.addProperty("path", "saves/" + worldName + "/stats/" + playerUUID + ".json");
                    FileWriter writer = new FileWriter(configFile);
                    writer.write(configData.toString());
                    writer.close();
                } catch (IOException e) {
                    FMLLog.severe("Failed to update 'statshelper.json': %s", e.getMessage());
                }
            } else {
                FMLLog.severe("'statshelper.json' not found.");
            }
        }
    }
    private String getExactWorldFolderName(World world) {
        String worldFolderName = null;

        ISaveHandler saveHandler = world.getSaveHandler();
        if (saveHandler instanceof SaveHandler) {
            File worldDir = ((SaveHandler) saveHandler).getWorldDirectory();
            worldFolderName = worldDir.getName();
        }

        return worldFolderName;
    }
    private boolean isSinglePlayer() {
        return !FMLCommonHandler.instance().getMinecraftServerInstance().isDedicatedServer();
    }
}