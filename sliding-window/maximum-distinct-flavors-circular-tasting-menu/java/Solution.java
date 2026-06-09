```java
/*
 * Title: Maximum Distinct Flavors in a Circular Tasting Menu
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * A restaurant offers a circular tasting menu with `n` dishes arranged in a circle,
 * where each dish has a flavor profile represented by an integer in `flavors[]`.
 * A group of `k` guests will each receive a contiguous segment of `k` dishes from
 * this circular arrangement (wrapping around if necessary). However, due to dietary
 * restrictions, the segment must contain at most `m` repeated flavor values
 * (i.e., the count of any single flavor within the chosen segment must not exceed `m`).
 *
 * Your task is to find the maximum number of distinct flavor values achievable in
 * any valid contiguous circular segment of exactly `k` dishes, subject to the
 * constraint that no single flavor appears more than `m` times within the segment.
 * If no valid segment exists, return `-1`.
 *
 * Constraints:
 * - 1 <= n <= 100000
 * - 1 <= k <= n
 * - 1 <= m <= k
 * - 1 <= flavors[i] <= 100000
 *
 * Example 1:
 * Input: flavors = [1, 2, 1, 3, 2, 4, 1], k = 4, m = 1
 * Output: 4
 * Explanation: The segment [3, 2, 4, 1] (indices 3-6) contains 4 distinct flavors,
 * each appearing exactly once, satisfying m = 1.
 *
 * Example 2:
 * Input: flavors = [5, 5, 5, 5], k = 3, m = 1
 * Output: -1
 * Explanation: Every contiguous segment of length 3 contains flavor 5 at least twice,
 * violating the m = 1 constraint. No valid segment exists.
 */

import java.util.HashMap;
import java.util.Map;

/**
 * Solution class for the Maximum Distinct Flavors in a Circular Tasting Menu problem.
 * 
 * <p>Approach: We use a fixed-size sliding window of length k over a "doubled" array
 * (to simulate circular wrapping). For each window of size k, we check if it is valid
 * (no flavor appears more than m times) and track the maximum number of distinct flavors.</p>
 */
public class Solution {

    /**
     * Finds the maximum number of distinct flavor values in any valid contiguous
     * circular segment of exactly k dishes, where no single flavor appears more
     * than m times.
     *
     * <p>Algorithm Overview:
     * 1. To handle the circular nature, we conceptually double the array: work on
     *    indices 0 to 2*n-1, but only consider windows whose starting index is
     *    in [0, n-1] (so we cover all circular windows of length k).
     * 2. We maintain a sliding window of exactly size k using a HashMap to count
     *    flavor frequencies within the current window.
     * 3. We also track how many flavors in the current window violate the constraint
     *    (i.e., appear more than m times). If violationCount == 0, the window is valid.
     * 4. For each valid window, we record the number of distinct flavors (map size).
     * 5. Return the maximum distinct count found, or -1 if no valid window exists.</p>
     *
     * @param flavors array of flavor values for each dish (1-indexed values)
     * @param k       the exact number of dishes in the segment (window size)
     * @param m       the maximum allowed occurrences of any single flavor in the segment
     * @return the maximum number of distinct flavors in a valid window, or -1 if none
     *
     * @implNote Time complexity: O(n) — each element is added and removed from the
     *           window at most once (amortized), and we iterate over at most 2*n elements.
     *           Space complexity: O(k) — the HashMap stores at most k distinct flavors
     *           at any point in time.
     */
    public int maxDistinctFlavors(int[] flavors, int k, int m) {
        int n = flavors.length;

        // Edge case: if k > n, it's impossible to have a valid segment
        // (since a circular array of n elements can only provide n unique positions)
        // Actually k <= n is guaranteed by constraints, but let's be safe.
        if (k > n) {
            return -1;
        }

        // -----------------------------------------------------------------------
        // Step 1: Set up the sliding window data structures.
        // -----------------------------------------------------------------------

        // freqMap: maps each flavor in the current window to its frequency count.
        // This lets us quickly check how many times each flavor appears.
        Map<Integer, Integer> freqMap = new HashMap<>();

        // violationCount: tracks how many distinct flavors currently exceed the
        // allowed maximum m. A window is valid only when violationCount == 0.
        int violationCount = 0;

        // maxDistinct: the best (maximum) number of distinct flavors seen in any
        // valid window so far. Start at -1 to indicate "no valid window found yet".
        int maxDistinct = -1;

        // -----------------------------------------------------------------------
        // Step 2: Initialize the first window (indices 0 to k-1).
        // We build the initial window before entering the main sliding loop.
        // -----------------------------------------------------------------------

        for (int i = 0; i < k; i++) {
            // Get the actual flavor at position i (using modulo for circular access,
            // though for the first window i < k <= n so modulo isn't strictly needed here).
            int flavor = flavors[i % n];

            // Add this flavor to the frequency map.
            int newCount = freqMap.merge(flavor, 1, Integer::sum);

            // Check if adding this flavor caused a violation:
            // - If newCount == m + 1, this flavor just crossed the threshold from
            //   "allowed" (count == m) to "violating" (count == m+1).
            if (newCount == m + 1) {
                violationCount++;
            }
        }

        // Check if the initial window (starting at index 0) is valid.
        if (violationCount == 0) {
            // The window is valid; record the number of distinct flavors.
            maxDistinct = freqMap.size();
        }

        // -----------------------------------------------------------------------
        // Step 3: Slide the window across all possible starting positions.
        //
        // We consider starting positions 1 through n-1 (total n windows).
        // For a circular array of size n, starting positions 0..n-1 cover all
        // possible circular windows of length k.
        //
        // When the window starts at position `start`, it covers indices:
        //   start, start+1, ..., start+k-1  (all taken mod n for circular access)
        //
        // We slide by:
        //   - Removing the element that just left the window (index: start-1, i.e., start-1 mod n)
        //   - Adding the element that just entered the window (index: start+k-1 mod n)
        // -----------------------------------------------------------------------

        for (int start = 1; start < n; start++) {
            // -------------------------------------------------------------------
            // Step 3a: Remove the element that is leaving the window.
            // The element leaving is at index (start - 1) in the original array.
            // -------------------------------------------------------------------
            int removedFlavor = flavors[(start - 1) % n];

            // Get the current count of the removed flavor before decrementing.
            int oldCount = freqMap.get(removedFlavor);

            // If the old count was exactly m+1, removing one occurrence brings it
            // back to m (no longer violating), so decrement violationCount.
            if (oldCount == m + 1) {
                violationCount--;
            }

            // Decrement the count of the removed flavor.
            if (oldCount == 1) {
                // If count drops to 0, remove the flavor from the map entirely
                // so that freqMap.size() accurately reflects distinct flavors.
                freqMap.remove(removedFlavor);
            } else {
                freqMap.put(removedFlavor, oldCount - 1);
            }

            // -------------------------------------------------------------------
            // Step 3b: Add the element that is entering the window.
            // The new element entering is at index (start + k - 1) mod n.
            // -------------------------------------------------------------------
            int addedFlavor = flavors[(start + k - 1) % n];

            // Increment the count of the added flavor.
            int newCount = freqMap.merge(addedFlavor, 1, Integer::sum);

            // If the new count just crossed the threshold (became m+1),
            // this flavor is now violating the constraint.
            if (newCount == m + 1) {
                violationCount++;
            }

            // -------------------------------------------------------------------
            // Step 3c: Check if the current window is valid and update the answer.
            // -------------------------------------------------------------------
            if (violationCount == 0) {
                // No flavor in this window exceeds m occurrences — window is valid.
                // The number of distinct flavors is the size of the frequency map.
                int distinctCount = freqMap.size();
                if (distinctCount > maxDistinct) {
                    maxDistinct = distinctCount;
                }
            }
        }

        // -----------------------------------------------------------------------
        // Step 4: Return the result.
        // If maxDistinct is still -1, no valid window was found.
        // -----------------------------------------------------------------------
        return maxDistinct;
    }

    /**
     * Main method to demonstrate the solution with sample inputs from the problem.
     *
     * <p>Traces through each example to verify correctness before presenting results.</p>
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1:
        // flavors = [1, 2, 1, 3, 2, 4, 1], k = 4, m = 1
        //
        // Let's trace all 7 circular windows of size 4 (n=7):
        //
        // Window starting at 0: [1, 2, 1, 3]
        //   Frequencies: {1:2, 2:1, 3:1} → flavor 1 appears 2 times > m=1 → INVALID
        //
        // Window starting at 1: [2, 1, 3, 2]
        //   Frequencies: {2:2, 1:1, 3:1} → flavor 2 appears 2 times > m=1 → INVALID
        //
        // Window starting at 2: [1, 3, 2, 4]
        //   Frequencies: {1:1, 3:1, 2:1, 4:1} → all counts = 1 ≤ m=1 → VALID, distinct=4
        //
        // Window starting at 3: [3, 2, 4, 1]
        //   Frequencies: {3:1, 2:1, 4:1, 1:1} → all counts = 1 ≤ m=1 → VALID, distinct=4
        //
        // Window starting at 4: [2, 4, 1, 1]  (indices 4,5,6,0 mod 7)
        //   flavors[4]=2, flavors[5]=4, flavors[6]=1, flavors[0]=1
        //   Frequencies: {2:1, 4:1, 1:2} → flavor 1 appears 2 times > m=1 → INVALID
        //
        // Window starting at 5: [4, 1, 1, 2]  (indices 5,6,0,1 mod 7)
        //   flavors[5]=4, flavors[6]=1, flavors[0]=1, flavors[1]=2
        //   Frequencies: {4:1, 1:2, 2:1} → flavor 1 appears 2 times > m=1 → INVALID
        //
        // Window starting at 6: [1, 1, 2, 1]  (indices 6,0,1,2 mod 7)
        //   flavors[6]=1, flavors[0]=1, flavors[1]=2, flavors[2]=1
        //   Frequencies: {1:3, 2:1} → flavor 1 appears 3 times > m=1 → INVALID
        //
        // Maximum valid distinct count = 4 ✓
        // -----------------------------------------------------------------------
        int[] flavors1 = {1, 2, 1, 3, 2, 4, 1};
        int k1 = 4, m1 = 1;
        int result1 = solution.maxDistinctFlavors(flavors1, k1, m1);
        System.out.println("Example 1:");
        System.out.println("  flavors = [1, 2, 1, 3, 2, 4, 1], k = 4, m = 1");
        System.out.println("  Expected Output: 4");
        System.out.println("  Actual Output:   " + result1);
        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2:
        // flavors = [5, 5, 5, 5], k = 3, m = 1
        //
        // All windows of size 3 (n=4):
        //
        // Window starting at 0: [5, 5, 5]
        //   Frequencies: {5:3} → 3 > m=1 → INVALID
        //
        // Window starting at 1: [5, 5, 5]
        //   Frequencies: {5:3} → 3 > m=1 → INVALID
        //
        // Window starting at 2: [5, 5, 5]
        //   Frequencies: {5:3} → 3 > m=1 → INVALID
        //
        // Window starting at 3: [5, 5, 5]  (indices 3,0,1 mod 4)
        //   Frequencies: {5:3} → 3 > m=1 → INVALID
        //
        // No valid window found → return -1 ✓
        // -----------------------------------------------------------------------
        int[] flavors2 = {5, 5, 5, 5};
        int k2 = 3, m2 = 1;
        int result2 = solution.maxDistinctFlavors(flavors2, k2, m2);
        System.out.println("Example 2:");
        System.out.println("  flavors = [5, 5, 5, 5], k = 3, m = 1");
        System.out.println("  Expected Output: -1");
        System.out.println("  Actual Output:   " + result2);
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Test Case 3:
        // flavors = [1, 2, 3, 4, 5], k = 3, m = 1
        //
        // All elements are distinct, so every window of size 3 is valid with m=1.
        // Expected: 3 distinct flavors in any window.
        // -----------------------------------------------------------------------
        int[] flavors3 = {1, 2, 3, 4, 5};
        int k3 = 3, m3 = 1;
        int result3 = solution.maxDistinctFlavors(flavors3, k3, m3);
        System.out.println("Additional Test Case 3:");
        System.out.println("  flavors = [1, 2, 3, 4, 5], k = 3, m = 1");
        System.out.println("  Expected Output: 3");
        System.out.println("  Actual Output:   " + result3);
        System.out.println();

        // -----------------------------------------------------------------------
        // Additional Test Case 4:
        // flavors = [1, 1, 2, 2, 3, 3],