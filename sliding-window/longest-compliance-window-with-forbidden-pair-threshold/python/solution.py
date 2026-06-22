"""
Title: Longest Compliance Window with Forbidden Pair Threshold

Problem Description:
A security team monitors a sequence of access events represented by an integer array
events, where each value denotes the policy group of the event. Some pairs of policy
groups are considered incompatible when they appear together too often in a short
time span. You are also given a list forbiddenPairs, where each element [a, b] means
groups a and b form a forbidden pair, and an integer limit.

For any contiguous window of events, define its conflict count as the total number of
index pairs (i, j) inside the window such that i < j, events[i] = a, events[j] = b,
and {a, b} is a forbidden pair. If a forbidden pair is [x, x], then every pair of
equal x values inside the window contributes to the conflict count. The order of values
in forbiddenPairs does not matter: [2, 5] and [5, 2] describe the same rule.

Return the length of the longest contiguous subarray whose conflict count is at most
limit.

Constraints:
- 1 <= events.length <= 200000
- 1 <= events[i] <= 200000
- 1 <= forbiddenPairs.length <= 200000
- 1 <= a, b <= 200000
- 0 <= limit <= 10^15
- forbiddenPairs may contain duplicates; duplicates should be treated as a single forbidden rule.
"""

from typing import Dict, List, Set


class Solution:
    def _build_forbidden_graph(self, forbidden_pairs: List[List[int]]) -> Dict[int, Set[int]]:
        """
        Build an undirected adjacency structure for forbidden value pairs.

        Each unique forbidden rule {a, b} is stored exactly once logically, but because
        we need fast lookup from either endpoint, we place b in graph[a] and a in graph[b].
        For self-pairs [x, x], x simply appears in its own adjacency set.

        Args:
            forbidden_pairs: List of forbidden value pairs.

        Returns:
            A dictionary mapping each value to the set of values that form a forbidden
            pair with it.

        Time complexity:
            O(m), where m is the number of forbiddenPairs entries.

        Space complexity:
            O(u), where u is the number of unique forbidden rules expanded into adjacency.
        """
        graph: Dict[int, Set[int]] = {}

        for a, b in forbidden_pairs:
            if a not in graph:
                graph[a] = set()
            if b not in graph:
                graph[b] = set()

            # Using sets automatically removes duplicate forbidden rules.
            graph[a].add(b)
            graph[b].add(a)

        return graph

    def longest_compliance_window(
        self,
        events: List[int],
        forbidden_pairs: List[List[int]],
        limit: int,
    ) -> int:
        """
        Find the longest contiguous subarray whose conflict count is at most limit.

        The key idea is a sliding window with an incrementally maintained conflict count.
        When we add a value x to the right side of the window, the number of NEW conflicts
        created equals the number of existing elements already in the window whose values
        are forbidden with x. That is:
            sum(count[y] for y in forbidden_neighbors_of_x)

        This works for self-pairs too:
        - If [x, x] is forbidden, then adding one more x creates exactly count[x] new
          equal-value conflict pairs, which is correct.

        When we remove a value x from the left side of the window, we must subtract the
        number of conflict pairs that involved that specific removed occurrence and some
        later element still remaining in the window. After decrementing count[x], the
        remaining window contains exactly the elements to the right of the removed one.
        Therefore the number of removed conflicts is:
            sum(count[y] for y in forbidden_neighbors_of_x)
        computed AFTER decrementing count[x].

        To stay efficient, we do not recompute the whole window's conflict count. We only
        update by the exact number of pairs created or destroyed at each step.

        Args:
            events: Array of event group IDs.
            forbidden_pairs: List of forbidden unordered pairs.
            limit: Maximum allowed conflict count inside the window.

        Returns:
            The maximum length of a contiguous subarray with conflict count <= limit.

        Time complexity:
            O((n + total_shrink_steps) * avg_degree_visited)
            More concretely, each add/remove scans the adjacency list of the involved value.
            In practice this is efficient for the given constraints and avoids any full
            window recomputation.

        Space complexity:
            O(n_distinct_values_in_window + number_of_unique_forbidden_edges)
        """
        # Build adjacency sets so that for any value x we can quickly iterate over all
        # values y such that {x, y} is forbidden.
        forbidden_graph: Dict[int, Set[int]] = self._build_forbidden_graph(forbidden_pairs)

        # Frequency map of values currently inside the sliding window [left, right].
        counts: Dict[int, int] = {}

        # Current total number of conflicting index pairs inside the window.
        conflict_count: int = 0

        # Standard sliding window pointers.
        left: int = 0

        # Best valid window length found so far.
        best: int = 0

        # Expand the window one event at a time from left to right.
        for right, value in enumerate(events):
            # Step 1: determine how many NEW conflict pairs are created by inserting
            # events[right] = value into the current window.
            #
            # Why this works:
            # - Every existing occurrence of a forbidden partner y forms exactly one new
            #   pair with this newly added value.
            # - We only count pairs where the new element is the right endpoint, so each
            #   pair is counted exactly once over the whole process.
            added_conflicts: int = 0
            if value in forbidden_graph:
                for neighbor in forbidden_graph[value]:
                    added_conflicts += counts.get(neighbor, 0)

            # Apply the new conflicts and then record the new value in the window.
            conflict_count += added_conflicts
            counts[value] = counts.get(value, 0) + 1

            # Step 2: if the window is invalid, shrink it from the left until the total
            # conflict count is back within the allowed limit.
            while conflict_count > limit:
                left_value: int = events[left]

                # We are removing the leftmost occurrence of left_value.
                # First decrement its count so that counts now represent exactly the
                # remaining window after removal.
                counts[left_value] -= 1
                if counts[left_value] == 0:
                    del counts[left_value]

                # Now compute how many conflict pairs disappeared because of removing this
                # specific leftmost element.
                #
                # Why compute AFTER decrementing?
                # - The removed element can only conflict with elements that remain to its
                #   right in the window.
                # - After decrementing, counts describes exactly those remaining elements.
                #
                # For a self-pair [x, x]:
                # - If there were k copies of x before removal, then the removed x formed
                #   conflicts with the other k - 1 copies.
                # - After decrementing, counts[x] = k - 1, so subtracting counts[x] is
                #   exactly correct.
                removed_conflicts: int = 0
                if left_value in forbidden_graph:
                    for neighbor in forbidden_graph[left_value]:
                        removed_conflicts += counts.get(neighbor, 0)

                conflict_count -= removed_conflicts
                left += 1

            # Step 3: the current window [left, right] is valid, so update the answer.
            current_length: int = right - left + 1
            if current_length > best:
                best = current_length

        return best


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    events1 = [1, 2, 1, 3, 2, 1]
    forbidden_pairs1 = [[1, 2], [2, 3]]
    limit1 = 2
    result1 = solution.longest_compliance_window(events1, forbidden_pairs1, limit1)
    print(result1)  # Expected: 4

    # Example 2
    events2 = [4, 4, 4, 2, 4]
    forbidden_pairs2 = [[4, 4], [2, 4]]
    limit2 = 3
    result2 = solution.longest_compliance_window(events2, forbidden_pairs2, limit2)
    print(result2)  # Expected: 3