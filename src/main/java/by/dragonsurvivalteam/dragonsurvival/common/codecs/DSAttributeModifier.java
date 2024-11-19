package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record DSAttributeModifier(Holder<Attribute> attribute, AttributeModifier modifier) {
    public static final Codec<DSAttributeModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Attribute.CODEC.fieldOf("attribute").forGetter(DSAttributeModifier::attribute),
            AttributeModifier.CODEC.fieldOf("modifier").forGetter(DSAttributeModifier::modifier)
    ).apply(instance, DSAttributeModifier::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DSAttributeModifier> STREAM_CODEC = StreamCodec.composite(
            Attribute.STREAM_CODEC, DSAttributeModifier::attribute,
            AttributeModifier.STREAM_CODEC, DSAttributeModifier::modifier,
            DSAttributeModifier::new
    );
}
