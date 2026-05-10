package fr.emattera.capsulecorp.client;

import fr.emattera.capsulecorp.client.tooltip.CapsuleWalletTooltip;
import fr.emattera.capsulecorp.client.tooltip.ClientCapsuleWalletTooltip;
import fr.emattera.capsulecorp.registry.ModEntities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

public final class CapsuleCorpClient {
    private CapsuleCorpClient() {
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(CapsuleCorpClient::registerEntityRenderers);
        modEventBus.addListener(CapsuleCorpClient::registerTooltipComponents);
    }

    private static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                ModEntities.CAPSULE_PROJECTILE.get(),
                ThrownItemRenderer::new);
    }

    private static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(
                CapsuleWalletTooltip.class,
                ClientCapsuleWalletTooltip::new);
    }
}