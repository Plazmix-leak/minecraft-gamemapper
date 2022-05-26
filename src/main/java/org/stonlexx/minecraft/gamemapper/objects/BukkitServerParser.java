package org.stonlexx.minecraft.gamemapper.objects;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("all")
@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class BukkitServerParser {

    @NonNull String host;
    @NonNull int port;

    int online;
    int maxOnline;

    // Код спиздил с bukkit.org, мне норм
    @SneakyThrows
    public void connectInit() {
        try (Socket socket = new Socket(host, port);
             DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
             InputStreamReader inputStreamReader = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_16BE)) {

            socket.setSoTimeout(3000);

            dataOutputStream.write(new byte[]{
                    (byte) 0xFE,
                    (byte) 0x01
            });

            int packetId = inputStreamReader.read();

            if (packetId == -1) {
                throw new IOException("Premature end of stream.");
            }

            if (packetId != 0xFF) {
                throw new IOException("Invalid packet ID (" + packetId + ").");
            }

            int length = inputStreamReader.read();

            if (length == -1) {
                throw new IOException("Premature end of stream.");
            }

            if (length == 0) {
                throw new IOException("Invalid string length.");
            }

            char[] chars = new char[length];

            if (inputStreamReader.read(chars,0,length) != length) {
                throw new IOException("Premature end of stream.");
            }

            String string = new String(chars);

            if (string.startsWith("§")) {
                String[] data = string.split("\0");

                this.online = Integer.parseInt(data[4]);
                this.maxOnline = Integer.parseInt(data[5]);

            } else {

                String[] data = string.split("§");

                this.online = Integer.parseInt(data[1]);
                this.maxOnline = Integer.parseInt(data[2]);
            }
        }
    }

}
