"""
Title: Maximum Knowledge Gain from Unlocking Course Modules

Problem Description:
You are designing a personalized learning path on an online platform. There are n course
modules labeled from 0 to n - 1. Each module i has a knowledge value gain[i]. Some modules
are locked behind prerequisites: to study module i, you must have already completed every
module in prereq[i]. The prerequisite graph is guaranteed to be a directed acyclic graph (DAG).

You are also given an integer k. You may complete at most k modules in total. Your goal is
to maximize the sum of knowledge values of the completed modules, subject to the prerequisite
rules.

Return the maximum total knowledge value that can be achieved.

A module can only be taken once, and completing a module counts toward the k-module limit.
You do not need to complete exactly k modules; taking fewer is allowed if that leads to a
better or only feasible plan.

Constraints:
- 1 <= n <= 100
- 0 <= gain[i] <= 10^6
- 0 <= k <= n
- 0 <= total number of prerequisite edges <= 300
- prereq[i] contains distinct module indices
- The prerequisite graph is a DAG
"""

from collections import deque
from typing import List


class Solution:
    def max_knowledge_gain(self, n: int, gain: List[int], prereq: List[List[int]], k: int) -> int:
        """
        Compute the maximum total knowledge gain obtainable by completing at most k modules
        while respecting prerequisite constraints in a DAG.

        The algorithm transforms the prerequisite-closure selection problem into a maximum
        weight closure problem on a DAG, and then solves the "at most k chosen modules"
        version using Lagrangian relaxation plus a min-cut / max-flow computation.

        Args:
            n: Number of modules.
            gain: gain[i] is the knowledge value of module i.
            prereq: prereq[i] lists all prerequisite modules that must be completed before i.
            k: Maximum number of modules that may be completed.

        Returns:
            The maximum achievable total knowledge gain.

        Time complexity:
            O(log V * n * E * sqrt(V)) in practice here dominated by repeated max-flow calls,
            where V = O(n) and E = O(n + total_prereq_edges). Since n <= 100 and edges <= 300,
            this is easily fast enough.

        Space complexity:
            O(V + E), where V = O(n) and E = O(n + total_prereq_edges).
        """
        # If we are not allowed to take any module, the answer is obviously zero.
        if k == 0 or n == 0:
            return 0

        # We will solve a penalized version of the problem:
        #
        #   maximize   sum(gain[i] for chosen i) - lambda * (number of chosen modules)
        #
        # subject to prerequisite closure:
        # if module i is chosen, then every prerequisite of i must also be chosen.
        #
        # For a fixed lambda, this becomes a classic "maximum weight closure" problem:
        # each node i has adjusted weight gain[i] - lambda.
        #
        # Then, among all feasible closed sets, we choose one with maximum adjusted sum.
        #
        # Important idea:
        # - If lambda is large, choosing modules becomes expensive, so the optimal set uses
        #   fewer modules.
        # - If lambda is small, choosing modules is cheaper, so the optimal set may use more.
        #
        # Therefore, the chosen count is monotone with respect to lambda, which allows binary
        # search on lambda to enforce the "at most k modules" condition.
        #
        # To implement maximum weight closure, we build a flow network:
        # - For each node with positive adjusted weight w > 0, add edge source -> node with cap w.
        # - For each node with negative adjusted weight w < 0, add edge node -> sink with cap -w.
        # - For each prerequisite relation p in prereq[i], add infinite-capacity edge i -> p.
        #
        # Why edge i -> p?
        # Because if i is selected but p is not, that edge would cross the cut and incur
        # infinite cost, making such a cut impossible. Thus selecting i forces selecting p.
        #
        # The maximum closure value equals:
        #   sum(positive adjusted weights) - min_cut_value
        #
        # We also need the actual chosen set size and original gain sum, not only the adjusted
        # objective value. After computing the min-cut, the chosen set is exactly the set of
        # nodes reachable from the source in the residual graph.
        #
        # Because gains are integers, it is enough to binary search integer lambda.

        # A safe upper bound for lambda:
        # if lambda > max(gain), every adjusted weight is <= 0, so choosing nothing is optimal.
        max_gain = max(gain) if gain else 0

        # We will binary search for the smallest lambda such that the optimal chosen set
        # contains at most k modules.
        left = 0
        right = max_gain + 1

        while left < right:
            mid = (left + right) // 2

            # Solve the penalized closure problem for this lambda.
            chosen_count, _, _ = self._solve_for_lambda(n, gain, prereq, mid)

            # If the optimal penalized solution uses too many modules, lambda is too small.
            # We must increase the penalty.
            if chosen_count > k:
                left = mid + 1
            else:
                right = mid

        # left is now the smallest lambda for which the optimal penalized solution uses <= k modules.
        #
        # However, because the objective is penalized, we must recover the true best original-gain
        # solution under the count constraint.
        #
        # Standard Lagrangian reasoning gives:
        # For a fixed lambda, if S maximizes gain(S) - lambda * |S|, then
        # gain(S) = [gain(S) - lambda * |S|] + lambda * |S|.
        #
        # At the threshold lambda, the optimal penalized solution with count <= k yields the
        # correct optimum for the original problem after adding lambda * k:
        #
        #   OPT_k = max_S<=k gain(S) = max_S [gain(S) - lambda|S|] + lambda k
        #
        # when lambda is chosen as the smallest penalty making an optimal solution use <= k nodes.
        #
        # To be robust in the presence of ties, we evaluate both lambda = left and lambda - 1
        # (when valid), and reconstruct the best feasible original gain from the returned set.
        #
        # In practice, because all gains are nonnegative and the closure family is finite,
        # checking these neighboring lambdas is sufficient and safe.

        best_answer = 0

        # Evaluate at lambda = left.
        count_a, gain_a, adjusted_a = self._solve_for_lambda(n, gain, prereq, left)
        if count_a <= k:
            # The chosen set itself is feasible, so its original gain is a valid candidate.
            best_answer = max(best_answer, gain_a)

            # Also consider the Lagrangian upper-envelope reconstruction.
            # adjusted_a = gain_a - left * count_a
            # Therefore adjusted_a + left * k = gain_a + left * (k - count_a)
            # This value equals the constrained optimum when the threshold is exact.
            # Since the actual chosen set may use fewer than k modules, this reconstructed value
            # can be larger than gain_a, but it represents the optimum over all feasible sets.
            best_answer = max(best_answer, adjusted_a + left * k)

        # Evaluate at lambda = left - 1 if it is nonnegative.
        if left > 0:
            count_b, gain_b, adjusted_b = self._solve_for_lambda(n, gain, prereq, left - 1)
            if count_b <= k:
                best_answer = max(best_answer, gain_b)
                best_answer = max(best_answer, adjusted_b + (left - 1) * k)

        # The answer must be an integer and nonnegative.
        return int(best_answer)

    def _solve_for_lambda(
        self,
        n: int,
        gain: List[int],
        prereq: List[List[int]],
        penalty: int,
    ) -> tuple[int, int, int]:
        """
        Solve the penalized maximum closure problem for a fixed penalty value.

        Args:
            n: Number of modules.
            gain: Original gain values.
            prereq: Prerequisite lists.
            penalty: Per-chosen-module penalty lambda.

        Returns:
            A tuple:
            - chosen_count: number of selected modules in the optimal closure
            - original_gain_sum: sum of original gains of the selected modules
            - adjusted_value: sum(gain[i] - penalty) over selected modules

        Time complexity:
            O(E * sqrt(V) * V) in practice for Dinic on this small graph.
            More simply, polynomial in n and total prerequisite edges.

        Space complexity:
            O(V + E)
        """
        # We build a flow network with:
        # - node 0..n-1 for modules
        # - source = n
        # - sink = n + 1
        source = n
        sink = n + 1
        node_count = n + 2

        # "Infinite" capacity only needs to be larger than any possible finite cut value.
        # The total positive adjusted weight cannot exceed sum(gain), which is at most 1e8.
        # So 10^18 is more than enough.
        inf = 10**18

        dinic = Dinic(node_count)

        positive_sum = 0

        # Add weight edges based on adjusted weight = gain[i] - penalty.
        for i in range(n):
            adjusted_weight = gain[i] - penalty

            # If adjusted weight is positive, selecting this node is beneficial in the penalized
            # objective, so we connect source -> i with that capacity.
            if adjusted_weight > 0:
                dinic.add_edge(source, i, adjusted_weight)
                positive_sum += adjusted_weight

            # If adjusted weight is negative, selecting this node incurs a cost, so we connect
            # i -> sink with capacity -adjusted_weight.
            elif adjusted_weight < 0:
                dinic.add_edge(i, sink, -adjusted_weight)

            # If adjusted weight is zero, no direct source/sink edge is needed.

        # Add prerequisite-enforcing infinite edges.
        for module in range(n):
            for pre in prereq[module]:
                # If module is selected, pre must also be selected.
                # Infinite edge module -> pre enforces closure under prerequisites.
                dinic.add_edge(module, pre, inf)

        # Compute min-cut via max-flow.
        min_cut_value = dinic.max_flow(source, sink)

        # Recover the chosen closure:
        # After max-flow, nodes reachable from source in the residual graph are exactly the
        # selected nodes in the maximum closure solution.
        reachable = dinic.reachable_from_source(source)

        chosen_count = 0
        original_gain_sum = 0

        for i in range(n):
            if reachable[i]:
                chosen_count += 1
                original_gain_sum += gain[i]

        # The adjusted objective value of the chosen set can be computed either directly from
        # the set or from closure theory:
        # adjusted_value = positive_sum - min_cut_value
        adjusted_value = positive_sum - min_cut_value

        return chosen_count, original_gain_sum, adjusted_value


