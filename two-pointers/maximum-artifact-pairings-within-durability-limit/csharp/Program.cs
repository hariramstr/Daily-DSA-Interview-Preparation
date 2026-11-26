/*
Title: Maximum Artifact Pairings Within Durability Limit
Difficulty: Hard
Topic: Two Pointers

Problem Description:
A museum is preparing fragile artifacts for transport. You are given an integer array durability where durability[i] is the durability score of the i-th artifact, and an integer limit. A shipping crate may contain either one artifact or exactly two artifacts. If two artifacts with durabilities a and b are packed together, the pair is valid only if |a - b| <= limit. Each artifact can be used in at most one crate.

Your task is to maximize the number of 2-artifact crates that can be formed.

Unlike simpler pairing problems, the artifacts are not pre-sorted, durability scores may repeat many times, and the answer must be computed efficiently for very large inputs. Returning the maximum number of valid pairs is enough; you do not need to output the actual pairing.

A pair uses two distinct indices. Single-artifact crates are allowed for leftovers, but they do not contribute to the answer. The challenge is to decide which artifacts to match so that local greedy choices do not reduce the total number of valid pairs.

Constraints:
- 1 <= durability.length <= 2 * 10^5
- 0 <= durability[i] <= 10^9
- 0 <= limit <= 10^9

Example 1:
Input: durability = [8, 1, 5, 6, 2, 9], limit = 1
Output: 2
Explanation: After sorting, one optimal pairing is (1,2) and (5,6). Artifact 8 and 9 cannot both be paired because using one of them blocks a better total arrangement.

Example 2:
Input: durability = [4, 4, 4, 7, 8, 9, 9], limit = 0
Output: 2
Explanation: Only equal durabilities can be paired. We can form two pairs from the three artifacts with durability 4 and the two artifacts with durability 9. The artifact with durability 7 and the artifact with durability 8 remain alone.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n log n)
    - We sort the array once, which costs O(n log n).
    - Then we scan the sorted array once with a single pointer loop, which costs O(n).
    - Overall: O(n log n).

    Space Complexity: O(1) extra space (ignoring the sorting implementation details)
    - We sort the input array in place.
    - We only use a few integer variables after sorting.
    */
    public int MaxArtifactPairs(int[] durability, int limit)
    {
        // If there are fewer than 2 artifacts, it is impossible to form even one pair.
        // This quick check is not strictly necessary because the main loop would also handle it,
        // but it makes the intent very clear for beginners.
        if (durability == null || durability.Length < 2)
        {
            return 0;
        }

        // Step 1: Sort the durability values.
        //
        // Why sorting helps:
        // ------------------
        // The condition for a valid pair is:
        //     |a - b| <= limit
        //
        // After sorting, for any two positions i < j:
        //     durability[j] >= durability[i]
        //
        // So the absolute difference becomes:
        //     durability[j] - durability[i]
        //
        // This is much easier to reason about than unsorted values.
        //
        // Most importantly, sorting reveals a key greedy fact:
        // If two neighboring values in sorted order can be paired, then pairing them is always safe.
        // Why? Because neighboring values are the "closest" available values. Using a close match now
        // does not reduce the number of future matches compared with skipping it and trying to match
        // one of them with something farther away.
        //
        // This transforms the problem into:
        // "Walk left to right through the sorted array and greedily pair adjacent elements whenever valid."
        Array.Sort(durability);

        // This variable will count how many valid 2-artifact crates we can form.
        int pairs = 0;

        // We use an index to scan the sorted array from left to right.
        int i = 0;

        // Step 2: Greedy scan through the sorted array.
        //
        // At each step, we look at the current artifact durability[i].
        // The best candidate to pair with it is durability[i + 1], the next artifact in sorted order.
        //
        // Why only check the next one?
        // ----------------------------
        // Because in sorted order, the next element is the closest possible unused value to durability[i].
        // If durability[i] cannot pair with durability[i + 1], then it also cannot pair with any later element,
        // since later elements are even larger and therefore have an equal or larger difference.
        //
        // That gives us two cases:
        //
        // 1) If durability[i + 1] - durability[i] <= limit:
        //      We should pair them.
        //      This is greedy and optimal.
        //
        // 2) Otherwise:
        //      durability[i] cannot pair with anyone at all.
        //      So we must leave it as a single-artifact crate and move on.
        //
        // This logic guarantees the maximum number of pairs.
        while (i < durability.Length - 1)
        {
            // Compute the difference between the current artifact and the next artifact.
            // Because the array is sorted, this difference is non-negative.
            int difference = durability[i + 1] - durability[i];

            // If the adjacent pair satisfies the durability limit, we form a pair.
            if (difference <= limit)
            {
                // We found one valid 2-artifact crate.
                pairs++;

                // Since both artifacts are now used, we skip both of them.
                i += 2;
            }
            else
            {
                // The current artifact cannot pair with the next one.
                //
                // Because the array is sorted, every later artifact is >= durability[i + 1],
                // so every later difference will be:
                //     durability[later] - durability[i] >= durability[i + 1] - durability[i]
                //
                // Therefore, if the next one already fails the limit, all later ones fail too.
                //
                // That means durability[i] can never be part of any valid pair.
                // We leave it unpaired and move to the next artifact.
                i++;
            }
        }

        // After scanning the array, 'pairs' contains the maximum number of valid pairs.
        return pairs;
    }
}

// Demo code:
// ----------
// We create the sample inputs from the problem statement,
// call the solution method, and print the results.

var solution = new Solution();

// Example 1
int[] durability1 = { 8, 1, 5, 6, 2, 9 };
int limit1 = 1;
int result1 = solution.MaxArtifactPairs(durability1, limit1);
Console.WriteLine(result1); // Expected: 2

// Example 2
int[] durability2 = { 4, 4, 4, 7, 8, 9, 9 };
int limit2 = 0;
int result2 = solution.MaxArtifactPairs(durability2, limit2);
Console.WriteLine(result2); // Expected: 2

// Additional quick checks

int[] durability3 = { 1 };
int limit3 = 10;
Console.WriteLine(solution.MaxArtifactPairs(durability3, limit3)); // Expected: 0

int[] durability4 = { 1, 100, 101, 102 };
int limit4 = 1;
Console.WriteLine(solution.MaxArtifactPairs(durability4, limit4)); // Expected: 1

int[] durability5 = { 3, 3, 3, 3, 3, 3 };
int limit5 = 0;
Console.WriteLine(solution.MaxArtifactPairs(durability5, limit5)); // Expected: 3