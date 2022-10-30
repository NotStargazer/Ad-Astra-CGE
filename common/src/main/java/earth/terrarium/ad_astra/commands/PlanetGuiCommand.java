package earth.terrarium.ad_astra.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import earth.terrarium.ad_astra.screen.PlanetSelectionScreenHandlerFactory;
import earth.terrarium.botarium.api.menu.MenuHooks;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.List;

import static net.minecraft.server.command.CommandManager.argument;

public class PlanetGuiCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("planetgui")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(PlanetGuiCommand::executeSimple)
                .then(argument("target", EntityArgumentType.entities())
                        .executes(PlanetGuiCommand::executePartial)
                        .then(argument("tier", IntegerArgumentType.integer())
                                .executes(PlanetGuiCommand::executeFull))));
    }

    private static int executeSimple(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return execute(List.of(context.getSource().getPlayer()), 4);
    }

    private static int executePartial(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return execute(EntityArgumentType.getEntities(context, "target"), 4);
    }

    private static int executeFull(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        Collection<? extends Entity> entities = EntityArgumentType.getEntities(context, "target");
        int tier = IntegerArgumentType.getInteger(context, "tier");

        return execute(entities, tier);
    }

    private static int execute(Collection<? extends Entity> entities, int tier) {

        entities.forEach((entity) -> {
            if (entity instanceof PlayerEntity player) {
                player.playerScreenHandler.close(player);
                MenuHooks.openMenu((ServerPlayerEntity) player, new PlanetSelectionScreenHandlerFactory(tier));
            }
        });

        return 1;
    }
}