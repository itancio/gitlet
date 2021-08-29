package gitlet;

// Java program to implement Graph
// with the help of Generics
/** https://www.geeksforgeeks.org/implementing-generic-graph-in-java/ */
/** https://www.geeksforgeeks.org/breadth-first-search-or-bfs-for-a-graph/ */

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static gitlet.Directory.*;
import static gitlet.Helper.createNewFile;
import static gitlet.Utils.*;

class Graph<T> implements Serializable {
    protected static Boolean BIDIRECTIONAL = false;

    // We use Hashmap to store the edges in the graph
    private Map<T, List<T>> adj;
    private int V;

    Graph()
    {
        adj = new HashMap<>();
        V = 0;
    }


    // This function adds a new vertex to the graph
    public void addVertex(T s)
    {
        adj.put(s, new LinkedList<T>());
        V++;
    }

    // This function adds the edge
    // between source to destination
    public void addEdge(T source,
                        T destination,
                        boolean bidirectional)
    {

        if (!adj.containsKey(source))
            addVertex(source);

        if (!adj.containsKey(destination))
            addVertex(destination);

        adj.get(source).add(destination);
        if (bidirectional == true) {
            adj.get(destination).add(source);
        }
    }

    // This function gives the count of vertices
    public void getVertexCount()
    {
        System.out.println("The graph has "
                + adj.keySet().size()
                + " vertex");
    }

    // This function gives the count of edges
    public void getEdgesCount(boolean biDirection)
    {
        int count = 0;
        for (T v : adj.keySet()) {
            count += adj.get(v).size();
        }
        if (biDirection == true) {
            count = count / 2;
        }
        System.out.println("The graph has "
                + count
                + " edges.");
    }

    // This function gives whether
    // a vertex is present or not.
    public void hasVertex(T s)
    {
        if (adj.containsKey(s)) {
            System.out.println("The graph contains "
                    + s + " as a vertex.");
        }
        else {
            System.out.println("The graph does not contain "
                    + s + " as a vertex.");
        }
    }

    // This function gives whether an edge is present or not.
    public void hasEdge(T s, T d)
    {
        if (adj.get(s).contains(d)) {
            System.out.println("The graph has an edge between "
                    + s + " and " + d + ".");
        }
        else {
            System.out.println("The graph has no edge between "
                    + s + " and " + d + ".");
        }
    }

    public Map<T, Boolean>initMarked() {
        Map<T, Boolean> marked = new HashMap<>();
        Set<T> keys = adj.keySet();
        for(T k : keys) {
            marked.put(k, false);
        }
        return marked;
    }

    public Deque<T> bfs(T s) {
        Map<T, Boolean> marked = initMarked();
        Deque<T> paths = new LinkedList<>();

        Queue<T> fringe = new LinkedList<>();

        fringe.add(s);
        marked.put(s, true);

        while(!fringe.isEmpty()) {
            /** Check the adj neighbors of this newly dequeued vertex */
            T v = fringe.poll();

            for (T w : adj.get(v)) {
                System.out.println(adj.get(v).toString());
                if (marked.get(w).equals(false)) {
                    fringe.add(w);
                    marked.put(w, true);
                    paths.add(w);
                }
            }
        }

        return paths;
    }

    // Prints the adjacency list of each vertex.
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        for (T v : adj.keySet()) {
            System.out.print(v);
            builder.append(v.toString() + ": ");
            for (T w : adj.get(v)) {
                System.out.print(w);
                builder.append(w.toString() + " ");
            }
            builder.append("\n");
        }

        return (builder.toString());
    }


//    public static void main(String args[]) {
//
//        // Object of graph is created.
//        Graph<String> g = new Graph<>();
//
////        Commit v0 = new Commit("commit00", null);
////        Commit v1 = new Commit("commit01", sha1(serialize(v0)));
////        Commit v2 = new Commit("commit02", sha1(serialize(v1)));
////        Commit v3 = new Commit("commit03", sha1(serialize(v2)));
////        Commit v4 = new Commit("commit04", sha1(serialize(v3)));
//        String v0 = "v0";
//        String v1 = "v1";
//        String v2 = "v2";
//        String v3 = "v3";
//        String v4 = "v4";
//        String v5 = "v5";
//        String v6 = "v6";
//        String v7 = "v7";
//        String v8 = "v8";
//        String v9 = "v9";
//        String v10 = "v10";
//        String v11 = "v11";
//
//
//        g.addEdge(v1, v5, false);
//        g.addEdge(v2, v1, false);
//        g.addEdge(v3, v2, false);
//        g.addEdge(v4, v1, false);
//        g.addEdge(v0, v4, false);
//        g.addEdge(v7, v4, false);
//        g.addEdge(v6, v3, false);
//        g.addEdge(v6, v0, false);
//        g.addEdge(v8, v6, false);
//        g.addEdge(v9, v8, false);
//        g.addEdge(v10, v9, false);
//        g.addEdge(v10, v11, false);
//        g.addEdge(v11, v7, false);
//
//
////        g.addEdge("id2", "id1", false);
////        g.addEdge("id3", "id4", false);
////        g.addEdge("id4", "id3", false);
////        g.addEdge("id5", "id2", false);
////        g.addEdge("id6", "id5", false);
////        g.addEdge("id7", "id6", false);
////        g.addEdge("id8", "id4", false);
////        g.addEdge("id8", "id7", false);
//
//        // print the graph.
//        System.out.println("Graph:\n"
//                + g.toString());
//
//        // gives the no of vertices in the graph.
//        g.getVertexCount();
//
//        // gives the no of edges in the graph.
//        g.getEdgesCount(true);
//
//        // tells whether the edge is present or not.
//        //g.hasEdge("id3", "id4");
//
//        // tells whether vertex is present or not
//        //g.hasVertex("id5");
//        List<String> path = g.bfs(v9);
//        List<String> path1 = g.bfs(v0);
//
//        System.out.println(path);
//        System.out.println(path1);
//
//    }


}

