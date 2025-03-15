package moe.chisumu.possums.model;

import moe.chisumu.possums.Possums;
import moe.chisumu.possums.entity.PossumEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class PossumEntityModel extends DefaultedEntityGeoModel<PossumEntity> {
    public PossumEntityModel() {
        super(new ResourceLocation(Possums.MOD_ID, "possum"));
    }

    @Override
    public ResourceLocation getTextureResource(PossumEntity possumEntity) {
        // if (possumEntity.isTame()) {
        //     return new ResourceLocation(Possums.MOD_ID, "textures/entity/possum_tamed.png");
        // }

        return new ResourceLocation(Possums.MOD_ID, "textures/entity/possum.png");
    }

}
