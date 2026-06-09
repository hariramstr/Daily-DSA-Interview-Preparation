/*
 * Title: Find the Single Displaced Element in a Rotated Sequence
 *
 * Problem Description:
 * You are given an array `nums` of `n` integers that was originally a sequence of
 * consecutive integers starting from 1 (i.e., [1, 2, 3, ..., n]). The array was then
 * rotated at some unknown pivot point, and exactly one element was replaced with a value
 * that does not belong to the original sequence.
 *
 * Your task is to find and return the displaced (replaced) element — the one element in
 * `nums` that does not fit the rotated consecutive sequence.
 *
 * A rotated consecutive sequence means the array looks like [k, k+1, ..., n, 1, 2, ..., k-1]
 * for some rotation point k.
 *
 * Constraints:
 * - 2 <= n <= 10^4
 * - 1 <= nums[i] <= 2 * n
 * - Exactly one element in `nums` is displaced (does not belong to the rotated sequence of 1 to n)
 * - All other elements are distinct and form a valid rotated consecutive sequence
 *
 * Example 1:
 * Input:  nums = [4, 5, 6, 7, 99, 1, 2, 3]
 * Output: 99
 * Explanation: The original rotated sequence would be [4, 5, 6, 7, 8, 1, 2, 3].
 *              The element at index 4 is 99 instead of 8, so 99 is the displaced element.
 *
 * Example 2:
 * Input:  nums = [3, 4, 0, 1, 2]
 * Output: 0
 * Explanation: The original rotated sequence would be [3, 4, 5, 1, 2].
 *              The element at index 2 is 0 instead of 5, so 0 is the displaced element.
 */

import java.util.*;

/**
 * Solution class for finding the single displaced element in a rotated consecutive sequence.
 *
 * <p>Core Insight:
 * In a valid rotated sequence of [1..n], the sum of all elements is always n*(n+1)/2.
 * The displaced element "broke" the sequence by replacing exactly one valid element.
 * We can reconstruct what the missing valid element should be, and the displaced element
 * is simply the one in the array that doesn't belong to [1..n].
 *
 * <p>Strategy:
 * 1. Compute the expected sum of [1..n] = n*(n+1)/2.
 * 2. Compute the actual sum of elements in nums that are in range [1..n].
 * 3. The difference tells us which valid value is missing.
 * 4. The displaced element is the one in nums that is NOT in [1..n].
 */
public class Solution {

