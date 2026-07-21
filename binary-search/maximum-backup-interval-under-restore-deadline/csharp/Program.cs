/*
Title: Maximum Backup Interval Under Restore Deadline
Difficulty: Hard
Topic: Binary Search

Problem Description:
A storage team wants to reduce the number of full backups taken for a large database. The database changes over time, and the amount of changed data on day i is given by changes[i]. If the team chooses a backup interval of k days, then every backup taken on day s must include the total changed data from days s through min(s + k - 1, n - 1). Restoring from any single backup is only allowed if the size of that backup does not exceed a system restore limit L.

The team always partitions the timeline into consecutive backup blocks of length at most k: the first backup covers days 0 to k - 1, the next covers the following k days, and so on. A backup interval k is considered feasible if every such backup block has total changed data at most L.

Your task is to return the maximum feasible backup interval k.

In other words, among all positive integers k, find the largest k such that for every block formed by splitting the array into consecutive chunks of size k (the last chunk may be shorter), the sum of each chunk is at most L.

This problem is designed to reward an efficient solution. A brute-force check over all k values and all ranges will be too slow for the largest inputs. You should exploit the monotonic nature of feasibility with respect to k and combine binary search with fast range-sum validation.

Constraints:
- 1 <= n == changes.length <= 200000
- 0 <= changes[i] <= 1000000000
- 0 <= L <= 1000000000000000000
- Return 0 if no positive backup interval is feasible

Example 1:
Input: changes = [2, 1, 3, 2, 2], L = 6
Output: 3
Explanation:
- k = 2 gives blocks [2,1], [3,2], [2] with sums 3, 5, 2, all <= 6.
- k = 3 gives blocks [2,1,3], [2,2] with sums 6 and 4, also feasible.
- k = 4 gives blocks [2,1,3,2], [2] with sums 8 and 2, so it is not feasible.
The maximum feasible interval is 3.

Example 2:
Input: changes = [7, 1, 2], L = 6
Output: 0
Explanation:
Even k = 1 is not feasible because the first backup would contain 7 units of changed data, which exceeds the restore limit.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Building the prefix sum array takes O(n).
    - Each feasibility check for a candidate k scans the array in jumps of size k, which is O(n / k) blocks,
      and each block sum is computed in O(1) using prefix sums.
    - We binary search over k from 1 to n, so there are O(log n) candidate checks.
    - Overall worst-case complexity is O(n + n log n), which is commonly described as O(n log n).

    Space Complexity:
    - O(n) for the prefix sum array.
    */
    public int MaximumBackupInterval(int[] changes, long L)
    {
        int n = changes.Length;

        // Step 1: Build a prefix sum array.
        //
        // Why we need this:
        // We must repeatedly ask questions like:
        // "What is the sum of changes from index left to index right?"
        //
        // If we computed each block sum by looping through the block every time,
        // the solution would become too slow for large inputs.
        //
        // Prefix sum idea:
        // prefix[i] will store the total of the first i elements.
        // So:
        // - prefix[0] = 0
        // - prefix[1] = changes[0]
        // - prefix[2] = changes[0] + changes[1]
        // - ...
        //
        // Then the sum of a range [left..right] can be computed as:
        // prefix[right + 1] - prefix[left]
        //
        // This makes each range-sum query O(1), which is essential for efficiency.
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++)
        {
            prefix[i + 1] = prefix[i] + changes[i];
        }

        // Step 2: Quick rejection for k = 1.
        //
        // Why this matters:
        // If even a single day exceeds the limit L, then k = 1 is not feasible.
        // Since every larger k contains blocks that are at least as large in total
        // (because values are non-negative), no positive k can be feasible.
        //
        // In that case, the required answer is 0.
        if (!IsFeasible(1, prefix, n, L))
        {
            return 0;
        }

        // Step 3: Binary search for the maximum feasible k.
        //
        // Important monotonic property:
        // Because all changes[i] are non-negative, if some k is feasible,
        // then every smaller k is also feasible.
        //
        // Why?
        // Smaller k means we split the array into smaller chunks.
        // A smaller chunk cannot have a larger sum than a larger chunk that contains it,
        // since all numbers are non-negative.
        //
        // Therefore feasibility looks like:
        // true, true, true, ..., true, false, false, ...
        //
        // This is exactly the pattern binary search needs.
        int left = 1;
        int right = n;
        int answer = 1;

        while (left <= right)
        {
            // Standard binary search midpoint calculation.
            // We use this form to avoid overflow in general.
            int mid = left + (right - left) / 2;

            // Check whether this candidate interval length mid is feasible.
            if (IsFeasible(mid, prefix, n, L))
            {
                // If mid works, it is a valid answer.
                // But we want the maximum feasible k, so we try larger values.
                answer = mid;
                left = mid + 1;
            }
            else
            {
                // If mid does not work, then any larger k also cannot work
                // due to the monotonic property.
                // So we search the smaller half.
                right = mid - 1;
            }
        }

        return answer;
    }

    private bool IsFeasible(int k, long[] prefix, int n, long L)
    {
        // This method checks whether a specific backup interval k is valid.
        //
        // The rules say we partition the timeline into consecutive blocks of size k:
        // [0..k-1], [k..2k-1], [2k..3k-1], ...
        // and the last block may be shorter.
        //
        // For k to be feasible, EVERY block sum must be <= L.
        //
        // Thanks to prefix sums, each block sum is computed in O(1).

        // We move start through the array one block at a time.
        for (int start = 0; start < n; start += k)
        {
            // The block ends at either start + k - 1 or the last index n - 1,
            // whichever comes first.
            int endExclusive = Math.Min(start + k, n);

            // Sum of block [start..endExclusive-1]
            // using prefix sums:
            // prefix[endExclusive] - prefix[start]
            long blockSum = prefix[endExclusive] - prefix[start];

            // If any block exceeds the restore limit, this k is not feasible.
            if (blockSum > L)
            {
                return false;
            }
        }

        // If we checked all blocks and none exceeded L, then k is feasible.
        return true;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] changes1 = { 2, 1, 3, 2, 2 };
long L1 = 6;
int result1 = solution.MaximumBackupInterval(changes1, L1);
Console.WriteLine(result1); // Expected: 3

// Example 2
int[] changes2 = { 7, 1, 2 };
long L2 = 6;
int result2 = solution.MaximumBackupInterval(changes2, L2);
Console.WriteLine(result2); // Expected: 0

// Additional quick sanity checks
int[] changes3 = { 1, 1, 1, 1 };
long L3 = 4;
int result3 = solution.MaximumBackupInterval(changes3, L3);
Console.WriteLine(result3); // Expected: 4

int[] changes4 = { 5, 0, 0, 0 };
long L4 = 5;
int result4 = solution.MaximumBackupInterval(changes4, L4);
Console.WriteLine(result4); // Expected: 4

int[] changes5 = { 5, 1, 0, 0 };
long L5 = 5;
int result5 = solution.MaximumBackupInterval(changes5, L5);
Console.WriteLine(result5); // Expected: 1