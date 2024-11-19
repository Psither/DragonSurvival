package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.commands.*;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.neoforged.neoforgespi.ILaunchContext.LOGGER;

@EventBusSubscriber
public class DSCommands {
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, DragonSurvival.MODID);

    static {
        ARGUMENT_TYPES.register("dragon_body", () -> ArgumentTypeInfos.registerByClass(DragonBodyArgument.class, SingletonArgumentInfo.contextAware(DragonBodyArgument::new)));
    }

    @SubscribeEvent
    public static void serverRegisterCommandsEvent(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> commandDispatcher = event.getDispatcher();
        DragonCommand.register(event);
        DragonEditorCommand.register(commandDispatcher);
        DragonAltarCommand.register(commandDispatcher);
        DragonSizeCommand.register(commandDispatcher);
        LOGGER.info("Registered commands");
    }
}
