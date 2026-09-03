/*
Title: Minimum Review Threshold for Passing All Build Gates
Difficulty: Hard
Topic: Binary Search

Problem Description:
You are given a software release pipeline with n sequential build gates. Gate i requires at least
requirements[i] approved review points before the release can pass that gate. You also have m review
batches, where batch j contributes reviews[j] points and can be split across multiple gates in any way.
However, to keep the process fair, you must choose a single threshold value T and cap every batch at
min(reviews[j], T) usable points. Any points above T in a batch are ignored. After capping, all usable
review points from all batches are pooled together and may be distributed arbitrarily among the gates.

Your task is to find the minimum integer threshold T such that the total capped review points are enough
to satisfy all gate requirements. If even using all review points is insufficient, return -1.

Formally, find the smallest integer T >= 0 such that:
sum(min(reviews[j], T) for j in [0..m-1]) >= sum(requirements[i] for i in [0..n-1])

If no such T exists because:
sum(reviews) < sum(requirements)
return -1.

Important note about Example 1:
The statement text says:
- T = 5 => 13
- T = 6 => 15
- "Actually the first valid threshold is T = 7", giving 17

But according to the formal condition, T = 6 is still NOT enough because 15 < 16,
and T = 7 is the first threshold that reaches at least 16. Therefore the correct output is 7.

Constraints:
- 1 <= n, m <= 2 * 10^5
- 1 <= requirements[i], reviews[j] <= 10^12
- The answer must fit in 64-bit signed integer range
*/

using System;
using System.Linq;

public class Solution
{
    /*
    Time Complexity:
    - O(n + m + m * log(maxReview))
      Explanation:
      1. We sum the requirements: O(n)
      2. We sum the reviews and find the maximum review value: O(m)
      3. We binary search on T from 0 to maxReview: O(log(maxReview))
      4. For each candidate T, we compute the capped sum across all review batches: O(m)
      So total is O(n + m + m * log(maxReview))

    Space Complexity:
    - O(1) extra space, ignoring the input arrays
      We only use a few variables for sums, bounds, and loop counters.
    */
    public long MinimumThreshold(long[] requirements, long[] reviews)
    {
        // ------------------------------------------------------------
        // STEP 1: Compute the total amount of review points required.
        // ------------------------------------------------------------
        // Why?
        // The problem allows us to distribute the pooled capped review points
        // arbitrarily among all gates. That means the only thing that matters
        // is whether the TOTAL usable review points is at least the TOTAL required.
        //
        // We do NOT need to simulate gate-by-gate assignment because there is no
        // restriction on how the pooled points are distributed after capping.
        long totalRequired = 0;
        foreach (long requirement in requirements)
        {
            totalRequired += requirement;
        }

        // ------------------------------------------------------------
        // STEP 2: Compute the total raw review points and the maximum batch size.
        // ------------------------------------------------------------
        // Why total raw review points?
        // If even the uncapped total sum(reviews) is smaller than totalRequired,
        // then no threshold T can ever help, because capping can only reduce or
        // keep the same amount of usable points.
        //
        // Why maximum batch size?
        // The capped sum function stops changing once T reaches the largest review batch,
        // because min(reviews[j], T) becomes reviews[j] for every j.
        // Therefore, the answer (if it exists) must lie in the range [0, maxReview].
        long totalAvailable = 0;
        long maxReview = 0;

        foreach (long review in reviews)
        {
            totalAvailable += review;
            if (review > maxReview)
            {
                maxReview = review;
            }
        }

        // ------------------------------------------------------------
        // STEP 3: Early impossibility check.
        // ------------------------------------------------------------
        // Why?
        // If the total available review points are still not enough even without any cap,
        // then there is no valid threshold at all.
        if (totalAvailable < totalRequired)
        {
            return -1;
        }

        // ------------------------------------------------------------
        // STEP 4: Binary search for the minimum threshold T.
        // ------------------------------------------------------------
        // Why binary search works:
        // Define f(T) = sum(min(reviews[j], T)).
        //
        // As T increases:
        // - min(reviews[j], T) never decreases for any batch
        // - therefore f(T) is monotonic non-decreasing
        //
        // We need the smallest T such that f(T) >= totalRequired.
        // This is exactly the classic "first true" binary search pattern.
        long left = 0;
        long right = maxReview;
        long answer = maxReview;

        while (left <= right)
        {
            // --------------------------------------------------------
            // STEP 4a: Pick the middle threshold.
            // --------------------------------------------------------
            // We use this overflow-safe midpoint formula.
            long mid = left + (right - left) / 2;

            // --------------------------------------------------------
            // STEP 4b: Compute the capped total for this threshold.
            // --------------------------------------------------------
            // For each review batch:
            // - if review <= mid, we can use the whole batch
            // - otherwise, we can only use mid from that batch
            //
            // This directly implements:
            // sum(min(reviews[j], mid))
            //
            // We also include an early stop optimization:
            // once cappedTotal reaches or exceeds totalRequired, we can stop summing,
            // because for the binary search decision we only care whether it is enough.
            long cappedTotal = 0;

            foreach (long review in reviews)
            {
                cappedTotal += Math.Min(review, mid);

                if (cappedTotal >= totalRequired)
                {
                    break;
                }
            }

            // --------------------------------------------------------
            // STEP 4c: Decide which half to keep.
            // --------------------------------------------------------
            // If cappedTotal is enough:
            // - mid is a valid threshold
            // - but we want the MINIMUM valid threshold
            // - so we record mid and continue searching left half
            //
            // If cappedTotal is not enough:
            // - mid is too small
            // - we must search the right half
            if (cappedTotal >= totalRequired)
            {
                answer = mid;
                right = mid - 1;
            }
            else
            {
                left = mid + 1;
            }
        }

        // ------------------------------------------------------------
        // STEP 5: Return the smallest valid threshold found.
        // ------------------------------------------------------------
        return answer;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
// requirements = [5, 7, 4], reviews = [3, 10, 8]
// totalRequired = 16
// T = 5 => 3 + 5 + 5 = 13 (not enough)
// T = 6 => 3 + 6 + 6 = 15 (not enough)
// T = 7 => 3 + 7 + 7 = 17 (enough)
// Minimum valid threshold = 7
long[] requirements1 = { 5, 7, 4 };
long[] reviews1 = { 3, 10, 8 };
long result1 = solution.MinimumThreshold(requirements1, reviews1);
Console.WriteLine(result1);

// Example 2
// requirements = [9, 6], reviews = [4, 3, 5]
// totalRequired = 15
// totalAvailable = 12
// Since 12 < 15, answer is -1
long[] requirements2 = { 9, 6 };
long[] reviews2 = { 4, 3, 5 };
long result2 = solution.MinimumThreshold(requirements2, reviews2);
Console.WriteLine(result2);