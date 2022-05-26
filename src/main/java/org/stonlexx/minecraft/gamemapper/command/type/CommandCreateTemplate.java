package org.stonlexx.minecraft.gamemapper.command.type;

import lombok.NonNull;
import org.stonlexx.minecraft.gamemapper.MinecraftGameMapper;
import org.stonlexx.minecraft.gamemapper.command.CommandSender;
import org.stonlexx.minecraft.gamemapper.command.MinecraftCommand;
import org.stonlexx.minecraft.gamemapper.utility.ChatColor;

public final class CommandCreateTemplate extends MinecraftCommand {

    public CommandCreateTemplate() {
        super("createtemplate", "createtemp", "template", "createshape", "shape");
    }

    @Override
    public void onExecute(@NonNull CommandSender commandSender, @NonNull String[] args) {

        if (args.length == 0) {
            commandSender.sendMessage(ChatColor.RED + "[Command] :: Use - /template <name>");
            return;
        }

        commandSender.sendMessage(ChatColor.GREEN + "[Command] Template " + args[0] + " success created!");
        MinecraftGameMapper.INSTANCE.createTemplate(args[0]);
    }

}
