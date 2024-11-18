package by.dragonsurvivalteam.dragonsurvival.network;

import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncBrokenTool;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawRender;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawsMenu;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawsMenuToggle;
import by.dragonsurvivalteam.dragonsurvival.network.container.AllowOpenDragonAltar;
import by.dragonsurvivalteam.dragonsurvival.network.container.RequestOpenDragonEditor;
import by.dragonsurvivalteam.dragonsurvival.network.container.RequestOpenDragonInventory;
import by.dragonsurvivalteam.dragonsurvival.network.container.RequestOpenInventory;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncDragonSkinSettings;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncPlayerSkinPreset;
import by.dragonsurvivalteam.dragonsurvival.network.emotes.SyncEmote;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncDeltaMovement;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncFlyingStatus;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncSpinStatus;
import by.dragonsurvivalteam.dragonsurvival.network.magic.*;
import by.dragonsurvivalteam.dragonsurvival.network.player.*;
import by.dragonsurvivalteam.dragonsurvival.network.status.*;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.SyncComplete;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "3";

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        // Sets the current network version
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playBidirectional(SyncTreasureRestStatus.Data.TYPE, SyncTreasureRestStatus.Data.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncTreasureRestStatus::handleClient, SyncTreasureRestStatus::handleServer));
        registrar.playBidirectional(SyncMagicSourceStatus.Data.TYPE, SyncMagicSourceStatus.Data.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncMagicSourceStatus::handleClient, SyncMagicSourceStatus::handleServer));

        // Generic packets
        registrar.playBidirectional(SyncDragonHandler.Data.TYPE, SyncDragonHandler.Data.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncDragonHandler::handleClient, SyncDragonHandler::handleServer));
        registrar.playBidirectional(SyncDragonMovement.Data.TYPE, SyncDragonMovement.Data.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncDragonMovement::handleClient, SyncDragonMovement::handleServer));
        registrar.playToClient(SyncDragonType.Data.TYPE, SyncDragonType.Data.STREAM_CODEC, SyncDragonType::handleClient);
        registrar.playToClient(SyncPlayerJump.Data.TYPE, SyncPlayerJump.Data.STREAM_CODEC, SyncPlayerJump::handleClient);
        registrar.playToClient(RefreshDragon.Data.TYPE, RefreshDragon.Data.STREAM_CODEC, RefreshDragon::handleClient);
        registrar.playBidirectional(SyncAltarCooldown.Data.TYPE, SyncAltarCooldown.Data.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncAltarCooldown::handleClient, SyncAltarCooldown::handleServer));
        registrar.playToClient(AllowOpenDragonAltar.TYPE, AllowOpenDragonAltar.STREAM_CODEC, AllowOpenDragonAltar::handleClient);
        registrar.playBidirectional(SyncComplete.Data.TYPE, SyncComplete.Data.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncComplete::handleClient, SyncComplete::handleServer));
        registrar.playToClient(SyncBrokenTool.Data.TYPE, SyncBrokenTool.Data.STREAM_CODEC, SyncBrokenTool::handleClient);
        registrar.playToServer(RequestOpenDragonInventory.Data.TYPE, RequestOpenDragonInventory.Data.STREAM_CODEC, RequestOpenDragonInventory::handleServer);
        registrar.playToServer(RequestOpenInventory.Data.TYPE, RequestOpenInventory.Data.STREAM_CODEC, RequestOpenInventory::handleServer);
        registrar.playBidirectional(SyncDestructionEnabled.Data.TYPE, SyncDestructionEnabled.Data.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncDestructionEnabled::handleClient, SyncDestructionEnabled::handleServer));
        registrar.playBidirectional(SyncDragonPassengerID.Data.TYPE, SyncDragonPassengerID.Data.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncDragonPassengerID::handleClient, SyncDragonPassengerID::handleServer));

        // Status
        registrar.playToClient(SyncGrowthState.Data.TYPE, SyncGrowthState.Data.STREAM_CODEC, SyncGrowthState::handleClient);
        registrar.playToClient(SyncSize.Data.TYPE, SyncSize.Data.STREAM_CODEC, SyncSize::handleClient);
        registrar.playToClient(SyncDiggingStatus.Data.TYPE, SyncDiggingStatus.Data.STREAM_CODEC, SyncDiggingStatus::handleClient);
        registrar.playToClient(RequestOpenDragonEditor.Data.TYPE, RequestOpenDragonEditor.Data.STREAM_CODEC, RequestOpenDragonEditor::handleClient);

        // Flight
        registrar.playBidirectional(SyncFlyingStatus.Data.TYPE, SyncFlyingStatus.Data.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncFlyingStatus::handleClient, SyncFlyingStatus::handleServer));
        registrar.playBidirectional(SyncDeltaMovement.Data.TYPE, SyncDeltaMovement.Data.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncDeltaMovement::handleClient, SyncDeltaMovement::handleServer));
        registrar.playBidirectional(SyncSpinStatus.Data.TYPE, SyncSpinStatus.Data.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncSpinStatus::handleClient, SyncSpinStatus::handleServer));

        // Render settings
        registrar.playBidirectional(SyncDragonClawsMenuToggle.Data.TYPE, SyncDragonClawsMenuToggle.Data.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncDragonClawsMenuToggle::handleClient, SyncDragonClawsMenuToggle::handleServer));
        registrar.playToClient(SyncDragonClawsMenu.Data.TYPE, SyncDragonClawsMenu.Data.STREAM_CODEC, SyncDragonClawsMenu::handleClient);

        // Ability packets
        registrar.playToClient(SyncMagicStats.Data.TYPE, SyncMagicStats.Data.STREAM_CODEC, SyncMagicStats::handleClient);
        registrar.playToClient(SyncHunterStacksRemoval.TYPE, SyncHunterStacksRemoval.STREAM_CODEC, SyncHunterStacksRemoval::handleClient);
        registrar.playBidirectional(SyncMagicCap.Data.TYPE, SyncMagicCap.Data.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncMagicCap::handleClient, SyncMagicCap::handleServer));
        registrar.playToServer(SyncDragonAbilitySlot.Data.TYPE, SyncDragonAbilitySlot.Data.STREAM_CODEC, SyncDragonAbilitySlot::handleServer);
        registrar.playBidirectional(SyncAbilityCasting.Data.TYPE, SyncAbilityCasting.Data.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncAbilityCasting::handleClient, SyncAbilityCasting::handleServer));
        registrar.playToServer(SyncSkillLevelChangeCost.Data.TYPE, SyncSkillLevelChangeCost.Data.STREAM_CODEC, SyncSkillLevelChangeCost::handleServer);

        // Potion sync
        registrar.playToClient(SyncVisualEffectRemoved.Data.TYPE, SyncVisualEffectRemoved.Data.STREAM_CODEC, SyncVisualEffectRemoved::handleClient);
        registrar.playToClient(SyncVisualEffectAdded.Data.TYPE, SyncVisualEffectAdded.Data.STREAM_CODEC, SyncVisualEffectAdded::handleClient);

        // Emote packets
        registrar.playBidirectional(SyncEmote.Data.TYPE, SyncEmote.Data.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncEmote::handleClient, SyncEmote::handleServer));

        // Client data
        registrar.playToClient(RequestClientData.Data.TYPE, RequestClientData.Data.STREAM_CODEC, RequestClientData::handleClient);
        registrar.playBidirectional(SyncPlayerSkinPreset.Data.TYPE, SyncPlayerSkinPreset.Data.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncPlayerSkinPreset::handleClient, SyncPlayerSkinPreset::handleServer));
        registrar.playBidirectional(SyncDragonClawRender.Data.TYPE, SyncDragonClawRender.Data.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncDragonClawRender::handleClient, SyncDragonClawRender::handleServer));
        registrar.playBidirectional(SyncDragonSkinSettings.Data.TYPE, SyncDragonSkinSettings.Data.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncDragonSkinSettings::handleClient, SyncDragonSkinSettings::handleServer));
    }
}