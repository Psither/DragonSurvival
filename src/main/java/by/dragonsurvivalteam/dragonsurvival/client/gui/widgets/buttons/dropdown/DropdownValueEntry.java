package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.dropdown;

import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.DropDownButton;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class DropdownValueEntry extends DropdownEntry {
    private final String value;
    private final Consumer<String> setter;
    private final DropDownButton source;
    private final Component message;
    private ExtendedButton button;

    public DropdownValueEntry(DropDownButton source, String translationKey, Consumer<String> setter) {
        this.value = translationKey;
        this.setter = setter;
        this.source = source;
        message = Component.translatable(translationKey);
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        return ImmutableList.of(button);
    }

    @Override
    public @NotNull List<? extends NarratableEntry> narratables() {
        return Collections.emptyList();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks) {
        if (button == null) {
            button = new ExtendedButton(pLeft + 3, 0, pWidth - 12, pHeight + 1, Component.empty(), action -> { /* Nothing to do */ }) {
                @Override
                public @NotNull Component getMessage() {
                    return message;
                }

                @Override
                public void onPress() {
                    source.current = value;
                    source.onPress();
                    setter.accept(value);
                }
            };
        } else {
            button.setY(pTop);
            button.visible = source.visible;
            button.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
        }
    }
}