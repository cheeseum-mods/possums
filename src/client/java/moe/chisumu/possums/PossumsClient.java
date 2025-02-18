package moe.chisumu.possums;

import moe.chisumu.possums.renderer.PossumEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class PossumsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		EntityRendererRegistry.register(Possums.POSSUM, PossumEntityRenderer::new);
	}
}