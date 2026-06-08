/*
 * ============================================================
 * Title: Longest Snack Break Within Calorie Limit
 * ============================================================
 * Problem Description:
 * You are tracking the snacks eaten by an employee during a workday.
 * Each snack is represented by its calorie count in an integer array
 * `calories`, where `calories[i]` is the calorie count of the i-th
 * snack consumed in order.
 *
 * An employee is allowed a continuous snack break, which is defined
 * as a contiguous subarray of snacks. The break is considered
 * 'within budget' if the total calories of all snacks in that break
 * does NOT exceed a given limit `maxCalories`.
 *
 * Return the maximum number of snacks the employee can eat in a
 * single contiguous snack break without exceeding the calorie limit.
 *
 * If no single snack fits within the limit, return 0.
 *
 * Constraints:
 *   - 1 <= calories.length <= 10^5
 *   - 1 <= calories[i] <= 1000
 *   - 1 <= maxCalories <= 10^7
 *
 * Example 1:
 *   Input:  calories = [100, 200, 150, 50, 300, 80], maxCalories = 400
 *   Output: 3
 *   Explanation: [200, 150, 50] sums to 400 and has 3 snacks.
 *
 * Example 2:
 *   Input:  calories = [500, 600, 700], maxCalories = 400
 *   Output: 0
 *   Explanation: Every individual snack exceeds the limit.
 * ============================================================
 */

