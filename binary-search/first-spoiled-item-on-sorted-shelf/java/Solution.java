/*
 * Title: First Spoiled Item on Sorted Shelf
 * Difficulty: Easy
 * Topic: Binary Search
 *
 * Problem Description:
 * A grocery store arranges items on a shelf sorted by their expiration dates in ascending order
 * (oldest expiration date first). Each item has an integer expiration date represented as the
 * number of days from today. An item is considered 'spoiled' if its expiration date is less than
 * or equal to 0 (meaning it has already expired or expires today).
 *
 * Given a sorted array `expirations` of integers representing the expiration dates of items on
 * the shelf, return the index of the FIRST NON-SPOILED item (i.e., the first item with an
 * expiration date strictly greater than 0). If all items are spoiled, return -1.
 * If no items are spoiled, return 0.
 *
 * You must solve this in O(log n) time complexity.
 *
 * Constraints:
 * - 1 <= expirations.length <= 10^5
 * - -10^4 <= expirations[i] <= 10^4
 * - The array is sorted in non-decreasing order.
 *
 * Example 1:
 * Input: expirations = [-5, -3, -1, 0, 2, 4, 7]
 * Output: 4
 * Explanation: Items at indices 0 through 3 have expiration dates <= 0 (spoiled).
 *              The first non-spoiled item is at index 4 with expiration date 2.
 *
 * Example 2:
 * Input: expirations = [1, 3, 5, 8]
 * Output: 0
 * Explanation: All items have expiration dates > 0, so the first non-spoiled item is at index 0.
 *
 * Example 3:
 * Input: expirations = [-4, -2, 0]
 * Output: -1
 * Explanation: All items are spoiled (expiration dates <= 0), so return -1.
 */

public class Solution {

    /**
     * Finds the index of the first non-spoiled item on the shelf using binary search.
     *
     * <p>Key Insight: Since the array is sorted in non-decreasing order, all spoiled items
     * (expiration <= 0) appear at the beginning, and all non-spoiled items (expiration > 0)
     * appear at the end. This is a classic "find first element satisfying a condition" pattern
     * perfectly suited for binary search.</p>
     *
     * <p>We use a "left boundary" binary search variant:
     * - If mid element is spoiled (<=0), the answer must be to the RIGHT → move left pointer up
     * - If mid element is non-spoiled (>0), this COULD be the answer, but there might be an
     *   earlier non-spoiled item → record this index and move right pointer down to search left</p>
     *
     * @param expirations A sorted (non-decreasing) integer array of expiration dates in days from today.
     *                    Negative values and zero mean the item is spoiled.
     * @return The index of the first item with expiration date strictly greater than 0.
     *         Returns 0 if no items are spoiled (all are fresh).
     *         Returns -1 if all items are spoiled.
     *
     * Time Complexity:  O(log n) — binary search halves the search space each iteration
     * Space Complexity: O(1)    — only a constant number of variables are used
     */
    public int firstNonSpoiled(int[] expirations) {
        // -----------------------------------------------------------------------
        // STEP 1: Handle edge cases
        // -----------------------------------------------------------------------
        // If the array is null or empty, there are no items to check.
        if (expirations == null || expirations.length == 0) {
            return -1;
        }

        // -----------------------------------------------------------------------
        // STEP 2: Quick boundary checks to avoid unnecessary binary search
        // -----------------------------------------------------------------------

        // If the LAST element is spoiled (<=0), then ALL elements are spoiled
        // because the array is sorted in non-decreasing order.
        // Example: [-4, -2, 0] → last element is 0 (<=0) → all spoiled → return -1
        if (expirations[expirations.length - 1] <= 0) {
            return -1;
        }

        // If the FIRST element is non-spoiled (>0), then ALL elements are non-spoiled
        // because the array is sorted in non-decreasing order.
        // Example: [1, 3, 5, 8] → first element is 1 (>0) → no spoiled items → return 0
        if (expirations[0] > 0) {
            return 0;
        }

        // -----------------------------------------------------------------------
        // STEP 3: Set up binary search boundaries
        // -----------------------------------------------------------------------
        // At this point, we know:
        //   - expirations[0] <= 0  (first element is spoiled)
        //   - expirations[n-1] > 0 (last element is NOT spoiled)
        // So the answer must be somewhere in the range [1, n-1].

        int left = 0;                          // Start of search range (inclusive)
        int right = expirations.length - 1;    // End of search range (inclusive)

        // 'result' stores the best (leftmost) non-spoiled index found so far.
        // We initialize it to -1 as a "not found yet" sentinel.
        int result = -1;

        // -----------------------------------------------------------------------
        // STEP 4: Binary search loop
        // -----------------------------------------------------------------------
        // Continue as long as there are elements to examine (left hasn't passed right).
        while (left <= right) {

            // Calculate the middle index.
            // We use left + (right - left) / 2 instead of (left + right) / 2
            // to AVOID INTEGER OVERFLOW when left and right are very large numbers.
            int mid = left + (right - left) / 2;

            // -----------------------------------------------------------------------
            // STEP 4a: Check if the middle element is spoiled or not
            // -----------------------------------------------------------------------
            if (expirations[mid] > 0) {
                // The middle element is NON-SPOILED (expiration > 0).
                // This is a CANDIDATE for our answer — record it.
                // But we must keep searching LEFT to see if there's an earlier non-spoiled item.
                //
                // Example trace for [-5, -3, -1, 0, 2, 4, 7]:
                //   Iteration 1: left=0, right=6, mid=3, expirations[3]=0 → spoiled → left=4
                //   Iteration 2: left=4, right=6, mid=5, expirations[5]=4 → non-spoiled → result=5, right=4
                //   Iteration 3: left=4, right=4, mid=4, expirations[4]=2 → non-spoiled → result=4, right=3
                //   Loop ends: left(4) > right(3)
                //   Return result = 4 ✓

                result = mid;    // Record this index as the current best answer

                // Narrow search to the LEFT half to find an even earlier non-spoiled item
                right = mid - 1;

            } else {
                // The middle element IS SPOILED (expiration <= 0).
                // The first non-spoiled item must be to the RIGHT of mid.
                // Narrow search to the RIGHT half.
                left = mid + 1;
            }
        }

        // -----------------------------------------------------------------------
        // STEP 5: Return the result
        // -----------------------------------------------------------------------
        // 'result' holds the leftmost index where expiration > 0, or -1 if none found.
        return result;
    }

