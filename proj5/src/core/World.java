package core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import tileengine.TETile;
import tileengine.Tileset;
import utils.RandomUtils;

public class World {
    public final int WIDTH;
    public final int HEIGHT;

    private final Random random;
    public TETile[][] theWorld;
    private int[][] roomIndexGrid;
    public WorldWQU roomsWQU;
    private List<ObjectPosition> roomCenters;
    private List<Room> roomList;
    public World(long seed, int width, int height) {
        this.random = new Random(seed);
        this.WIDTH = width;
        this.HEIGHT = height;
        this.roomList = new ArrayList<>();
        this.roomCenters = new ArrayList<>();
        this.roomIndexGrid = new int[WIDTH][HEIGHT];
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                roomIndexGrid[i][j] = -1;
            }
        }
        theWorld = new TETile[WIDTH][HEIGHT];
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                theWorld[i][j] = Tileset.NOTHING;
            }
        }
        this.generateRooms();
        this.generateHallways();
    }
    private void generateRooms() {
        int roomsArea = 0;
        int totalRooms = 0;
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
            Room newRoom = new Room(x,y,width,height);
            this.roomList.add(newRoom);
            createRoom(x, y, width, height, this.roomList.size()-1);
            //count number of rooms create
            totalRooms += 1;
            //add roomarea to the totalroomarea; we don't want our rooms to take up the entire space
            roomsArea += width * height;
            //store centers of the room later for hallway generation
            int cx = x + width / 2;
            int cy = y + height / 2;
            roomCenters.add(new ObjectPosition(cx, cy));
        }
        this.roomsWQU = new WorldWQU(totalRooms);
    }
    private void createRoom(int startX, int startY, int width, int height, int roomID) {
        //place the floors
        for (int i = startX + 1; i < startX + width; i++) {
            for (int j = startY + 1; j < startY + height; j++) {
                this.theWorld[i][j] = Tileset.FLOOR;
                this.roomIndexGrid[i][j] = roomID;
            }
        }
        //place the walls
        for (int i = startY; i <= startY + height; i++) {
            this.theWorld[startX][i] = Tileset.WALL;
            this.roomIndexGrid[startX][i] = roomID;
            this.theWorld[startX + width][i] = Tileset.WALL;
            this.roomIndexGrid[startX + width][i] = roomID;
        }
        for (int i = startX; i <= startX + width; i++) {
            this.theWorld[i][startY] = Tileset.WALL;
            this.roomIndexGrid[i][startY] = roomID;
            this.theWorld[i][startY+height] = Tileset.WALL;
            this.roomIndexGrid[i][startY+height] = roomID;
        }
    }
    private boolean overlapsTiles(int startX, int startY, int width, int height) {
        // give rooms a buffer of 2 tiles on every side
        int padding = 2;
        int x1 = Math.max(0, startX - padding);
        int x2 = Math.min(WIDTH - 1, startX + width + padding);
        int y1 = Math.max(0, startY - padding);
        int y2 = Math.min(HEIGHT - 1, startY + height + padding);

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
        if (roomList.size() <= 1) {
            return;
        }

        // keep connecting random unconnected pairs until everything is in one component
        while (!this.roomsWQU.allConnected()) {
            int[] pair = pickTwoRoomsForHallway();
            int room1index = pair[0];
            int room2index = pair[1];
            connectRooms(room1index, room2index);
        }

    }
    private void connectRooms(int r1index, int r2index) {
        ObjectPosition door1 = pickDoorOnRoom(r1index);
        ObjectPosition door2 = pickDoorOnRoom(r2index);

        int x1 = door1.x;
        int y1 = door1.y;
        int x2 = door2.x;
        int y2 = door2.y;

        // carve the doorway tiles
        carveDoorTile(x1, y1);
        carveDoorTile(x2, y2);

        // pick corridor style: horizontal-then-vertical or reversed
        if (random.nextBoolean()) {
            carveHorizontally(x1, x2, y1);  // corridor from door1.x → door2.x
            carveVertically(y1, y2, x2);    // then down/up at x2
        } else {
            carveVertically(y1, y2, x1);
            carveHorizontally(x1, x2, y2);
        }

        roomsWQU.union(r1index, r2index);
    }
    private void carveDoorTile(int x, int y) {
        theWorld[x][y] = Tileset.FLOOR;
        roomIndexGrid[x][y] = -1; // door belongs to hallway, not the room interior
    }
    private void carveHorizontally(int startX, int endX, int y) {
        int step;
        if (endX >= startX) {
            step = 1;
        } else {
            step = -1;
        }
        for (int x = startX; x != endX + step; x += step) {
            carveHallTile(x, y);
        }
    }

    private void carveVertically(int startY, int endY, int x) {
        int step;
        if (endY >= startY) {
            step = 1;
        } else {
            step = -1;
        }
        for (int y = startY; y != endY + step; y += step) {
            carveHallTile(x, y);
        }
    }
    private void carveHallTile(int x, int y) {
        // hall floor
        theWorld[x][y] = Tileset.FLOOR;
        roomIndexGrid[x][y] = -1;

        addHallWallsAround(x, y);
    }
    private void addHallWallsAround(int x, int y) {
        tryPlaceHallWall(x + 1, y);
        tryPlaceHallWall(x - 1, y);
        tryPlaceHallWall(x, y + 1);
        tryPlaceHallWall(x, y - 1);
    }
    private ObjectPosition pickDoorOnRoom(int roomIndex) {
        Room r = roomList.get(roomIndex);
        // room floor spans (x+1 ... x+width-1), (y+1 ... y+height-1)
        // room walls are at x, x+width, y, y+height
        // pick a random wall side
        int side = random.nextInt(4);

        int doorX = 0;
        int doorY = 0;

        switch (side) {
            case 0: // bottom wall
                doorX = RandomUtils.uniform(random, r.x + 1, r.x + r.w - 1);
                doorY = r.y;
                break;
            case 1: // top wall
                doorX = RandomUtils.uniform(random, r.x + 1, r.x + r.w - 1);
                doorY = r.y + r.h;
                break;
            case 2: // left wall
                doorX = r.x;
                doorY = RandomUtils.uniform(random, r.y + 1, r.y + r.h - 1);
                break;
            case 3: // right wall
                doorX = r.x + r.w;
                doorY = RandomUtils.uniform(random, r.y + 1, r.y + r.h - 1);
                break;
        }

        return new ObjectPosition(doorX, doorY);
    }
    private void tryPlaceHallWall(int x, int y) {

        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) {
            return;
        }

        TETile tile = theWorld[x][y];

        // If it's already a room wall, leave it
        if (tile == Tileset.WALL) {
            return;
        }

        // If it's room floor, do not touch it
        if (tile == Tileset.FLOOR && roomIndexGrid[x][y] != -1) {
            return;
        }

        // If it's hallway floor, do nothing
        if (tile == Tileset.FLOOR && roomIndexGrid[x][y] == -1) {
            return;
        }

        // If it's empty, place a hall wall
        if (tile == Tileset.NOTHING) {
            theWorld[x][y] = Tileset.WALL;
            roomIndexGrid[x][y] = -1; // hall-wall indicator
        }
    }
    private int[] pickTwoRoomsForHallway() {
        int a = random.nextInt(roomList.size());
        int b = 0;
        while (true) {
            b = random.nextInt(roomList.size());
            if (b != a && !roomsWQU.connected(a, b)) {
                break;
            }
        }
        return new int[] {a, b};
    }

}
