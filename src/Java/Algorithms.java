import java.util.Map;

/**
 * Implement the six methods and add your route-reconstruction helper.
 * Input extraction is provided; all algorithm state belongs to the current request.
 * See ../../README.md for contracts and Java guidance.
 */
public final class Algorithms {
    /** DSU with path compression and union by size/rank; return states. */
    static Map<String, Object> dsu(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] operations = Input.operations(input);

        int[] parent = new int[n];
        int[] size = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }
        int components = n;
        int largest = n > 0 ? 1 : 0;
        int[][] states = new int[operations.length][2];
        for (int k = 0; k < operations.length; k++) {
            int a = find(parent, operations[k][0]);
            int b = find(parent, operations[k][1]);
            if (a != b) {
                if (size[a] < size[b]) {
                    int t = a;
                    a = b;
                    b = t;
                }
                parent[b] = a;
                size[a] += size[b];
                components--;
                if (size[a] > largest)
                    largest = size[a];
            }
            states[k][0] = components;
            states[k][1] = largest;
        }
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("states", states);
        return result;
    }

    private static int find(int[] parent, int x) {
        while (parent[x] != x) {
            parent[x] = parent[parent[x]];
            x = parent[x];
        }
        return x;
    }

    /** Separate Kruskal implementation; return cost, edge_ids, components. */
    static Map<String, Object> kruskal(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);

        Integer[] order = new Integer[edges.length];
        for (int i = 0; i < order.length; i++)
            order[i] = i;
        java.util.Arrays.sort(order, (x, y) -> {
            int c = Integer.compare(edges[x][2], edges[y][2]);
            return c != 0 ? c : Integer.compare(x, y);
        });

        int[] parent = new int[n];
        int[] size = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }
        long cost = 0;
        int[] selected = new int[Math.max(0, n - 1)];
        int count = 0;
        for (int eid : order) {
            int u = edges[eid][0], v = edges[eid][1];
            int a = find(parent, u), b = find(parent, v);
            if (a == b)
                continue;
            if (size[a] < size[b]) {
                int t = a;
                a = b;
                b = t;
            }
            parent[b] = a;
            size[a] += size[b];
            cost += edges[eid][2];
            selected[count++] = eid;
        }
        int[] ids = java.util.Arrays.copyOf(selected, count);
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("cost", cost);
        result.put("edge_ids", ids);
        result.put("components", countComponents(n, edges, ids));
        return result;
    }

    private static int countComponents(int n, int[][] edges, int[] ids) {
        int[] parent = new int[n];
        for (int i = 0; i < n; i++)
            parent[i] = i;
        for (int eid : ids) {
            int a = find(parent, edges[eid][0]);
            int b = find(parent, edges[eid][1]);
            if (a != b)
                parent[a] = b;
        }
        java.util.HashSet<Integer> roots = new java.util.HashSet<>();
        for (int i = 0; i < n; i++)
            roots.add(find(parent, i));
        return roots.size();
    }

    /** Separate Prim implementation; restart for every unvisited component. */
    static Map<String, Object> prim(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);

        java.util.ArrayList<int[]>[] adj = new java.util.ArrayList[n];
        for (int i = 0; i < n; i++)
            adj[i] = new java.util.ArrayList<>();
        for (int eid = 0; eid < edges.length; eid++) {
            int u = edges[eid][0], v = edges[eid][1], w = edges[eid][2];
            if (u == v)
                continue;
            adj[u].add(new int[]{w, v, eid});
            adj[v].add(new int[]{w, u, eid});
        }
        boolean[] seen = new boolean[n];
        long cost = 0;
        int[] selected = new int[Math.max(0, n - 1)];
        int count = 0;
        java.util.PriorityQueue<int[]> heap = new java.util.PriorityQueue<>((x, y) -> x[0] != y[0] ? Integer.compare(x[0], y[0]) : Integer.compare(x[2], y[2]));
        for (int start = 0; start < n; start++) {
            if (seen[start])
                continue;
            seen[start] = true;
            heap.addAll(adj[start]);
            while (!heap.isEmpty()) {
                int[] top = heap.poll();
                int w = top[0], v = top[1], eid = top[2];
                if (seen[v])
                    continue;
                seen[v] = true;
                cost += w;
                selected[count++] = eid;
                for (int[] next : adj[v])
                    if (!seen[next[1]])
                        heap.add(next);
            }
        }
        int[] ids = java.util.Arrays.copyOf(selected, count);
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("cost", cost);
        result.put("edge_ids", ids);
        result.put("components", countComponents(n, edges, ids));
        return result;
    }

    /** Kosaraju's two DFS passes; return component and exact condensation. */
    static Map<String, Object> scc(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);

        java.util.ArrayList<Integer>[] fwd = new java.util.ArrayList[n];
        java.util.ArrayList<Integer>[] rev = new java.util.ArrayList[n];
        for (int i = 0; i < n; i++) {
            fwd[i] = new java.util.ArrayList<>();
            rev[i] = new java.util.ArrayList<>();
        }
        for (int[] e : edges) {
            fwd[e[0]].add(e[1]);
            rev[e[1]].add(e[0]);
        }
        boolean[] seen = new boolean[n];
        int[] order = new int[n];
        int ordLen = 0;
        for (int s = 0; s < n; s++) {
            if (seen[s])
                continue;
            seen[s] = true;
            java.util.ArrayDeque<int[]> stack = new java.util.ArrayDeque<>();
            stack.push(new int[]{s, 0});
            while (!stack.isEmpty()) {
                int[] top = stack.peek();
                int v = top[0], i = top[1];
                if (i < fwd[v].size()) {
                    int nxt = fwd[v].get(i);
                    top[1] = i + 1;
                    if (!seen[nxt]) {
                        seen[nxt] = true;
                        stack.push(new int[]{nxt, 0});
                    }
                }
                else {
                    order[ordLen++] = v;
                    stack.pop();
                }
            }
        }
        int[] label = new int[n];
        java.util.Arrays.fill(label, -1);
        int next = 0;
        java.util.ArrayDeque<Integer> stack = new java.util.ArrayDeque<>();
        for (int i = ordLen - 1; i >= 0; i--) {
            int s = order[i];
            if (label[s] != -1)
                continue;
            label[s] = next;
            stack.push(s);
            while (!stack.isEmpty()) {
                int v = stack.pop();
                for (int nxt : rev[v]) {
                    if (label[nxt] == -1) {
                        label[nxt] = next;
                        stack.push(nxt);
                    }
                }
            }
            next++;
        }
        java.util.TreeSet<Long> pairsSet = new java.util.TreeSet<>();
        for (int[] e : edges) {
            if (label[e[0]] != label[e[1]]) {
                pairsSet.add((long) label[e[0]] * 1000000L + label[e[1]]);
            }
        }
        int[][] condensation = new int[pairsSet.size()][];
        int idx = 0;
        for (long p : pairsSet) {
            condensation[idx++] = new int[]{(int) (p / 1000000L), (int) (p % 1000000L)};
        }
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("component", label);
        result.put("condensation", condensation);
        return result;
    }

    /** Three-state DFS and reverse finish order; return acyclic and order. */
    static Map<String, Object> topo(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int[][] edges = Input.edges(input);

        java.util.ArrayList<Integer>[] adj = new java.util.ArrayList[n];
        for (int i = 0; i < n; i++)
            adj[i] = new java.util.ArrayList<>();
        for (int[] e : edges)
            adj[e[0]].add(e[1]);
        byte[] state = new byte[n];
        int[] order = new int[n];
        int ordLen = 0;
        for (int s = 0; s < n; s++) {
            if (state[s] != 0)
                continue;
            state[s] = 1;
            java.util.ArrayDeque<int[]> stack = new java.util.ArrayDeque<>();
            stack.push(new int[]{s, 0});
            while (!stack.isEmpty()) {
                int[] top = stack.peek();
                int v = top[0], i = top[1];
                if (i < adj[v].size()) {
                    int nxt = adj[v].get(i);
                    top[1] = i + 1;
                    if (state[nxt] == 1) {
                        Map<String, Object> r = new java.util.LinkedHashMap<>();
                        r.put("acyclic", false);
                        r.put("order", new int[0]);
                        return r;
                    }
                    if (state[nxt] == 0) {
                        state[nxt] = 1;
                        stack.push(new int[]{nxt, 0});
                    }
                }
                else {
                    state[v] = 2;
                    order[ordLen++] = v;
                    stack.pop();
                }
            }
        }
        int[] finalOrder = new int[ordLen];
        for (int i = 0; i < ordLen; i++)
            finalOrder[i] = order[ordLen - 1 - i];
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("acyclic", true);
        result.put("order", finalOrder);
        return result;
    }

    private static boolean topoOrder(int n, int[][] edges, int[] orderOut) {
        java.util.ArrayList<Integer>[] adj = new java.util.ArrayList[n];
        for (int i = 0; i < n; i++)
            adj[i] = new java.util.ArrayList<>();
        for (int[] e : edges)
            adj[e[0]].add(e[1]);
        byte[] state = new byte[n];
        int[] reversed = new int[n];
        int ordLen = 0;
        for (int s = 0; s < n; s++) {
            if (state[s] != 0)
                continue;
            state[s] = 1;
            java.util.ArrayDeque<int[]> stack = new java.util.ArrayDeque<>();
            stack.push(new int[]{s, 0});
            while (!stack.isEmpty()) {
                int[] top = stack.peek();
                int v = top[0], i = top[1];
                if (i < adj[v].size()) {
                    int nxt = adj[v].get(i);
                    top[1] = i + 1;
                    if (state[nxt] == 1)
                        return false;
                    if (state[nxt] == 0) {
                        state[nxt] = 1;
                        stack.push(new int[]{nxt, 0});
                    }
                }
                else {
                    state[v] = 2;
                    reversed[ordLen++] = v;
                    stack.pop();
                }
            }
        }
        for (int i = 0; i < ordLen; i++)
            orderOut[i] = reversed[ordLen - 1 - i];
        return true;
    }

    /** Whole-graph cycle check and DAG relaxation; return acyclic, distance, parent_edge. */
    static Map<String, Object> dag(Map<String, Object> input) {
        int n = Input.vertexCount(input);
        int source = Input.source(input);
        int[][] edges = Input.edges(input);

        // Add and test your own route-reconstruction helper.
        int[][] plainEdges = new int[edges.length][2];
        for (int i = 0; i < edges.length; i++) {
            plainEdges[i][0] = edges[i][0];
            plainEdges[i][1] = edges[i][1];
        }
        int[] order = new int[n];
        if (!topoOrder(n, plainEdges, order)) {
            Map<String, Object> r = new java.util.LinkedHashMap<>();
            r.put("acyclic", false);
            r.put("distance", new int[0]);
            r.put("parent_edge", new int[0]);
            return r;
        }
        java.util.ArrayList<int[]>[] adj = new java.util.ArrayList[n];
        for (int i = 0; i < n; i++)
            adj[i] = new java.util.ArrayList<>();
        for (int eid = 0; eid < edges.length; eid++)
            adj[edges[eid][0]].add(new int[]{edges[eid][1], edges[eid][2], eid});
        Long[] dist = new Long[n];
        int[] parent = new int[n];
        java.util.Arrays.fill(parent, -1);
        dist[source] = 0L;
        for (int v : order) {
            if (dist[v] == null)
                continue;
            for (int[] e : adj[v]) {
                int nv = e[0], w = e[1], eid = e[2];
                if (nv == source)
                    continue;
                long cand = dist[v] + w;
                if (dist[nv] == null || cand < dist[nv]) {
                    dist[nv] = cand;
                    parent[nv] = eid;
                }
            }
        }
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("acyclic", true);
        result.put("distance", dist);
        result.put("parent_edge", parent);
        return result;
    }

    static java.util.List<Integer> reconstructRoute(int[] parentEdge, int[][] edges, int source, int target) {
        java.util.List<Integer> path = new java.util.ArrayList<>();
        if (target == source) {
            path.add(source);
            return path;
        }
        if (parentEdge[target] == -1)
            return path;
        int cur = target;
        while (cur != -1) {
            path.add(cur);
            if (cur == source)
                break;
            cur = edges[parentEdge[cur]][0];
        }
        java.util.Collections.reverse(path);
        return path;
    }
}
