package cc.openhome.forgemod.command.building;

import cc.openhome.forgemod.command.DefaultCommand;
import cc.openhome.forgemod.command.FstDimension;
import cc.openhome.forgemod.command.FstPos;
import cc.openhome.forgemod.command.drawing.Cube;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class Maze implements DefaultCommand {
    private enum WallType {
        NONE, UP, RIGHT, UP_RIGHT,
    }
    
    private class Grid {
        final FstPos origin;
        
        final int width;
        final int wallThickness;
        final int wallHeight;
        
        WallType wallType = WallType.UP_RIGHT;
        
        boolean visited;
        
        public Grid(FstPos origin, int width, int wallThickness, int wallHeight) {
            this.origin = origin;
            this.width = width;
            this.wallThickness = wallThickness;
            this.wallHeight = wallHeight;
        }        
        
        
        void buildWith(ICommandSender sender, Cube cube) {
            int offset = this.width - this.wallThickness;
            
            String[] upWallArgs = toCubeArgs(
                new FstPos(origin.ux + offset, origin.uy, origin.uz), 
                new FstDimension(wallThickness, width, wallHeight)
                
            );
            String[] rightWallArgs = toCubeArgs(
                new FstPos(origin.ux, origin.uy, origin.uz + offset), 
                new FstDimension(width, wallThickness, wallHeight)
            );
            
            if(wallType == WallType.UP || wallType == WallType.UP_RIGHT) {
                cube.doCommandWithoutCheckingBlock(sender, upWallArgs);
            }
            if(wallType == WallType.RIGHT || wallType == WallType.UP_RIGHT)
                cube.doCommandWithoutCheckingBlock(sender, rightWallArgs); {
            }
        }
    }

    
    @Override
    public String getName() {
        return "maze";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return String.format("/%s <ux> <uy> <uz> <rows> <columns> <grid_width> <wall_thickness> <wall_height>", getName());
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {        
        int ux = Integer.valueOf(args[0]);
        int uy = Integer.valueOf(args[1]);
        int uz = Integer.valueOf(args[2]);
        
        int rows = Integer.valueOf(args[3]);
        int columns = Integer.valueOf(args[4]);
        
        int gridWidth = Integer.valueOf(args[5]);
        int wallThickness = Integer.valueOf(args[6]);
        int wallHeight = Integer.valueOf(args[7]);  
        
        FstDimension mazeDimension = new FstDimension(rows, columns, wallHeight);
        
        buildMaze(sender, 
            new FstPos(ux, uy, uz),
            mazeDimension,
            gridWidth, wallThickness
        );
        
        /*
        WallType[][] maze = {
            {WallType.UP, WallType.UP, WallType.UP, WallType.UP},
            {WallType.RIGHT, WallType.UP_RIGHT, WallType.UP, WallType.RIGHT},
            {WallType.NONE, WallType.RIGHT, WallType.RIGHT, WallType.RIGHT},
            {WallType.UP, WallType.UP, WallType.RIGHT, WallType.RIGHT}  
        };
        
        buildMaze(sender, 
            ux, uy, uz, rows, columns, 
            gridWidth, wallThickness, wallHeight, 
            maze
        );
        */
    }

    private void buildMaze(ICommandSender sender, 
            FstPos origin, 
            FstDimension mazeDimension,
            int gridWidth, int wallThickness) {
        
        Cube cube = new Cube();
        
        Grid[][] grids = initGrids(origin, mazeDimension, gridWidth, wallThickness);
        buildGrids(sender, cube, grids);
        
        // The most left and bottom walls
        cube.doCommandWithoutCheckingBlock(
            sender, toCubeArgs(origin, 
                       new FstDimension(
                              mazeDimension.rows * gridWidth + wallThickness, 
                              wallThickness, 
                              mazeDimension.layers
                       )
                    )
        );
        cube.doCommandWithoutCheckingBlock(
            sender, toCubeArgs(new FstPos(origin.ux, origin.uy, origin.uz + gridWidth), 
                        new FstDimension(
                              wallThickness, 
                              (mazeDimension.columns - 1) * gridWidth + wallThickness, 
                              mazeDimension.layers
                        )
                    )
        );
    }
    
    private Grid[][] initGrids(FstPos origin, 
            FstDimension mazeDimension, 
            int gridWidth, int wallThickness) {
        
        Grid[][] mazeGrids = new Grid[mazeDimension.rows][mazeDimension.columns];
        for(int i = 0; i < mazeDimension.rows; i++) {
            for(int j = 0; j < mazeDimension.columns; j++) {
                int gridUserX = i * gridWidth + wallThickness;
                int gridUserZ = j * gridWidth + wallThickness;
                
                mazeGrids[mazeDimension.rows - i - 1][j] = new Grid(
                    new FstPos(origin.ux + gridUserX, origin.uy, origin.uz + gridUserZ),
                    gridWidth, wallThickness, mazeDimension.layers
                );
            }
        }
        mazeGrids[0][mazeDimension.columns - 1].wallType = WallType.UP;
        
        return mazeGrids;
    }
    
    private void buildGrids(ICommandSender sender, Cube cube, Grid[][] mazeGrids) {
        for(Grid[] rowGrids : mazeGrids) {
            for(Grid grid : rowGrids) {
                grid.buildWith(sender, cube);
            }
        }
    }    
    
    private boolean isVisitable(Grid[][] grids, int i, int j) {
        return i >= 0 && i < grids.length &&
               j >= 0 && j < grids[0].length &&
               !grids[i][j].visited;
    }
/*
    private void buildMaze(ICommandSender sender, 
            int ux, int uy, int uz, 
            int rows, int columns, int gridWidth,
            int wallThickness, int wallHeight, 
            WallType[][] maze) {
        
        Cube cube = new Cube();
        // maze
        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < columns; j++) {
                int gridUserX = i * gridWidth + wallThickness;
                int gridUserZ = j * gridWidth + wallThickness;
                
                Grid grid = new Grid(ux + gridUserX, uy, uz + gridUserZ, gridWidth, wallThickness, wallHeight);
                grid.wallType = maze[rows - i - 1][j];
                grid.buildWith(sender, cube);
            }
        }
        
       // maze sides
       cube.doCommandWithoutCheckingBlock(sender, toCubeArgs(ux, uy, uz, rows * gridWidth + wallThickness, wallThickness, wallHeight));
       cube.doCommandWithoutCheckingBlock(sender, toCubeArgs(ux, uy, uz + gridWidth, wallThickness, (columns - 1) * gridWidth + wallThickness, wallHeight));
    }
    */
    private String[] toCubeArgs(FstPos fstPos, FstDimension fstDimension) {
        return new String[ ]{
            String.valueOf(fstPos.ux),
            String.valueOf(fstPos.uy),
            String.valueOf(fstPos.uz),
            String.valueOf(fstDimension.rows),
            String.valueOf(fstDimension.columns),
            String.valueOf(fstDimension.layers)
        };
    }
}
