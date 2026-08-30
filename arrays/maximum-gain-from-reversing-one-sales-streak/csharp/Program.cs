/*
Title: Maximum Gain from Reversing One Sales Streak
Difficulty: Medium
Topic: Arrays

Problem Description:
You are given an integer array nums representing the day-by-day profit impact of a product campaign.
A positive value means the campaign gained money that day, while a negative value means it lost money.
Management allows you to perform at most one operation: choose a contiguous subarray and reverse its order.
After this optional reversal, you must evaluate the maximum possible sum of any contiguous subarray in the modified array.

Return that maximum achievable contiguous profit.

A reversal does not change the values themselves, only their positions. You may also choose not to reverse anything
if the original array already gives the best answer.

Your task is to design an efficient algorithm for arrays large enough that trying every possible reversal and
recomputing every subarray would be too slow.

Constraints:
- 1 <= nums.length <= 2000
- -10^4 <= nums[i] <= 10^4
- The answer fits in a 32-bit signed integer

Example 1:
Input: nums = [4, -10, 3, 5]
Output: 12
Explanation: Reverse the subarray [-10, 3, 5] to get [4, 5, 3, -10]. The best contiguous subarray is [4, 5, 3], which has sum 12.

Example 2:
Input: nums = [-2, 8, -1, 6, -7]
Output: 13
Explanation: The best achievable contiguous sum is 13.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n^2)
    Space Complexity: O(n^2)

    Why this is acceptable:
    - n <= 2000
    - O(n^2) = 4,000,000 states, which is practical in C#

    High-level idea:
    We want the maximum subarray sum after reversing at most one contiguous segment.

    A subarray chosen in the final array can be viewed in three possible ways:
    1. It does not use any reversed part at all.
       Then the answer is simply the normal maximum subarray sum (Kadane's algorithm).

    2. It lies completely inside the reversed segment.
       But reversing a segment does not change the multiset of values inside it, only the order.
       Any contiguous segment inside the reversed block corresponds to some contiguous segment in the original block.
       Therefore this case is already covered by the normal maximum subarray sum.

    3. It crosses one or both boundaries of the reversed segment.
       This is the only interesting case.

    Suppose we reverse nums[l..r].
    In the new array, the order inside that block becomes:
        nums[r], nums[r-1], ..., nums[l]

    Now consider a final chosen subarray that intersects this reversed block.
    Inside the reversed block, any contiguous piece in the new array corresponds to a contiguous piece
    nums[i..j] in the original array, but taken in reverse order. Its sum is still the same:
        sum(nums[i..j])

    The only thing that matters for subarray sum is:
    - maybe we attach a best suffix ending at l-1 on the left
    - maybe we attach a best prefix starting at r+1 on the right
    - and in the middle we take some contiguous original segment nums[i..j] where l <= i <= j <= r

    Because after reversal, that middle segment appears as a contiguous block between the outside attachments.

    Therefore for every pair (l, r), the best subarray sum achievable using reversal [l..r] is:
        bestMiddleInside[l][r]
        or leftAttach + bestMiddleEndingAtR? / rightAttach + ...
    But we can simplify further.

    A more direct and reliable O(n^2) DP formulation:
    For every pair (i, j), define dp[i, j] as the maximum sum of a contiguous subarray in the array formed by
    taking the original interval nums[i..j] and allowing it to be read from the outside inward as if it were
    the active reversed window contribution.

    Even simpler for implementation:
    We compute the best subarray that can be formed after one reversal by expanding the chosen reversed interval
    from the center outward.

    Let:
    - leftBestEnd[k] = maximum sum of a subarray ending exactly at index k
    - rightBestStart[k] = maximum sum of a subarray starting exactly at index k

    If we reverse [l..r], and in the final array choose a subarray that uses the entire reversed block contribution
    from original indices [i..j] where l <= i <= j <= r, then:
    - in the final array, original nums[j] becomes near the left side of the reversed block
    - original nums[i] becomes near the right side of the reversed block

    The best crossing construction for a fixed pair (i, j) is:
        (best suffix ending at i-1, optional) + sum(i..j) + (best prefix starting at j+1, optional)

    But this expression is actually independent of reversal and equals a normal subarray sum.
    So where does reversal help?
    Reversal helps because after reversing a larger segment [l..r], we can connect:
        left outside near l-1 to original nums[j]
    and
        original nums[i] to right outside near r+1
    Thus the useful value becomes:
        best suffix ending at l-1 + sum(i..j) + best prefix starting at r+1
    with l <= i <= j <= r

    For fixed (l, r), the best middle is simply the maximum subarray sum inside nums[l..r].
    So candidate:
        optionalLeft(l) + maxSubarrayInside(l, r) + optionalRight(r)

    This is correct because any chosen final subarray that crosses the reversed block corresponds to:
    - some suffix before l
    - some contiguous segment inside [l..r] after reversal, which is still a contiguous segment sum from [l..r]
    - some prefix after r

    Therefore we need:
    1. best suffix sum ending at each index
    2. best prefix sum starting at each index
    3. maximum subarray sum for every interval [l..r]

    Then answer is:
        max over all l, r of
            maxSubInside[l, r]
            leftGain(l) + maxSubInside[l, r]
            maxSubInside[l, r] + rightGain(r)
            leftGain(l) + maxSubInside[l, r] + rightGain(r)
    where:
        leftGain(l) = max(0, best suffix sum ending at l-1)
        rightGain(r) = max(0, best prefix sum starting at r+1)

    This includes the "no reversal" case because maxSubInside over all intervals already includes the original best,
    and also we explicitly compute Kadane for clarity.

    We also verify the examples:
    - [4, -10, 3, 5]
      For l=1, r=3, maxSubInside(1,3)=8 from [3,5]
      leftGain(1)=4, rightGain(3)=0 => 12
    - [-2, 8, -1, 6, -7]
      Best remains 13.
    */
    public int MaxSubarraySumAfterOneReverse(int[] nums)
    {
        int n = nums.Length;

        // Step 1:
        // Compute the standard maximum subarray sum in the original array using Kadane's algorithm.
        // Why we do this:
        // - The problem allows "at most one" reversal, so doing nothing is always allowed.
        // - If reversing does not help, we must still return the original best answer.
        int answer = Kadane(nums);

        // Step 2:
        // Compute best suffix sum ending exactly at each index.
        //
        // Meaning:
        // leftBestEnd[i] = maximum sum of any contiguous subarray that MUST end at index i.
        //
        // Example:
        // nums = [4, -10, 3, 5]
        // leftBestEnd = [4, -6, 3, 8]
        //
        // Why this matters:
        // If our reversed block starts at l, then any chosen final subarray may extend to the left of l.
        // The best possible left extension is the best suffix ending at l-1.
        int[] leftBestEnd = new int[n];
        leftBestEnd[0] = nums[0];
        for (int i = 1; i < n; i++)
        {
            // Either:
            // 1. start a new subarray at i
            // 2. extend the best subarray that ended at i-1
            leftBestEnd[i] = Math.Max(nums[i], leftBestEnd[i - 1] + nums[i]);
        }

        // Step 3:
        // Compute best prefix sum starting exactly at each index.
        //
        // Meaning:
        // rightBestStart[i] = maximum sum of any contiguous subarray that MUST start at index i.
        //
        // Why this matters:
        // If our reversed block ends at r, then any chosen final subarray may extend to the right of r.
        // The best possible right extension is the best prefix starting at r+1.
        int[] rightBestStart = new int[n];
        rightBestStart[n - 1] = nums[n - 1];
        for (int i = n - 2; i >= 0; i--)
        {
            // Either:
            // 1. start and stop at i
            // 2. extend into the best prefix starting at i+1
            rightBestStart[i] = Math.Max(nums[i], nums[i] + rightBestStart[i + 1]);
        }

        // Step 4:
        // Precompute maxSubInside[l, r]:
        // the maximum subarray sum completely contained inside interval [l..r].
        //
        // Why we need this:
        // For a chosen reversed segment [l..r], the part of the final chosen subarray that lies inside
        // the reversed block can be any contiguous segment from that interval.
        // Reversal changes order, but the sum of any contiguous segment inside the reversed block is still
        // the sum of some contiguous segment from the original interval [l..r].
        //
        // So for each [l..r], we need the best contiguous sum available inside that interval.
        //
        // How we compute it:
        // For each fixed left boundary l, we scan r from l to n-1 and run a Kadane-like update restricted
        // to that interval.
        int[,] maxSubInside = new int[n, n];

        for (int l = 0; l < n; l++)
        {
            int bestEndingHere = 0;
            int bestSoFar = int.MinValue;

            for (int r = l; r < n; r++)
            {
                if (r == l)
                {
                    // Base case: interval [l..l] contains only one element.
                    bestEndingHere = nums[r];
                    bestSoFar = nums[r];
                }
                else
                {
                    // Standard Kadane transition, but only within the current interval starting at l.
                    bestEndingHere = Math.Max(nums[r], bestEndingHere + nums[r]);
                    bestSoFar = Math.Max(bestSoFar, bestEndingHere);
                }

                maxSubInside[l, r] = bestSoFar;
            }
        }

        // Step 5:
        // Try every possible reversed interval [l..r].
        //
        // For each interval:
        // - middle = best contiguous sum available inside [l..r]
        // - leftGain = best suffix ending at l-1, or 0 if we choose not to extend left
        // - rightGain = best prefix starting at r+1, or 0 if we choose not to extend right
        //
        // Candidate total:
        //     leftGain + middle + rightGain
        //
        // Why max with 0 on left/right:
        // - Extending with a negative sum would only make the result worse.
        // - Since we are free not to include those outside parts, we treat them as optional.
        for (int l = 0; l < n; l++)
        {
            int leftGain = 0;
            if (l > 0)
            {
                leftGain = Math.Max(0, leftBestEnd[l - 1]);
            }

            for (int r = l; r < n; r++)
            {
                int rightGain = 0;
                if (r + 1 < n)
                {
                    rightGain = Math.Max(0, rightBestStart[r + 1]);
                }

                int middle = maxSubInside[l, r];
                int candidate = leftGain + middle + rightGain;

                if (candidate > answer)
                {
                    answer = candidate;
                }
            }
        }

        return answer;
    }

    private int Kadane(int[] nums)
    {
        int bestEndingHere = nums[0];
        int bestSoFar = nums[0];

        for (int i = 1; i < nums.Length; i++)
        {
            bestEndingHere = Math.Max(nums[i], bestEndingHere + nums[i]);
            bestSoFar = Math.Max(bestSoFar, bestEndingHere);
        }

        return bestSoFar;
    }
}

// Demo code
var solution = new Solution();

int[] nums1 = { 4, -10, 3, 5 };
int result1 = solution.MaxSubarraySumAfterOneReverse(nums1);
Console.WriteLine(result1); // Expected: 12

int[] nums2 = { -2, 8, -1, 6, -7 };
int result2 = solution.MaxSubarraySumAfterOneReverse(nums2);
Console.WriteLine(result2); // Expected: 13

int[] nums3 = { 1 };
int result3 = solution.MaxSubarraySumAfterOneReverse(nums3);
Console.WriteLine(result3); // Expected: 1

int[] nums4 = { -5, -1, -8 };
int result4 = solution.MaxSubarraySumAfterOneReverse(nums4);
Console.WriteLine(result4); // Expected: -1