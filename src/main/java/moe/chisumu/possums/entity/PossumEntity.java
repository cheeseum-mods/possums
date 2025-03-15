package moe.chisumu.possums.entity;

import moe.chisumu.possums.Possums;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PossumEntity extends TamableAnimal implements GeoEntity {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Integer> DATA_COLLAR_COLOR = SynchedEntityData.defineId(PossumEntity.class, EntityDataSerializers.INT);
    private static final Ingredient TAME_INGREDIENT = Ingredient.of(new ItemLike[]{Items.SWEET_BERRIES, Items.GLOW_BERRIES});
    private static final int TAME_DIFFICULTY = 5;

    public PossumEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_COLLAR_COLOR, DyeColor.RED.getId());
    }

    public void setCollarColor(DyeColor dyeColor) {
        this.entityData.set(DATA_COLLAR_COLOR, dyeColor.getId());
    }

    public DyeColor getCollarColor() {
        return DyeColor.byId((Integer)this.entityData.get(DATA_COLLAR_COLOR));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, (double)0.5F, 7.0F, 3.0F, false));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.4f));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 10.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        Item item = itemStack.getItem();
        if (this.level().isClientSide) {
            if (this.isTame() && this.isOwnedBy(player)) {
                return InteractionResult.SUCCESS;
            } else {
                return !this.isFood(itemStack) || !(this.getHealth() < this.getMaxHealth()) && this.isTame() ? InteractionResult.PASS : InteractionResult.SUCCESS;
            }
        } else {
            if (this.isTame()) {
                if (this.isOwnedBy(player)) {
                    if (!(item instanceof DyeItem)) {
                        if (item.isEdible() && this.isFood(itemStack) && this.getHealth() < this.getMaxHealth()) {
                            this.usePlayerItem(player, interactionHand, itemStack);
                            this.heal((float)item.getFoodProperties().getNutrition());
                            return InteractionResult.CONSUME;
                        }

                        InteractionResult interactionResult = super.mobInteract(player, interactionHand);
                        if (!interactionResult.consumesAction() || this.isBaby()) {
                            this.setOrderedToSit(!this.isOrderedToSit());
                        }

                        return interactionResult;
                    }

                    DyeColor dyeColor = ((DyeItem)item).getDyeColor();
                    if (dyeColor != this.getCollarColor()) {
                        this.setCollarColor(dyeColor);
                        if (!player.getAbilities().instabuild) {
                            itemStack.shrink(1);
                        }

                        this.setPersistenceRequired();
                        return InteractionResult.CONSUME;
                    }
                }
            } else if (this.isFood(itemStack)) {
                this.usePlayerItem(player, interactionHand, itemStack);
                if (this.random.nextInt(TAME_DIFFICULTY) == 0) {
                    this.tame(player);
                    this.setOrderedToSit(true);
                    this.level().broadcastEntityEvent(this, (byte)7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte)6);
                }

                this.setPersistenceRequired();
                return InteractionResult.CONSUME;
            }

            InteractionResult interactionResult = super.mobInteract(player, interactionHand);
            if (interactionResult.consumesAction()) {
                this.setPersistenceRequired();
            }

            return interactionResult;
        }
    }

    public boolean isFood(ItemStack itemStack) { return TAME_INGREDIENT.test(itemStack); }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericWalkIdleController(this) );
        controllers.add( new AnimationController<>(this, "Sitting", 0, this::poseBody) );
    }

    private PlayState poseBody(AnimationState<PossumEntity> state) {
        if (this.isInSittingPose()) {
            state.setAndContinue(DefaultAnimations.SIT);
        } else {
            state.setAndContinue(DefaultAnimations.IDLE);
        }

        return PlayState.CONTINUE;
    }
}
