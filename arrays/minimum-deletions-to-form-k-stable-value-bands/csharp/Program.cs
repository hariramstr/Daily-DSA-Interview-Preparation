/*
Title: Minimum Deletions to Form K Stable Value Bands

Problem Description:
You are given an integer array nums of length n and an integer k. A subsequence of nums is called stable if the difference between its maximum and minimum value is at most 1. You may delete any number of elements from nums, and the remaining elements must be partitioned into exactly k non-empty stable groups. Each remaining element must belong to exactly one group, and the order of elements inside a group does not matter. Your task is to return the minimum number of deletions required so that such a partition is possible. If it is impossible to form exactly k non-empty stable groups from any subsequence of nums, return -1.

A stable group may contain repeated values, and it may also contain two adjacent values such as x and x+1, but it cannot contain values whose difference is 2 or more. Note that the groups are defined by values, not by contiguous positions in the original array. In other words, after deleting elements, you are free to assign the remaining elements into groups in any way that respects the stability rule.

Constraints:
- 1 <= n <= 200000
- -10^9 <= nums[i] <= 10^9
- 1 <= k <= n

Example 1:
Input: nums = [1,1,2,2,3,5,5], k = 3
Output: 1
Explanation: Delete the value 3. The remaining elements can be partitioned into three stable groups: [1,1,2,2], [5], [5]. Each group has max-min <= 1, so the answer is 1.

Example 2:
Input: nums = [4,4,4,7,8], k = 2
Output: 0
Explanation: No deletion is needed. One valid partition is [4,4,4] and [7,8]. Both groups are stable, non-empty, and together use all remaining elements.
*/

using System;
using System.Collections.Generic;
using System.Linq;

