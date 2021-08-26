package me.krypek.mcfb;

import java.util.Collection;
import java.util.Iterator;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.CommandFunctionArgumentType;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.FunctionCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.text.Text;

public class mcfb implements ModInitializer {

	public void onInitialize() {
		registerCommand();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	void registerCommand() {
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			dispatcher.register((LiteralArgumentBuilder) ((LiteralArgumentBuilder) CommandManager.literal("mcfb")
					.requires((source) -> {
						return source.hasPermissionLevel(2);
					})).then(CommandManager.argument("name", CommandFunctionArgumentType.commandFunction())
							.suggests(FunctionCommand.SUGGESTION_PROVIDER).executes((context) -> {
								return execute(context);
							})));
		});
	}

	@SuppressWarnings("rawtypes")
	private static int execute(CommandContext<ServerCommandSource> context) {

		ServerCommandSource source = (ServerCommandSource) context.getSource();
		Collection<CommandFunction> functions = null;
		try {
			functions = CommandFunctionArgumentType.getFunctions(context, "name");
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}

		long time1 = System.currentTimeMillis();

		int i = 0;

		CommandFunction commandFunction;
		for (Iterator var3 = functions.iterator(); var3.hasNext(); i += source.getServer().getCommandFunctionManager()
				.execute(commandFunction, source.withSilent().withMaxLevel(2))) {
			commandFunction = (CommandFunction) var3.next();
		}

		long time2 = System.currentTimeMillis();

		String functionName = context.getInput().split(" ")[1];

		// MCMPC stuff start
		boolean doMCMPC_core_start_ips = true;
		if (doMCMPC_core_start_ips) {

			Text text;
			if (functionName.equals("core:start") || functionName.equals("c:k")) {

				ScoreboardObjective scoreboardMCM = source.getWorld().getServer().getScoreboard().getObjective("mcm");

				int instructionAmount = source.getWorld().getServer().getScoreboard()
						.getPlayerScore("commandAmount", scoreboardMCM).getScore();

				long totalTime = (time2 - time1);

				text = Text.of("§2§lFinished function: §o§7§l" + functionName + "§2§l! \n§6§lCommand Amout: §o§7§l" + i
						+ "  \n§6§lTotal time: §o§7§l" + totalTime + " §6§lms\n§6§lInstruction Amount: §o§7§l"
						+ instructionAmount + "\n§6§lIPS §8= §o§7§l" + instructionAmount + " §8/ §o§7§l"
						+ (((double) totalTime) / 1000d) + " §8= §o§7§l"
						+ (int) (instructionAmount / (((double) totalTime) / 1000d)));

			} else {
				text = Text.of("§2§lFinished function: §l§o§7§l" + functionName + "§2§l! \n§6§lCommand Amout: §l§o§7§l"
						+ i + "  \n§6§lTotal time: §l§o§7§l" + (time2 - time1) + " §6§lms");
			}
			source.sendFeedback(text, false);

		} // MCMPC stuff end
		else {
			Text text = Text.of("§2§lFinished function: §o§7§l" + functionName + "§2§l! \n§6§lCommand Amout: §o§7§l" + i
					+ "  \n§6§lTotal time: §o§7§l" + (time2 - time1) + " §6§lms");
			source.sendFeedback(text, false);
		}

		return i;
	}

}
