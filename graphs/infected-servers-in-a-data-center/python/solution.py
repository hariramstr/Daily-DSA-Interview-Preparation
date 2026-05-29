```python
"""
Title: Infected Servers in a Data Center
Difficulty: Medium
Topic: Graphs

Problem Description:
You are managing a data center with `n` servers numbered from `0` to `n - 1`. The servers
are connected by bidirectional network cables represented as a list of edges, where
`edges[i] = [u, v]` means server `u` and server `v` are directly connected.

At time `0`, a subset of servers listed in `initial` become infected with a virus. Every
minute, each infected server spreads the virus to all of its directly connected neighbors.
However, your team can **quarantine exactly one server at time 0** (before any spreading
occurs) to prevent it from infecting others or receiving the infection.

Your goal is to **minimize the total number of infected servers** after the virus has fully
spread. Return the index of the server you should quarantine. If multiple choices result in
the same minimum infection count, return the **smallest index** among them.

Constraints:
- 2 <= n <= 10^4
- 0 <= edges.length <= 2 * 10^4
- edges[i].length == 2
- 0 <= edges[i][0], edges[i][1] < n
- edges[i][0] != edges[i][1]
- 1 <= initial.length <= n
- 0 <= initial[i] < n
- All values in `initial` are distinct.
"""

from typing import List, Dict, Set
from collections import defaultdict, deque


class Solution:
    def minMalwareSpread(self, n: int, edges: List[List[int]], initial: List[int]) -> int:
        """
        Find the server to quarantine to minimize total infected servers.

        The key insight is:
        1. Build connected components of the graph.
        2. For each component, count how many initially infected servers it contains.
        3. If a component has exactly ONE initially infected server, quarantining that
           server saves the entire component from infection.
        4. Among all such "saveable" components, pick the one with the largest size
           (most servers saved). If tied, pick the smallest server index.
        5. If no component has exactly one initial server (all components have 2+),
           quarantining any initial server saves nothing in terms of components —
           the virus still spreads through all components. In this case, return the
           smallest index in `initial` (we can't do better, so minimize index).

        Args:
            n: Number of servers (0 to n-1)
            edges: List of [u, v] pairs representing bidirectional connections
            initial: List of initially infected server indices

        Returns:
            Index of the server to quarantine to minimize total infections.

        Time Complexity: O(n + E) where E is the number of edges
            - Building adjacency list: O(E)
            - Union-Find operations: nearly O(n + E) with path compression
            - Iterating over components: O(n)

        Space Complexity: O(n + E)
            - Adjacency list or Union-Find parent/rank arrays: O(n)
            - Component size and initial count maps: O(n)
        """

        # -----------------------------------------------------------------------
        # STEP 1: Build Union-Find (Disjoint Set Union) data structure
        # -----------------------------------------------------------------------
        # Union-Find helps us efficiently group servers into connected components.
        # Each server starts as its own component (parent[i] = i).
        # We use "union by rank" to keep trees shallow, and "path compression"
        # in find() to speed up future lookups.

        parent = list(range(n))  # parent[i] = representative (root) of i's component
        rank = [0] * n           # rank[i] = approximate depth of tree rooted at i

        def find(x: int) -> int:
            """Find the root/representative of x's component with path compression."""
            # Path compression: make every node on the path point directly to root
            if parent[x] != x:
                parent[x] = find(parent[x])
            return parent[x]

        def union(x: int, y: int) -> None:
            """Merge the components containing x and y."""
            rx, ry = find(x), find(y)
            if rx == ry:
                return  # Already in the same component
            # Union by rank: attach smaller tree under larger tree
            if rank[rx] < rank[ry]:
                rx, ry = ry, rx  # Ensure rx has >= rank
            parent[ry] = rx      # ry's tree goes under rx
            if rank[rx] == rank[ry]:
                rank[rx] += 1    # Only increase rank when merging equal-rank trees

        # -----------------------------------------------------------------------
        # STEP 2: Process all edges to build connected components
        # -----------------------------------------------------------------------
        # For each edge [u, v], union u and v into the same component.
        # After processing all edges, servers in the same component share the same root.

        for u, v in edges:
            union(u, v)

        # -----------------------------------------------------------------------
        # STEP 3: Count the size of each component
        # -----------------------------------------------------------------------
        # comp_size[root] = number of servers in the component with that root.
        # We iterate over all servers and find their root to tally sizes.

        comp_size: Dict[int, int] = defaultdict(int)
        for i in range(n):
            root = find(i)
            comp_size[root] += 1

        # -----------------------------------------------------------------------
        # STEP 4: Count how many initially infected servers are in each component
        # -----------------------------------------------------------------------
        # comp_initial_count[root] = number of initially infected servers in that component.
        # This tells us whether a component has 1 or more initial infection sources.

        comp_initial_count: Dict[int, int] = defaultdict(int)
        for server in initial:
            root = find(server)
            comp_initial_count[root] += 1

        # -----------------------------------------------------------------------
        # STEP 5: Determine the best server to quarantine
        # -----------------------------------------------------------------------
        # Strategy:
        # - We can only "save" a component if it has EXACTLY ONE initially infected server.
        #   If we quarantine that one server, the entire component avoids infection.
        # - Among all such saveable components, we want to save the LARGEST one
        #   (maximize servers saved = minimize total infected).
        # - If two components have the same size, we pick the one whose initial server
        #   has the SMALLEST index (tie-breaking rule).
        # - If NO component has exactly one initial server, we can't save any component
        #   by quarantining. In this case, return the smallest index in `initial`.

        # Sort initial so that when we iterate, we naturally encounter smaller indices first.
        # This helps with tie-breaking (smallest index wins among equal savings).
        initial_sorted = sorted(initial)

        best_server = initial_sorted[0]   # Default: smallest initial index (worst case)
        best_size = -1                    # Size of the component we'd save by quarantining best_server

        for server in initial_sorted:
            root = find(server)
            count_in_comp = comp_initial_count[root]
            size_of_comp = comp_size[root]

            # Only consider this server if its component has exactly one initial server
            # (meaning quarantining it would save the entire component)
            if count_in_comp == 1:
                # We save `size_of_comp` servers by quarantining this server
                if size_of_comp > best_size:
                    # This is a better choice (saves more servers)
                    best_size = size_of_comp
                    best_server = server
                # If size_of_comp == best_size, we don't update because initial_sorted
                # is sorted, so the first one we found already has the smaller index.

        # -----------------------------------------------------------------------
        # STEP 6: Return the result
        # -----------------------------------------------------------------------
        # If best_size is still -1, no component had exactly one initial server,
        # so best_server remains the smallest index in initial (set as default above).
        # Otherwise, best_server is the optimal quarantine choice.

        return best_server


# -------------------------------------------------------------------------------
# VERIFICATION / TRACING THROUGH EXAMPLES
# -------------------------------------------------------------------------------
# Example 1:
#   n=6, edges=[[0,1],[0,2],[1,3],[2,3],[3,4],[4,5]], initial=[0,3]
#
#   After union-find:
#   All servers 0-5 are in ONE component (they're all connected).
#   comp_size[root] = 6
#   comp_initial_count[root] = 2 (both 0 and 3 are initial)
#
#   Since the only component has count=2 (not 1), no server can "save" a component alone.
#   Wait — but the expected answer is 3, which saves {3,4,5} from being infected by 3.
#
#   Hmm, let me reconsider. The problem says quarantine removes the server entirely
#   (it can't infect OR be infected). So if we quarantine server 3:
#   - Server 0 spreads to {1, 2}, and 1 spreads to 3's neighbors but 3 is quarantined.
#   - Actually 1 connects to 3 (quarantined), so 1 can't spread through 3.
#   - 2 connects to 3 (quarantined), same.
#   - So infected = {0, 1, 2} = 3 servers.
#
#   If we quarantine server 0:
#   - Server 3 spreads to {1, 2, 4}, then {5}, etc.
#   - Infected = {3, 1, 2, 4, 5} = 5 servers.
#
#   So the graph is NOT one component after quarantine — quarantining 3 disconnects it!
#   
#   The standard approach for this problem (LeetCode 924) is:
#   - Find connected components WITHOUT the quarantined server.
#   - But that's O(n * (n+E)) if done naively.
#
#   The efficient approach:
#   - Build components of the FULL graph.
#   - A component with exactly 1 initial server: quarantining that server saves the whole component.
#   - A component with 2+ initial servers: no matter which one we quarantine, the rest still infect it.
#
#   BUT in Example 1, all servers are in one component with 2 initial servers.
#   The answer should still be 3 (saves 3 servers vs 0 saves 5 servers).
#
#   Wait, I need to re-examine. Let me re-read the problem.
#   
#   Actually the standard LeetCode 924 solution works differently:
#   The graph has one big component. Both 0 and 3 are initial.
#   Quarantining 3: the remaining initial servers = {0}. 
#     0's component (without 3) = {0,1,2} (size 3).
#   Quarantining 0: the remaining initial servers = {3}.
#     3's component (without 0) = {1,2,3,4,5} (size 5).
#   
#   So we need to simulate removing each initial server and compute infection spread.
#   
#   For the efficient O(n+E) solution, the key insight is:
#   - Build components of the full graph.
#   - For each component, if it has exactly 1 initial server, quarantining that server
#     saves `component_size` servers (the whole component stays clean).
#   - For components with 2+ initial servers, quarantining any one of them doesn't help
#     because the others still infect the component.
#   
#   In Example 1, the whole graph is one component with 2 initial servers.
#   Neither quarantine "saves" the component entirely. But we still need to pick one.
#   The answer is the smallest index initial server (default = 0)... but expected is 3!
#   
#   Hmm, that contradicts. Let me re-examine Example 1 more carefully.
#
#   Oh wait — I think I'm wrong about the graph being fully connected. Let me re-check.
#   edges = [[0,1],[0,2],[1,3],[2,3],[3,4],[4,5]]
#   0-1, 0-2, 1-3, 2-3, 3-4, 4-5
#   Yes, all 6 nodes are connected. One component.
#
#   So with the standard approach, since the component has 2 initial servers,
#   we'd return the smallest index = 0. But expected is 3.
#
#   This means the standard LeetCode 924 approach doesn't directly apply here,
#   OR I'm misunderstanding the problem.
#
#   Let me re-read: "quarantine exactly one server at time 0 to prevent it from
#   infecting others or receiving the infection."
#
#   So the quarantined server is completely removed. We need to find which removal
#   minimizes total infected count.
#
#   For Example 1 with one big component:
#   - Remove 0: graph becomes {1,2,3,4,5} with initial={3}. BFS from 3 infects all 5.
#   - Remove 3: graph becomes {0,1,2,4,5} with initial={0}. 
#     0 connects to 1,2. 1 connects to 0,3(removed). 2 connects to 0,3(removed).
#     4 connects to 3(removed),5. 5 connects to 4.
#     So from 0: infects {0,1,2} = 3 servers. {4,5} are unreachable from 0.
#     Total infected = 3.
#
#   So removing 3 gives 3 infected, removing 0 gives 5 infected. Answer = 3. ✓
#
#   The key: when we remove a server, the graph may SPLIT into multiple components,
#   and only components reachable from remaining initial servers get infected.
#
#   So the correct approach is: for each candidate in initial, simulate removing it,
#   do BFS/DFS from remaining initial servers, count infected nodes.
#   Pick the candidate that minimizes this count.
#
#   With n up to 10^4 and |initial| up to n, naive O(|initial| * (n+E)) could be
#   O(n * (n+E)) = O(10^4 * 3*10^4) = O(3*10^8) which might be too slow.
#
#   BUT: we only need to try each server in `initial` as the quarantine candidate.
#   |initial| <= n = 10^4, and each BFS is O(n+E) = O(3*10^4).
#   Total: O(10^4 * 3*10^4) = O(3*10^8). Might be tight but let's try.
#
#   Actually for this problem size, O(|initial| * (n+E)) should be acceptable.
#   Let's implement the straightforward simulation approach.
# -------------------------------------------------------------------------------


class SolutionSimulation:
    def minMalwareSpread(self, n: int, edges: List[List[int]], initial: List[int]) -> int:
        """
        Find the server to quarantine to minimize total infected servers.

        Approach: For each server in `initial`, simulate removing it and count
        how many servers get infected by the remaining initial servers via BFS.
        Pick the removal that minimizes infection count. Ties broken by smallest index.

        Args:
            n: Number of servers (0 to n-1)
            edges: List of [u, v] pairs representing bidirectional connections
            initial: List of initially infected server indices

        Returns:
            Index of the server to quarantine to minimize total infections.

        Time Complexity: O(|initial| * (n + E))
            - For each candidate in initial, we do a BFS over the graph.
            - |initial| <= n, so worst case O(n * (n + E)).
            - With n=10^4 and E=2*10^4, this is about 3*10^8 — acceptable for most judges.

        Space Complexity: O(n + E)
            - Adjacency list: O(n + E)
            - BFS visited set: O(n)
        """

        # -----------------------------------------------------------------------
        # STEP 1: Build adjacency list for the graph
        #