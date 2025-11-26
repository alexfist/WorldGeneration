package core;

import tileengine.TERenderer;
import edu.princeton.cs.algs4.StdDraw;
import java.awt.Color;
import java.awt.Font;
public class Main {
    public static void main(String[] args) {
        //this gets the world that the user wants from the main menu
        //will either be a saved world or a new world.
        World the_world = chooseOption(getChoice());

        //renders the world.
        TERenderer ter = new TERenderer();
        ter.initialize(the_world.WIDTH, the_world.HEIGHT);
        ter.renderFrame(the_world.theWorld);

        //enters the gameplay loop
        gameplayLoop(the_world, ter);
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
        while(true) {
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
    public static void gameplayLoop(World world, TERenderer ter) {
        boolean waitingForColon = false;
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
                }
            }

            // After loop so that HUD string isnt constantly being
            // put on top of previous strings
            ter.renderFrame(world.theWorld);

            int x =(int) StdDraw.mouseX();
            int y =(int) StdDraw.mouseY();

            String hudText = world.HUD(x, y);

            StdDraw.setPenColor(Color.WHITE);
            StdDraw.setFont(new Font("Monaco", Font.PLAIN, 14));
            StdDraw.textLeft(1, world.HEIGHT - 1, hudText);

            StdDraw.show();

            StdDraw.pause(10);
        }

    }
}
