package core;

import tileengine.TERenderer;
import edu.princeton.cs.algs4.StdDraw;
import tileengine.TETile;
import tileengine.Tileset;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    private static final Random RNG = new Random();
    private static final double randomMovementChance = 0.5;
    public static void main(String[] args) {
        resetStdDraw();
        while (true) {
            //this gets the world that the user wants from the main menu
            //will either be a saved world or a new world.
            World the_world = chooseOption(getChoice());

            //renders the world.
            TERenderer ter = new TERenderer();
            ter.initialize(the_world.WIDTH, the_world.HEIGHT);

            // Path Finder
            Pathfinder pf = new Pathfinder(the_world);

            ter.renderFrame(the_world.theWorld);

            //enters the gameplay loop
            String result = gameplayLoop(the_world, ter, pf);
            resetStdDraw();
            if (result.equals("WIN") || result.equals("LOSE")) {
                SaveFile.resetSave("save.txt");
            }
            if (result.equals("QUIT")) {
                // user typed :Q
                System.exit(0);
            }
        }


    }

    private static void resetStdDraw() {
        StdDraw.clear();
        StdDraw.setCanvasSize(800, 800);
        StdDraw.setXscale(0, 1);
        StdDraw.setYscale(0, 1);
        StdDraw.enableDoubleBuffering();
    }
    public static char getChoice() {
        //this creates the menu using the StdDraw library
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 30));
        StdDraw.text(0.5, 0.7, "CS61B: The World Generator");
        StdDraw.setFont(new Font("Monaco", Font.PLAIN, 20));
        StdDraw.text(0.5, 0.5, "New World (N)");
        StdDraw.text(0.5, 0.45, "Load World (L)");
        StdDraw.text(0.5, 0.40, "Quit (Q)");
        StdDraw.show();
        //this loop waits until the user gives a valid option, then returns
        //the option that is chosen
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toUpperCase(StdDraw.nextKeyTyped());
                if (c == 'N' || c == 'L' || c == 'Q') {
                    return c;
                }
            }
        }
    }

    public static World chooseOption(char c) {
        //given the option, execute the expected behavior
        if (c == 'N') {
            long seed = solicitSeed();
            return new World(seed, 60, 40);
        } else if (c == 'L') {
            return SaveFile.loadGame("save.txt");
        } else {
            System.exit(0);
            return null;
        }
    }

    public static long solicitSeed() {
        //if the user creates a new world, get an inputted seed.
        //stringbuilder to store the inputted integers for the seed
        StringBuilder sb = new StringBuilder();
        // Draw the initial stuff
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(0.5, 0.7, "Enter Seed, then press L:");
        StdDraw.show();
        //gradually build up the seed integer by integer; exit when the user types L
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                if (c == 'L' || c == 'l') {
                    return Long.parseLong(sb.toString());
                }
                if (Character.isDigit(c)) {
                    sb.append(c);
                    // update only the seed text area
                    StdDraw.clear(Color.BLACK);
                    StdDraw.setPenColor(Color.WHITE);
                    StdDraw.text(0.5, 0.7, "Enter Seed, then press L:");
                    StdDraw.text(0.5, 0.5, sb.toString());
                    StdDraw.show();
                }
            }
        }
    }

    public static String gameplayLoop(World world, TERenderer ter, Pathfinder pf) {
        boolean waitingForColon = false;
        boolean togglePath = false;

        while (true) {

            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toUpperCase(StdDraw.nextKeyTyped());

                // :Q quit
                if (waitingForColon && c == 'Q') {
                    SaveFile.saveGame(world, "save.txt");
                    return "QUIT";
                }
                if (c == ':') {
                    waitingForColon = true;
                    continue;
                } else {
                    waitingForColon = false;
                }

                // Movement
                if (c == 'W' || c == 'A' || c == 'S' || c == 'D') {
                    world.move(c);

                    // Player stepped into enemy
                    if (world.playerX == world.enemyX && world.playerY == world.enemyY) {
                        gameOver(world);
                        return "LOSE";
                    }

                    // Player wins
                    if (world.coinsCollected >= world.maxCoins) {
                        youWin(world);
                        return "WIN";
                    }

                    // Enemy moves
                    if (moveEnemyTowardsPlayer(world, pf)) {
                        return "LOSE";
                    }
                }

                if (c == 'P') {
                    togglePath = !togglePath;
                }
            }

            // Draw world frame *only if still in gameplay*
            ter.renderFrame(world.theWorld);

            int mx = (int) StdDraw.mouseX();
            int my = (int) StdDraw.mouseY();
            String hudText = world.HUD(mx, my);

            StdDraw.setPenColor(Color.WHITE);
            StdDraw.setFont(new Font("Monaco", Font.PLAIN, 14));
            StdDraw.textLeft(1, world.HEIGHT - 1, hudText);

            if (togglePath) {
                int ex = world.enemyX;
                int ey = world.enemyY;

                List<int[]> path = pf.shortestPath(
                        world.playerX, world.playerY,
                        ex, ey
                );

                StdDraw.setPenColor(Color.RED);
                for (int[] cell : path) {
                    StdDraw.filledCircle(cell[0] + 0.5, cell[1] + 0.5, 0.25);
                }
            }

            StdDraw.show();
            StdDraw.pause(10);
        }
    }
    private static boolean moveEnemyTowardsPlayer(World world, Pathfinder pf) {
        int ex = world.enemyX;
        int ey = world.enemyY;
        int px = world.playerX;
        int py = world.playerY;

        boolean doRandom = RNG.nextDouble() < randomMovementChance;
        if (doRandom) {
            return randomEnemyMove(world);
        }

        List<int[]> path = pf.shortestPath(ex, ey, px, py);
        if (path.size() <= 1) {
            return false; // already on player or no path
        }

        int[] next = path.get(1);
        int newX = next[0];
        int newY = next[1];

        // Clear old enemy tile
        world.theWorld[ex][ey] = Tileset.FLOOR;

        world.enemyX = newX;
        world.enemyY = newY;

        // Enemy has stepped onto the player
        if (newX == px && newY == py) {
            gameOver(world);
            return true;
        } else {
            world.theWorld[newX][newY] = Tileset.MOUNTAIN;
            return false;
        }
    }
    private static boolean randomEnemyMove (World world) {
        int ex = world.enemyX;
        int ey = world.enemyY;

        int[][] dirs = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}
        };

        List<int[]> valid = new ArrayList<>();

        for (int[] d : dirs) {
            int nx = ex + d[0];
            int ny = ey + d[1];

            if (nx < 0 || nx >= world.WIDTH || ny < 0 || ny >= world.HEIGHT) {
                continue;
            }
            TETile t = world.theWorld[nx][ny];
            if (t == Tileset.FLOOR || t == Tileset.AVATAR) {
                valid.add(new int[]{nx, ny});
            }
        }

        if (valid.isEmpty()) return false; // nowhere to go

        int[] choice = valid.get(RNG.nextInt(valid.size()));
        int newX = choice[0];
        int newY = choice[1];

        // Clear old tile
        world.theWorld[ex][ey] = Tileset.FLOOR;

        world.enemyX = newX;
        world.enemyY = newY;

        if (world.playerX == newX && world.playerY == newY) {
            gameOver(world);
            return true;
        } else {
            world.theWorld[newX][newY] = Tileset.MOUNTAIN;
            return false;
        }
    }
    private static void youWin(World world) {
        StdDraw.clear(Color.GREEN);
        StdDraw.setPenColor(Color.BLACK);
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 30));
        StdDraw.text(world.WIDTH / 2.0, world.HEIGHT / 2.0 + 2, "YOU WIN!!");
        StdDraw.setFont(new Font("Monaco", Font.PLAIN, 18));
        StdDraw.text(world.WIDTH / 2.0, world.HEIGHT / 2.0 - 2, "Press any key to return to menu");
        StdDraw.show();
        // wait for key press
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                StdDraw.nextKeyTyped(); // eat it
                break;
            }
        }
        SaveFile.resetSave("save.txt");
    }
    private static void gameOver(World world) {
        // Normal game over screen first
        StdDraw.clear(Color.RED);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 30));
        StdDraw.text(world.WIDTH / 2.0, world.HEIGHT / 2.0 + 2, "GAME OVER");
        StdDraw.setFont(new Font("Monaco", Font.PLAIN, 18));
        StdDraw.text(world.WIDTH / 2.0, world.HEIGHT / 2.0 - 2, "Press Q to quit");
        StdDraw.show();

        // Random delay between 0.5–1.5 seconds
        int delayMillis = 500 + RNG.nextInt(1000);
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Jumpscare overlay
        StdDraw.clear(Color.RED);
        StdDraw.picture(world.WIDTH / 2.0, world.HEIGHT / 2.0,
                "jumpscare.png", world.WIDTH, world.HEIGHT);
        StdDraw.show();
        playJumpscareSound();
        SaveFile.resetSave("save.txt");
        // Wait for Q to quit
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toUpperCase(StdDraw.nextKeyTyped());
                if (c == 'Q') {
                    System.exit(0);
                }
            }
        }
    }
    private static void playJumpscareSound() {
        // have to do this in try/catch pair
        try {
            File soundFile = new File("src/jumpscare_sound.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}