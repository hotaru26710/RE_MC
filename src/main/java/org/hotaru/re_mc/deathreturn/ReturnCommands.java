package org.hotaru.re_mc.deathreturn;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.hotaru.re_mc.deathreturn.memory.DeathRecord;
import org.hotaru.re_mc.deathreturn.memory.PlayerMemory;
import org.hotaru.re_mc.deathreturn.memory.ReturnMemoryService;

public final class ReturnCommands {
    private ReturnCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("remc")
                .then(Commands.literal("return")
                        .then(Commands.literal("status")
                                .executes(context -> status(context.getSource())))
                        .then(Commands.literal("log")
                                .executes(context -> log(context.getSource(), context.getSource().getPlayerOrException()))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .requires(source -> source.hasPermission(2))
                                        .executes(context -> log(context.getSource(), EntityArgument.getPlayer(context, "player")))))
                        .then(Commands.literal("checkpoint")
                                .then(Commands.literal("create")
                                        .requires(source -> source.hasPermission(2))
                                        .executes(context -> checkpointCreate(context.getSource())))
                                .then(Commands.literal("force")
                                        .requires(source -> source.hasPermission(4))
                                        .executes(context -> checkpointForce(context.getSource()))))
                        .then(Commands.literal("recover")
                                .requires(source -> source.hasPermission(4))
                                .executes(context -> recover(context.getSource()))));
        dispatcher.register(root);
    }

    private static int status(CommandSourceStack source) {
        ReturnManager manager = ReturnManager.getOrNull();
        if (manager == null) {
            source.sendFailure(Component.literal("Death Return is not initialized."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal(manager.status()), false);
        return 1;
    }

    private static int log(CommandSourceStack source, ServerPlayer player) {
        PlayerMemory memory = ReturnMemoryService.getOrCreate(source.getServer(), player.getUUID());
        source.sendSuccess(() -> Component.literal("Death Return log for " + player.getGameProfile().getName())
                .withStyle(ChatFormatting.GOLD), false);
        source.sendSuccess(() -> Component.literal("Returns: " + memory.getTotalReturns()
                + " | Last checkpoint: " + memory.getLastCheckpointDimension()
                + " @ " + memory.getLastCheckpointTime()), false);
        int shown = 0;
        for (DeathRecord record : memory.getDeaths()) {
            source.sendSuccess(() -> Component.literal("#" + record.getDeathCount()
                    + " " + record.getDimension()
                    + " (" + format(record.getX()) + ", " + format(record.getY()) + ", " + format(record.getZ()) + ") "
                    + record.getCause()).withStyle(ChatFormatting.GRAY), false);
            if (++shown >= 10) {
                break;
            }
        }
        if (shown == 0) {
            source.sendSuccess(() -> Component.literal("No death records.").withStyle(ChatFormatting.GRAY), false);
        }
        return 1;
    }

    private static int checkpointCreate(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ReturnManager manager = ReturnManager.getOrNull();
        if (manager == null) {
            source.sendFailure(Component.literal("Death Return is not initialized."));
            return 0;
        }
        String result = manager.manualCreate(source.getPlayerOrException());
        source.sendSuccess(() -> Component.literal(result), false);
        return 1;
    }

    private static int checkpointForce(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ReturnManager manager = ReturnManager.getOrNull();
        if (manager == null) {
            source.sendFailure(Component.literal("Death Return is not initialized."));
            return 0;
        }
        String result = manager.manualForce(source.getPlayerOrException());
        source.sendSuccess(() -> Component.literal(result), false);
        return 1;
    }

    private static int recover(CommandSourceStack source) {
        ReturnManager manager = ReturnManager.getOrNull();
        if (manager == null) {
            source.sendFailure(Component.literal("Death Return is not initialized."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal(manager.recover()), false);
        return 1;
    }

    private static String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }
}
