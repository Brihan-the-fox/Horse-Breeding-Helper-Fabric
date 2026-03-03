package net.thecubecollective.horsebreedinghelper;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.thecubecollective.horsebreedinghelper.event.HorseMountHandler;
import net.thecubecollective.horsebreedinghelper.render.HorseHighlightRenderer;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HorseBreedingHelperClient implements ClientModInitializer {
    public static final String MOD_ID = "horsebreedinghelper";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static boolean isEnabled = false;

    private static KeyBinding toggleKeyBinding;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Horse Breeding Helper Client");

        // Initialize renderer
        HorseHighlightRenderer.initialize();

        // Register horse mount handler for score display
        HorseMountHandler.register();

        // Create custom keybinding category
        KeyMapping.Category CATEGORY = new KeyMapping.Category(
            Identifier.fromNamespaceAndPath(MOD_ID, "category.horsebreedinghelper")
        );

        // Register keybinding for F6
        toggleKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.horsebreedinghelper.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F6,
            CATEGORY
        ));

        // Register client tick event to handle key presses and horse highlighting
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Handle key press
            while (toggleKeyBinding.wasPressed()) {
                isEnabled = !isEnabled;

                // Enable/disable the new highlighting system
                HorseHighlightRenderer.setEnabled(isEnabled);

                if (!isEnabled) {
                    // Clear all highlights when disabled
                    HorseHighlightRenderer.clearAllHighlights();
                }

                if (client.player != null) {
                    client.player.sendMessage(
                        net.minecraft.text.Text.literal("Horse Breeding Helper: " + (isEnabled ? "Enabled" : "Disabled")),
                        false
                    );
                }
            }
        });
    }
}
