package org.stonlexx.minecraft.gamemapper.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ServerProcessException
        extends Exception {

    public ServerProcessException(String message) {
        super(message);
    }
}
