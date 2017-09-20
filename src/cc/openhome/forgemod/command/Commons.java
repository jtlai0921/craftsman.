package cc.openhome.forgemod.command;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockStairs;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import scala.actors.threadpool.Arrays;

public class Commons {
    public static void sendMessageTo(EntityPlayer player, String message) {
        player.sendMessage(new TextComponentString(message));
    }
    
    public static void runIfAirOrBlockHeld(ICommandSender sender, Runnable airOrBlockHeld) {
        runIfAirOrBlockHeld(sender, airOrBlockHeld, airOrBlockHeld); 
    }

    public static void runIfAirOrBlockHeld(ICommandSender sender, Runnable blockHeld, Runnable airHeld) {
        EntityPlayer player = (EntityPlayer) sender;
        
        Item heldItem = player.getHeldItemMainhand().getItem();
        
        if(heldItem.equals(Items.AIR)) {
            Commons.sendMessageTo(player, "You don't hold a block. The command will do the cleaning.");
            airHeld.run();
        } else if(!(heldItem instanceof ItemBlock)) {
            Commons.sendMessageTo(player, "Hold a block");
        } else {
            blockHeld.run();
        }
    }    
    
    public static void runIfAirOrStairsHeld(ICommandSender sender, Runnable runnable) {
        EntityPlayer player = (EntityPlayer) sender;
        
        Item heldItem = player.getHeldItemMainhand().getItem();
        Block heldBlock = Block.getBlockFromItem(heldItem);
        
        if(heldItem.equals(Items.AIR)) {
            Commons.sendMessageTo(player, "You don't hold stairs. The command will do the cleaning.");
        } else if(!(heldBlock instanceof BlockStairs)) {
            Commons.sendMessageTo(player, "Hold stairs");
            return;
        }        
        
        runnable.run();
    }    
    
    public static BlockPos origin(EntityPlayer player, int ux, int uy, int uz) {
        return new Walker(player.getAdjustedHorizontalFacing(), player.getPosition())
                        .forward(1 + ux) 
                        .up(uy)           
                        .right(uz)      
                        .getBlockPos();
    }   

    public static Map<String, Integer> argsToInteger(String[] argNames, String[] args) {
        Map<String, Integer> argsToInteger = new HashMap<>();
        for(int i = 0; i < argNames.length; i++) {
            argsToInteger.put(argNames[i], Integer.parseInt(args[i]));
        }
        return argsToInteger;
    }
    
    public static String[] copyArgs(String[] args, int from) {
        return (String[]) Arrays.copyOfRange(args, from, args.length);
    }
    
    public static BlockPos toBlockPos(FstPos pos, EntityPlayer player) {
        Walker walker = new Walker(player.getAdjustedHorizontalFacing(), player.getPosition());
        return walker.forward(pos.ux).up(pos.uy).right(pos.uz).getBlockPos();
    }
     
    public static void buildHeldBlock(FstPos fstPos, EntityPlayer player) {
        buildHeldBlock(toBlockPos(fstPos, player), player);
    }
    
    public static void buildHeldBlock(BlockPos pos, EntityPlayer player) {
        Item heldItem = player.getHeldItemMainhand().getItem();
        player.getEntityWorld()
            .setBlockState(pos, Block.getBlockFromItem(heldItem).getDefaultState());  
    }    
}
