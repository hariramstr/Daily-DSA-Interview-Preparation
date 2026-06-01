/*
 * Title: Squeeze Water Between Walls
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * You are given an integer array `heights` representing the heights of vertical walls
 * positioned at consecutive unit intervals along a horizontal axis. Between any two walls,
 * water can be trapped if the surrounding walls are tall enough. However, there is a twist:
 * each wall has a durability value given in a second integer array `durability`. A wall can
 * only hold water if its durability is strictly greater than the amount of water units
 * pressing against it (i.e., the water level at that wall's position).
 *
 * Your task is to find the maximum amount of water that can be trapped between exactly two
 * walls (not necessarily adjacent), such that both chosen walls satisfy the durability
 * constraint. The water trapped between wall i and wall j (where i < j) is calculated as:
 *
 *   water = min(heights[i], heights[j]) * (j - i)
 *
 * A wall at index k satisfies the durability constraint if durability[k] > min(heights[i], heights[j]).
 *
 * Return the maximum water that can be trapped. If no valid pair exists, return 0.
 *
 * Constraints:
 * - 2 <= heights.length <= 10^5
 * - heights.length == durability.length
 * - 1 <= heights[i] <= 10^4
 * - 1 <= durability[i] <= 10^4
 *
 * Example 1:
 *   Input: heights = [1, 8, 6, 2, 5, 4, 8, 3, 7], durability = [10, 9, 7, 5, 6, 5, 9, 4, 8]
 *   Output: 49
 *   Explanation: Using walls at index 1 (height=8, durability=9) and index 8 (height=7, durability=8).
 *   Water = min(8,7) * (8-1) = 7*7 = 49. Water level is 7, both durability values (9>7 and 8>7) satisfy.
 *
 * Example 2:
 *   Input: heights = [3, 1, 2, 4], durability = [3, 5, 3, 4]
 *   Output: 4
 *   Explanation: The pair (0,3) gives water=9 but durability[0]=3 is NOT > 3 (water level=3), invalid.
 *   Best valid pair is (0,2): min(3,2)*2=4, durability[0]=3>2 and durability[2]=3>2. Output: 4.
 */

public class Solution {

    /**
     * Finds the maximum water that can be trapped between two walls,
     * subject to the durability constraint on both walls.
     *
     * <p>Algorithm Overview (Two-Pointer with validity check):
     * We use the classic two-pointer approach for the "Container With Most Water" problem,
     * but add a durability check. At each step, we compute the water for the current pair,
     * check if both walls are durable enough, update the maximum if valid, and then move
     * the pointer pointing to the shorter wall inward (standard two-pointer logic).
     *
     * <p>Why two pointers work here:
     * The water level is min(heights[left], heights[right]). Moving the taller wall inward
     * can never increase the water level (it's bounded by the shorter wall), so we always
     * move the shorter wall's pointer. This guarantees we explore all potentially optimal pairs.
     *
     * @param heights    array of wall heights (1-indexed positions along horizontal axis)
     * @param durability array of durability values corresponding to each wall
     * @return the maximum valid water that can be trapped; 0 if no valid pair exists
     *
     * Time Complexity:  O(n) — single pass with two pointers
     * Space Complexity: O(1) — only a few integer variables used
     */
    public int maxWater(int[] heights, int[] durability) {
        // Step 1: Initialize two pointers at the leftmost and rightmost walls.
        //         This is the standard starting point for the two-pointer technique.
        int left = 0;
        int right = heights.length - 1;

        // Step 2: Track the best (maximum) valid water found so far.
        int maxWaterFound = 0;

        // Step 3: Loop until the two pointers meet.
        //         When left == right, there's only one wall — no pair possible.
        while (left < right) {

            // Step 4: Compute the water level for this pair.
            //         The water level is limited by the shorter of the two walls.
            int waterLevel = Math.min(heights[left], heights[right]);

            // Step 5: Compute the total water volume for this pair.
            //         Volume = water level * horizontal distance between walls.
            int water = waterLevel * (right - left);

            // Step 6: Check the durability constraint for BOTH walls.
            //         A wall is valid only if its durability is STRICTLY GREATER than
            //         the water level pressing against it.
            boolean leftValid  = durability[left]  > waterLevel;
            boolean rightValid = durability[right] > waterLevel;

            // Step 7: If both walls satisfy the durability constraint, this is a
            //         candidate for the answer. Update maxWaterFound if it's better.
            if (leftValid && rightValid) {
                maxWaterFound = Math.max(maxWaterFound, water);
            }

            // Step 8: Move the pointer pointing to the shorter wall inward.
            //         Rationale: The water level is determined by the shorter wall.
            //         Moving the taller wall inward cannot increase the water level,
            //         so we move the shorter wall to potentially find a taller one.
            //         If heights are equal, move either pointer (we move left here).
            if (heights[left] <= heights[right]) {
                left++;
            } else {
                right--;
            }
        }

        // Step 9: Return the maximum valid water found, or 0 if none was valid.
        return maxWaterFound;
    }