public class Solution
{
    /*
    Time Complexity:
    - Counting frequencies: O(n)
    - Sorting distinct values: O(m log m), where m is the number of distinct values
    - Dynamic programming over the sorted distinct values: O(m)
    Overall: O(n + m log m), which is efficient for n up to 200000.

    Space Complexity:
    - Frequency map + arrays + DP tables: O(m)

    Key idea:
    We want to keep as many elements as possible, because:
        minimum deletions = n - maximum kept elements

    So the real problem becomes:
        "What is the maximum number of elements we can keep such that the kept elements
         can be partitioned into exactly k non-empty stable groups?"

    Important observation about stable groups:
    - A group may contain only one value x
    - Or two adjacent values x and x+1
    - It can never contain values with gap >= 2

    Another crucial observation:
    For any chosen set of kept values, the minimum number of stable groups needed is determined
    by how values connect through adjacency:
    - If two kept values differ by at least 2, they must belong to different connected components.
    - Inside one connected component of consecutive kept values, suppose it contains L distinct values.
      Then:
         * minimum groups needed = ceil(L / 2)
           because one group can cover at most two consecutive values
         * maximum groups possible = total number of kept elements in that component
           because every element can be split into its own singleton group

    Therefore, for each connected component of consecutive values, if we keep all occurrences of
    those values, that component contributes:
        - weight = total number of kept elements in the component
        - minGroups = ceil(length / 2)
        - maxGroups = weight

    Since different components are separated by gaps >= 2, they are independent and their group
    counts simply add.

    So we process the sorted distinct values and build DP over components incrementally.
    At each distinct value, we decide:
    - skip it entirely
    - start a new component with it
    - extend the current open consecutive component

    To make this efficient, we keep DP states for:
    - "closed" components: no currently open consecutive run
    - "open odd length" component ending at previous value
    - "open even length" component ending at previous value

    Why only parity matters for the open run:
    - For a run of consecutive distinct values of length L:
        minGroups = ceil(L / 2)
      When we extend the run by one more distinct value:
        if previous L was odd, minGroups increases by 1
        if previous L was even, minGroups stays the same
      So parity is enough to know how minGroups changes.
    */
    public int MinimumDeletions(int[] nums, int k)
    {
        int n = nums.Length;

        // If we need more groups than elements, impossible.
        // Here constraints guarantee k <= n, but this check is still harmless and clear.
        if (k > n) return -1;

        // ------------------------------------------------------------
        // STEP 1: Count how many times each value appears.
        // ------------------------------------------------------------
        // Why?
        // The order of elements does not matter at all in this problem.
        // Only the values and how many copies of each value exist matter.
        //
        // Example:
        // [1,2,1,2] behaves exactly the same as [1,1,2,2] for this problem.
        //
        // So we compress the array into:
        // value -> frequency
        var freq = new Dictionary<int, int>();
        foreach (int x in nums)
        {
            if (!freq.TryAdd(x, 1))
            {
                freq[x]++;
            }
        }

        // ------------------------------------------------------------
        // STEP 2: Sort the distinct values.
        // ------------------------------------------------------------
        // Why?
        // Stability depends on value differences.
        // In particular, adjacency of values (x and x+1) is extremely important.
        // Sorting lets us process consecutive values from left to right.
        var values = freq.Keys.ToList();
        values.Sort();

        int m = values.Count;

        // ------------------------------------------------------------
        // DP arrays:
        //
        // closed[g]   = maximum kept elements after processing some prefix of values,
        //               using exactly g groups in fully closed components,
        //               and with NO open consecutive component waiting to be extended.
        //
        // openOdd[g]  = maximum kept elements after processing some prefix,
        //               using exactly g groups as the current minimum required groups,
        //               and having an OPEN consecutive component ending at the last processed value,
        //               whose number of distinct values is odd.
        //
        // openEven[g] = same, but open component length parity is even.
        //
        // Value stored:
        // - maximum number of kept elements achievable for that state
        //
        // Invalid states are stored as NEG.
        // ------------------------------------------------------------
        const int NEG = int.MinValue / 4;

        int[] closed = new int[k + 1];
        int[] openOdd = new int[k + 1];
        int[] openEven = new int[k + 1];

        Array.Fill(closed, NEG);
        Array.Fill(openOdd, NEG);
        Array.Fill(openEven, NEG);

        // Base case:
        // Before processing any values:
        // - 0 groups used
        // - 0 elements kept
        // - no open component
        closed[0] = 0;

        int previousValue = 0;
        bool hasPrevious = false;

        // ------------------------------------------------------------
        // STEP 3: Process each distinct value in sorted order.
        // ------------------------------------------------------------
        foreach (int v in values)
        {
            int count = freq[v];

            // New arrays for the next step.
            int[] nextClosed = new int[k + 1];
            int[] nextOpenOdd = new int[k + 1];
            int[] nextOpenEven = new int[k + 1];

            Array.Fill(nextClosed, NEG);
            Array.Fill(nextOpenOdd, NEG);
            Array.Fill(nextOpenEven, NEG);

            bool isConsecutiveToPrevious = hasPrevious && v == previousValue + 1;

            // --------------------------------------------------------
            // If current value is NOT consecutive to the previous value,
            // then any open component from the previous step can no longer
            // be extended. It must be closed first.
            //
            // Why?
            // Because a stable group can only mix x with x+1.
            // If there is a gap >= 2, the old component and the new value
            // belong to different connected components.
            // --------------------------------------------------------
            if (!isConsecutiveToPrevious)
            {
                for (int g = 0; g <= k; g++)
                {
                    if (openOdd[g] > closed[g]) closed[g] = openOdd[g];
                    if (openEven[g] > closed[g]) closed[g] = openEven[g];
                }

                // Once we force-close because of a gap, there is no open component left.
                Array.Fill(openOdd, NEG);
                Array.Fill(openEven, NEG);
            }

            // --------------------------------------------------------
            // Transition 1: Skip the current value entirely.
            //
            // Why is this allowed?
            // We may delete any number of elements.
            // Skipping a distinct value means deleting all its occurrences.
            //
            // This is important because sometimes keeping a value would force
            // extra groups or create an impossible structure.
            // --------------------------------------------------------
            for (int g = 0; g <= k; g++)
            {
                if (closed[g] > nextClosed[g]) nextClosed[g] = closed[g];
                if (openOdd[g] > nextOpenOdd[g]) nextOpenOdd[g] = openOdd[g];
                if (openEven[g] > nextOpenEven[g]) nextOpenEven[g] = openEven[g];
            }

            // --------------------------------------------------------
            // Transition 2: Start a new component with the current value.
            //
            // This can only come from a CLOSED state.
            //
            // A component containing just one distinct value:
            // - length = 1 (odd)
            // - minGroups = 1
            // - kept elements added = count
            //
            // So:
            // closed[g] -> openOdd[g + 1] with +count kept elements
            // --------------------------------------------------------
            for (int g = 0; g < k; g++)
            {
                if (closed[g] == NEG) continue;

                int candidate = closed[g] + count;
                if (candidate > nextOpenOdd[g + 1])
                {
                    nextOpenOdd[g + 1] = candidate;
                }
            }

            // --------------------------------------------------------
            // Transition 3: Extend an existing open component with current value.
            //
            // This is only valid if current value is consecutive to previous value.
            //
            // Suppose current open component has length L before extension.
            // After adding one more consecutive distinct value:
            // - kept elements increase by count
            // - parity flips
            // - minGroups changes as follows:
            //     * if L was odd: ceil((L+1)/2) = ceil(L/2) + 1
            //       so group count increases by 1
            //     * if L was even: ceil((L+1)/2) = ceil(L/2)
            //       so group count stays the same
            //
            // Therefore:
            // - openOdd[g]  -> openEven[g + 1]
            // - openEven[g] -> openOdd[g]
            // --------------------------------------------------------
            if (isConsecutiveToPrevious)
            {
                for (int g = 0; g <= k; g++)
                {
                    if (openOdd[g] != NEG && g + 1 <= k)
                    {
                        int candidate = openOdd[g] + count;
                        if (candidate > nextOpenEven[g + 1])
                        {
                            nextOpenEven[g + 1] = candidate;
                        }
                    }

                    if (openEven[g] != NEG)
                    {
                        int candidate = openEven[g] + count;
                        if (candidate > nextOpenOdd[g])
                        {
                            nextOpenOdd[g] = candidate;
                        }
                    }
                }
            }

            // Move to next iteration.
            closed = nextClosed;
            openOdd = nextOpenOdd;
            openEven = nextOpenEven;

            previousValue = v;
            hasPrevious = true;
        }

        // ------------------------------------------------------------
        // STEP 4: Finalize any open component.
        //
        // After processing all values, an open component is perfectly valid;
        // we just need to consider it as closed for the final answer.
        // ------------------------------------------------------------
        for (int g = 0; g <= k; g++)
        {
            if (openOdd[g] > closed[g]) closed[g] = openOdd[g];
            if (openEven[g] > closed[g]) closed[g] = openEven[g];
        }

        // ------------------------------------------------------------
        // STEP 5: We now know the maximum number of kept elements whose
        // minimum required number of groups is exactly g = k.
        //
        // But wait: the problem asks for EXACTLY k groups, not minimum k groups.
        //
        // Why is using closed[k] enough?
        // Because if a chosen subsequence has minimum required groups = g,
        // then it can be partitioned into ANY number of groups between:
        //     g and total kept elements
        // by splitting groups further into smaller non-empty stable groups.
        //
        // So to achieve exactly k groups, we need:
        //     minimum required groups <= k <= kept elements
        //
        // Our DP state index g stores the minimum required groups.
        // Therefore, among all states with g <= k and kept >= k,
        // we can realize exactly k groups.
        //
        // We choose the one with maximum kept elements.
        // ------------------------------------------------------------
        int bestKept = NEG;
        for (int g = 0; g <= k; g++)
        {
            int kept = closed[g];
            if (kept == NEG) continue;

            // Need at least k kept elements to split into exactly k non-empty groups.
            if (kept >= k && kept > bestKept)
            {
                bestKept = kept;
            }
        }

        if (bestKept == NEG) return -1;

        return n - bestKept;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
int[] nums1 = { 1, 1, 2, 2, 3, 5, 5 };
int k1 = 3;
int result1 = solution.MinimumDeletions(nums1, k1);
Console.WriteLine(result1); // Expected: 1

// Example 2
int[] nums2 = { 4, 4, 4, 7, 8 };
int k2 = 2;
int result2 = solution.MinimumDeletions(nums2, k2);
Console.WriteLine(result2); // Expected: 0

// Additional quick checks
int[] nums3 = { 1 };
int k3 = 1;
Console.WriteLine(solution.MinimumDeletions(nums3, k3)); // Expected: 0

int[] nums4 = { 1, 3 };
int k4 = 1;
Console.WriteLine(solution.MinimumDeletions(nums4, k4)); // Expected: 1

int[] nums5 = { 1, 3 };
int k5 = 2;
Console.WriteLine(solution.MinimumDeletions(nums5, k5)); // Expected: 0