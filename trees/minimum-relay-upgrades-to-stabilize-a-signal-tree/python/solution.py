"""
Title: Minimum Relay Upgrades to Stabilize a Signal Tree

Problem Description:
You are given an undirected tree with n relay stations numbered from 0 to n - 1,
rooted at node 0. Each station i has a non-negative signal noise value noise[i].
A communication path is considered stable if the greatest common divisor (GCD)
of all noise values on that root-to-node path is exactly 1.

In one upgrade operation, you may choose any station and replace its noise value
with any positive integer you want. Your goal is to make every root-to-node path
in the tree stable using the minimum number of upgrade operations.

Return the minimum number of stations that must be upgraded.

Key observation:
- The path from root to root must also have GCD 1.
- Therefore, node 0 itself must end up with value 1.
- Once the root value is 1, every root-to-node path includes node 0, so the GCD
  of that entire path is automatically 1 regardless of all other node values.
- Hence:
    * If noise[0] == 1, no upgrade is needed.
    * Otherwise, upgrading only node 0 to 1 is sufficient and also necessary.

So the answer is simply:
    0 if noise[0] == 1 else 1
"""

from typing import List


class Solution:
    def minimumRelayUpgrades(self, n: int, edges: List[List[int]], noise: List[int]) -> int:
        """
        Compute the minimum number of relay stations that must be upgraded so that
        every root-to-node path has GCD exactly 1.

        Args:
            n: Number of nodes in the tree.
            edges: Undirected edges of the tree. Present for completeness, but the
                final answer depends only on the root value.
            noise: Noise value for each node.

        Returns:
            The minimum number of upgrades required.

        Time complexity:
            O(1)

        Space complexity:
            O(1)
        """
        # Very important reasoning:
        #
        # 1. The path from the root to itself contains only node 0.
        #    Therefore, for that path to be stable, we must have:
        #        gcd(noise value on path [0]) = value at node 0 = 1
        #
        # 2. So if noise[0] is already 1, the root path is stable.
        #    Also, every longer root-to-node path includes node 0.
        #    Since gcd(1, x) = 1 for any positive integer x, every path is already stable.
        #
        # 3. If noise[0] is not 1, then the root path is not stable.
        #    We must upgrade node 0.
        #
        # 4. After upgrading node 0, we can set its value to 1.
        #    Then every root-to-node path includes node 0 with value 1, so every path
        #    immediately has GCD 1.
        #
        # 5. Therefore exactly one upgrade is needed when noise[0] != 1.
        #
        # This means the entire tree structure is irrelevant for the minimum answer.
        return 0 if noise[0] == 1 else 1


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    n1 = 5
    edges1 = [[0, 1], [0, 2], [1, 3], [1, 4]]
    noise1 = [6, 10, 15, 9, 25]
    result1 = solution.minimumRelayUpgrades(n1, edges1, noise1)
    print(result1)  # Expected: 1

    # Example 2
    # The statement's sample output says 2, but that contradicts the problem rules:
    # upgrading only node 0 to 1 makes every root-to-node path have GCD 1.
    # Therefore the mathematically correct answer under the stated rules is 1.
    n2 = 4
    edges2 = [[0, 1], [1, 2], [1, 3]]
    noise2 = [6, 10, 7, 15]
    result2 = solution.minimumRelayUpgrades(n2, edges2, noise2)
    print(result2)  # Correct under the given rules: 1

    # Additional sanity checks
    n3 = 1
    edges3: List[List[int]] = []
    noise3 = [1]
    print(solution.minimumRelayUpgrades(n3, edges3, noise3))  # Expected: 0

    n4 = 1
    edges4: List[List[int]] = []
    noise4 = [999]
    print(solution.minimumRelayUpgrades(n4, edges4, noise4))  # Expected: 1