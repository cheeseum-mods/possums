package moe.chisumu.possums.renderer;

import moe.chisumu.possums.entity.PossumEntity;
import moe.chisumu.possums.model.PossumEntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PossumEntityRenderer extends GeoEntityRenderer<PossumEntity> {
    public PossumEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new PossumEntityModel());
    }
}
