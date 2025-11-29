package core;

import tileengine.TERenderer;
import edu.princeton.cs.algs4.StdDraw;
import tileengine.Tileset;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.util.List;
import java.util.Random;

public class Main {
    private static final Random RNG = new Random();
    public static void main(String[] args) {
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
        gameplayLoop(the_world, ter, pf);
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
            World newWorld = new World(seed, 60, 40);
            return newWorld;
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

    public static void gameplayLoop(World world, TERenderer ter, Pathfinder pf) {
        boolean waitingForColon = false;
        boolean togglePath = false;
        long lastEnemyMove = System.currentTimeMillis(); // Move enemy every prespecificed amt of time
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toUpperCase(StdDraw.nextKeyTyped());
                // quit command; need to have the colon and Q combination to quit
                if (waitingForColon && c == 'Q') {
                    SaveFile.saveGame(world, "save.txt");
                    System.exit(0);
                }
                if (c == ':') {
                    waitingForColon = true;
                    continue;
                } else {
                    waitingForColon = false;
                }
                // movement stuff
                if (c == 'W' || c == 'A' || c == 'S' || c == 'D') {
                    world.move(c);
                    // Check if game over (player moved to eneemy)
                    if (world.getPlayerX() == world.enemyX && world.getPlayerY() == world.enemyY) {
                        gameOver(world);
                    }
                }


                if (c == 'P') {
                    togglePath = !togglePath; // Opposite instead of true so we can turn it off
                }
            }

            long now = System.currentTimeMillis();
            if (now - lastEnemyMove >= 1000) { // 1000 ms = 1 second
                moveEnemyTowardsPlayer(world, pf);
                lastEnemyMove = now;
            }

            // After loop so that HUD string isnt constantly being
            // put on top of previous strings
            ter.renderFrame(world.theWorld);

            int x = (int) StdDraw.mouseX();
            int y = (int) StdDraw.mouseY();
            String hudText = world.HUD(x, y);

            StdDraw.setPenColor(Color.WHITE);
            StdDraw.setFont(new Font("Monaco", Font.PLAIN, 14));
            StdDraw.textLeft(1, world.HEIGHT - 1, hudText);


            if (togglePath) {
                int enemyX = world.enemyX;
                int enemyY = world.enemyY;

                List<int[]> path = pf.shortestPath(
                        world.getPlayerX(), world.getPlayerY(),
                        enemyX, enemyY
                );

                StdDraw.setPenColor(Color.RED);
                for (int[] cell : path) {
                    double px = cell[0] + 0.5;
                    double py = cell[1] + 0.5;
                    // Marker
                    StdDraw.filledCircle(px, py, 0.25);
                }
            }
            StdDraw.show();
            StdDraw.pause(10);
            }
        }
    private static void moveEnemyTowardsPlayer(World world, Pathfinder pf) {
        int ex = world.enemyX;
        int ey = world.enemyY;
        int px = world.getPlayerX();
        int py = world.getPlayerY();

        List<int[]> path = pf.shortestPath(ex, ey, px, py);
        if (path.size() <= 1) {
            return; // already on player or no path
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
        } else {
            world.theWorld[newX][newY] = Tileset.MOUNTAIN;
        }
    }
    private static void gameOver(World world) {
        // Normal game over screen first
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 30));
        StdDraw.text(world.WIDTH / 2.0, world.HEIGHT / 2.0 + 2, "GAME OVER");
        StdDraw.setFont(new Font("Monaco", Font.PLAIN, 18));
        StdDraw.text(world.WIDTH / 2.0, world.HEIGHT / 2.0 - 2, "Press Q to quit");
        StdDraw.show();

        // Random delay between 0.1–5 seconds
        int delayMillis = 100 + RNG.nextInt(4000); // 0.1-5 seconds
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Jumpscare overlay
        StdDraw.clear(Color.BLACK);
        StdDraw.picture(world.WIDTH / 2.0, world.HEIGHT / 2.0,
                "jumpscare.png", world.WIDTH, world.HEIGHT);
        StdDraw.show();
        playJumpscareSound();
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
        try {
            File soundFile = new File("src/jumpscare_sound.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start(); // starts playing, method returns immediately
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}