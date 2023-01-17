package com.civilization.civil_utils.modules.restart;

import com.civilization.civil_utils.Config;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class RestartModule {
    private static final Logger LOGGER = LogManager.getLogger(RestartModule.class);

    private static MinecraftServer server = null;

    private Timer timer = null;

    //-----------------------------//

    private static boolean shouldDoRestart = false;

    public static void restart(MinecraftServer server) {
        shouldDoRestart = true;
        sendMessage(Config.RESTART_STOP_MESSAGE.get());
        writeRestartStatusInFile(1);
        server.halt(false);
    }

    public static void writeRestartStatusInFile(int status) {
        //-1 - Краш
        //0 - Остановка сервера
        //1 - Перезапуск

        FileWriter fileWriter = null;
        try {
            LOGGER.info("Saving restart status \"{}\" to file", status);
            File file = new File( "." + File.separator + "restart" );
            if(file.exists() || file.createNewFile()) {
                fileWriter = new FileWriter(file);
                fileWriter.write(String.valueOf(status));
                fileWriter.flush();
            } else {
                LOGGER.error( "Restart File could not be created" );
            }
        } catch( IOException exception ) {
            LOGGER.error( "FileWriter failed", exception );
        } finally {
            if( fileWriter != null ) {
                try {
                    fileWriter.close();
                } catch( IOException exception ) {
                    LOGGER.error( "FileWriter failed to close", exception );
                }
            }
        }
    }

    //-----------------------------//

    private static void sendMessage(String message) {
        server.getCommands().performCommand(server.createCommandSourceStack(), message);
    }

    //-----------------------------//

    private static class RestartTask extends TimerTask {
        private final String arg;

        public RestartTask(String arg) {
            this.arg = arg;
        }

        @Override
        public void run() {
            LOGGER.info("Timer point");

            if (server != null) {
                if (arg != null) {
                    String message = String.format(Config.RESTART_MESSAGE.get(), arg);
                    sendMessage(message);
                } else {
                    //Рестарт
                    sendMessage(Config.RESTART_STOP_MESSAGE.get());
                    restart(server);
                }
            }
        }
    }

    public void setup(final FMLCommonSetupEvent ev) {
        LOGGER.info("Setup timer");

        int hour = Config.RESTART_HOUR.get();
        int minutes = Config.RESTART_MINUTES.get();

        long startupTime = (hour - 1) * 60L * 60L * 1000L + minutes * 60L * 1000L;

        timer = new Timer("Civilization-Restart");

        for (long i = 60L; i > 0L; i -= 15L) {
            String arg = "";

            if (i == 60) {
                arg = "1 час";
            } else {
                arg = i + " минут";
            }

            timer.schedule(new RestartTask(arg), startupTime + (60L - i) * 60L * 1000L);
        }

        timer.schedule(new RestartTask("10 минут"), startupTime + 50L * 60L * 1000L);
        timer.schedule(new RestartTask("5 минут"), startupTime + 55L * 60L * 1000L);
        timer.schedule(new RestartTask("1 минуту"), startupTime + 59L * 60L * 1000L);
        timer.schedule(new RestartTask("30 секунд"), startupTime + 59L * 60L * 1000L + 30L * 1000L);

        for (long i = 10L; i > 0L; i--) {
            String arg = "";

            if (i > 4) {
                arg = i + " секунд";
            } else if (i < 5 && i > 1) {
                arg = i + " секунды";
            } else {
                arg = "1 секунду";
            }

            timer.schedule(new RestartTask(arg), startupTime + 59L * 60L * 1000L + (60L - i) * 1000L);
        }

        timer.schedule(new RestartTask(null), startupTime + 60L * 60L * 1000L);
    }

    public void serverRegisterCommands(RegisterCommandsEvent event) {
        RestartCommand.register(event.getDispatcher());
    }

    public void serverStarting(ServerStartingEvent ev) {
        server = ev.getServer();
//        writeRestartStatusInFile(-1);
    }

    public void serverStopping(ServerStoppingEvent event) {
        if (timer != null) {
            timer.cancel();
        }

        writeRestartStatusInFile(shouldDoRestart ? 1 : 0);
    }
}
