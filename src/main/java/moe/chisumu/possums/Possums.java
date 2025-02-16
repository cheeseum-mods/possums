package moe.chisumu.possums;

import moe.chisumu.possums.entity.PossumEntity;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Possums implements ModInitializer {
	public static final String MOD_ID = "possums";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final EntityType<PossumEntity> POSSUM = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			new ResourceLocation(MOD_ID, "possum"),
			EntityType.Builder.of(PossumEntity::new, MobCategory.CREATURE).sized(0.75f, 0.75f).build("possum")
	);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		LOGGER.info("Hello Fabric world!");

		FabricDefaultAttributeRegistry.register(POSSUM, PossumEntity.createMobAttributes());
	}
}