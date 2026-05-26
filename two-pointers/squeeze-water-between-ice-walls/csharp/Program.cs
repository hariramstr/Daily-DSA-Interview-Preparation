/*
 * Title: Squeeze Water Between Ice Walls
 * Difficulty: Easy
 * Topic: Two Pointers
 *
 * Problem Description:
 * You are given an array `heights` representing the heights of ice walls standing upright in a row.
 * A bucket of water is placed between any two walls at positions i and j (where i < j).
 * The amount of water the bucket can hold is determined by the shorter of the two walls
 * multiplied by the number of gaps between them: min(heights[i], heights[j]) * (j - i).
 *
 * Your task is to find the maximum amount of water that can be held between any two walls.
 *
 * Note: This problem is about choosing the optimal pair of walls, not filling every gap between them.
 *
 * Constraints:
 *   2 <= heights.length <= 10^5
 *   1 <= heights[i] <= 10^4
 *
 * Example 1:
 *   Input:  heights = [2, 7, 4, 1, 6, 3]
 *   Output: 18
 *   Explanation: Using walls at index 1 (height 7) and index 4 (height 6),
 *                water = min(7, 6) * (4 - 1) = 6 * 3 = 18.
 *
 * Example 2:
 *   Input:  heights = [3, 1, 2, 4, 5]
 *   Output: 12
 *   Explanation: Using walls at index 0 (height 3) and index 4 (height 5),
 *                water = min(3, 5) * (4 - 0) = 3 * 4 = 12.
 */

