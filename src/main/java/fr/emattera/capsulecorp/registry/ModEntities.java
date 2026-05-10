package fr.emattera.capsulecorp.registry;

import fr.emattera.capsulecorp.CapsuleCorp;
import fr.emattera.capsulecorp.entity.CapsuleProjectileEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(
            Registries.ENTITY_TYPE,
            CapsuleCorp.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<CapsuleProjectileEntity>> CAPSULE_PROJECTILE =
            ENTITIES.register(
                    "capsule_projectile",
                    () -> EntityType.Builder.<CapsuleProjectileEntity>of(
                            CapsuleProjectileEntity::new,
                            MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build("capsule_projectile"));

    private ModEntities() {
    }
}