// ── Solution class ────────────────────────────────────────────────────────────
public class Solution
{
    /// <summary>
    /// Finds the length of the longest contiguous subarray whose sum
    /// does not exceed <paramref name="maxCalories"/>.
    ///
    /// Algorithm: Sliding Window (Two-Pointer / Variable-Size Window)
    ///
    /// Time Complexity : O(n)  — each element is added once and removed at most once.
    /// Space Complexity: O(1)  — only a handful of integer variables are used.
    /// </summary>
    /// <param name="calories">Array of per-snack calorie counts.</param>
    /// <param name="maxCalories">Maximum total calories allowed in one break.</param>
    /// <returns>Maximum number of snacks in a valid contiguous break.</returns>
    public int MaxSnacks(int[] calories, int maxCalories)
    {
        // ── Step 1: Initialise the sliding-window bookkeeping variables ──────
        //
        // `left`       – the index of the LEFT edge of our current window.
        //                We start with an empty window that begins at index 0.
        //
        // `windowSum`  – the running total of calories inside the window
        //                [left .. right].  We maintain this incrementally so
        //                we never have to recompute the sum from scratch.
        //
        // `maxLength`  – the best (longest) valid window length seen so far.
        //                Starts at 0 so that "no valid window" naturally
        //                returns 0 as required by the problem.
        int left       = 0;
        int windowSum  = 0;
        int maxLength  = 0;

        // ── Step 2: Expand the window by advancing the RIGHT pointer ─────────
        //
        // We iterate `right` from 0 to n-1.  Think of `right` as the index
        // of the newest snack we are considering adding to the break.
        //
        // At each iteration we:
        //   a) Add calories[right] to the window  (expand right edge).
        //   b) Shrink from the left if the window is over budget.
        //   c) Record the window length if it is the best so far.
        for (int right = 0; right < calories.Length; right++)
        {
            // ── Step 2a: Include the snack at `right` in the current window ──
            //
            // By adding calories[right] we are saying:
            //   "Let's try extending our snack break to include this snack."
            windowSum += calories[right];

            // ── Step 2b: Shrink the window from the LEFT while over budget ───
            //
            // If the total calories now exceed maxCalories we must remove
            // snacks from the beginning of the break (the left side) until
            // the window is within budget again.
            //
            // WHY a while loop?
            //   In theory, because each calorie value is at most 1000 and
            //   maxCalories >= 1, a single removal is always enough here.
            //   However, using `while` makes the logic general and correct
            //   for any input — it is the standard sliding-window pattern.
            while (windowSum > maxCalories)
            {
                // Remove the leftmost snack from the window sum …
                windowSum -= calories[left];

                // … and slide the left boundary one position to the right.
                // This effectively "forgets" the snack at the old `left`.
                left++;
            }

            // ── Step 2c: Update the best answer ─────────────────────────────
            //
            // At this point the window [left .. right] is guaranteed to be
            // within budget (windowSum <= maxCalories).
            //
            // The number of snacks in the window is (right - left + 1).
            //   • right - left  gives the distance between the two pointers.
            //   • +1            accounts for the fact that both endpoints are
            //                   inclusive (e.g. indices 2..4 → 3 snacks).
            //
            // We keep the maximum across all valid windows seen so far.
            int currentLength = right - left + 1;
            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // ── Step 3: Return the best answer found ─────────────────────────────
        //
        // If every individual snack exceeded maxCalories, `maxLength` was
        // never updated from 0, which is exactly the required return value.
        return maxLength;
    }
}

// ── Demo / verification code ──────────────────────────────────────────────────
//
// We trace through both examples manually to confirm correctness before running.
//
// Example 1: calories = [100, 200, 150, 50, 300, 80], maxCalories = 400
//   right=0: sum=100, window=[100]          len=1  max=1
//   right=1: sum=300, window=[100,200]      len=2  max=2
//   right=2: sum=450 > 400 → remove 100 → sum=350, left=1
//            window=[200,150]               len=2  max=2
//   right=3: sum=400, window=[200,150,50]   len=3  max=3  ✓
//   right=4: sum=700 > 400 → remove 200 → sum=500, left=2
//            still > 400  → remove 150 → sum=350, left=3
//            window=[50,300]                len=2  max=3
//   right=5: sum=430 > 400 → remove 50 → sum=380, left=4
//            window=[300,80]                len=2  max=3
//   Result: 3  ✓
//
// Example 2: calories = [500, 600, 700], maxCalories = 400
//   right=0: sum=500 > 400 → remove 500 → sum=0, left=1
//            window is empty (right-left+1 = 0)  max=0
//   right=1: sum=600 > 400 → remove 600 → sum=0, left=2  max=0
//   right=2: sum=700 > 400 → remove 700 → sum=0, left=3  max=0
//   Result: 0  ✓

var solution = new Solution();

// ── Example 1 ────────────────────────────────────────────────────────────────
int[] calories1   = [100, 200, 150, 50, 300, 80];
int   maxCal1     = 400;
int   result1     = solution.MaxSnacks(calories1, maxCal1);
Console.WriteLine("=== Example 1 ===");
Console.WriteLine($"Input  : calories = [{string.Join(", ", calories1)}], maxCalories = {maxCal1}");
Console.WriteLine($"Output : {result1}");          // Expected: 3
Console.WriteLine($"Correct: {result1 == 3}");
Console.WriteLine();

// ── Example 2 ────────────────────────────────────────────────────────────────
int[] calories2   = [500, 600, 700];
int   maxCal2     = 400;
int   result2     = solution.MaxSnacks(calories2, maxCal2);
Console.WriteLine("=== Example 2 ===");
Console.WriteLine($"Input  : calories = [{string.Join(", ", calories2)}], maxCalories = {maxCal2}");
Console.WriteLine($"Output : {result2}");          // Expected: 0
Console.WriteLine($"Correct: {result2 == 0}");
Console.WriteLine();

// ── Extra edge-case: single snack exactly at the limit ───────────────────────
int[] calories3   = [400];
int   maxCal3     = 400;
int   result3     = solution.MaxSnacks(calories3, maxCal3);
Console.WriteLine("=== Edge Case: single snack at limit ===");
Console.WriteLine($"Input  : calories = [{string.Join(", ", calories3)}], maxCalories = {maxCal3}");
Console.WriteLine($"Output : {result3}");          // Expected: 1
Console.WriteLine($"Correct: {result3 == 1}");
Console.WriteLine();

// ── Extra edge-case: all snacks fit in one window ────────────────────────────
int[] calories4   = [10, 20, 30, 40];
int   maxCal4     = 200;
int   result4     = solution.MaxSnacks(calories4, maxCal4);
Console.WriteLine("=== Edge Case: entire array fits ===");
Console.WriteLine($"Input  : calories = [{string.Join(", ", calories4)}], maxCalories = {maxCal4}");
Console.WriteLine($"Output : {result4}");          // Expected: 4
Console.WriteLine($"Correct: {result4 == 4}");