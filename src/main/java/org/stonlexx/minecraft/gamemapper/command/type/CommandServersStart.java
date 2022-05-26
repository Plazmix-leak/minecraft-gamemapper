package org.stonlexx.minecraft.gamemapper.command.type;

import lombok.NonNull;
import org.stonlexx.minecraft.gamemapper.MinecraftGameMapper;
import org.stonlexx.minecraft.gamemapper.command.CommandSender;
import org.stonlexx.minecraft.gamemapper.command.MinecraftCommand;
import org.stonlexx.minecraft.gamemapper.objects.TemplateServer;
import org.stonlexx.minecraft.gamemapper.utility.ChatColor;

public final class CommandServersStart extends MinecraftCommand {

    public CommandServersStart() {
        super("start");
    }

    @Override
    public void onExecute(@NonNull CommandSender commandSender, @NonNull String[] args) {
        if (args.length == 0) {
            commandSender.sendMessage(ChatColor.RED + "[Commands] :: Use - start <template/*>");
            return;
        }

        TemplateServer templateServer = MinecraftGameMapper.INSTANCE.getTemplateServer(args[0]);

        if (templateServer == null && args[0].equals("*")) {
            MinecraftGameMapper.INSTANCE.startCachedTemplates();
            return;
        }

        MinecraftGameMapper.INSTANCE.startTemplate(templateServer);
    }

}
