package moe.chisumu.possums.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import moe.chisumu.possums.Possums;
import moe.chisumu.possums.entity.PossumEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class PossumAngryLayer<T extends PossumEntity> extends GeoRenderLayer<T> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Possums.MOD_ID, "textures/entity/possum_angry.png");


    public PossumAngryLayer(GeoRenderer<T> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, T possum, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        // No render if the possum isn't tamed
        if (!possum.isAngry())
            return;

        RenderType overlayRenderType = RenderType.entityCutoutNoCull(TEXTURE);
        this.renderer.reRender(
                bakedModel, poseStack, bufferSource,
                possum, overlayRenderType, bufferSource.getBuffer(overlayRenderType),
                partialTick, packedLight, packedOverlay,
                1.0f, 1.0f, 1.0f, 1.0f
        );
    }
}
