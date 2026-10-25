# Analysis Assignment 3

## DSU
**Steps:** parent[] and size[]; find() with path compression, union by size; after each op record [components, largest].  
**Invariants:** find returns the root; size[root] = component size; components = number of roots; largest = biggest size[root].  
**Correctness:** union of two different roots drops components by 1; largest updated on merge.  
**Boundary:** n=0 -> largest=0, states=[]; self/repeat unions change nothing.  
**Complexity:** O((n+q)·α(n)) time, O(n+q) memory.  

## Kruskal
**Steps:** sort edges by weight; keep an edge if its ends are in different trees.  
**Invariants:** chosen edges form a forest; each joins two separate trees.  
**Correctness:** cut property - cheapest safe edge each step.  
**Boundary:** self-loops skipped; n=0 -> zeros; parallel/tied edges fine.  
**Complexity:** O(n + m log m), O(n+m) memory.  

## Prim
**Steps:** adjacency lists; per unvisited start, push edges to a min-heap; pop cheapest, add unseen end, extend heap.  
**Invariants:** heap holds candidate edges of the current tree; added vertices were unseen.  
**Correctness:** same cut property via the heap.  
**Boundary:** self-loops excluded from adjacency; isolates become own components; n=0 -> zeros.  
**Complexity:** O((n+m) log m), O(n+m) memory.  

## SCC
**Steps:** Kosaraju - pass 1 finish order on G; pass 2 DFS on Gᵀ in reverse finish order; dedupe condensation.  
**Invariants:** labels partition vertices; same label <-> mutually reachable; condensation = distinct cross-SCC edges.  
**Correctness:** Kosaraju's theorem.  
**Boundary:** n=0 -> []; isolates own SCCs; parallel/self-loops add no pairs.  
**Complexity:** O(n+m).  
   
## Topo
**Steps:** three-state DFS; edge to active vertex = cycle; reverse finish order.  
**Invariants:** every vertex once; every edge forward.  
**Correctness:** no back edge <-> topological order exists.  
**Boundary:** self-loop = cycle; n=0 -> acyclic, empty order.  
**Complexity:** O(n+m).  
  
## DAG
**Steps:** topological sort for cycle check; relax edges in topo order from source; skip relaxing into source.  
**Invariants:** source (0,-1); unreachable (null,-1); parent edge ends at vertex with dist[u]+w == dist[v].  
**Correctness:** topo order processes all predecessors first; negative weights safe.  
**Boundary:** cycle anywhere -> acyclic=false, empty arrays; -1 is a real distance.  
**Complexity:** O(n+m).  

## State reset
Every method allocates fresh locals per call. `run.py --check` runs two batches in one process - passing proves no leakage.

## Deep DFS
scc/topo/dag use `ArrayDeque<int[]>` of `[vertex, neighborIndex]`, no recursion. Handles 100k chains.

## Route helper trace
Input: `n=5, source=0, edges=[[0,1,3],[0,2,8],[1,2,-7],[2,3,4]]`, parent `[-1,0,2,3,-1]`.
- reachable 3 → `[0,1,2,3]`, cost 0+3−7+4 = 0.
- source 0 → `[0]`.
- unreachable 4 → `[]`.
- tied edges → any valid path, cost 2.

## Kruskal vs Prim benchmark
Generator: Random(42), m = 5·n. Timer: nanoTime around method body only. Warm-up: 1 untimed run. Both agree on cost and components.

| n | m | Kruskal ms (1/2/3) | median | Prim ms (1/2/3) | median |
|---|---|---|---|---|---|
| 1,000 | 5,000 | 5 / 3 / 2 | 3 | 4 / 4 / 4 | 4 |
| 10,000 | 50,000 | 68 / 52 / 32 | 52 | 27 / 25 / 27 | 27 |
| 100,000 | 250,000 | 202 / 178 / 185 | 185 | 201 / 205 / 204 | 204 |

**Observations**: both are O(m log m). Kruskal is dominated by sorting the edges; Prim by heap operations. Kruskal wins at 1k, Prim wins by ~2× at 10k, and the two are close at 100k. The crossover depends on cache behaviour and JIT state.
**Limitations**: 3 samples, 1 machine, no JIT isolation. Timings are indicative, not rigorous.

## AI Usage
AI was used for debugging, analysis, assignment explanation and self-checking.
