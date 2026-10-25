public class RouteCheck {
    public static void main(String[] args) {
        int[][] edges = {{0,1,3},{0,2,8},{1,2,-7},{2,3,4}};
        int[] parent = {-1, 0, 2, 3, -1};
        System.out.println("reachable 3: " + Algorithms.reconstructRoute(parent, edges, 0, 3));
        System.out.println("source 0:    " + Algorithms.reconstructRoute(parent, edges, 0, 0));
        System.out.println("unreachable 4: " + Algorithms.reconstructRoute(parent, edges, 0, 4));
    }
}