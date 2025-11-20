package core;

import tileengine.TERenderer;

public class Main {
    public static void main(String[] args) {
        World newWorld = new World(5766286691213492728L, 50, 50);
        TERenderer ter = new TERenderer();
        ter.initialize(newWorld.WIDTH, newWorld.HEIGHT);
        ter.renderFrame(newWorld.theWorld);
    }
}
