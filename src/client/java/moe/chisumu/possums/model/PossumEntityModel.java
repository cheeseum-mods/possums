package moe.chisumu.possums.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import moe.chisumu.possums.Possums;
import moe.chisumu.possums.entity.PossumEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PossumEntityModel extends GeoModel<PossumEntity> {

    @Override
    public ResourceLocation getModelResource(PossumEntity possumEntity) {
        return new ResourceLocation(Possums.MOD_ID, "geo/possum.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PossumEntity possumEntity) {
        return new ResourceLocation(Possums.MOD_ID, "textures/entity/possum.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PossumEntity possumEntity) {
        return null;
    }
}
