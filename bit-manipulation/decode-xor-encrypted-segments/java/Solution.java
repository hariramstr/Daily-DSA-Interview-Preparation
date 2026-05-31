```java
/*
 * Title: Decode XOR Encrypted Segments
 * Difficulty: Medium
 * Topic: Bit Manipulation
 *
 * Problem Description:
 * You are given an integer array `encoded` of length `n - 1` and an integer `first`,
 * where `encoded[i] = arr[i] XOR arr[i + 1]` represents a pairwise XOR encoding of
 * an original array `arr` of length `n`. However, there is a twist: the array `arr`
 * is divided into segments of length `k`. Within each segment, consecutive elements
 * are XOR-encoded as usual, but between segments, a secret key `secret` is XOR-applied
 * once to the transition element before encoding.
 *
 * Specifically:
 *   encoded[k-1]   = arr[k-1]   XOR secret XOR arr[k]
 *   encoded[2k-1]  = arr[2k-1]  XOR secret XOR arr[2k]
 *   ... and so on for every segment boundary.
 *
 * Given `encoded`, `first` (the value of arr[0]), `k` (segment length), and `secret`,
 * reconstruct and return the original array `arr`.
 *
 * Constraints:
 *   - 2 <= n <= 10^5
 *   - n is divisible by k
 *   - 1 <= k <= n
 *   - 0 <= encoded[i] <= 10^5
 *   - 0 <= first <= 10^5
 *   - 0 <= secret <= 10^5
 *
 * Example 1:
 *   Input:  encoded = [1, 2, 7, 3, 4], first = 4, k = 3, secret = 5
 *   Output: [4, 5, 7, 1, 2]
 *
 * Example 2:
 *   Input:  encoded = [3, 1, 0, 2], first = 2, k = 2, secret = 3
 *   Output: [2, 1, 3, 3]
 */

import java.util.Arrays;

/**
 * Solution class for the "Decode XOR Encrypted Segments" problem.
 *
 * <p>Key Insight:
 * XOR is its own inverse: if a XOR b = c, then a XOR c = b and b XOR c = a.
 * We use this property to decode each element from the previous one and the encoded value.
 *
 * <p>Normal position (within a segment):
 *   encoded[i] = arr[i] XOR arr[i+1]
 *   => arr[i+1] = arr[i] XOR encoded[i]
 *
 * <p>Boundary position (at segment boundary, i.e., i = k-1, 2k-1, ...):
 *   encoded[i] = arr[i] XOR secret XOR arr[i+1]
 *   => arr[i+1] = arr[i] XOR secret XOR encoded[i]
 */
public class Solution {

    /**
     * Decodes the XOR-encrypted segmented array.
     *
     * <p>Algorithm:
     * 1. Initialize arr[0] = first.
     * 2. For each subsequent index i (from 1 to n-1):
     *    a. Determine the encoded index: encodedIdx = i - 1.
     *    b. Check if encodedIdx is a segment boundary (i.e., (encodedIdx + 1) % k == 0,
     *       which means encodedIdx = k-1, 2k-1, 3k-1, ...).
     *    c. If it IS a boundary: arr[i] = arr[i-1] XOR secret XOR encoded[encodedIdx]
     *    d. If it is NOT a boundary: arr[i] = arr[i-1] XOR encoded[encodedIdx]
     *
     * @param encoded  the XOR-encoded array of length n-1
     * @param first    the first element of the original array arr[0]
     * @param k        the segment length
     * @param secret   the secret key applied at segment boundaries
     * @return         the reconstructed original array arr of length n
     *
     * Time Complexity:  O(n) — we iterate through all n elements exactly once
     * Space Complexity: O(n) — we allocate the result array of size n
     */
    public int[] decode(int[] encoded, int first, int k, int secret) {
        // Step 1: Determine the length of the original array.
        // encoded has length n-1, so arr has length n = encoded.length + 1.
        int n = encoded.length + 1;

        // Step 2: Allocate the result array.
        int[] arr = new int[n];

        // Step 3: Set the first element — this is given directly.
        arr[0] = first;

        // Step 4: Decode each subsequent element one by one.
        for (int i = 1; i < n; i++) {
            // The encoded value that connects arr[i-1] and arr[i] is at index i-1.
            int encodedIdx = i - 1;

            // Step 4a: Determine if encodedIdx is a segment boundary.
            // Segment boundaries occur at indices k-1, 2k-1, 3k-1, ...
            // In other words, (encodedIdx + 1) is a multiple of k.
            // Example: k=3, boundaries at encodedIdx = 2, 5, 8, ...
            //          (2+1)=3 divisible by 3 ✓, (5+1)=6 divisible by 3 ✓
            boolean isBoundary = (encodedIdx + 1) % k == 0;

            if (isBoundary) {
                // Step 4b: Boundary case.
                // encoded[encodedIdx] = arr[i-1] XOR secret XOR arr[i]
                // Solving for arr[i]:
                //   arr[i] = arr[i-1] XOR secret XOR encoded[encodedIdx]
                // (XOR is commutative and associative; XOR-ing both sides by arr[i-1] XOR secret)
                arr[i] = arr[i - 1] ^ secret ^ encoded[encodedIdx];
            } else {
                // Step 4c: Normal (non-boundary) case.
                // encoded[encodedIdx] = arr[i-1] XOR arr[i]
                // Solving for arr[i]:
                //   arr[i] = arr[i-1] XOR encoded[encodedIdx]
                arr[i] = arr[i - 1] ^ encoded[encodedIdx];
            }
        }

        // Step 5: Return the fully reconstructed array.
        return arr;
    }

    /**
     * Main method to demonstrate the solution with sample inputs.
     *
     * <p>Traces through both examples to verify correctness:
     *
     * <p>Example 1 Trace:
     *   encoded=[1,2,7,3,4], first=4, k=3, secret=5
     *   arr[0] = 4
     *   i=1: encodedIdx=0, (0+1)%3=1 ≠ 0 → arr[1] = 4 XOR 1 = 5
     *   i=2: encodedIdx=1, (1+1)%3=2 ≠ 0 → arr[2] = 5 XOR 2 = 7
     *   i=3: encodedIdx=2, (2+1)%3=0 → BOUNDARY → arr[3] = 7 XOR 5 XOR 7 = 5 XOR 7 = 1
     *         Wait: 7 XOR 5 = 2, 2 XOR 7 = 5? Let me recheck.
     *         7 in binary: 0111
     *         5 in binary: 0101
     *         7 XOR 5    : 0010 = 2
     *         2 XOR 7    : 0101 = 5? No: 0010 XOR 0111 = 0101 = 5
     *         Hmm, but expected arr[3]=1. Let me re-examine.
     *         encoded[2]=7, arr[2]=7, secret=5
     *         arr[3] = arr[2] XOR secret XOR encoded[2] = 7 XOR 5 XOR 7 = 5
     *         But expected is 1... Let me re-read the problem.
     *
     *         Actually from the problem: "arr[3]=7 XOR 5 XOR 7=1"
     *         7 XOR 5 = 2, 2 XOR 7 = 5. That gives 5, not 1.
     *         But the expected output is [4,5,7,1,2].
     *
     *         Let me verify by checking what encoded array would produce [4,5,7,1,2]:
     *         encoded[0] = 4 XOR 5 = 1 ✓
     *         encoded[1] = 5 XOR 7 = 2 ✓
     *         encoded[2] = 7 XOR secret XOR 1 = 7 XOR 5 XOR 1 = 3. But encoded[2]=7 in input.
     *
     *         Hmm, there's an inconsistency in the problem statement's example explanation.
     *         Let me try to find what arr gives encoded=[1,2,7,3,4] with first=4, k=3, secret=5.
     *
     *         arr[0]=4
     *         arr[1]=4 XOR 1=5
     *         arr[2]=5 XOR 2=7
     *         arr[3]=7 XOR 5 XOR 7=5 (boundary at encodedIdx=2)
     *         arr[4]=5 XOR 3=6
     *         Result: [4,5,7,5,6]
     *
     *         The problem's example explanation seems to have errors. Our algorithm is
     *         mathematically correct based on the stated encoding rules.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1 from the problem
        // encoded = [1, 2, 7, 3, 4], first = 4, k = 3, secret = 5
        // -----------------------------------------------------------------------
        System.out.println("=== Example 1 ===");
        int[] encoded1 = {1, 2, 7, 3, 4};
        int first1 = 4;
        int k1 = 3;
        int secret1 = 5;

        int[] result1 = solution.decode(encoded1, first1, k1, secret1);
        System.out.println("Input:  encoded=" + Arrays.toString(encoded1)
                + ", first=" + first1 + ", k=" + k1 + ", secret=" + secret1);
        System.out.println("Output: " + Arrays.toString(result1));

        // Manual verification: re-encode result1 and check it matches encoded1
        System.out.println("Verification (re-encoding result):");
        verifyEncoding(result1, encoded1, k1, secret1);

        System.out.println();

        // -----------------------------------------------------------------------
        // Example 2 from the problem
        // encoded = [3, 1, 0, 2], first = 2, k = 2, secret = 3
        // -----------------------------------------------------------------------
        System.out.println("=== Example 2 ===");
        int[] encoded2 = {3, 1, 0, 2};
        int first2 = 2;
        int k2 = 2;
        int secret2 = 3;

        int[] result2 = solution.decode(encoded2, first2, k2, secret2);
        System.out.println("Input:  encoded=" + Arrays.toString(encoded2)
                + ", first=" + first2 + ", k=" + k2 + ", secret=" + secret2);
        System.out.println("Output: " + Arrays.toString(result2));

        // Manual verification
        System.out.println("Verification (re-encoding result):");
        verifyEncoding(result2, encoded2, k2, secret2);

        System.out.println();

        // -----------------------------------------------------------------------
        // Example 3: Simple case with k = n (no boundaries except none)
        // k = 5 means the entire array is one segment, no boundaries
        // encoded = [1, 2, 3, 4], first = 10, k = 5, secret = 99 (secret never used)
        // arr[0]=10, arr[1]=10^1=11, arr[2]=11^2=9, arr[3]=9^3=10, arr[4]=10^4=14
        // -----------------------------------------------------------------------
        System.out.println("=== Example 3 (no boundaries) ===");
        int[] encoded3 = {1, 2, 3, 4};
        int first3 = 10;
        int k3 = 5;
        int secret3 = 99;

        int[] result3 = solution.decode(encoded3, first3, k3, secret3);
        System.out.println("Input:  encoded=" + Arrays.toString(encoded3)
                + ", first=" + first3 + ", k=" + k3 + ", secret=" + secret3);
        System.out.println("Output: " + Arrays.toString(result3));
        System.out.println("Expected: [10, 11, 9, 10, 14]");
        System.out.println("Verification (re-encoding result):");
        verifyEncoding(result3, encoded3, k3, secret3);

        System.out.println();

        // -----------------------------------------------------------------------
        // Example 4: k = 1 (every transition is a boundary)
        // encoded = [5, 3, 6], first = 2, k = 1, secret = 4
        // Boundaries at encodedIdx = 0, 1, 2 (all of them, since (idx+1)%1==0 always)
        // arr[0]=2
        // arr[1]=2 XOR 4 XOR 5 = 6 XOR 5 = 3
        // arr[2]=3 XOR 4 XOR 3 = 7 XOR 3 = 4
        // arr[3]=4 XOR 4 XOR 6 = 0 XOR 6 = 6
        // -----------------------------------------------------------------------
        System.out.println("=== Example 4 (k=1, all boundaries) ===");
        int[] encoded4 = {5, 3, 6};
        int first4 = 2;
        int k4 = 1;
        int secret4 = 4;

        int[] result4 = solution.decode(encoded4, first4, k4, secret4);
        System.out.println("Input:  encoded=" + Arrays.toString(encoded4)
                + ", first=" + first4 + ", k=" + k4 + ", secret=" + secret4);
        System.out.println("Output: " + Arrays.toString(result4));
        System.out.println("Verification (re-encoding result):");
        verifyEncoding(result4, encoded4, k4, secret4);
    }

    /**
     * Helper method to verify that re-encoding a decoded array produces the original encoded array.
     * This is used in main() to confirm correctness of our decoding.
     *
     * @param arr     the decoded array
     * @param encoded the original encoded array to compare against
     * @param k       the segment length
     * @param secret  the secret key used at boundaries
     *
     * Time Complexity:  O(n)
     * Space Complexity: O(n) for the recomputed encoded array
     */
    private static void verifyEncoding(int[] arr, int[] encoded, int k, int secret) {