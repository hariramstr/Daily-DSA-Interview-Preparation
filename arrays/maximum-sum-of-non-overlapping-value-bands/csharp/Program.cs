/*
Title: Maximum Sum of Non-Overlapping Value Bands
Difficulty: Hard
Topic: Arrays

Problem Description:
You are given an integer array nums and an integer k. A value band is a contiguous subarray nums[l..r]
such that the difference between the maximum and minimum value inside that subarray is at most k.
You may choose any number of value bands, but no two chosen bands may overlap. The score of a chosen
band is the sum of its elements. Your task is to return the maximum total score obtainable by selecting
a set of non-overlapping value bands.

A band of length 1 is always valid. You are allowed to skip elements entirely if doing so increases
the total score. Note that even if a subarray satisfies the value-band condition, it may be better not
to take it if its sum is negative or if taking smaller bands leads to a larger total.

Design an algorithm that works efficiently for large inputs.

Constraints:
- 1 <= nums.length <= 2 * 10^5
- -10^9 <= nums[i] <= 10^9
- 0 <= k <= 10^9
- The answer fits in a signed 64-bit integer.

Examples:
1) nums = [4, 2, 3, 7, 6, 5], k = 2
   Output: 27
   Explanation: One optimal choice is [4, 2, 3] with sum 9 and [7, 6, 5] with sum 18.

2) nums = [5, -4, 6, 6, -2, 7], k = 1
   Correct Output: 24
   Explanation: Choose [5], [6, 6], and [7]. They are non-overlapping and individually valid.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n log n)
    Space Complexity: O(n)

    High-level idea:
    ----------------
    Let dp[i] = maximum total score we can obtain using only the first i elements
    (that is, from indices 0 to i - 1).

    Transition:
    - Skip nums[i - 1]: dp[i] = dp[i - 1]
    - Or end a chosen band at position i - 1:
        dp[i] = max over all valid starts l of:
                dp[l] + sum(nums[l..i-1])

    Using prefix sums:
        sum(nums[l..i-1]) = prefix[i] - prefix[l]
    so:
        dp[i] = max(dp[i - 1], prefix[i] + max over valid l of (dp[l] - prefix[l]))

    Therefore, for each right endpoint r = i - 1, we need:
        best value of (dp[l] - prefix[l]) among all starts l such that nums[l..r] is valid.

    The validity condition is:
        max(nums[l..r]) - min(nums[l..r]) <= k

    For each r, there is a smallest valid start leftBound[r], and every l >= leftBound[r]
    is also valid for the same r. This monotonic property lets us maintain a sliding window.

    We compute leftBound on the fly using:
    - a monotonic decreasing deque for maximums
    - a monotonic increasing deque for minimums

    Then we need the maximum of (dp[l] - prefix[l]) over l in [leftBound..r].
    That is a sliding-window maximum problem, solved with another deque.
    */
    public long MaxTotalScore(int[] nums, int k)
    {
        int n = nums.Length;

        // prefix[i] = sum of nums[0..i-1]
        // This allows O(1) subarray sum queries:
        // sum(l..r) = prefix[r+1] - prefix[l]
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++)
        {
            prefix[i + 1] = prefix[i] + nums[i];
        }

        // dp[i] = best answer using first i elements (indices 0..i-1)
        long[] dp = new long[n + 1];

        // These two deques maintain the current sliding window [left..right]
        // for the value-band validity condition.
        //
        // maxDeque:
        //   stores indices in decreasing order of nums[index]
        //   front always points to the maximum value in the current window
        //
        // minDeque:
        //   stores indices in increasing order of nums[index]
        //   front always points to the minimum value in the current window
        LinkedList<int> maxDeque = new LinkedList<int>();
        LinkedList<int> minDeque = new LinkedList<int>();

        // candidateDeque stores candidate start indices l for the DP transition.
        //
        // For each possible start l, define:
        //   value(l) = dp[l] - prefix[l]
        //
        // For current right endpoint r, we need the maximum value(l) among all valid l.
        //
        // We maintain candidateDeque so that:
        // - indices are in increasing order
        // - their values(l) are in decreasing order
        //
        // Then the front always gives the best valid start.
        LinkedList<int> candidateDeque = new LinkedList<int>();

        int left = 0;

        // We process right endpoints one by one.
        for (int right = 0; right < n; right++)
        {
            // ------------------------------------------------------------
            // STEP 1: Expand the value-validity window to include nums[right]
            // ------------------------------------------------------------
            //
            // Update maxDeque:
            // Remove smaller values from the back because they can never become
            // the maximum while nums[right] is inside the window.
            while (maxDeque.Count > 0 && nums[maxDeque.Last!.Value] <= nums[right])
            {
                maxDeque.RemoveLast();
            }
            maxDeque.AddLast(right);

            // Update minDeque:
            // Remove larger values from the back because they can never become
            // the minimum while nums[right] is inside the window.
            while (minDeque.Count > 0 && nums[minDeque.Last!.Value] >= nums[right])
            {
                minDeque.RemoveLast();
            }
            minDeque.AddLast(right);

            // ------------------------------------------------------------
            // STEP 2: Shrink the left side until the current window becomes valid
            // ------------------------------------------------------------
            //
            // The current window is [left..right].
            // It is valid if:
            //   max(nums[left..right]) - min(nums[left..right]) <= k
            //
            // The front of maxDeque gives the index of the maximum.
            // The front of minDeque gives the index of the minimum.
            while ((long)nums[maxDeque.First!.Value] - nums[minDeque.First!.Value] > k)
            {
                // If the outgoing index is at the front of one of the deques,
                // remove it because it is no longer inside the window.
                if (maxDeque.First!.Value == left)
                {
                    maxDeque.RemoveFirst();
                }

                if (minDeque.First!.Value == left)
                {
                    minDeque.RemoveFirst();
                }

                left++;
            }

            // At this point, [left..right] is the smallest valid-start window:
            // every start l in [left..right] gives a valid band [l..right],
            // and every l < left is invalid.

            // ------------------------------------------------------------
            // STEP 3: Add the new possible start index l = right
            // ------------------------------------------------------------
            //
            // Why do we add l = right here?
            // Because for future right endpoints, a band may start at 'right'.
            //
            // We store candidate starts in a deque ordered by decreasing
            // value(l) = dp[l] - prefix[l].
            //
            // Important timing detail:
            // We add l = right before computing dp[right + 1].
            // This ensures the single-element band [right..right] is available
            // for the current endpoint as well, which is necessary because
            // every length-1 band is valid.
            long currentCandidateValue = dp[right] - prefix[right];

            while (candidateDeque.Count > 0)
            {
                int lastIndex = candidateDeque.Last!.Value;
                long lastValue = dp[lastIndex] - prefix[lastIndex];

                // If the new candidate is at least as good as the old one,
                // the old one will never be better in any future window that
                // also contains the new one, so we remove it.
                if (lastValue <= currentCandidateValue)
                {
                    candidateDeque.RemoveLast();
                }
                else
                {
                    break;
                }
            }

            candidateDeque.AddLast(right);

            // ------------------------------------------------------------
            // STEP 4: Remove candidate starts that are no longer valid
            // ------------------------------------------------------------
            //
            // For the current right endpoint, valid starts are exactly [left..right].
            // So any candidate index < left must be removed from the front.
            while (candidateDeque.Count > 0 && candidateDeque.First!.Value < left)
            {
                candidateDeque.RemoveFirst();
            }

            // ------------------------------------------------------------
            // STEP 5: Compute dp[right + 1]
            // ------------------------------------------------------------
            //
            // Option A: skip nums[right]
            long best = dp[right];

            // Option B: take a valid band ending at 'right'
            //
            // The best start is at the front of candidateDeque.
            // If bestStart = l, then:
            //   score = dp[l] + (prefix[right + 1] - prefix[l])
            //         = prefix[right + 1] + (dp[l] - prefix[l])
            if (candidateDeque.Count > 0)
            {
                int bestStart = candidateDeque.First!.Value;
                long take = prefix[right + 1] + (dp[bestStart] - prefix[bestStart]);
                if (take > best)
                {
                    best = take;
                }
            }

            dp[right + 1] = best;
        }

        return dp[n];
    }
}

// Demo code
var solution = new Solution();

int[] nums1 = { 4, 2, 3, 7, 6, 5 };
int k1 = 2;
long result1 = solution.MaxTotalScore(nums1, k1);
Console.WriteLine(result1); // Expected: 27

int[] nums2 = { 5, -4, 6, 6, -2, 7 };
int k2 = 1;
long result2 = solution.MaxTotalScore(nums2, k2);
Console.WriteLine(result2); // Expected: 24

int[] nums3 = { -5, -2, -7 };
int k3 = 10;
long result3 = solution.MaxTotalScore(nums3, k3);
Console.WriteLine(result3); // Expected: 0

int[] nums4 = { 8 };
int k4 = 0;
long result4 = solution.MaxTotalScore(nums4, k4);
Console.WriteLine(result4); // Expected: 8