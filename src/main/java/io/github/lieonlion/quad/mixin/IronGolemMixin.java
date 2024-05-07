package io.github.lieonlion.quad.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.lieonlion.quad.tags.QuadItemTags;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(IronGolem.class)
public abstract class IronGolemMixin {
    @WrapOperation(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private boolean applyTagIronGolemHealer(ItemStack instance, Item item, Operation<Boolean> original) {
        return (item.builtInRegistryHolder().is(QuadItemTags.IRON_GOLEM_HEALER) && original.call(instance, item))
                || instance.is(QuadItemTags.IRON_GOLEM_HEALER);
    }
}