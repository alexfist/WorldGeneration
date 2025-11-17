package core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import tileengine.TERenderer;
import tileengine.TETile;
import tileengine.Tileset;
import utils.RandomUtils;

public class World {
    public final int WIDTH;
    public final int HEIGHT;
    private final long seed;
    private final Random random;
    public TETile[][] theWorld;
    public WorldWQU roomsWQU;
    private int area;
    private List<ObjectPosition> roomCenters;
    public World(long seed, int width, int height) {
        random = new Random(seed);
        this.seed = seed;
        this.WIDTH = width;
        this.HEIGHT = height;
        theWorld = new TETile[WIDTH][HEIGHT];
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                theWorld[i][j] = Tileset.NOTHING;
            }
        }
        this.area = WIDTH*HEIGHT;
        this.roomCenters = new ArrayList<>();
        this.generateRooms();
    }
    private void generateRooms() {
        int roomsArea = 0;

        while (roomsArea <= 0.2 * area) {
            //pick valid width/height for the room
            int width, height;
            while (true) {
                width  = RandomUtils.uniform(random, 4, 10);
                height = RandomUtils.uniform(random, 4, 10);
                boolean tooSkinny = (width > height * 2) || (height > width * 2);
                if (!tooSkinny) {
                    break;
                }
            }
            //pick a bottom left corner for the room on the map
            int x = RandomUtils.uniform(random, 1, WIDTH - width - 1);
            int y = RandomUtils.uniform(random, 1, HEIGHT - height - 1);

            //check if there's any overlap with other rooms
            if (overlapsTiles(x, y, width, height)) {
                continue;
            }
            //create the room
            createRoom(x, y, width, height);
            roomsArea += width * height;

            // 6. Store the center for later hallway generation
            int cx = x + width / 2;
            int cy = y + height / 2;
            roomCenters.add(new ObjectPosition(cx, cy));
        }
    }
    private void createRoom(int startX, int startY, int width, int height) {
        //place the flooring
        for (int i = startX + 1; i < startX + width; i++) {
            for (int j = startY + 1; j < startY + height; j++) {
                this.theWorld[i][j] = Tileset.FLOOR;
            }
        }
        //place the walls
        for (int i = startY; i <= startY + height; i++) {
            this.theWorld[startX][i] = Tileset.WALL;
            this.theWorld[startX + width][i] = Tileset.WALL;
        }
        for (int i = startX; i <= startX + width; i++) {
            this.theWorld[i][startY] = Tileset.WALL;
            this.theWorld[i][startY+height] = Tileset.WALL;
        }
    }
    private boolean overlapsTiles(int startX, int startY, int width, int height) {
        int x1 = Math.max(0, startX - 1);
        int x2 = Math.min(WIDTH - 1, startX + width + 1);

        int y1 = Math.max(0, startY - 1);
        int y2 = Math.min(HEIGHT - 1, startY + height + 1);

        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                if (theWorld[x][y] != Tileset.NOTHING) {
                    return true;
                }
            }
        }
        return false;
    }
    private void generateHallways() {

    }

}
