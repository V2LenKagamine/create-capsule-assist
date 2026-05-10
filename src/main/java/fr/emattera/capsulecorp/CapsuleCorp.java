package fr.emattera.capsulecorp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.emattera.capsulecorp.client.CapsuleCorpClient;
import fr.emattera.capsulecorp.config.CapsuleCorpConfig;
import fr.emattera.capsulecorp.registry.ModBlocks;
import fr.emattera.capsulecorp.registry.ModCreativeTabs;
import fr.emattera.capsulecorp.registry.ModEntities;
import fr.emattera.capsulecorp.registry.ModItems;
import fr.emattera.capsulecorp.registry.ModSounds;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(CapsuleCorp.MOD_ID)
public final class CapsuleCorp {
    public static final String MOD_ID = "capsulecorp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public CapsuleCorp(IEventBus modEventBus, ModContainer modContainer) {
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.BLOCK_ITEMS.register(modEventBus);
        ModEntities.ENTITIES.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);

        modContainer.registerConfig(
                ModConfig.Type.COMMON,
                CapsuleCorpConfig.SPEC,
                "capsulecorp-common.toml");

        if (FMLEnvironment.dist == Dist.CLIENT) {
            CapsuleCorpClient.register(modEventBus);
        }

        LOGGER.info("Loaded Create: Capsule Corp.");
    }
}