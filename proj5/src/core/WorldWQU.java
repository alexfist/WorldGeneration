package core;
//use this for hallway generation; each object in the WQU represents a room in the world
public class WorldWQU {
    private int[] parent;
    private int[] rank;

    public WorldWQU(int n) {
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            rank[i] = 0;
        }
    }
    public int find(int i) {
        if (parent[i] == i) {
            return i;
        }
        return parent[i] = find(parent[i]);
    }
    public void union(int i, int j) {
        int rootI = find(i);
        int rootJ = find(j);

        if (rootI != rootJ) {
            if (rank[rootI] < rank[rootJ]) {
                parent[rootI] = rootJ;
            } else if (rank[rootJ] < rank[rootI]) {
                parent[rootJ] = rootI;
            } else {
                parent[rootJ] = rootI;
                rank[rootI]++;
            }
        }
    }
    public boolean connected(int i, int j) {
        return find(i) == find(j);
    }
    public boolean allConnected() {
        int root0 = find(0);
        for (int i = 1; i < parent.length; i++) {
            if (find(i) != root0) {
                return false;
            }
        }
        return true;
    }
}
