package com.sci.torcherino;

import com.sci.torcherino.init.ModBlocks;
import com.sci.torcherino.init.Recipes;
import com.sci.torcherino.tile.TileTorcherino;
import com.sci.torcherino.update.IUpdatableMod;
import com.sci.torcherino.update.ModVersion;
import com.sci.torcherino.update.UpdateChecker;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

/**
 * @author sci4me
 * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
 */
@Mod(modid = Props.ID, name = Props.NAME, version = Props.VERSION)
public class Torcherino implements IUpdatableMod {
    private static Torcherino instance;

    public static boolean animatedTextures;

    @Mod.InstanceFactory
    public static Torcherino instance() {
        if (Torcherino.instance == null)
            Torcherino.instance = new Torcherino();
        return Torcherino.instance;
    }

    private Torcherino() {
    }

    @Mod.EventHandler
    public void preInit(final FMLPreInitializationEvent evt) {
        final File folder = new File(evt.getModConfigurationDirectory(), "sci4me");

        if (!folder.exists())
            folder.mkdir();

        UpdateChecker.register(this);

        final Configuration cfg = new Configuration(new File(folder, "Torcherino.cfg"));
        try {
            cfg.load();

            animatedTextures = cfg.getBoolean("animatedTextures", "visual", true, "Should Torcherino use animated textures?");

            ModBlocks.init();
            Recipes.init(cfg);
        } finally {
            if (cfg.hasChanged())
                cfg.save();
        }
    }

    @Mod.EventHandler
    public void init(final FMLInitializationEvent evt) {
        TileTorcherino.blacklistBlock(Blocks.air);
        TileTorcherino.blacklistBlock(ModBlocks.torcherino);
        TileTorcherino.blacklistTile(TileTorcherino.class);

        TileTorcherino.blacklistBlock(Blocks.water);
        TileTorcherino.blacklistBlock(Blocks.flowing_water);

        TileTorcherino.blacklistBlock(Blocks.lava);
        TileTorcherino.blacklistBlock(Blocks.flowing_lava);
    }

    @Mod.EventHandler
    public void postInit(final FMLPostInitializationEvent evt) {
    }

    @Mod.EventHandler
    public void imcMessage(final FMLInterModComms.IMCEvent evt) {
        for (final FMLInterModComms.IMCMessage message : evt.getMessages()) {
            if (!message.isStringMessage()) {
                System.out.println("Received non-string message! Ignoring");
                continue;
            }

            final String s = message.getStringValue();

            if (message.key.equals("blacklist-block")) {
                final String[] parts = s.split(":");

                if (parts.length != 2) {
                    System.out.println("Received malformed message: " + s);
                    continue;
                }

                final Block block = GameRegistry.findBlock(parts[0], parts[1]);

                if (block == null) {
                    System.out.println("Could not find block: " + s + ", ignoring");
                    continue;
                }

                System.out.println("Blacklisting block: " + block.getUnlocalizedName());

                TileTorcherino.blacklistBlock(block);
            } else if (message.key.equals("blacklist-tile")) {
                try {
                    final Class<?> clazz = Class.forName(s);

                    if (clazz == null) {
                        System.out.println("Class null: " + s);
                        continue;
                    }

                    if (!clazz.isAssignableFrom(TileEntity.class)) {
                        System.out.println("Class not a TileEntity: " + s);
                        continue;
                    }

                    TileTorcherino.blacklistTile((Class<? extends TileEntity>) clazz);
                } catch (ClassNotFoundException e) {
                    System.out.println("Class not found: " + s + ", ignoring");
                }
            }
        }
    }

    @Override
    public String name() {
        return Props.NAME;
    }

    @Override
    public String updateURL() {
        return Props.UPDATE_URL;
    }

    @Override
    public ModVersion version() {
        return ModVersion.parse(Props.VERSION);
    }
}