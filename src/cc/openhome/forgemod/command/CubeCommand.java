package cc.openhome.forgemod.command;

import java.util.Arrays;
import java.util.List;

import cc.openhome.forgemod.FstPerspective;
import cc.openhome.forgemod.Messenger;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import static cc.openhome.forgemod.FstPerspective.Vertical.*;

public abstract class CubeCommand implements ICommand {
	@Override
	public String getUsage(ICommandSender sender) {
		return String.format("/%s <up|down> <rows> <columns> <layers>", getName());
	}

	@Override
	public List<String> getAliases() {
		return Arrays.asList(getName());
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {	
		if(args.length != 4) {
			Messenger.sendMessageTo(((EntityPlayer) sender), getUsage(sender));
			return;
		}
		
		FstPerspective perspective = new FstPerspective(
			args[0].equals("up") ? UP : DOWN,
			Integer.parseInt(args[1]),
		    Integer.parseInt(args[2]),
		    Integer.parseInt(args[3])
        );
		
		doCommand(server, sender, perspective);
	} 
	
	public abstract void doCommand(MinecraftServer server, ICommandSender sender, FstPerspective perspective) throws CommandException;

	@Override
	public int compareTo(ICommand o) {
		return 0;
	}	
	
	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return true;
	}

	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			BlockPos targetPos) {
		return null;
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return false;
	}	
}