    /**
     * Entry point — demonstrates the solution with the provided examples and
     * prints results to standard output.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // ---------------------------------------------------------------
        // Example 1:
        //   heights     = [1, 8, 6, 2, 5, 4, 8, 3, 7]
        //   durability  = [10, 9, 7, 5, 6, 5, 9, 4, 8]
        //   Expected output: 49
        //
        //   Trace of key steps:
        //   left=0 (h=1,d=10), right=8 (h=7,d=8)
        //     waterLevel=min(1,7)=1, water=1*8=8
        //     leftValid=10>1=true, rightValid=8>1=true → maxWater=8
        //     heights[0]=1 <= heights[8]=7 → move left to 1
        //
        //   left=1 (h=8,d=9), right=8 (h=7,d=8)
        //     waterLevel=min(8,7)=7, water=7*7=49
        //     leftValid=9>7=true, rightValid=8>7=true → maxWater=49
        //     heights[1]=8 > heights[8]=7 → move right to 7
        //
        //   left=1 (h=8,d=9), right=7 (h=3,d=4)
        //     waterLevel=min(8,3)=3, water=3*6=18
        //     leftValid=9>3=true, rightValid=4>3=true → maxWater stays 49
        //     heights[1]=8 > heights[7]=3 → move right to 6
        //
        //   left=1 (h=8,d=9), right=6 (h=8,d=9)
        //     waterLevel=min(8,8)=8, water=8*5=40
        //     leftValid=9>8=true, rightValid=9>8=true → maxWater stays 49
        //     heights[1]=8 <= heights[6]=8 → move left to 2
        //
        //   ... remaining pairs all produce water <= 49
        //   Final answer: 49 ✓
        // ---------------------------------------------------------------
        int[] heights1    = {1, 8, 6, 2, 5, 4, 8, 3, 7};
        int[] durability1 = {10, 9, 7, 5, 6, 5, 9, 4, 8};
        int result1 = solution.maxWater(heights1, durability1);
        System.out.println("Example 1:");
        System.out.println("  heights     = [1, 8, 6, 2, 5, 4, 8, 3, 7]");
        System.out.println("  durability  = [10, 9, 7, 5, 6, 5, 9, 4, 8]");
        System.out.println("  Expected: 49");
        System.out.println("  Got:      " + result1);
        System.out.println();

        // ---------------------------------------------------------------
        // Example 2:
        //   heights    = [3, 1, 2, 4]
        //   durability = [3, 5, 3, 4]
        //   Expected output: 4
        //
        //   Trace of all pairs (brute-force verification):
        //   (0,1): waterLevel=min(3,1)=1, water=1*1=1
        //          d[0]=3>1 ✓, d[1]=5>1 ✓ → valid, water=1
        //   (0,2): waterLevel=min(3,2)=2, water=2*2=4
        //          d[0]=3>2 ✓, d[2]=3>2 ✓ → valid, water=4
        //   (0,3): waterLevel=min(3,4)=3, water=3*3=9
        //          d[0]=3>3? NO (3 is NOT > 3) → INVALID
        //   (1,2): waterLevel=min(1,2)=1, water=1*1=1
        //          d[1]=5>1 ✓, d[2]=3>1 ✓ → valid, water=1
        //   (1,3): waterLevel=min(1,4)=1, water=1*2=2
        //          d[1]=5>1 ✓, d[3]=4>1 ✓ → valid, water=2
        //   (2,3): waterLevel=min(2,4)=2, water=2*1=2
        //          d[2]=3>2 ✓, d[3]=4>2 ✓ → valid, water=2
        //   Maximum valid water = 4 ✓
        //
        //   Two-pointer trace:
        //   left=0 (h=3,d=3), right=3 (h=4,d=4)
        //     waterLevel=3, water=9
        //     leftValid=3>3? false → INVALID, skip
        //     heights[0]=3 <= heights[3]=4 → move left to 1
        //
        //   left=1 (h=1,d=5), right=3 (h=4,d=4)
        //     waterLevel=1, water=2
        //     leftValid=5>1=true, rightValid=4>1=true → maxWater=2
        //     heights[1]=1 <= heights[3]=4 → move left to 2
        //
        //   left=2 (h=2,d=3), right=3 (h=4,d=4)
        //     waterLevel=2, water=2
        //     leftValid=3>2=true, rightValid=4>2=true → maxWater stays 2
        //     heights[2]=2 <= heights[3]=4 → move left to 3
        //
        //   left=3 == right=3 → loop ends
        //   Result from two-pointer: 2 — but expected is 4!
        //
        //   The two-pointer approach MISSES pair (0,2) because it skips index 0
        //   after finding pair (0,3) invalid. We need a more thorough approach.
        // ---------------------------------------------------------------

        // NOTE: The standard two-pointer approach can miss valid pairs when an
        // invalid pair causes us to skip a pointer position that would have formed
        // a valid pair with a different partner. We need a brute-force O(n^2) or
        // a smarter strategy. Given n <= 10^5, O(n^2) may be too slow for large
        // inputs, but let's first verify correctness, then optimize if needed.
        //
        // For this problem, since the durability constraint can invalidate pairs
        // in a non-monotonic way, we use a careful two-pointer that, when a pair
        // is invalid, still moves the pointer of the shorter wall (same as valid
        // case) — this is what the code above does. Let's re-examine example 2:
        //
        // The issue: pair (0,2) has water=4 but the two-pointer never evaluates it
        // because after (0,3) is invalid, we move left to 1, skipping (0,2).
        //
        // Solution: We need to also check pairs where we move the pointer of the
        // wall that FAILED the durability check, not just the shorter wall.
        // A robust approach: use brute force for correctness, or use a modified
        // two-pointer that handles invalid cases differently.
        //
        // Given constraints (n up to 10^5), O(n^2) = 10^10 is too slow.
        // We need a smarter O(n log n) or O(n) approach.
        //
        // Key insight: When a pair (left, right) is invalid because one wall's
        // durability is insufficient, we should move THAT wall's pointer (not
        // necessarily the shorter wall's pointer), because increasing/decreasing
        // the water level might make it valid.
        //
        // Revised strategy:
        // - If both valid: record water, move shorter wall pointer
        // - If left invalid (durability[left] <= waterLevel): move left pointer
        //   (left wall can't hold this water level; moving inward might find a
        //    shorter partner reducing waterLevel, but that's counterproductive...
        //    actually we need a taller left wall or shorter right wall)
        // - If right invalid: move right pointer
        // - If both invalid: move both (or move shorter)
        //
        // Actually, let's think more carefully. The water level = min(h[l], h[r]).
        // durability[l] must be > waterLevel = min(h[l], h[r]).
        // If h[l] <= h[r]: waterLevel = h[l], so we need durability[l] > h[l].
        //   This is a property of wall l alone! It doesn't depend on the partner.
        //   So if durability[l] <= h[l], wall l can NEVER be the shorter wall.
        //   We should skip it entirely as the left pointer.
        // If h[l] > h[r]: waterLevel = h[r], so we need durability[r] > h[r].
        //   Similarly, if durability[r] <= h[r], wall r can never be the shorter wall.
        //
        // But a wall can still participate as the TALLER wall in a valid pair,
        // as long as the shorter wall has sufficient durability.
        //
        // This is getting complex. Let me think of a cleaner approach.
        //
        // Alternative: For each pair (i,j), waterLevel