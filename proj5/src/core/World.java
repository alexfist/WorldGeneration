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

    private final Random random;
    public TETile[][] theWorld;
    //TODO: roomsWQU for hallway generation; check design doc for more info
    public WorldWQU roomsWQU;
    private List<ObjectPosition> roomCenters;
    public World(long seed, int width, int height) {
        random = new Random(seed);
        this.WIDTH = width;
        this.HEIGHT = height;
        theWorld = new TETile[WIDTH][HEIGHT];
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                theWorld[i][j] = Tileset.NOTHING;
            }
        }
        this.roomCenters = new ArrayList<>();
        this.generateRooms();
    }
    private void generateRooms() {
        int roomsArea = 0;
        double max_room_area = 0.25 * this.HEIGHT * this.WIDTH; //can change this to change how filled the screen is w/ rooms
        while (roomsArea <= max_room_area) {
            //pick valid width/height for the room
            int width, height;
            while (true) {
                width  = RandomUtils.uniform(random, 4, 10); // we don't want rooms to be too big
                height = RandomUtils.uniform(random, 4, 10); // also don't want room generation to take forever
                boolean tooSkinny = (width > height * 2) || (height > width * 2); //need rooms not to look like hallways
                if (!tooSkinny) {
                    break;
                }
            }
            //pick a bottom left corner to anchor the room on the map
            int x = RandomUtils.uniform(random, 1, WIDTH - width - 1);
            int y = RandomUtils.uniform(random, 1, HEIGHT - height - 1);
            //check if there's any overlap with other rooms
            if (overlapsTiles(x, y, width, height)) {
                continue;
            }
            //create the room
            createRoom(x, y, width, height);
            //add roomarea to the totalroomarea; we don't want our rooms to take up the entire space
            roomsArea += width * height;
            //store centers of the room later for hallway generation
            int cx = x + width / 2;
            int cy = y + height / 2;
            roomCenters.add(new ObjectPosition(cx, cy));
        }
    }
    private void createRoom(int startX, int startY, int width, int height) {
        //place the floors
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
        //need to make sure we don't get any invalid x/y positions
        //these constants also make sure our rooms aren't touching each other,
        // which will make hallway generation easier down the line
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
    //TODO: finish hallway generation
    private void generateHallways() {

    }

}
