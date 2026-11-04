"""
Title: Minimum Cost to Reorder a Warehouse Picking List with Shelf Jump Fees

Problem Description:
A warehouse robot must collect items in the exact order given by a picking list.
The warehouse has shelves labeled from 1 to m, and each item type may appear on
several shelves. For the i-th request in the list, you are given all shelves that
currently contain a valid copy of that requested item. The robot starts before
processing the first request and may begin on any shelf that stores the first
requested item without paying a movement cost.

If the robot picks the i-th item from shelf a and the (i+1)-th item from shelf b,
then it pays a movement cost of |a - b|. In addition, changing movement direction
is expensive: if the previous move was to a higher-numbered shelf and the next move
is to a lower-numbered shelf, or vice versa, an extra turn fee T is paid for that
transition. The very first move does not incur a turn fee because there is no
previous direction yet. Staying on the same shelf counts as direction 0 and does
not itself create a turn; however, after staying still, the next nonzero move is
compared against the last nonzero direction used.

Your task is to compute the minimum total cost to fulfill the entire picking list.

Formally, you are given an array positions where positions[i] is a sorted list of
distinct shelf numbers that can satisfy the i-th request, and an integer T.
Choose one shelf from each positions[i] so that the sum of movement costs and turn
fees is minimized.

Constraints:
- 1 <= n <= 2 * 10^5, where n = positions.length
- 1 <= total number of shelf entries across all positions[i] <= 2 * 10^5
- 1 <= shelf number <= 10^9
- 0 <= T <= 10^9
- Each positions[i] is non-empty, sorted, and contains distinct integers
"""

from typing import List, Tuple


INF = 10**30


