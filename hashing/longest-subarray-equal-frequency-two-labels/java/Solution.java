/*
 * Title: Find Longest Subarray with Equal Frequency of Two Labels
 *
 * Problem Description:
 * You are given an array of strings `labels` where each element is either 'A' or 'B'.
 * Your task is to find the length of the longest contiguous subarray in which the
 * number of 'A's equals the number of 'B's.
 *
 * This is a classic balance-tracking problem: assign +1 for each 'A' and -1 for each 'B',
 * then find the longest subarray with a prefix sum of 0. Use a hash map to record the
 * first occurrence of each prefix sum value.
 *
 * Constraints:
 * - 1 <= labels.length <= 10^5
 * - labels[i] is either 'A' or 'B'
 *
 * Example 1:
 * Input: labels = ["A", "B", "B", "A", "A", "B", "A"]
 * Output: 6
 * Explanation: labels[0..5] = ["A","B","B","A","A","B"] has 3 A's and 3 B's.
 *
 * Example 2:
 * Input: labels = ["A", "A", "A", "B"]
 * Output: 2
 * Explanation: labels[2..3] = ["A","B"] has 1 A and 1 B.
 *
 * Follow-up: Can you generalize your solution to handle k distinct labels and find
 * the longest subarray where all labels appear with equal frequency?
 */

import java.util.*;

/**
 * Solution class for finding the longest subarray with equal frequency of two labels.
 *
 * <p>Core Insight:
 * By mapping 'A' -> +1 and 'B' -> -1, we transform the problem into finding the
 * longest subarray whose sum equals 0. This is equivalent to finding two indices
 * i < j where prefixSum[i] == prefixSum[j], and the subarray length is j - i.
 *
 * <p>We use a HashMap to store the FIRST time we see each prefix sum value.
 * Whenever we see the same prefix sum again at index j, the subarray from
 * (firstOccurrence + 1) to j has a sum of 0, meaning equal A's and B's.
 */
public class Solution {

    /**
     * Finds the length of the longest contiguous subarray with equal number of 'A's and 'B's.
     *
     * <p>Algorithm Overview:
     * 1. Treat 'A' as +1 and 'B' as -1.
     * 2. Compute a running prefix sum as we scan the array.
     * 3. Use a HashMap to record the first index where each prefix sum value was seen.
     * 4. If we see the same prefix sum at two different indices, the subarray between
     *    them has sum 0 (equal A's and B's).
     * 5. Track the maximum such subarray length.
     *
     * @param labels an array of strings, each either "A" or "B"
     * @return the length of the longest subarray with equal count of 'A' and 'B'
     *
     * Time Complexity:  O(n) — single pass through the array, O(1) HashMap operations
     * Space Complexity: O(n) — HashMap can store at most n+1 distinct prefix sums
     */
    public int findLongestSubarray(String[] labels) {

        // -----------------------------------------------------------------------
        // Step 1: Initialize the HashMap with a sentinel entry.
        //
        // We map "prefix sum value" -> "first index where this sum was seen".
        //
        // IMPORTANT: We seed the map with (sum=0, index=-1).
        // Why index -1?
        //   - Before we process any element, the prefix sum is 0.
        //   - If at index j the prefix sum is again 0, the entire subarray [0..j]
        //     has equal A's and B's. Its length = j - (-1) = j + 1. ✓
        // -----------------------------------------------------------------------
        Map<Integer, Integer> firstOccurrence = new HashMap<>();
        firstOccurrence.put(0, -1); // sentinel: prefix sum 0 seen "before" index 0

        int prefixSum = 0;   // running balance: +1 per 'A', -1 per 'B'
        int maxLength = 0;   // best answer found so far

        // -----------------------------------------------------------------------
        // Step 2: Iterate through each label in the array.
        // -----------------------------------------------------------------------
        for (int i = 0; i < labels.length; i++) {

            // Step 2a: Update the prefix sum based on the current label.
            //   'A' contributes +1 (more A's pushes sum up)
            //   'B' contributes -1 (more B's pushes sum down)
            if (labels[i].equals("A")) {
                prefixSum += 1;
            } else {
                // labels[i].equals("B")
                prefixSum -= 1;
            }

            // Step 2b: Check if this prefix sum has been seen before.
            //
            // If firstOccurrence contains prefixSum, it means:
            //   - At some earlier index `prev`, the prefix sum was the same value.
            //   - The subarray from (prev+1) to i has a net sum of 0.
            //   - Net sum = 0 means: (number of A's) - (number of B's) = 0
            //                    => number of A's == number of B's  ✓
            //
            // The length of this subarray is: i - prev
            if (firstOccurrence.containsKey(prefixSum)) {
                int prev = firstOccurrence.get(prefixSum);
                int currentLength = i - prev; // subarray from index (prev+1) to i

                // Step 2c: Update maxLength if this subarray is longer.
                maxLength = Math.max(maxLength, currentLength);

                // NOTE: We do NOT update the map here!
                // We always want the FIRST (earliest) occurrence of each prefix sum,
                // because that gives us the longest possible subarray ending at i.
            } else {
                // Step 2d: First time seeing this prefix sum — record it.
                firstOccurrence.put(prefixSum, i);
            }
        }

        // Step 3: Return the maximum length found.
        return maxLength;
    }

