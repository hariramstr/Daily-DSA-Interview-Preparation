/*
Title: Maximum Score from Choosing Endpoints with Growing Penalties

Problem Description:
You are given an integer array nums of length n. You must remove every element from the array, one at a time.
At each step, you may remove either the leftmost remaining element or the rightmost remaining element.
If this is the t-th removal (1-indexed), removing a value x adds x * t to your score.

However, there is an additional penalty for switching sides. If your previous removal was from the left and
your current removal is from the right, or vice versa, then you pay a fixed penalty p for that step.
No penalty is paid on the first removal because there is no previous side.

Return the maximum total score you can obtain after removing all elements.

The challenge is to choose both the order of removals and when to switch sides. A greedy approach is not sufficient,
because taking a large value early may reduce the multiplier available for other values, and unnecessary side switches
may erase the gain.

Constraints:
- 1 <= n <= 2000
- -10^9 <= nums[i] <= 10^9
- 0 <= p <= 10^9
- The answer fits in a signed 64-bit integer.
*/

using System;

public class Solution
{
    // Time Complexity: O(n^2)
    // Space Complexity: O(n^2)
    //
    // Idea:
    // We use interval dynamic programming.
    //
    // Let dpL[l, r] = maximum score obtainable after we have already removed everything
    // outside the current remaining interval [l..r], and the LAST removal we performed
    // (the most recent one before reaching this state) was from the LEFT side.
    //
    // Let dpR[l, r] = same idea, but the last removal was from the RIGHT side.
    //
    // Why this state works:
    // - The remaining array is always a contiguous interval [l..r].
    // - The next multiplier t depends only on how many elements have already been removed.
    //   If remaining length is len = r - l + 1, then removed count is n - len,
    //   so the next removal uses multiplier t = n - len + 1.
    // - The switch penalty depends only on whether the previous side and current side differ.
    //   Therefore, we only need to remember the side of the last removal.
    //
    // Transition:
    // From state [l..r], we can remove:
    // 1) left endpoint nums[l]
    // 2) right endpoint nums[r]
    //
    // If previous side was Left:
    // - taking left again: no switch penalty
    // - taking right: pay penalty p
    //
    // If previous side was Right:
    // - taking right again: no switch penalty
    // - taking left: pay penalty p
    //
    // Base case:
    // When only one element remains [i..i], that final removal uses multiplier n.
    // Since there is a previous side in dpL/dpR states, removing that final element may or may not
    // incur a switch penalty depending on whether we remove it from the same side or opposite side.
    // But when only one element remains, left and right are the same physical element.
    //
    // Important subtle point:
    // In a single-element interval [i..i], removing "from left" and "from right" are both valid conceptual
    // choices for DP purposes because the penalty depends on the chosen side label relative to the previous side.
    // So:
    // - From a state whose last side was Left, the best final move is max(
    //       remove as Left: nums[i] * n,
    //       remove as Right: nums[i] * n - p
    //   )
    // - From a state whose last side was Right, the best final move is max(
    //       remove as Right: nums[i] * n,
    //       remove as Left: nums[i] * n - p
    //   )
    // Since both expressions are the same, both dpL[i,i] and dpR[i,i] equal nums[i] * n.
    // (Because p >= 0, choosing the same side is never worse.)
    //
    // Starting the process:
    // The first removal has no previous side, so no switch penalty can apply.
    // We handle this outside the DP:
    // - First remove left element nums[0] with multiplier 1, then the remaining interval is [1..n-1]
    //   and the last side becomes Left.
    // - First remove right element nums[n-1] with multiplier 1, then the remaining interval is [0..n-2]
    //   and the last side becomes Right.
    // We take the better of those two starts.
    public long MaximumScore(int[] nums, long p)
    {
        int n = nums.Length;

        // Special case:
        // If there is only one element, we must remove it first and only.
        // No previous side exists, so no penalty can ever be paid.
        if (n == 1)
        {
            return (long)nums[0];
        }

        // We allocate two n x n DP tables.
        //
        // dpL[l, r]:
        //   best score from removing all elements in interval [l..r],
        //   assuming the previous removal (the one just before this subproblem starts) was from the LEFT.
        //
        // dpR[l, r]:
        //   same, but previous removal was from the RIGHT.
        //
        // We use long because:
        // - nums[i] can be up to 1e9
        // - multiplier can be up to 2000
        // - total score can be large
        long[,] dpL = new long[n, n];
        long[,] dpR = new long[n, n];

        // Base case: intervals of length 1.
        //
        // Suppose only nums[i] remains.
        // This is the final removal, so multiplier t = n.
        //
        // If previous side was Left:
        // - remove from Left => nums[i] * n
        // - remove from Right => nums[i] * n - p
        // Best is nums[i] * n because p >= 0.
        //
        // Same reasoning for previous side Right.
        for (int i = 0; i < n; i++)
        {
            long value = (long)nums[i] * n;
            dpL[i, i] = value;
            dpR[i, i] = value;
        }

        // Build larger intervals from smaller intervals.
        //
        // We process by increasing interval length so that when computing [l..r],
        // the smaller intervals [l+1..r] and [l..r-1] are already known.
        for (int len = 2; len <= n - 1; len++)
        {
            // For each interval length, enumerate all valid starting positions l.
            for (int l = 0; l + len - 1 < n; l++)
            {
                int r = l + len - 1;

                // Number of elements already removed before this state:
                // removed = n - len
                //
                // Therefore the next removal is the (removed + 1)-th removal.
                long t = n - len + 1L;

                // ------------------------------------------------------------
                // Compute dpL[l, r]
                // ------------------------------------------------------------
                //
                // Meaning:
                // We are about to remove all elements in [l..r],
                // and the previous removal (before entering this interval state) was from LEFT.
                //
                // Option 1: remove left endpoint nums[l] now.
                // - Current side = Left
                // - Previous side = Left
                // - No switch penalty
                // - Gain now = nums[l] * t
                // - Remaining interval becomes [l+1..r]
                // - The last side for the next state becomes Left
                long takeLeftAfterLeft =
                    (long)nums[l] * t
                    + dpL[l + 1, r];

                // Option 2: remove right endpoint nums[r] now.
                // - Current side = Right
                // - Previous side = Left
                // - This is a side switch, so pay penalty p
                // - Gain now = nums[r] * t - p
                // - Remaining interval becomes [l..r-1]
                // - The last side for the next state becomes Right
                long takeRightAfterLeft =
                    (long)nums[r] * t
                    - p
                    + dpR[l, r - 1];

                // We choose the better of the two legal actions.
                dpL[l, r] = Math.Max(takeLeftAfterLeft, takeRightAfterLeft);

                // ------------------------------------------------------------
                // Compute dpR[l, r]
                // ------------------------------------------------------------
                //
                // Meaning:
                // We are about to remove all elements in [l..r],
                // and the previous removal was from RIGHT.
                //
                // Option 1: remove left endpoint nums[l] now.
                // - Current side = Left
                // - Previous side = Right
                // - This is a switch, so pay penalty p
                // - Remaining interval becomes [l+1..r]
                // - New last side becomes Left
                long takeLeftAfterRight =
                    (long)nums[l] * t
                    - p
                    + dpL[l + 1, r];

                // Option 2: remove right endpoint nums[r] now.
                // - Current side = Right
                // - Previous side = Right
                // - No switch penalty
                // - Remaining interval becomes [l..r-1]
                // - New last side becomes Right
                long takeRightAfterRight =
                    (long)nums[r] * t
                    + dpR[l, r - 1];

                // Again, keep the better choice.
                dpR[l, r] = Math.Max(takeLeftAfterRight, takeRightAfterRight);
            }
        }

        // Now handle the very first removal separately.
        //
        // Why separate handling is necessary:
        // The first move has no "previous side", so no switch penalty can apply.
        // Our DP states assume a previous side already exists, so we start DP only after making move 1.
        //
        // Start option A: first remove leftmost element nums[0].
        // - Multiplier is 1
        // - No penalty
        // - Remaining interval is [1..n-1]
        // - Last side becomes Left
        long startWithLeft =
            (long)nums[0]
            + dpL[1, n - 1];

        // Start option B: first remove rightmost element nums[n-1].
        // - Multiplier is 1
        // - No penalty
        // - Remaining interval is [0..n-2]
        // - Last side becomes Right
        long startWithRight =
            (long)nums[n - 1]
            + dpR[0, n - 2];

        // The answer is the better of the two possible first moves.
        return Math.Max(startWithLeft, startWithRight);
    }
}

// Demo code
var solution = new Solution();

int[] nums1 = { 4, 2, 9 };
long p1 = 3;
long result1 = solution.MaximumScore(nums1, p1);
Console.WriteLine(result1);

int[] nums2 = { 8, -5, 7, 3 };
long p2 = 4;
long result2 = solution.MaximumScore(nums2, p2);
Console.WriteLine(result2);

// Additional quick sanity checks
int[] nums3 = { 5 };
long p3 = 100;
Console.WriteLine(solution.MaximumScore(nums3, p3));

int[] nums4 = { 1, 2 };
long p4 = 10;
Console.WriteLine(solution.MaximumScore(nums4, p4));