class Solution:
    def _merge_min(self, a: List[int], b: List[int]) -> List[int]:
        """
        Return element-wise minimum of two equal-length arrays.

        Args:
            a: First list of costs.
            b: Second list of costs.

        Returns:
            A new list where result[i] = min(a[i], b[i]).

        Time complexity:
            O(len(a))

        Space complexity:
            O(len(a))
        """
        return [x if x < y else y for x, y in zip(a, b)]

    def _build_prefix_suffix(self, values: List[int]) -> Tuple[List[int], List[int]]:
        """
        Build prefix-min and suffix-min arrays.

        Args:
            values: Input cost array.

        Returns:
            A tuple (prefix_min, suffix_min).

        Time complexity:
            O(len(values))

        Space complexity:
            O(len(values))
        """
        n: int = len(values)
        prefix: List[int] = [0] * n
        suffix: List[int] = [0] * n

        best: int = INF
        for i in range(n):
            if values[i] < best:
                best = values[i]
            prefix[i] = best

        best = INF
        for i in range(n - 1, -1, -1):
            if values[i] < best:
                best = values[i]
            suffix[i] = best

        return prefix, suffix

    def _transition(
        self,
        prev_pos: List[int],
        prev_none: List[int],
        prev_up: List[int],
        prev_down: List[int],
        curr_pos: List[int],
        turn_fee: int,
    ) -> Tuple[List[int], List[int], List[int]]:
        """
        Compute DP transition from one request to the next.

        State meaning:
        - prev_none[j]: minimum cost to end at prev_pos[j] with no previous nonzero
          direction yet. This can only happen before any nonzero move has ever been
          made, or after only zero moves so far.
        - prev_up[j]: minimum cost to end at prev_pos[j] where the last nonzero move
          direction used so far is upward (+1).
        - prev_down[j]: minimum cost to end at prev_pos[j] where the last nonzero move
          direction used so far is downward (-1).

        For each current shelf y, we compute:
        - curr_none[y]: still no nonzero direction used yet
        - curr_up[y]: last nonzero direction is up
        - curr_down[y]: last nonzero direction is down

        Args:
            prev_pos: Sorted shelves for previous request.
            prev_none: DP costs for "no previous nonzero direction".
            prev_up: DP costs for last nonzero direction = up.
            prev_down: DP costs for last nonzero direction = down.
            curr_pos: Sorted shelves for current request.
            turn_fee: Extra fee when changing between up and down.

        Returns:
            Tuple of arrays (curr_none, curr_up, curr_down).

        Time complexity:
            O(len(prev_pos) + len(curr_pos))

        Space complexity:
            O(len(prev_pos) + len(curr_pos))
        """
        p: int = len(prev_pos)
        q: int = len(curr_pos)

        # ------------------------------------------------------------
        # STEP 1: Prepare helper arrays for fast "move upward" queries.
        #
        # If we move from previous shelf x to current shelf y with y > x,
        # then the movement direction of this step is UP.
        #
        # The transition cost is:
        #   previous_cost + (y - x) + possible_turn_fee
        #
        # Rearranged:
        #   (previous_cost - x + possible_turn_fee) + y
        #
        # So for a fixed y, among all x <= y-1, we need the minimum of
        #   previous_cost - x + possible_turn_fee
        #
        # Because both shelf lists are sorted, we can answer this for all y
        # in one left-to-right sweep using prefix minima.
        # ------------------------------------------------------------

        # Coming into an UP move:
        # - from prev_none: no turn fee, because there was no previous nonzero direction
        # - from prev_up: continuing upward, no turn fee
        # - from prev_down: changing direction down -> up, pay turn_fee
        up_base_from_none: List[int] = [prev_none[i] - prev_pos[i] for i in range(p)]
        up_base_from_up: List[int] = [prev_up[i] - prev_pos[i] for i in range(p)]
        up_base_from_down: List[int] = [prev_down[i] - prev_pos[i] + turn_fee for i in range(p)]

        up_base: List[int] = self._merge_min(
            self._merge_min(up_base_from_none, up_base_from_up),
            up_base_from_down,
        )
        up_prefix_min, _ = self._build_prefix_suffix(up_base)

        # ------------------------------------------------------------
        # STEP 2: Prepare helper arrays for fast "move downward" queries.
        #
        # If we move from previous shelf x to current shelf y with y < x,
        # then the movement direction of this step is DOWN.
        #
        # The transition cost is:
        #   previous_cost + (x - y) + possible_turn_fee
        #
        # Rearranged:
        #   (previous_cost + x + possible_turn_fee) - y
        #
        # So for a fixed y, among all x >= y+1, we need the minimum of
        #   previous_cost + x + possible_turn_fee
        #
        # Since lists are sorted, we can answer this for all y in one
        # right-to-left sweep using suffix minima.
        # ------------------------------------------------------------

        # Coming into a DOWN move:
        # - from prev_none: no turn fee
        # - from prev_down: continuing downward, no turn fee
        # - from prev_up: changing direction up -> down, pay turn_fee
        down_base_from_none: List[int] = [prev_none[i] + prev_pos[i] for i in range(p)]
        down_base_from_down: List[int] = [prev_down[i] + prev_pos[i] for i in range(p)]
        down_base_from_up: List[int] = [prev_up[i] + prev_pos[i] + turn_fee for i in range(p)]

        down_base: List[int] = self._merge_min(
            self._merge_min(down_base_from_none, down_base_from_down),
            down_base_from_up,
        )
        _, down_suffix_min = self._build_prefix_suffix(down_base)

        # ------------------------------------------------------------
        # STEP 3: Handle zero-move transitions (same shelf x == y).
        #
        # Staying on the same shelf costs 0 and does NOT create a new
        # direction. That means:
        # - none stays none
        # - up stays up
        # - down stays down
        #
        # We can process equal values with a two-pointer merge because
        # both lists are sorted.
        # ------------------------------------------------------------
        curr_none: List[int] = [INF] * q
        curr_up: List[int] = [INF] * q
        curr_down: List[int] = [INF] * q

        i: int = 0
        j: int = 0
        while i < p and j < q:
            if prev_pos[i] == curr_pos[j]:
                if prev_none[i] < curr_none[j]:
                    curr_none[j] = prev_none[i]
                if prev_up[i] < curr_up[j]:
                    curr_up[j] = prev_up[i]
                if prev_down[i] < curr_down[j]:
                    curr_down[j] = prev_down[i]
                i += 1
                j += 1
            elif prev_pos[i] < curr_pos[j]:
                i += 1
            else:
                j += 1

        # ------------------------------------------------------------
        # STEP 4: Add all UP transitions.
        #
        # For each current shelf y, we need all previous shelves x < y.
        # We maintain a pointer k = count of previous shelves < y.
        # Then the best candidate is:
        #   up_prefix_min[k - 1] + y
        # if k > 0.
        # ------------------------------------------------------------
        k: int = 0
        for idx, y in enumerate(curr_pos):
            while k < p and prev_pos[k] < y:
                k += 1
            if k > 0:
                candidate: int = up_prefix_min[k - 1] + y
                if candidate < curr_up[idx]:
                    curr_up[idx] = candidate

        # ------------------------------------------------------------
        # STEP 5: Add all DOWN transitions.
        #
        # For each current shelf y, we need all previous shelves x > y.
        # Let k be the first index with prev_pos[k] > y.
        # Then the best candidate is:
        #   down_suffix_min[k] - y
        # if k < p.
        #
        # We can compute this with another pointer sweep because both
        # arrays are sorted.
        # ------------------------------------------------------------
        k = 0
        for idx, y in enumerate(curr_pos):
            while k < p and prev_pos[k] <= y:
                k += 1
            if k < p:
                candidate = down_suffix_min[k] - y
                if candidate < curr_down[idx]:
                    curr_down[idx] = candidate

        return curr_none, curr_up, curr_down

    def minimum_cost(self, positions: List[List[int]], turn_fee: int) -> int:
        """
        Compute the minimum total cost to fulfill the picking list.

        The algorithm uses dynamic programming over requests. For every candidate
        shelf in the current request, we keep three DP values based on the last
        nonzero movement direction seen so far:
        - none: no nonzero move has happened yet
        - up: last nonzero move was upward
        - down: last nonzero move was downward

        The key optimization is that transitions between two consecutive requests
        are processed in linear time using the sorted shelf lists:
        - all upward moves are handled by prefix minima
        - all downward moves are handled by suffix minima
        - equal shelves are handled by a merge scan

        This avoids the quadratic comparison of every previous shelf against every
        current shelf.

        Args:
            positions: positions[i] is a sorted list of distinct shelves that can
                satisfy request i.
            turn_fee: Extra fee paid when the movement direction changes between
                consecutive nonzero moves.

        Returns:
            The minimum possible total cost.

        Time complexity:
            O(total number of shelf entries across all requests)

        Space complexity:
            O(max size of two consecutive request lists)
        """
        n: int = len(positions)

        # ------------------------------------------------------------
        # BASE CASE:
        # Before processing the first request, the robot may start on any
        # valid shelf for that request at zero cost.
        #
        # Since no movement has happened yet, the "last nonzero direction"
        # does not exist, so all first-request states belong to the "none"
        # category with cost 0.
        # ------------------------------------------------------------
        prev_pos: List[int] = positions[0]
        prev_none: List[int] = [0] * len(prev_pos)
        prev_up: List[int] = [INF] * len(prev_pos)
        prev_down: List[int] = [INF] * len(prev_pos)

        # ------------------------------------------------------------
        # Process requests one by one.
        # Each step transforms DP on positions[i-1] into DP on positions[i].
        # ------------------------------------------------------------
        for idx in range(1, n):
            curr_pos: List[int] = positions[idx]
            curr_none, curr_up, curr_down = self._transition(
                prev_pos,
                prev_none,
                prev_up,
                prev_down,
                curr_pos,
                turn_fee,
            )
            prev_pos = curr_pos
            prev_none = curr_none
            prev_up = curr_up
            prev_down = curr_down

        # ------------------------------------------------------------
        # The answer is the best cost among all final shelves and all
        # possible direction states.
        # ------------------------------------------------------------
        answer: int = INF
        for a, b, c in zip(prev_none, prev_up, prev_down):
            if a < answer:
                answer = a
            if b < answer:
                answer = b
            if c < answer:
                answer = c
        return answer


if __name__ == "__main__":
    solver = Solution()

    # Example 1 from the prompt.
    positions1: List[List[int]] = [[2, 8], [3], [7, 10], [6]]
    t1: int = 5
    result1: int = solver.minimum_cost(positions1, t1)
    print(result1)

    # Example 2 from the prompt.
    positions2: List[List[int]] = [[1, 5], [4, 6], [2, 8], [3]]
    t2: int = 2
    result2: int = solver.minimum_cost(positions2, t2)
    print(result2)