// ─────────────────────────────────────────────────────────────────────────────
// Solution Class
// ─────────────────────────────────────────────────────────────────────────────
class Solution
{
    /// <summary>
    /// Finds the maximum water that can be held between any two walls.
    ///
    /// Time Complexity:  O(n) — we visit each element at most once with two pointers.
    /// Space Complexity: O(1) — only a handful of integer variables are used;
    ///                          no extra data structures that grow with input size.
    ///
    /// WHY TWO POINTERS?
    /// -----------------
    /// A brute-force approach would check every pair (i, j), giving O(n²) time.
    /// The two-pointer technique lets us discard unpromising pairs intelligently:
    ///
    ///   • Start with the widest possible container (left = 0, right = n-1).
    ///   • The water held is limited by the SHORTER wall.
    ///   • Moving the pointer of the TALLER wall inward can only make things worse
    ///     (width shrinks AND the limiting height stays the same or gets smaller).
    ///   • Therefore, we always move the pointer of the SHORTER wall inward,
    ///     because that is the only move that could possibly find a taller wall
    ///     and increase the water held.
    /// </summary>
    public int MaxWater(int[] heights)
    {
        // ── Step 1: Initialise two pointers ───────────────────────────────────
        // 'left'  starts at the very first wall (index 0).
        // 'right' starts at the very last  wall (index n-1).
        // Beginning at the extremes gives us the maximum possible width,
        // which we will trade off against height as we move the pointers inward.
        int left  = 0;
        int right = heights.Length - 1;

        // ── Step 2: Track the best answer seen so far ─────────────────────────
        // We initialise maxWater to 0 because no water has been measured yet.
        int maxWater = 0;

        // ── Step 3: Loop until the two pointers meet ──────────────────────────
        // When left == right both pointers point to the same wall,
        // so there is no gap between them and no water can be held.
        // We stop before that happens.
        while (left < right)
        {
            // ── Step 3a: Measure the current container ────────────────────────
            // Width  = distance between the two walls = (right - left).
            // Height = the shorter of the two walls   = min(heights[left], heights[right]).
            //          Water cannot rise above the shorter wall — it would spill over.
            // Water  = height * width  (the formula given in the problem).
            int width        = right - left;
            int boundingWall = Math.Min(heights[left], heights[right]);
            int currentWater = boundingWall * width;

            // ── Step 3b: Update the running maximum ───────────────────────────
            // If this container holds more water than any we have seen before,
            // record it as the new best.
            if (currentWater > maxWater)
            {
                maxWater = currentWater;
            }

            // ── Step 3c: Move the pointer of the shorter wall inward ──────────
            // KEY INSIGHT:
            //   The current water is limited by the shorter wall.
            //   If we move the TALLER wall's pointer inward:
            //     - Width decreases (bad).
            //     - The bounding height is still capped by the shorter wall (no gain).
            //   So moving the taller wall's pointer can never improve the result.
            //
            //   If we move the SHORTER wall's pointer inward:
            //     - Width decreases (unavoidable as we search).
            //     - But we might find a taller wall that raises the bounding height,
            //       potentially giving more water despite the narrower gap.
            //   This is the only move that has any chance of finding a better answer.
            //
            // When both walls are equal in height, it does not matter which pointer
            // we move; moving either one is safe (we handle this with the else branch).
            if (heights[left] < heights[right])
            {
                // Left wall is shorter — move left pointer one step to the right.
                left++;
            }
            else
            {
                // Right wall is shorter (or equal) — move right pointer one step to the left.
                right--;
            }
        }

        // ── Step 4: Return the best answer ────────────────────────────────────
        // After the loop, maxWater holds the largest amount of water found
        // across all pairs of walls examined by the two-pointer sweep.
        return maxWater;
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Demo / Driver Code  (top-level statements — no explicit Main needed in .NET 6+)
// ─────────────────────────────────────────────────────────────────────────────

Solution solver = new Solution();

// ── Example 1 ─────────────────────────────────────────────────────────────────
// heights = [2, 7, 4, 1, 6, 3]
// Expected output: 18
// Trace:
//   left=0(h=2), right=5(h=3) → water=min(2,3)*5=10, maxWater=10, move left (2<3)
//   left=1(h=7), right=5(h=3) → water=min(7,3)*4=12, maxWater=12, move right (7>3)
//   left=1(h=7), right=4(h=6) → water=min(7,6)*3=18, maxWater=18, move right (7>6)
//   left=1(h=7), right=3(h=1) → water=min(7,1)*2= 2, maxWater=18, move right (7>1)
//   left=1(h=7), right=2(h=4) → water=min(7,4)*1= 4, maxWater=18, move right (7>4)
//   left=1, right=1 → loop ends
//   Result: 18 ✓
int[] heights1 = { 2, 7, 4, 1, 6, 3 };
int result1 = solver.MaxWater(heights1);
Console.WriteLine("Example 1:");
Console.WriteLine($"  Input:    heights = [{string.Join(", ", heights1)}]");
Console.WriteLine($"  Output:   {result1}");
Console.WriteLine($"  Expected: 18");
Console.WriteLine($"  Correct:  {result1 == 18}");
Console.WriteLine();

// ── Example 2 ─────────────────────────────────────────────────────────────────
// heights = [3, 1, 2, 4, 5]
// Expected output: 12
// Trace:
//   left=0(h=3), right=4(h=5) → water=min(3,5)*4=12, maxWater=12, move left (3<5)
//   left=1(h=1), right=4(h=5) → water=min(1,5)*3= 3, maxWater=12, move left (1<5)
//   left=2(h=2), right=4(h=5) → water=min(2,5)*2= 4, maxWater=12, move left (2<5)
//   left=3(h=4), right=4(h=5) → water=min(4,5)*1= 4, maxWater=12, move left (4<5)
//   left=4, right=4 → loop ends
//   Result: 12 ✓
int[] heights2 = { 3, 1, 2, 4, 5 };
int result2 = solver.MaxWater(heights2);
Console.WriteLine("Example 2:");
Console.WriteLine($"  Input:    heights = [{string.Join(", ", heights2)}]");
Console.WriteLine($"  Output:   {result2}");
Console.WriteLine($"  Expected: 12");
Console.WriteLine($"  Correct:  {result2 == 12}");
Console.WriteLine();

// ── Additional edge-case: minimum input (two walls) ───────────────────────────
// heights = [4, 9]
// Expected: min(4,9) * (1-0) = 4 * 1 = 4
int[] heights3 = { 4, 9 };
int result3 = solver.MaxWater(heights3);
Console.WriteLine("Edge Case (two walls):");
Console.WriteLine($"  Input:    heights = [{string.Join(", ", heights3)}]");
Console.WriteLine($"  Output:   {result3}");
Console.WriteLine($"  Expected: 4");
Console.WriteLine($"  Correct:  {result3 == 4}");
Console.WriteLine();

// ── Additional edge-case: all walls equal height ──────────────────────────────
// heights = [5, 5, 5, 5]
// Best pair is index 0 and index 3: min(5,5)*(3-0) = 5*3 = 15
int[] heights4 = { 5, 5, 5, 5 };
int result4 = solver.MaxWater(heights4);
Console.WriteLine("Edge Case (all equal heights):");
Console.WriteLine($"  Input:    heights = [{string.Join(", ", heights4)}]");
Console.WriteLine($"  Output:   {result4}");
Console.WriteLine($"  Expected: 15");
Console.WriteLine($"  Correct:  {result4 == 15}");