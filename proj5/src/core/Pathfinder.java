package core;

import edu.princeton.cs.algs4.*;
import tileengine.TETile;
import tileengine.Tileset;

import java.util.*;

public class Pathfinder {
    private final World world;

    public Pathfinder(World world) {
        this.world = world;
    }
    // 2D position to 1D so we can use dijkstra
    // Similar function to how it's done in percolation
    private int to1D(int x, int y) {
        return y * world.WIDTH + x;
    }

    private int xPos(int id) {
        return id % world.WIDTH;
    }

    private int yPos(int id) {
        return id / world.WIDTH;
    }

    private boolean validEdge(int x, int y) {
        TETile t = world.getWorldTiles()[x][y];
        return t == Tileset.FLOOR || t == Tileset.AVATAR || t == Tileset.MOUNTAIN;
    }

    // Dijkstra from (playerX, playerY) to (enemyX, enemyY)
    public List<int[]> shortestPath(int playerX, int playerY, int enemyX, int enemyY) {
        int w = world.WIDTH;
        int h = world.HEIGHT;
        int V = w * h; // number of vertices

        double[] distTo = new double[V];
        int[] edgeTo = new int[V];
        Arrays.fill(distTo, Double.POSITIVE_INFINITY);

        int s = to1D(playerX, playerY);
        int t = to1D(enemyX, enemyY);

        IndexMinPQ<Double> pq = new IndexMinPQ<>(V);
        distTo[s] = 0.0;
        pq.insert(s, 0.0);

        // all possible direcitons from a given vertex
        int[][] directions = {{1,0}, {-1,0}, {0,1}, {0,-1}};

        while (!pq.isEmpty()) {
            int v = pq.delMin();
            if (v == t) break; // reached target

            // x and y pos of given vertex
            int vx = xPos(v);
            int vy = yPos(v);

            for (int[] d : directions) {
                // adding x and y values in the possible
                // ways you can move from given vertex
                int nx = vx + d[0];
                int ny = vy + d[1];
                if (nx < 0 || nx >= w || ny < 0 || ny >= h) {
                    continue;
                }
                if (!validEdge(nx, ny)) {
                    continue;
                }

                int v1d = to1D(nx, ny);
                double weight = 1.0; // each step costs 1
                double newDist = distTo[v] + weight;

                if (newDist < distTo[v1d]) {
                    distTo[v1d] = newDist;
                    edgeTo[v1d] = v;
                    if (pq.contains(v1d)) pq.decreaseKey(v1d, newDist);
                    else pq.insert(v1d, newDist);
                }
            }
        }
        if (distTo[t] == Double.POSITIVE_INFINITY) {
            return Collections.emptyList(); // no path
        }
        LinkedList<int[]> path = new LinkedList<>();
        for (int v = t; v != s; v = edgeTo[v]) {
            path.addFirst(new int[]{xPos(v), yPos(v)});
        }
        path.addFirst(new int[]{playerX, playerY});
        return path;
    }
}