    // ==========================================================================
    // FOLLOW-UP: Generalized solution for k distinct labels
    // ==========================================================================

    /**
     * Generalized version: finds the longest subarray where ALL distinct labels
     * appear with equal frequency.
     *
     * <p>Key Insight for Generalization:
     * With k labels, "equal frequency" means every label appears the same number of times.
     * We can reduce this to a 2-label problem by comparing each label's count to
     * the count of a chosen "reference" label (e.g., the first label seen).
     *
     * <p>Specifically, for each label L (other than the reference R), we track:
     *   diff(L) = count(L) - count(R)
     *
     * Equal frequency for all labels means all these differences are 0.
     * We encode the entire difference vector as a string key for the HashMap.
     *
     * <p>Example with 3 labels {A, B, C}:
     *   Reference = A. Track (count(B)-count(A), count(C)-count(A)).
     *   Equal frequency ⟺ both differences = 0 ⟺ key = "0,0".
     *
     * @param labels an array of strings with arbitrary label values
     * @return the length of the longest subarray where all labels have equal frequency
     *
     * Time Complexity:  O(n * k) — n elements, each producing a key of length O(k)
     * Space Complexity: O(n * k) — HashMap stores keys of length O(k)
     */
    public int findLongestSubarrayGeneralized(String[] labels) {

        // Step 1: Discover all distinct labels and assign them indices.
        //         We also pick labels[0] as the "reference" label (index 0).
        List<String> labelList = new ArrayList<>();
        Map<String, Integer> labelIndex = new HashMap<>();

        for (String label : labels) {
            if (!labelIndex.containsKey(label)) {
                labelIndex.put(label, labelList.size());
                labelList.add(label);
            }
        }

        int k = labelList.size(); // number of distinct labels

        // Edge case: only one distinct label — the whole array is trivially balanced.
        if (k == 1) {
            return labels.length;
        }

        // Step 2: counts[i] = frequency of labelList.get(i) so far.
        int[] counts = new int[k];

        // Step 3: HashMap from "difference vector string" -> first index seen.
        //         The difference vector: for each label i (i > 0), store counts[i] - counts[0].
        //         When this vector is all zeros, all labels have equal frequency.
        Map<String, Integer> firstOccurrence = new HashMap<>();

        // Seed with the zero-difference vector at index -1 (before any element).
        String zeroKey = buildDiffKey(counts, k);
        firstOccurrence.put(zeroKey, -1);

        int maxLength = 0;

        // Step 4: Scan through the array.
        for (int i = 0; i < labels.length; i++) {

            // Update the count for the current label.
            int idx = labelIndex.get(labels[i]);
            counts[idx]++;

            // Build the difference key: diff[j] = counts[j] - counts[0] for j in [1, k-1].
            String key = buildDiffKey(counts, k);

            if (firstOccurrence.containsKey(key)) {
                int prev = firstOccurrence.get(key);
                maxLength = Math.max(maxLength, i - prev);
            } else {
                firstOccurrence.put(key, i);
            }
        }

        return maxLength;
    }

