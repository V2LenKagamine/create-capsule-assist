package fr.emattera.capsulecorp.registry;

import java.util.function.Supplier;

import fr.emattera.capsulecorp.CapsuleCorp;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(
            Registries.SOUND_EVENT,
            CapsuleCorp.MOD_ID);

    public static final Supplier<SoundEvent> CAPSULE_CAPTURE = registerSound("capsule.capture");
    public static final Supplier<SoundEvent> CAPSULE_DEPLOY = registerSound("capsule.deploy");

    private ModSounds() {
    }

    private static Supplier<SoundEvent> registerSound(String name) {
        return SOUND_EVENTS.register(
                name,
                () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(
                        CapsuleCorp.MOD_ID,
                        name)));
    }
}