    /**
     * Main method to demonstrate and test the firstNonSpoiled algorithm with sample inputs.
     * Traces through all provided examples to verify correctness.
     *
     * @param args Command-line arguments (not used in this solution).
     */
    public static void main(String[] args) {
        // Create an instance of Solution to call the non-static method
        Solution solution = new Solution();

        System.out.println("=== First Spoiled Item on Sorted Shelf ===");
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 1: Mixed spoiled and non-spoiled items
        // Expected Output: 4
        // Explanation: Indices 0-3 have values [-5, -3, -1, 0] which are all <= 0 (spoiled).
        //              Index 4 has value 2 which is > 0 (first non-spoiled).
        // -----------------------------------------------------------------------
        int[] expirations1 = {-5, -3, -1, 0, 2, 4, 7};
        int result1 = solution.firstNonSpoiled(expirations1);
        System.out.println("Example 1:");
        System.out.println("  Input:    [-5, -3, -1, 0, 2, 4, 7]");
        System.out.println("  Expected: 4");
        System.out.println("  Got:      " + result1);
        System.out.println("  Pass:     " + (result1 == 4));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2: No spoiled items at all
        // Expected Output: 0
        // Explanation: All items have expiration > 0, so the first non-spoiled is at index 0.
        // -----------------------------------------------------------------------
        int[] expirations2 = {1, 3, 5, 8};
        int result2 = solution.firstNonSpoiled(expirations2);
        System.out.println("Example 2:");
        System.out.println("  Input:    [1, 3, 5, 8]");
        System.out.println("  Expected: 0");
        System.out.println("  Got:      " + result2);
        System.out.println("  Pass:     " + (result2 == 0));
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 3: All items are spoiled
        // Expected Output: -1
        // Explanation: All items have expiration <= 0, so return -1.
        // -----------------------------------------------------------------------
        int[] expirations3 = {-4, -2, 0};
        int result3 = solution.firstNonSpoiled(expirations3);
        System.out.println("Example 3:");
        System.out.println("  Input:    [-4, -2, 0]");
        System.out.println("  Expected: -1");
        System.out.println("  Got:      " + result3);
        System.out.println("  Pass:     " + (result3 == -1));
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Edge Case 1: Single non-spoiled item
        // Expected Output: 0
        // -----------------------------------------------------------------------
        int[] expirations4 = {5};
        int result4 = solution.firstNonSpoiled(expirations4);
        System.out.println("Edge Case 1 (Single non-spoiled item):");
        System.out.println("  Input:    [5]");
        System.out.println("  Expected: 0");
        System.out.println("  Got:      " + result4);
        System.out.println("  Pass:     " + (result4 == 0));
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Edge Case 2: Single spoiled item
        // Expected Output: -1
        // -----------------------------------------------------------------------
        int[] expirations5 = {-3};
        int result5 = solution.firstNonSpoiled(expirations5);
        System.out.println("Edge Case 2 (Single spoiled item):");
        System.out.println("  Input:    [-3]");
        System.out.println("  Expected: -1");
        System.out.println("  Got:      " + result5);
        System.out.println("  Pass:     " + (result5 == -1));
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Edge Case 3: Item expiring exactly today (0 = spoiled boundary)
        // Expected Output: -1 (0 is considered spoiled)
        // -----------------------------------------------------------------------
        int[] expirations6 = {0};
        int result6 = solution.firstNonSpoiled(expirations6);
        System.out.println("Edge Case 3 (Single item expiring today, value=0):");
        System.out.println("  Input:    [0]");
        System.out.println("  Expected: -1");
        System.out.println("  Got:      " + result6);
        System.out.println("  Pass:     " + (result6 == -1));
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Edge Case 4: All same non-spoiled values
        // Expected Output: 0
        // -----------------------------------------------------------------------
        int[] expirations7 = {3, 3, 3, 3};
        int result7 = solution.firstNonSpoiled(expirations7);
        System.out.println("Edge Case 4 (All same non-spoiled values):");
        System.out.println("  Input:    [3, 3, 3, 3]");
        System.out.println("  Expected: 0");
        System.out.println("  Got:      " + result7);
        System.out.println("  Pass:     " + (result7 == 0));
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Edge Case 5: Transition right at the boundary
        // Expected Output: 1
        // -----------------------------------------------------------------------
        int[] expirations8 = {-1, 1};
        int result8 = solution.firstNonSpoiled(expirations8);
        System.out.println("Edge Case 5 (Two elements, transition at index 1):");
        System.out.println("  Input:    [-1, 1]");
        System.out.println("  Expected: 1");
        System.out.println("  Got:      " + result8);
        System.out.println("  Pass:     " + (result8 == 1));
        System.out.println();

        System.out.println("=== All tests completed ===");
    }
}