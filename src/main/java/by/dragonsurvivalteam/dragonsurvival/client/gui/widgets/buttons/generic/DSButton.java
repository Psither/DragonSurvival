package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic;

import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.MouseTooltipPositioner;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

public class DSButton extends ExtendedButton {
	public Component[] tooltips;

	public DSButton(int x, int y, int width, int height, final OnPress onPress, final Component... tooltip) {
		this(x, y, width, height, Component.empty(), onPress, tooltip);
	}

	public DSButton(int x, int y, int width, int height, final Component message, final OnPress onPress, final Component... tooltip) {
		super(x, y, width, height, message, onPress);
		setCustomTooltip(tooltip);
	}

	public void setCustomTooltip(final Component... tooltip) {
		this.tooltips = tooltip;
		MutableComponent base = Component.empty();

		for (Component element : tooltip) {
			base.append(element);
		}

		setTooltip(Tooltip.create(base));
	}

	@Override
	protected @NotNull ClientTooltipPositioner createTooltipPositioner() {
		return new MouseTooltipPositioner(this);
	}
}