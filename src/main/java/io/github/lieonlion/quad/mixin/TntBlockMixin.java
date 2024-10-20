package io.github.lieonlion.quad.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.lieonlion.quad.util.QuadUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import nonamecrackers2.witherstormmod.common.init.WitherStormModEntityTypes;
import nonamecrackers2.witherstormmod.common.init.WitherStormModSoundEvents;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Random;

@Mixin(value = TntBlock.class, priority = 1004)
public abstract class TntBlockMixin {
    @Shadow
    private static void explode(Level level, BlockPos pos, @Nullable LivingEntity living) {}

    @ModifyReturnValue(method = "use", at = @At(value = "RETURN"))
    private InteractionResult applyTagFireLighters(InteractionResult original, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        Random random = new Random();
        if (QuadUtil.isFireLighter(stack)) {
            ResourceLocation targetLoc = ForgeRegistries.BLOCKS.getKey(state.getBlock());
            //Absolutely disgusting fix where i just remake the cwsm bombs if the lit block is them.
            //This should be deleted once the next cwsm version comes out and makes it obsolete
            if(targetLoc!=null && targetLoc.getNamespace().equals("witherstormmod")){
                if(targetLoc.getPath().equals("formidibomb")) {
                    if(!level.isClientSide){
                        Entity newBomb = WitherStormModEntityTypes.FORMIDIBOMB.get().create(level);
                        if(newBomb!=null){CompoundTag nbt = new CompoundTag();
                            nbt.putInt("StartFuse", 1200);
                            nbt.putInt("Fuse", 1200);
                            nbt.putString("direction", state.getValue(BlockStateProperties.HORIZONTAL_FACING).getSerializedName());
                            newBomb.load(nbt);

                            newBomb.setPos(pos.getX()+0.5,pos.getY(),pos.getZ()+0.5);
                            newBomb.setDeltaMovement(new Vec3((random.nextDouble() - 0.5) * 0.1, 0.2, (random.nextDouble() - 0.5) * 0.1));
                            level.playSound(null, pos, SoundEvents.TNT_PRIMED, SoundSource.PLAYERS, 1.0F, 1.0F);

                            level.addFreshEntity(newBomb);
                        }
                    }
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
                } else if(targetLoc.getPath().equals("super_tnt")) {
                    if(!level.isClientSide){
                        Entity newBomb = WitherStormModEntityTypes.SUPER_TNT.get().create(level);
                        if(newBomb!=null){
                            CompoundTag nbt = new CompoundTag();
                            nbt.putInt("Fuse", 300);
                            newBomb.load(nbt);

                            newBomb.setPos(pos.getX()+0.5,pos.getY(),pos.getZ()+0.5);
                            newBomb.setDeltaMovement(new Vec3((random.nextDouble() - 0.5) * 0.1, 0.2, (random.nextDouble() - 0.5) * 0.1));
                            level.playSound(null, pos, WitherStormModSoundEvents.SUPER_TNT_PRIMED.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

                            level.addFreshEntity(newBomb);
                        }
                    }
                }
            }
            else {
                explode(level, pos, player);
            }
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
            QuadUtil.usedFireLighter(level, state, pos, player, hand, stack);
            return InteractionResult.sidedSuccess(level.isClientSide);
        } return original;
    }

    @ModifyExpressionValue(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private boolean ignoreStack(boolean original) {
        return false;
    }
}