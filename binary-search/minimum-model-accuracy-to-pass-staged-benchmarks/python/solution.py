"""
Title: Minimum Model Accuracy to Pass Staged Benchmarks

Problem Description:
You are given the results of a machine learning model evaluated on a sequence of
benchmark stages. Stage i contains tasks with difficulty score difficulties[i]
and contributes weight weights[i] to the final certification score.

The model is deployed with a single accuracy threshold A. A task is considered
passed if difficulties[i] <= A. However, certification is stricter than counting
passed tasks: the model passes the full benchmark only if there exists a partition
of the stages into at most k contiguous groups such that, in every group, the total
weight of passed stages is at least the total weight of failed stages.

For a fixed A, convert each stage into:
    +weights[i]  if difficulties[i] <= A
    -weights[i]  otherwise

The threshold A is feasible if the array can be divided into at most k contiguous
non-empty segments, and every segment has sum >= 0.

Return the minimum feasible integer threshold A.
If no threshold can make the benchmark pass, return -1.

Constraints:
- 1 <= n <= 200000
- 1 <= k <= n
- 1 <= difficulties[i] <= 10^9
- 1 <= weights[i] <= 10^9

Key observation:
If the whole transformed array has total sum >= 0, then it is always feasible,
because using exactly one segment is allowed, and "at most k" includes 1.
Therefore feasibility does not actually depend on k (as long as k >= 1, which is
guaranteed by the constraints). The problem reduces to finding the minimum A such
that the total transformed sum is non-negative.

Let total_weight = sum(weights).
For a threshold A, let passed_weight(A) be the sum of weights[i] where
difficulties[i] <= A.

Then transformed total sum is:
    passed_weight(A) - (total_weight - passed_weight(A))
  = 2 * passed_weight(A) - total_weight

So A is feasible iff:
    2 * passed_weight(A) >= total_weight

This can be solved by sorting stages by difficulty and scanning cumulative passed
weight until the condition becomes true.
"""

from typing import List, Tuple


class Solution:
    def minimum_accuracy_threshold(
        self, difficulties: List[int], weights: List[int], k: int
    ) -> int:
        """
        Find the minimum integer threshold A such that the transformed array can be
        partitioned into at most k contiguous non-empty segments, each with sum >= 0.

        Because using one segment is allowed, feasibility is equivalent to the total
        transformed sum being non-negative.

        Args:
            difficulties: Difficulty of each stage.
            weights: Weight contributed by each stage.
            k: Maximum number of contiguous non-empty segments allowed.

        Returns:
            The minimum feasible integer threshold A, or -1 if impossible.

        Time complexity:
            O(n log n), due to sorting stages by difficulty.

        Space complexity:
            O(n), for storing paired (difficulty, weight) values.
        """
        # The input size.
        n: int = len(difficulties)

        # Defensive check for malformed input lengths.
        # The problem statement implies equal lengths, but this keeps the method robust.
        if n != len(weights) or n == 0:
            return -1

        # Although k does not affect the final feasibility test once k >= 1,
        # we still keep the parameter because it is part of the required signature.
        # If k were somehow invalid, no partition would be possible.
        if k <= 0:
            return -1

        # ------------------------------------------------------------
        # Step 1: Compute the total weight of all stages.
        #
        # For a fixed threshold A:
        # - passed stage contributes +weight
        # - failed stage contributes -weight
        #
        # Therefore total transformed sum is:
        #   sum(+w for passed) + sum(-w for failed)
        # = passed_weight - failed_weight
        # = passed_weight - (total_weight - passed_weight)
        # = 2 * passed_weight - total_weight
        #
        # A threshold is feasible iff this total is >= 0, because one single
        # segment containing the whole array is allowed.
        # ------------------------------------------------------------
        total_weight: int = sum(weights)

        # ------------------------------------------------------------
        # Step 2: Pair each difficulty with its weight and sort by difficulty.
        #
        # Why sorting helps:
        # As A increases, more stages become "passed".
        # If we process stages in increasing difficulty order, we can maintain
        # a running sum of passed weights. The first difficulty where
        #   2 * passed_weight >= total_weight
        # is exactly the minimum feasible threshold.
        # ------------------------------------------------------------
        stages: List[Tuple[int, int]] = sorted(zip(difficulties, weights))

        # ------------------------------------------------------------
        # Step 3: Sweep through difficulties in increasing order.
        #
        # We add the weights of all stages whose difficulty is now <= current A.
        # Since multiple stages can share the same difficulty, we process them
        # together before checking feasibility. This ensures we return a threshold
        # that is actually one of the input difficulty values and is minimal.
        # ------------------------------------------------------------
        passed_weight: int = 0
        index: int = 0

        while index < n:
            current_difficulty: int = stages[index][0]

            # Process every stage with this exact same difficulty.
            # All of them become passed simultaneously when A reaches current_difficulty.
            while index < n and stages[index][0] == current_difficulty:
                passed_weight += stages[index][1]
                index += 1

            # --------------------------------------------------------
            # Step 4: Check whether the total transformed sum is now >= 0.
            #
            # Condition:
            #   2 * passed_weight - total_weight >= 0
            # equivalent to:
            #   2 * passed_weight >= total_weight
            #
            # If true, then using the whole array as one segment gives a valid
            # partition into at most k segments. Since we are scanning in sorted
            # order, this is the minimum feasible threshold.
            # --------------------------------------------------------
            if 2 * passed_weight >= total_weight:
                return current_difficulty

        # ------------------------------------------------------------
        # Step 5: If even after all stages are passed the condition is not met,
        # then no threshold can work.
        #
        # In the given constraints, this situation cannot actually happen because
        # when all stages are passed:
        #   passed_weight = total_weight
        # so:
        #   2 * passed_weight = 2 * total_weight >= total_weight
        #
        # Still, returning -1 is the correct fallback for completeness.
        # ------------------------------------------------------------
        return -1


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    difficulties_1: List[int] = [4, 2, 7, 3, 6]
    weights_1: List[int] = [5, 2, 4, 3, 6]
    k_1: int = 2
    result_1: int = solution.minimum_accuracy_threshold(difficulties_1, weights_1, k_1)
    print(result_1)  # Expected: 4

    # Example 2
    difficulties_2: List[int] = [8, 9, 10]
    weights_2: List[int] = [3, 4, 5]
    k_2: int = 3
    result_2: int = solution.minimum_accuracy_threshold(difficulties_2, weights_2, k_2)
    print(result_2)  # Expected: 10