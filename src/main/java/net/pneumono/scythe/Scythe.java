package net.pneumono.scythe;

import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;
import net.pneumono.scythe.content.ScytheRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Scythe implements ModInitializer {
	public static final String MOD_ID = "scythe";
    public static final Logger LOGGER = LoggerFactory.getLogger("Scythe");

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Scythe content!");

		ScytheRegistry.registerScytheContent();
	}

	public static Identifier identifier(String path) {
		return Identifier.of(MOD_ID, path);
	}
}