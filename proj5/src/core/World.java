package core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.Out;
import tileengine.TETile;
import tileengine.Tileset;
import utils.RandomUtils;

public class World {
    public final int WIDTH;
    public final int HEIGHT;

    private final Random random;
    public TETile[][] theWorld;
    private final int[][] roomIndexGrid;
    public WorldWQU roomsWQU;
    private final List<ObjectPosition> roomCenters;
    private final List<Room> roomList;
    public int playerX;
    public int playerY;
    public int enemyX;
    public int enemyY;

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
        this.placePlayer();
        this.placeEnemy();
    }

    // Used only for loading from file
    // notice DOES NOT have seed as argument
    public World(int width, int height) {
        this.WIDTH = width;
        this.HEIGHT = height;
        this.random = new java.util.Random();

        this.roomList = new ArrayList<>();
        this.roomCenters = new ArrayList<>();
        this.roomIndexGrid = new int[WIDTH][HEIGHT];
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                roomIndexGrid[i][j] = -1;
            }
        }

        this.theWorld = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                theWorld[x][y] = Tileset.NOTHING;
            }
        }

        this.roomsWQU = new WorldWQU(0);
    }

    private void generateRooms() {
        int roomsArea = 0;
        int totalRooms = 0;
        int attempts = 0;
        final int MAXATTEMPTS = 5000;
        double max_room_area = 0.25 * this.HEIGHT * this.WIDTH; // can change this to change how filled the screen is w/ rooms
        while (roomsArea <= max_room_area && attempts < MAXATTEMPTS) {
            attempts += 1;
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
            //add roomArea to the totalRoomArea; we don't want our rooms to take up the entire space
            roomsArea += width * height;
            //store centers of the room later for hallway generation
            int cx = x + width / 2;
            int cy = y + height / 2;
            roomCenters.add(new ObjectPosition(cx, cy));
        }
        this.roomsWQU = new WorldWQU(totalRooms);
    }
    //room generation functions
    private void createRoom(int startX, int startY, int width, int height, int roomID) {
        // place the floors
        for (int i = startX + 1; i < startX + width; i++) {
            for (int j = startY + 1; j < startY + height; j++) {
                this.theWorld[i][j] = Tileset.FLOOR;
                this.roomIndexGrid[i][j] = roomID;
            }
        }
        // place the walls
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
    //hallway generation functions
    // Static edge class to compare distances
    private static class Edge implements Comparable<Edge> {
        int room1;
        int room2;
        double dist;

        Edge(int room1, int room2, double dist) {
            this.room1 = room1;
            this.room2 = room2;
            this.dist = dist;
        }

        @Override
        public int compareTo(Edge other) {
            return Double.compare(this.dist, other.dist);
        }
    }
    // Pythagoras Theorem
    private double distanceFunc(double x, double y) {
        return Math.sqrt(x*x + y*y);
    }
    private void generateHallways() {
        int n = roomList.size();
        if (n <= 1) {
            return;
        }
        // List of all rooms and their distances
        // to every other room
        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            ObjectPosition c1 = roomCenters.get(i);
            for (int j = i + 1; j < n; j++) {
                ObjectPosition c2 = roomCenters.get(j);
                double xDist = c1.x - c2.x;
                double yDist = c1.y - c2.y;
                double dist = distanceFunc(xDist, yDist); // Straight line Dist
                edges.add(new Edge(i, j, dist));
            }
        }
        edges.sort(null); // Sort distances
        for (Edge e : edges) {
            if (!roomsWQU.connected(e.room1, e.room2)) {
                connectRooms(e.room1, e.room2);
            }
        }
    }
    private void connectRooms(int r1index, int r2index) {
        ObjectPosition c1 = roomCenters.get(r1index);
        ObjectPosition c2 = roomCenters.get(r2index);

        int x1 = c1.x;
        int y1 = c1.y;
        int x2 = c2.x;
        int y2 = c2.y;

        if (random.nextBoolean()) {
            carveHorizontally(x1, x2, y1);
            carveVertically(y1, y2, x2);
        } else {
            carveVertically(y1, y2, x1);
            carveHorizontally(x1, x2, y2);
        }
        roomsWQU.union(r1index, r2index);
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
    //player movement stuff
    public void placePlayer() {
        //initialize a pseudorandom position for the player to start at
        while (true) {
            int x = this.random.nextInt(WIDTH);
            int y = this.random.nextInt(HEIGHT);
            if (theWorld[x][y] == Tileset.FLOOR) {
                playerX = x;
                playerY = y;
                theWorld[x][y] = Tileset.AVATAR;
                break;
            }
        }
    }
    /// ##############################################################
    /// USED MOUNTAIN TO REPRESENT ENEMY
    /// ##############################################################
    public void placeEnemy() {
        // Same thing as above but for enemy
        while (true) {
            int x = this.random.nextInt(WIDTH);
            int y = this.random.nextInt(HEIGHT);
            if (theWorld[x][y] == Tileset.FLOOR) {
                enemyX = x;
                enemyY = y;
                theWorld[x][y] = Tileset.MOUNTAIN; // Mounntaini for enemy
                break;
            }
        }
    }


    public void move(char direction) {
        //movement of the player
        int newX = playerX;
        int newY = playerY;
        if (direction == 'W') {
            newY += 1;
        }
        if (direction == 'S') {
            newY -= 1;
        }
        if (direction == 'A') {
            newX -= 1;
        }
        if (direction == 'D') {
            newX += 1;
        }
        // check if newY and newX are valid, then move player to the new coordinates
        // ONLY IF the new coordinates doesn't have a wall; also make sure we turn
        // the previous spot the player was in into a floor tile again
        if (newX >= 0 && newX < WIDTH && newY >= 0 && newY < HEIGHT) {
            if (theWorld[newX][newY] != Tileset.WALL) {
                theWorld[playerX][playerY] = Tileset.FLOOR;
                playerX = newX;
                playerY = newY;
                theWorld[playerX][playerY] = Tileset.AVATAR;
            }
        }
    }
    public String HUD(int x, int y) {
        return getObject(x, y);
    }
    public String getObject(int x, int y) {
        // Not in field
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) {
            return "";
        }
        TETile tile = theWorld[x][y];
        return tile.description();
    }


    // Save helpers
    public int getWidth()  {
        return WIDTH;
    }
    public int getHeight() {
        return HEIGHT;
    }
    public int getPlayerX() {
        return playerX;
    }
    public int getPlayerY() {
        return playerY;
    }
    public TETile[][] getWorldTiles() {
        return theWorld;
    }

    public void setPlayerPosition(int x, int y) {
        playerX = x;
        playerY = y;
    }
}

