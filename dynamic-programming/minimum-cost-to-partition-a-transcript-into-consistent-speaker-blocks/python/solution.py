"""
Title: Minimum Cost to Partition a Transcript into Consistent Speaker Blocks

Problem Description:
You are given a transcript of a meeting represented by an array `labels` of length `n`,
where `labels[i]` is the speaker ID of the `i`-th utterance. You want to split the
transcript into one or more contiguous blocks. For each block, you will assign exactly
one speaker as the block's "owner". Every utterance in that block spoken by the owner is
considered consistent and costs `0`, while every utterance spoken by a different speaker
is considered a correction and costs `1`.

In addition to correction costs, creating a block from index `l` to `r` incurs a fixed
overhead `overhead`. Therefore, the total cost of a partition is the sum, over all blocks,
of:

    overhead + (block length - maximum frequency of any speaker inside that block)

Your task is to return the minimum possible total cost to partition the entire transcript.

Intuitively, each block should be assigned to the speaker who appears most often in that
block, and all other utterances in the block are treated as mismatches. The challenge is
to decide where to cut the transcript so that the sum of block overheads and mismatch
counts is minimized.

Constraints:
- 1 <= n <= 5000
- 1 <= labels[i] <= 5000
- 0 <= overhead <= 10^9
- The answer fits in a 64-bit signed integer.
"""

from typing import List


class Solution:
    def minimum_partition_cost(self, labels: List[int], overhead: int) -> int:
        """
        Compute the minimum total cost to partition the transcript into contiguous blocks.

        We use dynamic programming:
        - Let dp[i] be the minimum cost to partition the first i utterances, meaning
          labels[0:i].
        - To compute dp[i], we try every possible last block ending at position i - 1.
          Suppose the last block starts at j, then its range is labels[j:i].
        - The cost of that block is:
              overhead + (length of block - maximum frequency in block)
        - Therefore:
              dp[i] = min(dp[j] + overhead + ((i - j) - max_freq(labels[j:i])))
              for all 0 <= j < i

        To evaluate all possible last blocks for a fixed ending position efficiently,
        we scan j backward from i - 1 to 0 while maintaining:
        - frequency of each speaker inside the current block labels[j:i]
        - the maximum frequency seen so far in that block

        Args:
            labels: List of speaker IDs for each utterance.
            overhead: Fixed cost paid for every created block.

        Returns:
            The minimum possible total partition cost.

        Time complexity:
            O(n^2), because for each ending position we scan all possible starting positions.

        Space complexity:
            O(n + V), where V is the range of speaker IDs used for the frequency array.
            Under the given constraints, V <= 5000, so this is effectively O(n).
        """
        n: int = len(labels)

        # dp[i] means:
        # minimum cost to partition the prefix labels[0:i]
        #
        # Important indexing detail:
        # - dp[0] = 0 because an empty prefix costs nothing.
        # - The final answer will be dp[n].
        #
        # We initialize all values to a very large number so that taking minimums works
        # naturally during the transitions.
        inf: int = 10**30
        dp: List[int] = [inf] * (n + 1)
        dp[0] = 0

        # The problem guarantees labels[i] <= 5000, so we can use a fixed-size list for
        # frequency counting instead of a dictionary.
        #
        # Why use a list?
        # - Faster constant factors than a dict
        # - Simpler for bounded integer labels
        #
        # We will create a fresh frequency array for each ending position i.
        max_label_value: int = 5000

        # Outer loop:
        # We compute dp[i] for every prefix length i from 1 to n.
        for i in range(1, n + 1):
            # freq[speaker] will store how many times that speaker appears in the current
            # candidate last block labels[j:i] as we move j backward.
            freq: List[int] = [0] * (max_label_value + 1)

            # max_freq tracks the highest frequency of any speaker in the current block.
            # This is exactly what we need because the mismatch cost of a block is:
            #     block_length - max_freq
            max_freq: int = 0

            # Inner loop:
            # Try every possible starting index j for the last block ending at i - 1.
            #
            # We move backward so that each step only adds one new element labels[j]
            # into the current block. This lets us update frequencies incrementally.
            for j in range(i - 1, -1, -1):
                speaker: int = labels[j]

                # Include labels[j] into the current block labels[j:i].
                freq[speaker] += 1

                # If this speaker now appears more times than any previous speaker in the
                # current block, update max_freq.
                if freq[speaker] > max_freq:
                    max_freq = freq[speaker]

                # Current block length is i - j because the block is labels[j:i].
                block_length: int = i - j

                # Mismatch/correction count:
                # All utterances not spoken by the most frequent speaker must be corrected.
                mismatch_cost: int = block_length - max_freq

                # Total cost if we cut before j and make labels[j:i] the final block.
                candidate_cost: int = dp[j] + overhead + mismatch_cost

                # Standard DP transition: keep the best possible partition cost.
                if candidate_cost < dp[i]:
                    dp[i] = candidate_cost

        return dp[n]


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    labels1: List[int] = [1, 2, 1, 1, 3]
    overhead1: int = 2
    result1: int = solution.minimum_partition_cost(labels1, overhead1)
    print("Example 1 Result:", result1)  # Expected: 4

    # Example 2
    labels2: List[int] = [4, 4, 2, 2, 2, 4, 4]
    overhead2: int = 1
    result2: int = solution.minimum_partition_cost(labels2, overhead2)
    print("Example 2 Result:", result2)  # Expected: 3

    # Additional quick sanity checks
    labels3: List[int] = [1]
    overhead3: int = 5
    result3: int = solution.minimum_partition_cost(labels3, overhead3)
    print("Single element Result:", result3)  # Expected: 5

    labels4: List[int] = [1, 1, 1, 1]
    overhead4: int = 3
    result4: int = solution.minimum_partition_cost(labels4, overhead4)
    print("All same speaker Result:", result4)  # Expected: 3