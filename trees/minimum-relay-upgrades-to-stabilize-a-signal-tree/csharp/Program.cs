/*
Title: Minimum Relay Upgrades to Stabilize a Signal Tree

Problem Description:
You are given an undirected tree with n relay stations numbered from 0 to n - 1, rooted at node 0.
Each station i has a non-negative signal noise value noise[i].

A communication path is considered stable if the greatest common divisor (GCD) of all noise values
on that root-to-node path is exactly 1.

In one upgrade operation, you may choose any station and replace its noise value with any positive
integer you want. Your goal is to make every root-to-node path in the tree stable using the minimum
number of upgrade operations.

Return the minimum number of stations that must be upgraded.

Important observation:
- The path from the root to the root itself contains only node 0.
- Therefore, for the root path to have GCD exactly 1, node 0 itself must end up with value 1.
- So if noise[0] != 1, upgrading node 0 is unavoidable.
- Once some node on a root-to-node path is changed to 1, the GCD of the entire path becomes 1 forever
  for that node and all descendants, because gcd(1, x) = 1.

This transforms the problem into:
- Choose the minimum number of upgraded nodes so that every root-to-node path contains at least one
  upgraded-to-1 node.
- Since the root path itself must be stable, node 0 must always be such a node unless it is already 1.
- If node 0 is 1 initially, every path already contains a 1 at the root, so answer is 0.
- Otherwise, upgrading node 0 to 1 makes every path stable immediately, so answer is 1.

That means the minimum answer is simply:
- 0 if noise[0] == 1
- 1 otherwise
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(1)
    Space Complexity: O(1)

    Explanation:
    We only need to inspect the root value.

    Why this works:
    1. The path from root to root is just [noise[0]].
       For that path to have GCD 1, the final value at node 0 must be 1.
    2. If noise[0] is already 1, then every root-to-node path includes node 0,
       so every such path has GCD 1 automatically.
    3. If noise[0] is not 1, we must upgrade node 0.
       We can set it to 1 in one operation.
       Then every root-to-node path includes a 1, so every path has GCD 1.

    Therefore the answer is exactly:
    - 0 when noise[0] == 1
    - 1 otherwise
    */
    public int MinimumRelayUpgrades(int n, int[][] edges, int[] noise)
    {
        // Step 1:
        // Check the root station's noise value.
        // This is the only value that matters for the final answer because:
        // - The root-to-root path must itself be stable.
        // - That path contains only the root.
        // So the root must be 1 in the final configuration.
        if (noise[0] == 1)
        {
            // Step 2:
            // If the root is already 1, then every path from root to any node
            // includes this root value 1.
            //
            // Since gcd(1, anything) = 1, every path is already stable.
            // No upgrades are needed.
            return 0;
        }

        // Step 3:
        // Otherwise, the root is not 1.
        // The root-to-root path is not stable, so at least one upgrade is mandatory.
        //
        // The best possible move is to upgrade the root itself to 1.
        // That single change instantly makes every root-to-node path stable,
        // because every such path passes through the root.
        return 1;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int n1 = 5;
int[][] edges1 =
{
    new[] { 0, 1 },
    new[] { 0, 2 },
    new[] { 1, 3 },
    new[] { 1, 4 }
};
int[] noise1 = { 6, 10, 15, 9, 25 };
int result1 = solution.MinimumRelayUpgrades(n1, edges1, noise1);
Console.WriteLine(result1); // Expected: 1

// Example 2
int n2 = 4;
int[][] edges2 =
{
    new[] { 0, 1 },
    new[] { 1, 2 },
    new[] { 1, 3 }
};
int[] noise2 = { 6, 10, 7, 15 };
int result2 = solution.MinimumRelayUpgrades(n2, edges2, noise2);
Console.WriteLine(result2); // Based on the actual path definition, this is 1

// Additional demo: root already 1
int n3 = 3;
int[][] edges3 =
{
    new[] { 0, 1 },
    new[] { 1, 2 }
};
int[] noise3 = { 1, 100, 200 };
int result3 = solution.MinimumRelayUpgrades(n3, edges3, noise3);
Console.WriteLine(result3); // Expected: 0