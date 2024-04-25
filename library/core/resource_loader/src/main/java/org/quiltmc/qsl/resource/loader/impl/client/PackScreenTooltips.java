package org.quiltmc.qsl.resource.loader.impl.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.widget.list.pack.PackEntryListWidget;

import org.quiltmc.qsl.resource.loader.impl.BuiltinResourcePackSource;
import org.quiltmc.qsl.resource.loader.mixin.client.PackScreenAccessor;
import org.quiltmc.qsl.resource.loader.mixin.client.ResourcePackEntryAccessor;
import org.quiltmc.qsl.screen.api.client.QuiltScreen;
import org.quiltmc.qsl.screen.api.client.ScreenEvents;

public class PackScreenTooltips implements ScreenEvents.AfterRender {
	@Override
	public void afterRender(Screen screen, GuiGraphics graphics, int mouseX, int mouseY, float tickDelta) {
		if (screen instanceof PackScreen packScreen) {
			PackEntryListWidget.PackEntry availableEntry = ((PackScreenAccessor) packScreen).getAvailablePackList().getHoveredEntry();
			if (availableEntry != null) {
				if (((ResourcePackEntryAccessor) availableEntry).getPack().getSource() instanceof BuiltinResourcePackSource source) {
					graphics.drawTooltip(((QuiltScreen) packScreen).getTextRenderer(), source.getTooltip(), mouseX, mouseY);
				}
			}

			PackEntryListWidget.PackEntry selectedEntry = ((PackScreenAccessor) packScreen).getSelectedPackList().getHoveredEntry();
			if (selectedEntry != null) {
				if (((ResourcePackEntryAccessor) selectedEntry).getPack().getSource() instanceof BuiltinResourcePackSource source) {
					graphics.drawTooltip(((QuiltScreen) packScreen).getTextRenderer(), source.getTooltip(), mouseX, mouseY);
				}
			}
		}
	}
}
