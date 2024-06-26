package io.github.benjamindavies.minecraftdbvolume.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameOptions.class)
public class GameOptionsMixin {
    private static final int DB_RANGE = 40;

    @Inject(at = @At("HEAD"), method = "createSoundVolumeOption", cancellable = true)
    private void createSoundVolumeOption(String key, SoundCategory category, CallbackInfoReturnable<SimpleOption<Double>> callbackInfo) {
        SimpleOption<Double> option = new SimpleOption<Double>(
                key,
                SimpleOption.emptyTooltip(),
                (prefix, value) -> {
                    if (value == 0.0) {
                        return GameOptions.getGenericValueText(prefix, ScreenTexts.OFF);
                    }
                    String dbString = String.format("%.1f", volumeToDb(value));
                    return Text.translatable("options.db_value", new Object[]{prefix, dbString});
                },
                SimpleOption.DoubleSliderCallbacks.INSTANCE
                        .withModifier(
                                sliderProgressValue -> {
                                    if (sliderProgressValue == 0.0) {
                                        return 0.0;
                                    }
                                    return dbToVolume(DB_RANGE * (sliderProgressValue - 1));
                                },
                                value -> {
                                    if (value == 0.0) {
                                        return 0.0;
                                    }
                                    return volumeToDb(value) / DB_RANGE + 1;
                                }),
                1.0,
                value -> MinecraftClient.getInstance().getSoundManager().updateSoundVolume(category, value.floatValue()));

        callbackInfo.setReturnValue(option);
    }

    private double dbToVolume(double db) {
        return Math.pow(10, db / 20);
    }

    private double volumeToDb(double volume) {
        return 20 * Math.log10(volume);
    }
}