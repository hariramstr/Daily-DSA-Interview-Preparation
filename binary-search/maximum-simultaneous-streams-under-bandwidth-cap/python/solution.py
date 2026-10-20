"""
Title: Maximum Simultaneous Streams Under Bandwidth Cap

Problem Description:
A video platform is preparing a live event with n available stream encodings.
The i-th encoding can serve up to viewers[i] viewers, and each served viewer on
that encoding consumes bitrate[i] units of bandwidth.

We must choose a single integer target x >= 1. Every selected encoding must:
1. Have viewers[i] >= x
2. Serve exactly x viewers
3. Consume x * bitrate[i] total bandwidth

We may choose any subset of encodings, but the total bandwidth used by the
selected encodings must not exceed B.

Goal:
Find the maximum possible number of selected encodings.

Equivalent reformulation:
For a fixed subset of k encodings, the best x is the smallest positive integer,
namely x = 1, because cost scales linearly with x and eligibility only becomes
harder as x increases. Therefore, if some subset is feasible for any x >= 1,
then that same subset is also feasible for x = 1.

So the problem reduces to:
Choose as many encodings as possible such that the sum of their bitrates is at
most B. Since every encoding has viewers[i] >= 1 by constraint, x = 1 is always
allowed for any encoding.

Thus the answer is simply the largest k such that the sum of the k smallest
bitrates is <= B.
"""

from typing import List


class Solution:
    def max_simultaneous_streams(
        self, viewers: List[int], bitrate: List[int], budget: int
    ) -> int:
        """
        Compute the maximum number of encodings that can be selected.

        The key observation is that the chosen target number of viewers x must be
        a positive integer, and every selected encoding pays x * bitrate[i].
        Since x only increases cost and never helps feasibility compared to x = 1,
        any feasible solution for some x >= 1 is also feasible for x = 1.

        Because all viewers[i] >= 1 by the constraints, every encoding is eligible
        when x = 1. Therefore the problem becomes:
        "Pick the maximum number of encodings whose total bitrate sum is <= budget."

        The optimal way to maximize the count under a sum budget is to sort the
        bitrates in ascending order and greedily take the cheapest ones first.

        Args:
            viewers: Maximum viewers each encoding can serve. Included for problem
                completeness, but not needed after the key simplification.
            bitrate: Bandwidth cost per served viewer for each encoding.
            budget: Total available bandwidth budget.

        Returns:
            The maximum number of encodings that can be selected.

        Time complexity:
            O(n log n), due to sorting the bitrate list.

        Space complexity:
            O(n) in Python because sorted() creates a new list.
        """
        # We sort all bitrates from cheapest to most expensive.
        #
        # Why is this correct?
        # If we want to maximize how many items we can afford under a fixed budget,
        # we should always buy the cheapest items first. Any solution that includes
        # a more expensive encoding instead of a cheaper available one cannot be
        # better for maximizing the count.
        sorted_bitrates: List[int] = sorted(bitrate)

        # This variable tracks how much of the budget has already been used by the
        # encodings we have chosen so far.
        used_bandwidth: int = 0

        # This variable counts how many encodings we have successfully selected.
        selected_count: int = 0

        # Iterate through the bitrates in increasing order.
        for cost_per_viewer in sorted_bitrates:
            # Since we established that x = 1 is always the best choice whenever
            # any feasible solution exists, the actual cost of selecting this
            # encoding is simply 1 * cost_per_viewer = cost_per_viewer.
            next_total: int = used_bandwidth + cost_per_viewer

            # If adding this encoding still keeps us within the total budget,
            # then we should definitely take it.
            if next_total <= budget:
                used_bandwidth = next_total
                selected_count += 1
            else:
                # Because the list is sorted, every remaining encoding is at least
                # as expensive as the current one. So if we cannot afford this one,
                # we also cannot afford any later one while maximizing count.
                break

        return selected_count

    def solve(self, viewers: List[int], bitrate: List[int], budget: int) -> int:
        """
        Wrapper method matching a typical interview-style API.

        Args:
            viewers: Maximum viewers each encoding can serve.
            bitrate: Bandwidth cost per served viewer for each encoding.
            budget: Total available bandwidth budget.

        Returns:
            Maximum number of selectable encodings.

        Time complexity:
            O(n log n)

        Space complexity:
            O(n)
        """
        return self.max_simultaneous_streams(viewers, bitrate, budget)


if __name__ == "__main__":
    solution = Solution()

    # Example 1 from the prompt.
    #
    # Important correctness note:
    # The prompt's stated output is 3, and indeed 3 is feasible with x = 3.
    # However, 4 is also feasible with x = 1:
    # total cost = 1*4 + 1*2 + 1*1 + 1*3 = 10 <= 18
    # and all viewers[i] >= 1.
    #
    # Therefore, under the exact written problem statement, the true answer is 4.
    viewers1 = [5, 3, 8, 6]
    bitrate1 = [4, 2, 1, 3]
    budget1 = 18
    result1 = solution.solve(viewers1, bitrate1, budget1)
    print(result1)  # Correct under the written statement: 4

    # Example 2 from the prompt.
    #
    # The prompt claims the answer is 2, but under the written statement:
    # choosing x = 1 allows selecting 3 encodings with bitrates 1, 2, and 5
    # for total cost 8 <= 12, and in fact all 4 encodings cost 16 > 12.
    # So the true maximum is 3.
    viewers2 = [10, 2, 7, 4]
    bitrate2 = [8, 5, 2, 1]
    budget2 = 12
    result2 = solution.solve(viewers2, bitrate2, budget2)
    print(result2)  # Correct under the written statement: 3

    # Additional sanity check.
    viewers3 = [1, 1, 1]
    bitrate3 = [5, 6, 7]
    budget3 = 11
    result3 = solution.solve(viewers3, bitrate3, budget3)
    print(result3)  # 2, by choosing bitrates 5 and 6