package com.civilization.civil_utils;

import com.civilization.civil_utils.modules.discord.DiscordModule;
import com.civilization.civil_utils.modules.restart.RestartModule;
import com.civilization.civil_utils.utils.Variables;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Variables.MOD_ID)
public class CivilizationUtils {
	private static final Logger LOGGER = LogManager.getLogger(CivilizationUtils.class);

	private DiscordModule discordModule;
	private RestartModule restartModule;

	public CivilizationUtils() {
		System.setProperty("log4j2.configurationFile","./log4j2.xml");

		var eventBus = FMLJavaModLoadingContext.get().getModEventBus();

		// Register the setup method for modloading
		eventBus.addListener(this::setup);

		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC, Variables.CONFIG_NAME);
		initModules(eventBus);

		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);
	}

	private void initModules(IEventBus eventBus) {
		LOGGER.info("Initialize Modules");

		discordModule = new DiscordModule();
		restartModule = new RestartModule();
	}

	private void setup(final FMLCommonSetupEvent event) {
		LOGGER.info("Setup Event");

		discordModule.setup(event);
		restartModule.setup(event);
	}

	// You can use SubscribeEvent and let the Event Bus discover methods to call
	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event) {
		LOGGER.info("Server Starting");
		discordModule.serverStarting(event);
		restartModule.serverStarting(event);
	}

	@SubscribeEvent
	public void onServerStopping(ServerStoppingEvent event) {
		LOGGER.info("Server Stopping");
		discordModule.serverStopping(event);
		restartModule.serverStopping(event);
	}

	@SubscribeEvent
	public void onServerRegisterCommands(RegisterCommandsEvent event) {
		restartModule.serverRegisterCommands(event);
	}
}
