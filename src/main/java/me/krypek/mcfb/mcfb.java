package me.krypek.mcfb;

import java.util.Collection;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.CommandFunctionArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.FunctionCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.text.Text;

public class mcfb implements ModInitializer {

	public void onInitialize() { register(); }

	@SuppressWarnings({ "unchecked", "rawtypes" })
	void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			dispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) CommandManager.literal("mcfb")
					.requires((source) -> {
				return source.hasPermissionLevel(2);
			})
			// player selector
			.then(CommandManager.argument("targets", EntityArgumentType.players())
			// function
			.then(CommandManager.argument("name", CommandFunctionArgumentType.commandFunction())
			.suggests(FunctionCommand.SUGGESTION_PROVIDER).executes(mcfb::execute)))));
		});
	}

	private static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerCommandSource source = context.getSource();
		Collection<CommandFunction> functions = CommandFunctionArgumentType.getFunctions(context, "name");
		String functionName = context.getInput().split(" ")[2];

		int i = 0;
		long time1 = System.currentTimeMillis();
		for (CommandFunction commandFunction : functions)
			i += source.getMinecraftServer().getCommandFunctionManager().execute(commandFunction, source.withSilent().withMaxLevel(2));

		long time2 = System.currentTimeMillis();
		long totalTime = time2 - time1;

		String msg = "§2§lFinished function: §o§7§l" + functionName + "§2§l! \n§6§lCommand Amout: §o§7§l" + i + "  \n§6§lTotal time: §o§7§l"
				+ (time2 - time1) + " §6§lms";

		if(functionName.equals("core:start")) {
			ScoreboardObjective scoreboardMCM = source.getWorld().getServer().getScoreboard().getObjective("mcm");

			int instructionAmount = source.getWorld().getServer().getScoreboard().getPlayerScore("commandAmount", scoreboardMCM).getScore();
			msg += " §6§lms\n§6§lInstruction Amount: §o§7§l" + instructionAmount + "\n§6§lIPS §8= §o§7§l" + instructionAmount + " §8/ §o§7§l"
					+ totalTime / 1000d + " §8= §o§7§l" + (int) (instructionAmount / (totalTime / 1000d));
		}
		Text text = Text.of(msg);
		EntityArgumentType.getPlayers(context, "targets").forEach(player -> player.sendMessage(text, false));

		return i;

	}
}