    /**
     * Helper: builds a string key representing the difference vector.
     * diff[j] = counts[j] - counts[0] for j in [1, k-1].
     * When all diffs are 0, all labels have equal frequency.
     *
     * @param counts array of label frequencies
     * @param k      number of distinct labels
     * @return a comma-separated string of differences
     *
     * Time Complexity:  O(k)
     * Space Complexity: O(k)
     */
    private String buildDiffKey(int[] counts, int k) {
        StringBuilder sb = new StringBuilder();
        for (int j = 1; j < k; j++) {
            if (j > 1) sb.append(',');
            sb.append(counts[j] - counts[0]);
        }
        return sb.toString();
    }

    // ==========================================================================
    // MAIN: Demonstration and verification
    // ==========================================================================

    /**
     * Main method demonstrating the solution with provided examples and additional tests.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution sol = new Solution();

        System.out.println("=== Find Longest Subarray with Equal Frequency of Two Labels ===");
        System.out.println();

        // ------------------------------------------------------------------
        // Example 1 (from problem):
        // labels = ["A", "B", "B", "A", "A", "B", "A"]
        // Expected output: 6
        //
        // Let's trace through manually:
        // Index:      0    1    2    3    4    5    6
        // Label:      A    B    B    A    A    B    A
        // Delta:     +1   -1   -1   +1   +1   -1   +1
        // PrefixSum:  1    0   -1    0    1    0    1
        //
        // Map starts: {0: -1}
        //
        // i=0: sum=1, not in map → map={0:-1, 1:0}
        // i=1: sum=0, in map at -1 → length = 1-(-1) = 2, maxLen=2; map unchanged
        // i=2: sum=-1, not in map → map={0:-1, 1:0, -1:2}
        // i=3: sum=0, in map at -1 → length = 3-(-1) = 4, maxLen=4; map unchanged
        // i=4: sum=1, in map at 0 → length = 4-0 = 4, maxLen=4; map unchanged
        // i=5: sum=0, in map at -1 → length = 5-(-1) = 6, maxLen=6; map unchanged ✓
        // i=6: sum=1, in map at 0 → length = 6-0 = 6, maxLen=6; map unchanged
        //
        // Final answer: 6 ✓
        // ------------------------------------------------------------------
        String[] labels1 = {"A", "B", "B", "A", "A", "B", "A"};
        int result1 = sol.findLongestSubarray(labels1);
        System.out.println("Example 1:");
        System.out.println("  Input:    [\"A\", \"B\", \"B\", \"A\", \"A\", \"B\", \"A\"]");
        System.out.println("  Expected: 6");
        System.out.println("  Got:      " + result1);
        System.out.println("  PASS: " + (result1 == 6));
        System.out.println();

        // ------------------------------------------------------------------
        // Example 2 (from problem):
        // labels = ["A", "A", "A", "B"]
        // Expected output: 2
        //
        // Trace:
        // Index:      0    1    2    3
        // Label:      A    A    A    B
        // Delta:     +1   +1   +1   -1
        // PrefixSum:  1    2    3    2
        //
        // Map starts: {0: -1}
        //
        // i=0: sum=1, not in map → map={0:-1, 1:0}
        // i=1: sum=2, not in map → map={0:-1, 1:0, 2:1}
        // i=2: sum=3, not in map → map={0:-1, 1:0, 2:1, 3:2}
        // i=3: sum=2, in map at 1 → length = 3-1 = 2, maxLen=2 ✓
        //
        // Final answer: 2 ✓
        // ------------------------------------------------------------------
        String[] labels2 = {"A", "A", "A", "B"};
        int result2 = sol.findLongestSubarray(labels2);
        System.out.println("Example 2:");
        System.out.println("  Input:    [\"A\", \"A\", \"A\", \"B\"]");
        System.out.println("  Expected: 2");
        System.out.println("  Got:      " + result2);
        System.out.println("  PASS: " + (result2 == 2));
        System.out.println();

        // ------------------------------------------------------------------
        // Additional Test 3: All A's — no balanced subarray
        // labels = ["A", "A", "A"]
        // Expected: 0
        // ------------------------------------------------------------------
        String[] labels3 = {"A", "A", "A"};
        int result3 = sol.findLongestSubarray(labels3);
        System.out.println("Additional Test 3 (all A's):");
        System.out.println("  Input:    [\"A\", \"A\", \"A\"]");
        System.out.println("  Expected: 0");
        System.out.println("  Got:      " + result3);
        System.out.println("  PASS: " + (result3 == 0));