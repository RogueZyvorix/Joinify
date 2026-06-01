package com.roguzyvorix.joinify;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Joinify implements ModInitializer {
    public static final String MOD_ID = "joinify";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Joinify Common Initialization Started!");
    }
}