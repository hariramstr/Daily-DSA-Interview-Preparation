/*
Title: Minimum Launch Day for Staggered Marketing Campaigns

Problem Description:
A company is preparing n marketing campaigns. The i-th campaign becomes ready on day readyDay[i].
Once a campaign is ready, it stays ready forever.

The company wants to form exactly m launch groups, and each launch group must contain k consecutive
campaigns in the original lineup. A campaign can belong to at most one group.

Return the minimum day on which it is possible to form at least m such launch groups.
If it is impossible even after all campaigns are ready, return -1.

Important idea:
- For any candidate day D, we can mark each campaign as "ready" if readyDay[i] <= D.
- Then we check whether we can extract at least m non-overlapping groups of size k using only
  consecutive ready campaigns.
- This feasibility is monotonic:
    * If day D works, then any later day also works.
    * If day D does not work, then any earlier day also does not work.
- Because of that monotonic behavior, binary search on the answer is the correct efficient approach.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Let n be the number of campaigns.
    - Each feasibility check scans the array once: O(n).
    - Binary search runs over the day range, at most about log2(1_000_000_000) ~ 31 steps.
    - Total: O(n log M), where M is the range of days.

    Space Complexity:
    - O(1) extra space.
    - We only use a few counters and variables.
    */
    public int MinDays(int[] readyDay, int m, int k)
    {
        // Step 1:
        // Before doing any binary search, we perform an immediate impossibility check.
        //
        // Why?
        // Each group needs exactly k campaigns.
        // To form m groups, we need at least m * k total campaigns.
        // If the array length is smaller than that, then no arrangement can ever succeed,
        // regardless of the day, because there simply are not enough campaigns.
        //
        // We cast to long to avoid integer overflow when multiplying m * k.
        if ((long)m * k > readyDay.Length)
        {
            return -1;
        }

        // Step 2:
        // Determine the search boundaries for binary search.
        //
        // The earliest possible answer cannot be smaller than the minimum ready day in the array.
        // The latest possible answer does not need to be larger than the maximum ready day in the array,
        // because by that day every campaign is ready.
        int left = int.MaxValue;
        int right = int.MinValue;

        foreach (int day in readyDay)
        {
            if (day < left) left = day;
            if (day > right) right = day;
        }

        // Step 3:
        // Binary search for the minimum feasible day.
        //
        // Invariant:
        // - If a day is feasible, we try to search smaller days to find the minimum.
        // - If a day is not feasible, we must search larger days.
        while (left < right)
        {
            // Use this form to avoid overflow:
            // mid = left + (right - left) / 2
            int mid = left + (right - left) / 2;

            // Check whether it is possible to form at least m groups by day = mid.
            if (CanMakeGroups(readyDay, m, k, mid))
            {
                // mid works, so the answer is <= mid.
                // Keep mid in the search space because it might be the minimum valid day.
                right = mid;
            }
            else
            {
                // mid does not work, so the answer must be > mid.
                left = mid + 1;
            }
        }

        // When the loop ends, left == right, and that value is the minimum feasible day.
        return left;
    }

    private bool CanMakeGroups(int[] readyDay, int m, int k, int candidateDay)
    {
        // This method answers:
        // "By candidateDay, can we form at least m non-overlapping groups,
        // each consisting of k consecutive ready campaigns?"

        // groupsFormed:
        // Counts how many valid groups we have successfully created so far.
        int groupsFormed = 0;

        // consecutiveReady:
        // Counts the current streak of consecutive campaigns that are ready by candidateDay.
        //
        // Why do we track a streak?
        // Because groups must be formed from adjacent indices.
        // If we encounter a campaign that is not ready, the streak is broken and must reset to 0.
        int consecutiveReady = 0;

        // Scan the campaigns from left to right.
        for (int i = 0; i < readyDay.Length; i++)
        {
            // If this campaign is ready by candidateDay, it can contribute to a consecutive block.
            if (readyDay[i] <= candidateDay)
            {
                consecutiveReady++;

                // Once the streak reaches size k, we can form one group.
                if (consecutiveReady == k)
                {
                    groupsFormed++;

                    // Important:
                    // We reset consecutiveReady to 0 because campaigns cannot be reused.
                    // This enforces non-overlapping groups.
                    //
                    // Example:
                    // If k = 2 and we have a ready streak [ready, ready, ready, ready],
                    // then we should form groups as:
                    //   first two -> one group
                    //   next two  -> second group
                    // Resetting to 0 after each group naturally achieves that.
                    consecutiveReady = 0;

                    // Early exit optimization:
                    // As soon as we have formed at least m groups, we can stop.
                    // No need to scan the rest of the array.
                    if (groupsFormed >= m)
                    {
                        return true;
                    }
                }
            }
            else
            {
                // This campaign is not ready by candidateDay.
                // Therefore, any consecutive block is interrupted here.
                // We must reset the streak.
                consecutiveReady = 0;
            }
        }

        // If we finish scanning and still have fewer than m groups, candidateDay is not feasible.
        return false;
    }
}

// Demo code

var solution = new Solution();

// Example 1 from the prompt.
// Correct reasoning:
// readyDay = [5, 2, 4, 6, 3, 7], m = 2, k = 2
//
// Day 5 => ready status:
// index:  0  1  2  3  4  5
// day:    5  2  4  6  3  7
// ready:  T  T  T  F  T  F
//
// Consecutive ready streaks:
// [0,1,2] is a streak of length 3 -> can form only 1 non-overlapping group of size 2
// index 3 breaks the streak
// index 4 alone is length 1 -> no group
// So day 5 is NOT enough.
//
// Day 6 => ready status becomes:
// T T T T T F
//
// Now indices [0,1,2,3,4] form a streak of length 5.
// From that, we can form 2 non-overlapping groups of size 2:
// [0,1] and [2,3]
// Therefore the minimum day is 6.
int[] readyDay1 = { 5, 2, 4, 6, 3, 7 };
int m1 = 2;
int k1 = 2;
int result1 = solution.MinDays(readyDay1, m1, k1);
Console.WriteLine(result1); // Expected: 6

// Example 2 from the prompt.
// readyDay = [1, 10, 3, 10, 2], m = 3, k = 1
//
// Since each group needs only 1 campaign, we just need 3 ready campaigns by that day.
// Day 3 gives ready campaigns at indices 0, 2, and 4.
// That is 3 groups of size 1, so answer is 3.
int[] readyDay2 = { 1, 10, 3, 10, 2 };
int m2 = 3;
int k2 = 1;
int result2 = solution.MinDays(readyDay2, m2, k2);
Console.WriteLine(result2); // Expected: 3

// Additional demo: impossible case because m * k > n.
int[] readyDay3 = { 1, 2, 3, 4 };
int m3 = 3;
int k3 = 2;
int result3 = solution.MinDays(readyDay3, m3, k3);
Console.WriteLine(result3); // Expected: -1