    /**
     * Finds the displaced element in a rotated consecutive sequence.
     *
     * <p>Algorithm walkthrough:
     * - A valid rotated sequence of length n contains every integer from 1 to n exactly once.
     * - Exactly one element has been replaced by a "displaced" value (outside [1..n] or a duplicate).
     * - We identify the displaced element by finding which value in nums does NOT belong to [1..n].
     *
     * <p>Step-by-step:
     * 1. n = nums.length
     * 2. Put all elements of nums into a HashSet for O(1) lookup.
     * 3. For each value v from 1 to n, check if v is present in the set.
     *    - If v is NOT present, it means v was the element that got displaced/replaced.
     *      We record this as the "missing" valid value.
     * 4. Now scan nums to find the element that is NOT in [1..n] — that is the displaced element.
     *    - Any number < 1 or > n cannot be part of the valid sequence.
     *    - If all numbers appear to be in [1..n] but one is duplicated, the duplicate is displaced.
     *
     * <p>Wait — there's a subtlety: the displaced value could itself be in [1..n] (a duplicate).
     * For example, if n=5 and the sequence should be [3,4,5,1,2] but index 2 has 0 instead of 5,
     * then 0 is outside [1..n] and is easy to spot.
     * But what if the displaced value is, say, 2 (already in the sequence)?
     * Then we'd have a duplicate 2 and a missing value.
     *
     * <p>Robust approach using sum + set:
     * - Compute expected sum S = n*(n+1)/2.
     * - Walk through nums:
     *     * If nums[i] is in [1..n], add it to a "seen" set and to actualValidSum.
     *     * If nums[i] is outside [1..n], it is immediately the displaced element (record it).
     * - After the walk:
     *     * missingValid = S - actualValidSum  (the value from [1..n] that was replaced)
     *     * If we found an out-of-range displaced element, return it.
     *     * Otherwise, the displaced element is the duplicate inside [1..n]:
     *       it equals nums[i] where nums[i] was already in "seen" — but we need to find it.
     *       Actually: actualValidSum counted each in-range value once (via set), so
     *       the duplicate contributed 0 extra to actualValidSum. The real sum of nums
     *       (counting duplicates) minus actualValidSum gives us the displaced duplicate value.
     *
     * <p>Let me re-examine with a cleaner formulation:
     * - realSum = sum of ALL elements in nums (including the displaced one).
     * - validSum = sum of elements in nums that are in [1..n] AND not yet seen (first occurrence).
     *   This equals S - missingValid.
     * - displaced = realSum - validSum - (duplicate in-range value, if any)
     *
     * <p>Simplest correct approach:
     * 1. Use a boolean visited[] of size n+1.
     * 2. Scan nums:
     *    - If nums[i] < 1 or nums[i] > n → this is the displaced element (out of range).
     *    - Else mark visited[nums[i]] = true.
     * 3. If we found an out-of-range element, return it.
     * 4. Otherwise, find the index j where visited[j] == false → that value j was replaced
     *    by a duplicate. The duplicate is the displaced element.
     *    To find the duplicate: scan nums again for the value that appears twice.
     *
     * @param nums the input array of n integers (rotated sequence with one displaced element)
     * @return the displaced element that does not belong to the valid rotated sequence
     *
     * Time Complexity:  O(n) — we make at most two linear passes over the array
     * Space Complexity: O(n) — we use a visited boolean array of size n+1
     */
    public int findDisplacedElement(int[] nums) {
        int n = nums.length;

        // -----------------------------------------------------------------------
        // Step 1: Create a visited array to track which values in [1..n] appear.
        // Index i corresponds to value i. visited[i] = true means value i was seen.
        // -----------------------------------------------------------------------
        boolean[] visited = new boolean[n + 1]; // indices 0..n; we use 1..n

        // -----------------------------------------------------------------------
        // Step 2: First pass — scan nums.
        //   - If a value is outside [1..n], it CANNOT be part of the valid sequence.
        //     It is immediately identified as the displaced element.
        //   - If a value is inside [1..n], mark it as visited.
        // -----------------------------------------------------------------------
        int outOfRangeDisplaced = -1; // will hold the displaced value if it's out of [1..n]

        for (int i = 0; i < n; i++) {
            int val = nums[i];

            if (val < 1 || val > n) {
                // This value cannot belong to a sequence of 1..n
                // It is the displaced element
                outOfRangeDisplaced = val;
                // Do NOT break — we still want to mark the remaining valid elements
                // so we can find the missing value if needed (though for out-of-range
                // displaced, we already know the answer).
            } else {
                // Value is in [1..n]; mark it as seen
                visited[val] = true;
            }
        }

        // -----------------------------------------------------------------------
        // Step 3: If we found an out-of-range displaced element, return it.
        // -----------------------------------------------------------------------
        if (outOfRangeDisplaced != -1) {
            return outOfRangeDisplaced;
        }

        // -----------------------------------------------------------------------
        // Step 4: All elements of nums are in [1..n], but one value is missing
        // (replaced by a duplicate). Find the missing value — the unvisited index.
        // Then find the duplicate — the value that appears twice in nums.
        // The duplicate is the displaced element.
        // -----------------------------------------------------------------------

        // Find the missing value in [1..n]
        int missingValue = -1;
        for (int v = 1; v <= n; v++) {
            if (!visited[v]) {
                missingValue = v;
                break; // exactly one value is missing
            }
        }

        // -----------------------------------------------------------------------
        // Step 5: Find the duplicate in nums (the value that appears twice).
        // We use a HashSet: if we try to add a value that's already present, it's the duplicate.
        // -----------------------------------------------------------------------
        Set<Integer> seen = new HashSet<>();
        for (int val : nums) {
            if (!seen.add(val)) {
                // seen.add() returns false when the element was already present → duplicate found
                return val; // this is the displaced element (it replaced missingValue)
            }
        }

        // -----------------------------------------------------------------------
        // Step 6: This line should never be reached given the problem guarantees,
        // but we return -1 as a safety fallback.
        // -----------------------------------------------------------------------
        return -1;
    }

