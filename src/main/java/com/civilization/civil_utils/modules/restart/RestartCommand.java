package com.civilization.civil_utils.modules.restart;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

public class RestartCommand {
    public static void register( CommandDispatcher<CommandSourceStack> dispatcher ) {
        dispatcher.register( Commands.literal( "restart" )
                .requires( source -> source.hasPermission( 4 ) )
                .executes( RestartCommand::restart ) );
				
		dispatcher.register( Commands.literal( "restart_now" )
                .requires( source -> source.hasPermission( 4 ) )
                .executes( RestartCommand::restart_now ) );
    }

    private static int restart( CommandContext<CommandSourceStack> context ) {

        CommandSourceStack source = context.getSource();
        source.sendSuccess( new TextComponent( "Restarting the server after a while" ), true );
        RestartModule.restart( source.getServer() );
        return Command.SINGLE_SUCCESS;
    }
	
	private static int restart_now( CommandContext<CommandSourceStack> context ) {

        CommandSourceStack source = context.getSource();
        source.sendSuccess( new TextComponent( "Restarting the server" ), true );
//        RestartModule.restart_now( source.getServer() );
        return Command.SINGLE_SUCCESS;
    }
}
