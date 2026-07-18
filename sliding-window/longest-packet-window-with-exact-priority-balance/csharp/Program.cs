/*
Title: Longest Packet Window With Exact Priority Balance
Difficulty: Hard
Topic: Sliding Window

Problem Description:
A network monitor records a stream of packet priorities in an integer array priorities, where each value is in the range [1, m]. You are also given an integer array target of length m, where target[i] represents the exact number of packets with priority i + 1 that must appear inside a valid window. A contiguous window is called balanced if, for every priority level p from 1 to m, the count of p inside the window is exactly target[p - 1].

However, the monitor is noisy: packets with priority values greater than m are considered corrupted and may appear in the stream. A balanced window cannot contain any corrupted packet. Your task is to return the length of the longest contiguous balanced window. If no such window exists, return 0.

Because the target counts are exact, a valid answer is not just any subarray with bounded frequencies. The window must match the full frequency profile exactly and contain no extra valid-priority packets beyond the required counts. This makes the problem subtle when many repeated values and corrupted packets split the stream into candidate regions.

Design an algorithm efficient enough for large inputs.

Constraints:
- 1 <= priorities.length <= 200000
- 1 <= m <= 100000
- 0 <= target[i] <= priorities.length
- 1 <= priorities[i] <= 1000000000
- The sum of all target values may be 0

Example 1:
Input: priorities = [1,2,1,3,2,1,2], target = [2,2,1]
Output: 5
Explanation: The window [1,2,1,3,2] contains priority 1 exactly twice, priority 2 exactly twice, and priority 3 exactly once. No longer valid window exists.

Example 2:
Input: priorities = [4,1,2,1,3,2,5,1,2,3], target = [1,1,1]
Output: 3
Explanation: Corrupted values 4 and 5 break possible windows because only priorities 1 through 3 are allowed. Valid windows include [1,3,2] and [1,2,3], each of length 3. Since target requires exactly one of each allowed priority, the answer is 3.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    O(n + m)
    - We scan the target array once to compute the required total length.
    - We scan the priorities array once with a sliding window.
    - Each element enters and leaves the window at most one time.

    Space Complexity:
    O(m)
    - We store frequency counts for priorities 1..m.
    */
    public int LongestBalancedWindow(int[] priorities, int[] target)
    {
        // Step 1:
        // Determine how many total packets a valid balanced window must contain.
        //
        // Why this is important:
        // A valid window must match the target counts EXACTLY.
        // Therefore, its length is not flexible:
        // it must be exactly sum(target).
        //
        // This observation is the key simplification of the problem.
        // Even though the statement asks for the "longest" balanced window,
        // every balanced window has the same length: the total required count.
        // So the problem becomes:
        // "Does there exist a contiguous window of length sum(target)
        //  whose frequency profile matches target exactly and contains no corrupted values?"
        long requiredLengthLong = 0;
        for (int i = 0; i < target.Length; i++)
        {
            requiredLengthLong += target[i];
        }

        int requiredLength = (int)requiredLengthLong;

        // Special case:
        // If the total required count is 0, then the exact target says:
        // every valid priority must appear zero times.
        //
        // The only window that satisfies this is the empty window, whose length is 0.
        // Since the problem asks for the length of the longest balanced window,
        // the answer is 0.
        if (requiredLength == 0)
        {
            return 0;
        }

        int n = priorities.Length;
        int m = target.Length;

        // If the required window length is larger than the entire array,
        // no valid window can possibly exist.
        if (requiredLength > n)
        {
            return 0;
        }

        // Step 2:
        // We maintain counts of priorities currently inside the sliding window.
        //
        // Data structure choice:
        // We use an int array of size m + 1 so that priority value p
        // can be accessed directly at index p.
        //
        // This is faster and simpler than a dictionary because priorities
        // that matter are exactly 1..m.
        int[] windowCount = new int[m + 1];

        // Step 3:
        // We need an efficient way to know whether the current window matches
        // the target counts exactly.
        //
        // A naive approach would compare all m counts for every window,
        // which would be O(n * m) and too slow.
        //
        // Instead, we keep a single number called "matchedTypes":
        // how many priority values p currently satisfy:
        // windowCount[p] == target[p - 1]
        //
        // If matchedTypes == m, then every priority count matches exactly.
        int matchedTypes = 0;

        // Initially the window is empty, so for each priority p:
        // if target[p - 1] == 0, then that priority is already matched.
        for (int p = 1; p <= m; p++)
        {
            if (target[p - 1] == 0)
            {
                matchedTypes++;
            }
        }

        // Step 4:
        // We also must ensure the window contains no corrupted values.
        //
        // A corrupted value is any priority > m.
        // (Values < 1 do not appear according to constraints, so we only check > m.)
        //
        // We maintain "corruptedCount" = number of corrupted packets in the current window.
        // A valid balanced window requires corruptedCount == 0.
        int corruptedCount = 0;

        // Helper local function:
        // Add one value into the current window and update all bookkeeping.
        void AddValue(int value)
        {
            // If the value is corrupted, it cannot participate in a valid window.
            // We simply count it.
            if (value > m)
            {
                corruptedCount++;
                return;
            }

            // For a valid priority value in 1..m:
            // Before changing its count, check whether it was previously matched.
            // If yes, adding one will break that match, so matchedTypes decreases.
            if (windowCount[value] == target[value - 1])
            {
                matchedTypes--;
            }

            // Actually add the value into the window.
            windowCount[value]++;

            // After the increment, maybe the count now matches the target again.
            // If so, matchedTypes increases.
            if (windowCount[value] == target[value - 1])
            {
                matchedTypes++;
            }
        }

        // Helper local function:
        // Remove one value from the current window and update all bookkeeping.
        void RemoveValue(int value)
        {
            // If the value is corrupted, removing it just decreases corruptedCount.
            if (value > m)
            {
                corruptedCount--;
                return;
            }

            // Before changing its count, check whether it is currently matched.
            // If yes, removing one will break that match first.
            if (windowCount[value] == target[value - 1])
            {
                matchedTypes--;
            }

            // Actually remove the value from the window.
            windowCount[value]--;

            // After the decrement, maybe it matches the target again.
            if (windowCount[value] == target[value - 1])
            {
                matchedTypes++;
            }
        }

        // Step 5:
        // Build the first window of exact length requiredLength.
        for (int i = 0; i < requiredLength; i++)
        {
            AddValue(priorities[i]);
        }

        // Step 6:
        // Check whether the first window is valid.
        //
        // A window is balanced if:
        // 1) it has no corrupted values
        // 2) every priority 1..m matches its exact target count
        //
        // Since the window length is fixed to sum(target),
        // if all counts for 1..m match target, then there cannot be any extra valid packets.
        // The exact profile is satisfied.
        if (corruptedCount == 0 && matchedTypes == m)
        {
            return requiredLength;
        }

        // Step 7:
        // Slide the window one step at a time across the array.
        //
        // For each new position:
        // - remove the leftmost element
        // - add the new rightmost element
        // - test validity in O(1)
        for (int right = requiredLength; right < n; right++)
        {
            int left = right - requiredLength;

            RemoveValue(priorities[left]);
            AddValue(priorities[right]);

            if (corruptedCount == 0 && matchedTypes == m)
            {
                return requiredLength;
            }
        }

        // If no exact-match window was found, answer is 0.
        return 0;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] priorities1 = { 1, 2, 1, 3, 2, 1, 2 };
int[] target1 = { 2, 2, 1 };
int result1 = solution.LongestBalancedWindow(priorities1, target1);
Console.WriteLine(result1); // Expected: 5

// Example 2
int[] priorities2 = { 4, 1, 2, 1, 3, 2, 5, 1, 2, 3 };
int[] target2 = { 1, 1, 1 };
int result2 = solution.LongestBalancedWindow(priorities2, target2);
Console.WriteLine(result2); // Expected: 3

// Additional quick checks

// No valid window because corrupted value splits and no exact match exists
int[] priorities3 = { 1, 4, 2, 3 };
int[] target3 = { 1, 1, 1 };
int result3 = solution.LongestBalancedWindow(priorities3, target3);
Console.WriteLine(result3); // Expected: 0

// Target sum is zero, so answer is 0
int[] priorities4 = { 1, 2, 3 };
int[] target4 = { 0, 0, 0 };
int result4 = solution.LongestBalancedWindow(priorities4, target4);
Console.WriteLine(result4); // Expected: 0