package org.stonlexx.minecraft.gamemapper.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ServerJarNotFoundException
        extends Exception {

    public ServerJarNotFoundException(String message) {
        super(message);
    }
}
