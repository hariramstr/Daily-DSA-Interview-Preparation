/*
 * Title: Minimum Window Containing All Favorite Numbers
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an integer array `nums` and a list of distinct integers `favorites`.
 * Your task is to find the shortest contiguous subarray of `nums` that contains all
 * the integers in `favorites` (each favorite number must appear at least once in the subarray).
 *
 * Return the length of the minimum such subarray. If no such subarray exists, return -1.
 *
 * Constraints:
 * - 1 <= nums.length <= 10^5
 * - 1 <= nums[i] <= 10^6
 * - 1 <= favorites.length <= 100
 * - 1 <= favorites[i] <= 10^6
 * - All values in `favorites` are distinct.
 * - It is guaranteed that `favorites.length <= nums.length`.
 *
 * Example 1:
 * Input: nums = [4, 1, 3, 2, 1, 5, 3, 2], favorites = [1, 3, 2]
 * Output: 3
 * Explanation: The subarray nums[1..3] = [1, 3, 2] contains all favorites with length 3.
 *
 * Example 2:
 * Input: nums = [7, 2, 5, 1, 8], favorites = [3, 6]
 * Output: -1
 * Explanation: Neither 3 nor 6 appears in nums, so there is no valid subarray.
 */

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Solution class for the "Minimum Window Containing All Favorite Numbers" problem.
 *
 * <p>Approach: Classic Sliding Window (Two Pointers)
 * We maintain a window [left, right] and expand it by moving `right` forward.
 * Whenever the window contains all favorites, we try to shrink it from the left
 * to find the minimum valid window.
 */
public class Solution {

