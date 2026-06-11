"""
Title: Minimum Energy to Process a Sensor Queue

Problem Description:
A monitoring device must process a queue of sensor packets in order. The packets are
represented by an integer array load, where load[i] is the energy cost of processing
the i-th packet by itself. To reduce overhead, the device is allowed to process either
one packet alone or two consecutive packets together as a batch.

If the device processes packet i alone, it spends load[i] energy. If it processes
packets i and i+1 together, it spends max(load[i], load[i+1]) + penalty energy, where
penalty is a fixed non-negative integer representing batching overhead. Every packet
must be processed exactly once, and batches cannot overlap. Your task is to return the
minimum total energy needed to process the entire queue.

This is an optimization problem over prefixes of the array. At each position, the best
choice may depend on the minimum energy needed for earlier packets, so a dynamic
programming solution is expected. A greedy choice such as always batching the two
smallest neighboring packets does not always lead to the global optimum.

Constraints:
- 1 <= load.length <= 100000
- 0 <= load[i] <= 1000000000
- 0 <= penalty <= 1000000000
- The answer fits in a 64-bit signed integer.
"""

from typing import List


class Solution:
    def minimum_energy(self, load: List[int], penalty: int) -> int:
        """
        Compute the minimum total energy needed to process the entire packet queue.

        We use dynamic programming over prefixes:
        - Let dp[i] represent the minimum energy needed to process the first i packets.
        - From each prefix, the last action is either:
          1. Process the last packet alone
          2. Process the last two packets together

        Args:
            load: List of packet processing costs.
            penalty: Extra energy cost added when batching two consecutive packets.

        Returns:
            The minimum total energy required to process all packets exactly once.

        Time complexity:
            O(n), where n is the number of packets.

        Space complexity:
            O(1) auxiliary space, because we only keep the last two DP states.
        """
        n: int = len(load)

        # Base case:
        # dp[0] = 0 means processing zero packets costs zero energy.
        prev2: int = 0

        # Base case for one packet:
        # dp[1] = load[0], because the only option is to process packet 0 alone.
        prev1: int = load[0]

        # If there is only one packet, we can return immediately.
        if n == 1:
            return prev1

        # We now build the DP values from left to right.
        #
        # Interpretation during the loop:
        # - prev2 holds dp[i - 2]
        # - prev1 holds dp[i - 1]
        #
        # We compute dp[i] using:
        #   dp[i] = min(
        #       dp[i - 1] + load[i - 1],                           # process packet i-1 alone
        #       dp[i - 2] + max(load[i - 2], load[i - 1]) + penalty  # batch packets i-2 and i-1
        #   )
        #
        # Here i means "first i packets processed", so packet indices involved are shifted by 1.
        for i in range(2, n + 1):
            # Option 1:
            # Process the last packet alone.
            #
            # If we already know the minimum cost to process the first i-1 packets,
            # then adding packet i-1 alone costs load[i-1] more.
            cost_single: int = prev1 + load[i - 1]

            # Option 2:
            # Process the last two packets together as one batch.
            #
            # If we already know the minimum cost to process the first i-2 packets,
            # then batching packets i-2 and i-1 costs:
            # max(load[i-2], load[i-1]) + penalty
            cost_batch: int = prev2 + max(load[i - 2], load[i - 1]) + penalty

            # The optimal cost for the first i packets is the cheaper of the two choices.
            current: int = min(cost_single, cost_batch)

            # Shift the rolling DP window forward:
            # - old prev1 becomes new prev2
            # - current becomes new prev1
            prev2, prev1 = prev1, current

        # After the loop, prev1 stores dp[n], the answer for all packets.
        return prev1

    def minEnergy(self, load: List[int], penalty: int) -> int:
        """
        Wrapper method using an alternative common interview-style name.

        Args:
            load: List of packet processing costs.
            penalty: Extra energy cost added when batching two consecutive packets.

        Returns:
            The minimum total energy required to process all packets exactly once.

        Time complexity:
            O(n), where n is the number of packets.

        Space complexity:
            O(1) auxiliary space.
        """
        return self.minimum_energy(load, penalty)


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # load = [4, 7, 2, 9], penalty = 1
    #
    # Let's verify carefully with the DP logic:
    # - Process all alone: 4 + 7 + 2 + 9 = 22
    # - Batch (0,1), then 2 alone, 3 alone: 8 + 2 + 9 = 19
    # - 0 alone, batch (1,2), 3 alone: 4 + 8 + 9 = 21
    # - 0 alone, 1 alone, batch (2,3): 4 + 7 + 10 = 21
    # - Batch (0,1) and batch (2,3): 8 + 10 = 18
    #
    # Therefore the true minimum is 18.
    # Note: The problem statement's final sentence says 19, but that contradicts
    # its own earlier calculation. The correct minimum is 18.
    load1: List[int] = [4, 7, 2, 9]
    penalty1: int = 1
    result1: int = solution.minimum_energy(load1, penalty1)
    print(f"Example 1 result: {result1}")  # Correct result is 18

    # Example 2:
    # load = [5, 1, 5, 1], penalty = 0
    # Batch (0,1) for 5 and (2,3) for 5 => total 10
    load2: List[int] = [5, 1, 5, 1]
    penalty2: int = 0
    result2: int = solution.minimum_energy(load2, penalty2)
    print(f"Example 2 result: {result2}")  # Expected: 10

    # Additional small sanity checks
    load3: List[int] = [8]
    penalty3: int = 5
    result3: int = solution.minimum_energy(load3, penalty3)
    print(f"Single packet result: {result3}")  # Expected: 8

    load4: List[int] = [3, 10]
    penalty4: int = 2
    result4: int = solution.minimum_energy(load4, penalty4)
    print(f"Two packets result: {result4}")  # min(3+10, max(3,10)+2) = min(13, 12) = 12