/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.mixin.renderer.client;

import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.WeightedBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DataPool;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;

@Mixin(WeightedBakedModel.class)
public class WeightedBakedModelMixin implements FabricBakedModel {
	@Shadow
	@Final
	private DataPool<BakedModel> models;
	@Unique
	boolean isVanilla = true;

	@Inject(at = @At("RETURN"), method = "<init>")
	private void onInit(DataPool<BakedModel> dataPool, CallbackInfo cb) {
		for (Weighted.Present<BakedModel> model : models.getEntries()) {
			if (!model.data().isVanillaAdapter()) {
				isVanilla = false;
				break;
			}
		}
	}

	@Override
	public boolean isVanillaAdapter() {
		return isVanilla;
	}

	@Override
	public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
		BakedModel selected = this.models.getDataOrEmpty(randomSupplier.get()).orElse(null);

		if (selected != null) {
			selected.emitBlockQuads(blockView, state, pos, () -> {
				Random random = randomSupplier.get();
				random.nextInt(); // Imitate vanilla modifying the random before passing it to the submodel
				return random;
			}, context);
		}
	}

	@Override
	public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
		BakedModel selected = this.models.getDataOrEmpty(randomSupplier.get()).orElse(null);

		if (selected != null) {
			selected.emitItemQuads(stack, () -> {
				Random random = randomSupplier.get();
				random.nextInt(); // Imitate vanilla modifying the random before passing it to the submodel
				return random;
			}, context);
		}
	}
}