    /**
     * Finds the length of the shortest contiguous subarray of {@code nums}
     * that contains every element in {@code favorites} at least once.
     *
     * <p>Algorithm Overview (Sliding Window):
     * <ol>
     *   <li>Build a "need" map: how many of each favorite we still need in the window.</li>
     *   <li>Use two pointers {@code left} and {@code right} to define the current window.</li>
     *   <li>Expand {@code right}: if the element is a favorite, decrement its need count.
     *       When a need count drops to 0, we have satisfied one more favorite.</li>
     *   <li>Once all favorites are satisfied ({@code satisfied == favorites.length}),
     *       record the window length and try to shrink from {@code left}.</li>
     *   <li>Shrink {@code left}: if the element leaving the window is a favorite and
     *       its count was exactly 0 (we were relying on this copy), increment need count
     *       and decrement {@code satisfied}.</li>
     *   <li>Repeat until {@code right} reaches the end of {@code nums}.</li>
     * </ol>
     *
     * @param nums      the input integer array to search within
     * @param favorites the list of distinct integers that must all appear in the subarray
     * @return the length of the minimum window subarray, or -1 if none exists
     *
     * @implNote Time Complexity:  O(n) — each element is added and removed from the window at most once.
     *           Space Complexity: O(f) — where f = favorites.length, for the need map and favorites set.
     */
    public int minWindow(int[] nums, int[] favorites) {

        // -----------------------------------------------------------------------
        // Step 1: Edge-case guard — if either array is empty, no window is possible.
        // -----------------------------------------------------------------------
        if (nums == null || nums.length == 0 || favorites == null || favorites.length == 0) {
            return -1;
        }

        // -----------------------------------------------------------------------
        // Step 2: Build a HashSet of favorites for O(1) membership checks,
        //         and a HashMap<number, needCount> to track how many more copies
        //         of each favorite we still need inside the current window.
        //
        //         Initially every favorite has needCount = 1 (we need exactly one copy).
        // -----------------------------------------------------------------------
        Set<Integer> favSet = new HashSet<>();
        Map<Integer, Integer> need = new HashMap<>();

        for (int fav : favorites) {
            favSet.add(fav);
            need.put(fav, 1); // we need 1 copy of each favorite
        }

        // -----------------------------------------------------------------------
        // Step 3: Initialize sliding-window variables.
        //
        //   left       — left boundary of the current window (inclusive)
        //   satisfied  — how many distinct favorites are currently fully covered
        //                (i.e., their needCount has reached 0 or below)
        //   minLen     — best (smallest) window length found so far
        //   required   — total number of distinct favorites we must satisfy
        // -----------------------------------------------------------------------
        int left = 0;
        int satisfied = 0;
        int minLen = Integer.MAX_VALUE;
        int required = favorites.length;

        // -----------------------------------------------------------------------
        // Step 4: Expand the window by moving `right` from 0 to nums.length - 1.
        // -----------------------------------------------------------------------
        for (int right = 0; right < nums.length; right++) {

            int currentNum = nums[right];

            // ------------------------------------------------------------------
            // Step 4a: If the current element is one of the favorites,
            //          update its need count in the map.
            //
            //          need.get(currentNum) before decrement:
            //            > 0  → we still needed this element; decrementing brings
            //                   us closer to satisfying this favorite.
            //            == 0 → we already had enough copies; this is an "extra"
            //                   copy (need goes negative), which is fine — it means
            //                   we have surplus.
            //            < 0  → already surplus; decrement makes it more negative.
            //
            //          We only increment `satisfied` when the count transitions
            //          from 1 → 0 (exactly when we fulfill the last needed copy).
            // ------------------------------------------------------------------
            if (favSet.contains(currentNum)) {
                int countBefore = need.get(currentNum);
                need.put(currentNum, countBefore - 1);

                if (countBefore == 1) {
                    // This element just satisfied the requirement for `currentNum`
                    satisfied++;
                }
            }

            // ------------------------------------------------------------------
            // Step 4b: While the window satisfies ALL favorites, try to shrink
            //          from the left to minimize the window length.
            // ------------------------------------------------------------------
            while (satisfied == required) {

                // Record the current window length if it's the best so far.
                int windowLen = right - left + 1;
                if (windowLen < minLen) {
                    minLen = windowLen;
                }

                // --------------------------------------------------------------
                // Step 4c: Attempt to shrink the window by advancing `left`.
                //
                //          Before moving left forward, check if nums[left] is a
                //          favorite. If it is, removing it might break the
                //          "all favorites covered" condition.
                //
                //          need.get(leftNum) after increment:
                //            == 1  → we just lost the last required copy of leftNum;
                //                    the window no longer covers this favorite.
                //                    Decrement `satisfied`.
                //            <= 0  → we had surplus copies; losing one still leaves
                //                    the favorite covered. `satisfied` unchanged.
                // --------------------------------------------------------------
                int leftNum = nums[left];
                left++; // shrink window from the left

                if (favSet.contains(leftNum)) {
                    int newCount = need.get(leftNum) + 1;
                    need.put(leftNum, newCount);

                    if (newCount == 1) {
                        // We just lost the last copy of leftNum from the window
                        satisfied--;
                    }
                }
            }
            // After the while loop, `satisfied < required`, so we need to keep
            // expanding `right` to find the next valid window.
        }

        // -----------------------------------------------------------------------
        // Step 5: Return the result.
        //         If minLen was never updated, no valid window was found → return -1.
        // -----------------------------------------------------------------------
        return (minLen == Integer.MAX_VALUE) ? -1 : minLen;
    }

    // ===========================================================================
    // Main method — demonstrates the solution with the provided examples and
    // additional edge cases, printing results to standard output.
    // ===========================================================================

