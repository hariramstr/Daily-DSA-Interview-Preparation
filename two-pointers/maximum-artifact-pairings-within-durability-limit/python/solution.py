"""
Title: Maximum Artifact Pairings Within Durability Limit

Problem Description:
A museum is preparing fragile artifacts for transport. You are given an integer array
durability where durability[i] is the durability score of the i-th artifact, and an
integer limit. A shipping crate may contain either one artifact or exactly two artifacts.
If two artifacts with durabilities a and b are packed together, the pair is valid only if
|a - b| <= limit. Each artifact can be used in at most one crate.

Your task is to maximize the number of 2-artifact crates that can be formed.

Unlike simpler pairing problems, the artifacts are not pre-sorted, durability scores may
repeat many times, and the answer must be computed efficiently for very large inputs.
Returning the maximum number of valid pairs is enough; you do not need to output the
actual pairing.

A pair uses two distinct indices. Single-artifact crates are allowed for leftovers, but
they do not contribute to the answer. The challenge is to decide which artifacts to match
so that local greedy choices do not reduce the total number of valid pairs.

Constraints:
- 1 <= durability.length <= 2 * 10^5
- 0 <= durability[i] <= 10^9
- 0 <= limit <= 10^9

Example 1:
Input: durability = [8, 1, 5, 6, 2, 9], limit = 1
Output: 2
Explanation: After sorting, one optimal pairing is (1,2) and (5,6). Artifact 8 and 9
cannot both be paired because using one of them blocks a better total arrangement.

Example 2:
Input: durability = [4, 4, 4, 7, 8, 9, 9], limit = 0
Output: 2
Explanation: Only equal durabilities can be paired. We can form two pairs from the three
artifacts with durability 4 and the two artifacts with durability 9. The artifact with
durability 7 and the artifact with durability 8 remain alone.
"""

from typing import List


class Solution:
    def max_artifact_pairs(self, durability: List[int], limit: int) -> int:
        """
        Compute the maximum number of valid 2-artifact pairs.

        The key idea is:
        1. Sort the durability values.
        2. In sorted order, if two neighboring unused artifacts can be paired,
           pairing them immediately is always safe and optimal.
        3. This becomes a simple left-to-right greedy scan.

        Why adjacent greedy works:
        - After sorting, if durability[i + 1] - durability[i] <= limit, then pairing
          these two smallest remaining artifacts cannot reduce the total number of pairs.
        - If the smallest artifact can be paired at all, pairing it with the earliest
          possible partner preserves as many options as possible for the remaining items.
        - If the smallest artifact cannot pair with its immediate neighbor, then it cannot
          pair with any later artifact either, because later artifacts are even larger.

        Args:
            durability: List of artifact durability scores.
            limit: Maximum allowed absolute difference inside a pair.

        Returns:
            The maximum number of valid pairs.

        Time complexity:
            O(n log n), due to sorting.

        Space complexity:
            O(1) extra space beyond the sort implementation details.
        """
        # First, sort the durability values.
        #
        # Why sorting helps:
        # - The condition for a valid pair is based on absolute difference.
        # - In sorted order, for any i < j, we know durability[j] >= durability[i],
        #   so |durability[j] - durability[i]| becomes durability[j] - durability[i].
        # - More importantly, if the closest possible neighbor for an item is already
        #   too far away, then every later item will also be too far away.
        #
        # This transforms a difficult global pairing problem into a clean local scan.
        durability.sort()

        # This variable stores how many valid 2-artifact crates we have formed so far.
        pairs: int = 0

        # We use an index to scan from left to right.
        #
        # Interpretation:
        # - At each step, we look at the current artifact and the next artifact.
        # - Because the array is sorted, these are the two smallest remaining artifacts.
        i: int = 0
        n: int = len(durability)

        # Continue while there are at least two artifacts left to consider.
        while i + 1 < n:
            # Compute the difference between adjacent artifacts.
            #
            # Since the array is sorted, this is the smallest possible difference
            # involving durability[i] and any artifact to its right.
            difference: int = durability[i + 1] - durability[i]

            # Case 1: The adjacent pair is valid.
            if difference <= limit:
                # We greedily pair these two artifacts.
                #
                # Why this is optimal:
                # - durability[i] is the smallest remaining artifact.
                # - durability[i + 1] is the closest possible partner for it.
                # - Using the closest partner leaves larger artifacts available to pair
                #   among themselves, which cannot hurt the total number of pairs.
                #
                # So we safely count one pair.
                pairs += 1

                # Move forward by 2 because both artifacts are now used.
                i += 2
            else:
                # Case 2: The adjacent pair is NOT valid.
                #
                # Because the array is sorted, every artifact after durability[i + 1]
                # is even larger, so:
                #   durability[j] - durability[i] >= durability[i + 1] - durability[i]
                # for all j > i + 1.
                #
                # Therefore, durability[i] cannot pair with any later artifact at all.
                # The only possible action is to leave durability[i] unpaired and move on.
                i += 1

        return pairs


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    durability_1: List[int] = [8, 1, 5, 6, 2, 9]
    limit_1: int = 1
    result_1: int = solution.max_artifact_pairs(durability_1, limit_1)
    print("Example 1 Result:", result_1)  # Expected: 2

    # Example 2
    durability_2: List[int] = [4, 4, 4, 7, 8, 9, 9]
    limit_2: int = 0
    result_2: int = solution.max_artifact_pairs(durability_2, limit_2)
    print("Example 2 Result:", result_2)  # Expected: 2

    # Additional quick checks
    durability_3: List[int] = [1]
    limit_3: int = 10
    result_3: int = solution.max_artifact_pairs(durability_3, limit_3)
    print("Single Artifact Result:", result_3)  # Expected: 0

    durability_4: List[int] = [1, 100, 101, 102]
    limit_4: int = 1
    result_4: int = solution.max_artifact_pairs(durability_4, limit_4)
    print("Additional Check Result:", result_4)  # Expected: 1