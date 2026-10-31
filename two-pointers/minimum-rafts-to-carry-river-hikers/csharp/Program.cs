/*
Title: Minimum Rafts to Carry River Hikers
Difficulty: Medium
Topic: Two Pointers

Problem Description:
A tour company needs to transport hikers across a river using small rafts. Each raft can carry at most two hikers, and the combined weight of the hikers on a raft cannot exceed a given limit. You are given an integer array weights where weights[i] is the weight of the i-th hiker, and an integer limit representing the maximum total weight a single raft can safely carry.

Return the minimum number of rafts required to carry every hiker across the river.

A raft may carry either one hiker or two hikers. Every hiker must be assigned to exactly one raft. You may pair hikers in any way you want, as long as the weight limit is not exceeded.

This problem is designed to test whether you can recognize when sorting and a two-pointer strategy lead to an optimal greedy solution. A correct solution should be efficient enough for large inputs.

Constraints:
- 1 <= weights.length <= 100000
- 1 <= weights[i] <= limit <= 100000
- It is guaranteed that each individual hiker can fit on a raft alone.

Example 1:
Input: weights = [3, 5, 3, 4], limit = 5
Output: 4
Explanation: No valid pair can include the hiker with weight 5, and the two hikers with weight 3 cannot share a raft because 3 + 3 > 5. So each hiker must ride alone.

Example 2:
Input: weights = [1, 2, 2, 3], limit = 3
Output: 3
Explanation: One optimal assignment is (1,2), (2), and (3). That uses 3 rafts, which is the minimum possible.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n log n)
    - We first sort the array, which costs O(n log n).
    - Then we scan the array once using two pointers, which costs O(n).
    - The sorting step dominates, so the total is O(n log n).

    Space Complexity: O(1) extra space (ignoring the sorting implementation details)
    - We sort the input array in place.
    - We only use a few integer variables after that.
    */
    public int NumRescueRafts(int[] weights, int limit)
    {
        // Step 1: Sort the hikers by weight from lightest to heaviest.
        //
        // Why do we sort?
        // Sorting lets us make a smart greedy decision:
        // - Try to place the heaviest remaining hiker on a raft.
        // - If possible, pair them with the lightest remaining hiker.
        //
        // This works because the heaviest hiker is the hardest one to place.
        // If even the lightest hiker cannot share a raft with the heaviest,
        // then the heaviest must go alone.
        //
        // Data structure choice:
        // We use the input array itself and sort it in place.
        // This avoids needing extra collections and keeps the solution simple.
        Array.Sort(weights);

        // Step 2: Create two pointers.
        //
        // left  -> points to the lightest hiker not yet assigned to a raft
        // right -> points to the heaviest hiker not yet assigned to a raft
        //
        // We will move these pointers inward as hikers get assigned.
        int left = 0;
        int right = weights.Length - 1;

        // Step 3: Count how many rafts we use.
        int rafts = 0;

        // Step 4: Process hikers until all have been assigned.
        //
        // The condition left <= right means:
        // - If left < right, there are at least two hikers left.
        // - If left == right, exactly one hiker remains and still needs a raft.
        while (left <= right)
        {
            // Every loop iteration will use exactly one raft.
            //
            // Why?
            // Because we always place the heaviest remaining hiker (weights[right])
            // onto a raft right now. That hiker must be transported somehow,
            // so this raft is definitely needed.
            rafts++;

            // Step 4a: Check whether the lightest and heaviest remaining hikers
            // can share the same raft without exceeding the weight limit.
            //
            // If they can fit together:
            // - Pair them
            // - Move both pointers inward
            //
            // If they cannot fit together:
            // - The heaviest hiker must go alone
            // - Only move the right pointer
            //
            // Why is this greedy choice correct?
            // The heaviest hiker is the most restrictive person to place.
            // If the lightest person cannot fit with the heaviest, then nobody can,
            // because everyone else is heavier than the lightest.
            if (weights[left] + weights[right] <= limit)
            {
                // The pair is valid, so both hikers are now assigned.
                left++;
                right--;
            }
            else
            {
                // The heaviest hiker cannot share with the lightest.
                // Therefore, the heaviest must ride alone.
                right--;
            }
        }

        // Step 5: Return the total number of rafts used.
        return rafts;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] weights1 = { 3, 5, 3, 4 };
int limit1 = 5;
int result1 = solution.NumRescueRafts(weights1, limit1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 4

// Example 2
int[] weights2 = { 1, 2, 2, 3 };
int limit2 = 3;
int result2 = solution.NumRescueRafts(weights2, limit2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 3

// Additional demo
int[] weights3 = { 1, 2 };
int limit3 = 3;
int result3 = solution.NumRescueRafts(weights3, limit3);
Console.WriteLine("Additional Example Result: " + result3); // Expected: 1