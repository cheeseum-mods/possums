package moe.chisumu.possums.entity;

import moe.chisumu.possums.PossumAnimations;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
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

import java.util.UUID;

public class PossumEntity extends TamableAnimal implements NeutralMob, GeoEntity {
    // Persistent Entity Data
    private static final EntityDataAccessor<Integer> DATA_COLLAR_COLOR = SynchedEntityData.defineId(PossumEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(PossumEntity.class, EntityDataSerializers.INT);

    // Behavioral Constants
    private static final Ingredient TAME_INGREDIENT = Ingredient.of(new ItemLike[]{Items.SWEET_BERRIES, Items.GLOW_BERRIES});
    private static final int TAME_DIFFICULTY = 5;
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(60, 120);

    // Animation State
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    // Behavioral State (Mutable)
    private boolean isScared;
    private UUID persistentAngerTarget;

    public PossumEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_COLLAR_COLOR, DyeColor.RED.getId());
        this.entityData.define(DATA_REMAINING_ANGER_TIME, 0);
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
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(3, new ResetUniversalAngerTargetGoal<>(this, true));
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
    public boolean hurt(DamageSource damageSource, float f) {
        if (this.isInvulnerableTo(damageSource)) {
            return false;
        } else {
            if (!this.level().isClientSide) {
                this.setOrderedToSit(false);
            }

            return super.hurt(damageSource, f);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericWalkIdleController(this) );
        controllers.add( new AnimationController<>(this, "Sitting", 0, this::poseBody) );
    }

    private PlayState poseBody(AnimationState<PossumEntity> state) {
        if (this.isInSittingPose()) {
            state.setAndContinue(DefaultAnimations.SIT);
        } else if (this.isAngry()) {
            state.setAndContinue(PossumAnimations.HISS);
        } else {
            state.setAndContinue(DefaultAnimations.IDLE);
        }

        return PlayState.CONTINUE;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide) {
            this.updatePersistentAnger((ServerLevel)this.level(), true);
        }
    }

    // Persistent Anger Behavior
    @Override
    public int getRemainingPersistentAngerTime() {
        return this.entityData.get(DATA_REMAINING_ANGER_TIME);
    }

    @Override
    public void setRemainingPersistentAngerTime(int i) { this.entityData.set(DATA_REMAINING_ANGER_TIME, i); }

    @Override
    public @Nullable UUID getPersistentAngerTarget() { return this.persistentAngerTarget; }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID uUID) { this.persistentAngerTarget = uUID; }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }
}