    /**
     * Alternative approach using mathematical sum difference.
     *
     * <p>Key insight:
     * - The expected sum of [1..n] is n*(n+1)/2.
     * - The actual sum of nums includes the displaced element instead of the missing valid element.
     * - actualSum = expectedSum - missingValid + displaced
     * - Therefore: displaced - missingValid = actualSum - expectedSum
     *
     * <p>But this alone gives us only the difference, not the individual values.
     * We combine it with the visited-array approach to find missingValid, then compute displaced.
     *
     * @param nums the input array of n integers
     * @return the displaced element
     *
     * Time Complexity:  O(n) — two linear passes
     * Space Complexity: O(n) — visited boolean array
     */
    public int findDisplacedElementMath(int[] nums) {
        int n = nums.length;

        // -----------------------------------------------------------------------
        // Step 1: Compute the expected sum of the valid sequence [1..n].
        // Formula: n * (n + 1) / 2
        // -----------------------------------------------------------------------
        long expectedSum = (long) n * (n + 1) / 2;
        // We use long to avoid integer overflow for large n.

        // -----------------------------------------------------------------------
        // Step 2: Compute the actual sum of all elements in nums.
        // -----------------------------------------------------------------------
        long actualSum = 0;
        for (int val : nums) {
            actualSum += val;
        }

        // -----------------------------------------------------------------------
        // Step 3: The difference tells us: displaced - missingValid = actualSum - expectedSum
        // We need to find missingValid separately.
        // -----------------------------------------------------------------------
        long diff = actualSum - expectedSum; // displaced = missingValid + diff

        // -----------------------------------------------------------------------
        // Step 4: Use a visited array to find which value in [1..n] is missing.
        // -----------------------------------------------------------------------
        boolean[] visited = new boolean[n + 1];
        for (int val : nums) {
            if (val >= 1 && val <= n) {
                visited[val] = true;
            }
        }

        // -----------------------------------------------------------------------
        // Step 5: Find the missing value (the one not visited).
        // -----------------------------------------------------------------------
        long missingValid = -1;
        for (int v = 1; v <= n; v++) {
            if (!visited[v]) {
                missingValid = v;
                break;
            }
        }

        // -----------------------------------------------------------------------
        // Step 6: Compute and return the displaced element.
        // displaced = missingValid + diff
        // -----------------------------------------------------------------------
        return (int) (missingValid + diff);
    }

    /**
     * Main method to demonstrate and verify the solution with sample inputs.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        System.out.println("=== Find the Single Displaced Element in a Rotated Sequence ===");
        System.out.println();

        // ------------------------------------------------------------------
        // Example 1 from the problem:
        // Input:  [4, 5, 6, 7, 99, 1, 2, 3]
        // Valid rotated sequence would be: [4, 5, 6, 7, 8, 1, 2, 3]
        // 99 replaced 8, so 99 is displaced.
        // Expected Output: 99
        // ------------------------------------------------------------------
        int[] nums1 = {4, 5, 6, 7, 99, 1, 2, 3};
        int result1 = solution.findDisplacedElement(nums1);
        int result1Math = solution.findDisplacedElementMath(nums1);
        System.out.println("Example 1:");
        System.out.println("  Input:           " + Arrays.toString(nums1));
        System.out.println("  Expected Output: 99");
        System.out.println("  Approach 1 Output: " + result1);
        System.out.println("  Approach 2 Output: " + result1Math);
        System.out.println("  Correct? " + (result1 == 99 && result1Math == 99));
        System.out.println();

        // ------------------------------------------------------------------
        // Example 2 from the problem:
        // Input:  [3, 4, 0, 1, 2]
        // Valid rotated sequence would be: [3, 4, 5, 1, 2]
        // 0 replaced 5, so 0 is displaced.
        // Expected Output: 0
        // ------------------------------------------------------------------
        int[] nums2 = {3, 4, 0, 1, 2};
        int result2 = solution.findDisplacedElement(nums2);
        int result2Math = solution.findDisplacedElementMath(nums2);
        System.out.println("Example 2:");
        System.out.println("  Input:           " + Arrays.toString(nums2));
        System.out.println("  Expected Output: 0");
        System.out.println("  Approach 1 Output: " + result2);
        System.out.println("  Approach 2 Output: " + result2Math);
        System.out.println("  Correct? " + (result2 == 0 && result2Math == 0));
        System.out.println();

        // ------------------------------------------------------------------
        // Additional Test Case 3:
        // Input:  [1, 2, 3, 4, 5, 6, 7, 8, 10]  (no rotation, 10 replaced 9)
        // n = 9, valid sequence [1..9], 10 is out of range → displaced = 10
        // Expected Output: 10
        // ------------------------------------------------------------------
        int[] nums3 = {1, 2, 3, 4, 5, 6, 7, 8, 10};
        int result3 = solution.findDisplacedElement(nums3);
        int