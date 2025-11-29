package core;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Out;
import tileengine.TETile;
import tileengine.Tileset;

import java.io.File;

public class SaveFile {

    public static void saveGame(World world, String filename) {
        Out out = new Out(filename);

        int width = world.WIDTH;
        int height = world.HEIGHT;

        out.println(width + " " + height);
        // Player X and Y
        out.println(world.playerX + " " + world.playerY);
        // Enemy X and Y
        out.println(world.enemyX + " " + world.enemyY);
        // Coins collected and Max coins
        out.println(world.coinsCollected + " " + world.maxCoins);

        // World tiles
        TETile[][] tiles = world.theWorld;
        for (int y = 0; y < height; y++) {
            StringBuilder sb = new StringBuilder();
            for (int x = 0; x < width; x++) {
                sb.append(tileToChar(tiles[x][y]));
            }
            out.println(sb.toString());
        }
        out.close();
    }

    public static World loadGame(String filename) {
        In in = new In(filename);
        if (!in.exists()) {
            return null;
        }

        int width = in.readInt();
        int height = in.readInt();
        int savedX = in.readInt();
        int savedY = in.readInt();
        int enemyX = in.readInt();
        int enemyY = in.readInt();
        int coinsCollected = in.readInt();
        int maxCoins = in.readInt();
        in.readLine();

        // Loader that IS NOT random
        World w = new World(width, height);

        TETile[][] tiles = w.theWorld;
        for (int y = 0; y < height; y+=1) {
            String line = in.readLine();
            for (int x = 0; x < width; x+=1) {
                char c = line.charAt(x);
                tiles[x][y] = charToTile(c);
            }
        }

        w.playerX = savedX;
        w.playerY = savedY;
        w.enemyX = enemyX;
        w.enemyY = enemyY;
        w.coinsCollected = coinsCollected;
        w.maxCoins = maxCoins;
        w.theWorld[enemyX][enemyY] = Tileset.MOUNTAIN;

        return w;
    }
    public static void resetSave(String filename) {
        File f = new File(filename);
        if (f.exists()) {
            f.delete();
        }
    }
    private static char tileToChar(TETile t) {
        if (t == Tileset.NOTHING) return ' ';
        if (t == Tileset.FLOOR)   return '.';
        if (t == Tileset.WALL)    return '#';
        if (t == Tileset.AVATAR)  return '@';
        if (t == Tileset.FLOWER) return 'C';
        if (t == Tileset.MOUNTAIN) return 'E';
        return '?';
    }

    private static TETile charToTile(char c) {
        if (c == ' ') return Tileset.NOTHING;
        if (c == '.') return Tileset.FLOOR;
        if (c == '#') return Tileset.WALL;
        if (c == '@') return Tileset.AVATAR;
        if (c == 'C') return Tileset.FLOWER;
        if (c == 'E') return Tileset.MOUNTAIN;
        return Tileset.NOTHING;
    }
}