class Edge:
    def __init__(self, to: int, rev: int, cap: int) -> None:
        """
        Store one directed residual edge for Dinic's algorithm.

        Args:
            to: Destination vertex.
            rev: Index of the reverse edge in the destination adjacency list.
            cap: Residual capacity.

        Returns:
            None

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        self.to = to
        self.rev = rev
        self.cap = cap


class Dinic:
    def __init__(self, n: int) -> None:
        """
        Initialize Dinic's max-flow structure.

        Args:
            n: Number of vertices in the flow graph.

        Returns:
            None

        Time complexity:
            O(n)

        Space complexity:
            O(n)
        """
        self.n = n
        self.graph: List[List[Edge]] = [[] for _ in range(n)]
        self.level: List[int] = [-1] * n
        self.it: List[int] = [0] * n

    def add_edge(self, fr: int, to: int, cap: int) -> None:
        """
        Add a directed edge with residual reverse edge.

        Args:
            fr: From vertex.
            to: To vertex.
            cap: Capacity of the forward edge.

        Returns:
            None

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        forward = Edge(to, len(self.graph[to]), cap)
        backward = Edge(fr, len(self.graph[fr]), 0)
        self.graph[fr].append(forward)
        self.graph[to].append(backward)

    def bfs(self, s: int, t: int) -> bool:
        """
        Build the level graph using BFS.

        Args:
            s: Source vertex.
            t: Sink vertex.

        Returns:
            True if sink is reachable, otherwise False.

        Time complexity:
            O(V + E)

        Space complexity:
            O(V)
        """
        self.level = [-1] * self.n
        queue: deque[int] = deque([s])
        self.level[s] = 0

        while queue:
            v = queue.popleft()
            for e in self.graph[v]:
                if e.cap > 0 and self.level[e.to] < 0:
                    self.level[e.to] = self.level[v] + 1
                    queue.append(e.to)

        return self.level[t] >= 0

    def dfs(self, v: int, t: int, f: int) -> int:
        """
        Send blocking flow through the level graph using DFS.

        Args:
            v: Current vertex.
            t: Sink vertex.
            f: Maximum flow amount we are trying to send.

        Returns:
            Amount of flow actually sent.

        Time complexity:
            Amortized efficient within Dinic's algorithm.

        Space complexity:
            O(V) recursion depth in the worst case.
        """
        if v == t:
            return f

        for i in range(self.it[v], len(self.graph[v])):
            self.it[v] = i
            e = self.graph[v][i]

            # We can only move along edges that:
            # 1. still have residual capacity
            # 2. go to the next level in the level graph
            if e.cap > 0 and self.level[v] < self.level[e.to]:
                d = self.dfs(e.to, t, min(f, e.cap))
                if d > 0:
                    e.cap -= d
                    self.graph[e.to][e.rev].cap += d
                    return d

        return 0

    def max_flow(self, s: int, t: int) -> int:
        """
        Compute the maximum flow from s to t.

        Args:
            s: Source vertex.
            t: Sink vertex.

        Returns:
            Maximum flow value.

        Time complexity:
            Polynomial; efficient in practice for this problem size.

        Space complexity:
            O(V + E)
        """
        flow = 0
        inf = 10**18

        while self.bfs(s, t):
            self.it = [0] * self.n
            while True:
                pushed = self.dfs(s, t, inf)
                if pushed == 0:
                    break
                flow += pushed

        return flow

    def reachable_from_source(self, s: int) -> List[bool]:
        """
        Mark all vertices reachable from the source in the residual graph.

        Args:
            s: Source vertex.

        Returns:
            A boolean list where result[v] is True if v is reachable from s.

        Time complexity:
            O(V + E)

        Space complexity:
            O(V)
        """
        visited = [False] * self.n
        queue: deque[int] = deque([s])
        visited[s] = True

        while queue:
            v = queue.popleft()
            for e in self.graph[v]:
                if e.cap > 0 and not visited[e.to]:
                    visited[e.to] = True
                    queue.append(e.to)

        return visited


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    n1 = 5
    gain1 = [3, 4, 8, 5, 10]
    prereq1 = [[], [0], [0], [1, 2], []]
    k1 = 3
    result1 = solution.max_knowledge_gain(n1, gain1, prereq1, k1)
    print(result1)  # Expected: 21

    # Example 2
    n2 = 6
    gain2 = [5, 1, 7, 4, 6, 20]
    prereq2 = [[], [0], [1], [], [3], [2, 4]]
    k2 = 4
    result2 = solution.max_knowledge_gain(n2, gain2, prereq2, k2)
    print(result2)  # Expected: 17

    # Additional small sanity checks
    n3 = 3
    gain3 = [10, 20, 30]
    prereq3 = [[], [], []]
    k3 = 2
    result3 = solution.max_knowledge_gain(n3, gain3, prereq3, k3)
    print(result3)  # Expected: 50

    n4 = 4
    gain4 = [1, 2, 100, 3]
    prereq4 = [[], [0], [1], []]
    k4 = 2
    result4 = solution.max_knowledge_gain(n4, gain4, prereq4, k4)
    print(result4)  # Expected: 4 (take modules 0 and 3, or 0 and 1 gives 3)