    /**
     * Entry point for demonstration purposes.
     *
     * <p>Traces through each example from the problem description and prints
     * the computed result alongside the expected result for easy verification.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {

        Solution sol = new Solution();

        // -----------------------------------------------------------------------
        // Example 1 (from problem):
        //   nums      = [4, 1, 3, 2, 1, 5, 3, 2]
        //   favorites = [1, 3, 2]
        //   Expected  = 3
        //
        // Trace:
        //   right=0 (4):  not a fav. satisfied=0
        //   right=1 (1):  fav, need[1]: 1→0, satisfied=1
        //   right=2 (3):  fav, need[3]: 1→0, satisfied=2
        //   right=3 (2):  fav, need[2]: 1→0, satisfied=3 == required(3)
        //     window=[1..3] len=3, minLen=3
        //     shrink: left=0 (4) not fav, left→1
        //     window=[1..3] len=3, still satisfied=3
        //     shrink: left=1 (1) fav, need[1]: 0→1==1, satisfied=2, left→2
        //     satisfied<3, stop shrinking
        //   right=4 (1):  fav, need[1]: 1→0, satisfied=3
        //     window=[2..4] len=3, minLen stays 3
        //     shrink: left=2 (3) fav, need[3]: 0→1==1, satisfied=2, left→3
        //   right=5 (5):  not fav. satisfied=2
        //   right=6 (3):  fav, need[3]: 1→0, satisfied=3
        //     window=[3..6] len=4, minLen stays 3
        //     shrink: left=3 (2) fav, need[2]: 0→1==1, satisfied=2, left→4
        //   right=7 (2):  fav, need[2]: 1→0, satisfied=3
        //     window=[4..7] len=4, minLen stays 3
        //     shrink: left=4 (1) fav, need[1]: 0→1==1, satisfied=2, left→5
        //   End. minLen=3 ✓
        // -----------------------------------------------------------------------
        int[] nums1 = {4, 1, 3, 2, 1, 5, 3, 2};
        int[] favorites1 = {1, 3, 2};
        int result1 = sol.minWindow(nums1, favorites1);
        System.out.println("Example 1:");
        System.out.println("  nums      = " + Arrays.toString(nums1));
        System.out.println("  favorites = " + Arrays.toString(favorites1));
        System.out.println("  Result    = " + result1 + "  (Expected: 3)");
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2 (from problem):
        //   nums      = [7, 2, 5, 1, 8]
        //   favorites = [3, 6]
        //   Expected  = -1
        //
        // Trace:
        //   Neither 3 nor 6 appears in nums, so satisfied never reaches 2.
        //   minLen stays Integer.MAX_VALUE → return -1 ✓
        // -----------------------------------------------------------------------
        int[] nums2 = {7, 2, 5, 1, 8};
        int[] favorites2 = {3, 6};
        int result2 = sol.minWindow(nums2, favorites2);
        System.out.println("Example 2:");
        System.out.println("  nums      = " + Arrays.toString(nums2));
        System.out.println("  favorites = " + Arrays.toString(favorites2));
        System.out.println("  Result    = " + result2 + "  (Expected: -1)");
        System.out.println();

        // -----------------------------------------------------------------------
        // Extra Test 1: Single element array matching single favorite.
        //   nums      = [5]
        //   favorites = [5]
        //   Expected  = 1
        // -----------------------------------------------------------------------
        int[] nums3 = {5};
        int[] favorites3 = {5};
        int result3 = sol.minWindow(nums3, favorites3);
        System.out.println("Extra Test 1 (single match):");
        System.out.println("  nums      = " + Arrays.toString(nums3));
        System.out.println("  favorites = " + Arrays.toString(favorites3));
        System.out.println("  Result    = " + result3 + "  (Expected: 1)");
        System.out.println();

        // -----------------------------------------------------------------------
        // Extra Test 2: Favorites appear but not all together.
        //   nums      = [1, 2, 3, 4, 5]
        //   favorites = [1, 5]
        //   Expected  = 5  (entire array is the only window containing both 1 and 5)
        // -----------------------------------------------------------------------
        int[] nums4 = {1, 2, 3, 4, 5};
        int[] favorites4 = {1, 5};
        int result4 = sol.minWindow(nums4, favorites4);
        System.out.println("Extra Test 2 (endpoints):");
        System.out.println("  nums      = " + Arrays.toString(nums4));
        System.out.println("  favorites = " + Arrays.toString(favorites4));
        System.out.println("  Result    = " + result4 + "  (Expected: 5)");
        System.out.println();

        // -----------------------------------------------------------------------
        // Extra Test 3: Duplicate favorites in nums, window can be minimized.
        //   nums      = [2, 1, 2, 1, 2]
        //   favorites = [1, 2]
        //   Expected  = 2  (e.g., [2,1] at index 0-1 or [1,2] at index 1-2)
        // -----------------------------------------------------------------------
        int[] nums5 = {2, 1, 2, 1, 2};
        int[] favorites5 = {1, 2};
        int result5 = sol.minWindow(nums5, favorites5);
        System.out.println("Extra Test 3 (duplicates):");
        System.out.println("  nums      = " + Arrays.toString(nums5));
        System.out.println("  favorites = " + Arrays.toString(favorites5));
        System.out.println("  Result    = " + result5 + "  (Expected: 2)");
        System.out.println();

        // -----------------------------------------------------------------------
        // Extra Test 4: Only one favorite, appears multiple times.
        //   nums      = [3, 7, 3, 9, 3]
        //   favorites = [3]
        //   Expected  = 1
        // -----------------------------------------------------------------------
        int[] nums6 = {3, 7, 3, 9, 3};
        int[] favorites6 = {3};
        int result6