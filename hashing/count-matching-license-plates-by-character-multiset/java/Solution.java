import java.util.*;

/*
 * Title: Count Matching License Plates by Character Multiset
 * Difficulty: Medium
 * Topic: Hashing
 *
 * Problem Description:
 * A parking analytics system stores vehicle license plates as uppercase alphanumeric strings.
 * Two plates are considered matching if they contain exactly the same characters with the same
 * frequencies, regardless of order. For example, "A1B1" and "1AB1" match, while "AB12" and
 * "AB21" also match. However, "AAB1" and "AB11" do not match because their character counts differ.
 *
 * Given an array plates, return the number of unordered pairs of indices (i, j) such that
 * i < j and plates[i] matches plates[j] by character multiset.
 *
 * You should design an efficient solution using hashing. A common approach is to convert each
 * plate into a canonical signature based on the frequency of all possible characters, then count
 * how many previous plates have the same signature.
 *
 * Constraints:
 * - 1 <= plates.length <= 100000
 * - 1 <= plates[i].length <= 20
 * - plates[i] consists only of characters 'A' to 'Z' and digits '0' to '9'
 * - The answer may be large, so use a 64-bit integer type where needed
 *
 * Example 1:
 * Input: plates = ["A1B1", "1AB1", "AB12", "B2A1", "XYZ", "ZYX"]
 * Output: 3
 * Explanation:
 * Matching pairs are:
 * - indices (0, 1): "A1B1" and "1AB1"
 * - indices (2, 3): "AB12" and "B2A1"
 * - indices (4, 5): "XYZ" and "ZYX"
 * Total pairs = 3
 *
 * Example 2:
 * Input: plates = ["AA11", "1A1A", "A11A", "BB2", "2BB", "B2B", "C3"]
 * Output: 6
 * Explanation:
 * The first three plates all have the same character multiset, contributing 3 choose 2 = 3 pairs.
 * The next three plates also share the same multiset, contributing another 3 choose 2 = 3 pairs.
 * "C3" matches none of the others.
 * Total pairs = 6.
 *
 * Your task is to return only the number of matching pairs.
 */
public class Solution {

    /**
     * Counts how many unordered pairs of license plates match by character multiset.
     *
     * The key idea is:
     * 1. Convert each plate into a canonical signature that uniquely represents
     *    the frequency of all 36 possible characters:
     *    - digits '0' to '9'
     *    - letters 'A' to 'Z'
     * 2. Use a hash map to count how many times each signature has already appeared.
     * 3. When we see the same signature again, every previous occurrence forms one new pair
     *    with the current plate.
     *
     * Example:
     * If a signature has already appeared 4 times, then the 5th plate with that signature
     * creates 4 new pairs.
     *
     * @param plates the array of uppercase alphanumeric license plates
     * @return the number of matching unordered pairs as a long
     * Time complexity: O(n * L), where n is the number of plates and L is the maximum plate length
     *                  (plus a constant-size 36-character signature construction per plate)
     * Space complexity: O(n) in the worst case for storing unique signatures in the hash map
     */
    public long countMatchingPairs(String[] plates) {
        // This map stores:
        // key   -> canonical signature of a plate
        // value -> how many previous plates had exactly this signature
        Map<String, Integer> signatureCount = new HashMap<>();

        // We use long because the number of pairs can be large.
        // For example, if all 100000 plates match, the number of pairs is:
        // 100000 * 99999 / 2 = 4,999,950,000, which does not fit in int.
        long pairs = 0L;

        // Process each plate one by one.
        for (String plate : plates) {
            // Build a canonical representation of the current plate.
            // Any two plates with the same character multiset will produce the same signature.
            String signature = buildSignature(plate);

            // Find how many times we have already seen this exact signature.
            // If seen k times before, then the current plate forms exactly k new pairs.
            int seenBefore = signatureCount.getOrDefault(signature, 0);

            // Add those newly formed pairs to the answer.
            pairs += seenBefore;

            // Record that we have now seen one more plate with this signature.
            signatureCount.put(signature, seenBefore + 1);
        }

        return pairs;
    }

    /**
     * Builds a canonical signature for a license plate based on character frequencies.
     *
     * We have exactly 36 possible characters:
     * - 10 digits: '0' to '9'
     * - 26 letters: 'A' to 'Z'
     *
     * We count how many times each character appears, then serialize those counts into a string.
     * This string acts as a unique fingerprint for the multiset of characters.
     *
     * Why this works:
     * - If two plates contain the same characters with the same frequencies, their count arrays
     *   will be identical, so their signatures will be identical.
     * - If they differ in any character count, their signatures will differ.
     *
     * @param plate the input license plate
     * @return a canonical signature string representing the plate's character multiset
     * Time complexity: O(L), where L is the length of the plate, plus O(36) to serialize counts
     * Space complexity: O(36) auxiliary space for the frequency array, which is O(1)
     */
    public String buildSignature(String plate) {
        // Frequency array of size 36:
        // indices 0..9   represent digits '0'..'9'
        // indices 10..35 represent letters 'A'..'Z'
        int[] freq = new int[36];

        // Count each character in the plate.
        for (int i = 0; i < plate.length(); i++) {
            char ch = plate.charAt(i);
            int index = charToIndex(ch);
            freq[index]++;
        }

        // Convert the frequency array into a string.
        // We include separators to avoid ambiguity.
        //
        // Example format:
        // #0#1#0#0#...#2#...
        //
        // Since the alphabet size is fixed (36), this signature is compact enough
        // and guarantees correctness.
        StringBuilder signature = new StringBuilder();

        for (int count : freq) {
            signature.append('#').append(count);
        }

        return signature.toString();
    }

    /**
     * Maps a valid license plate character to its index in the 36-length frequency array.
     *
     * Mapping:
     * - '0'..'9' -> 0..9
     * - 'A'..'Z' -> 10..35
     *
     * @param ch the character to map
     * @return the corresponding frequency-array index
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public int charToIndex(char ch) {
        if (ch >= '0' && ch <= '9') {
            return ch - '0';
        }
        return 10 + (ch - 'A');
    }

    /**
     * Runs sample demonstrations of the solution and prints the results.
     *
     * This method verifies the examples from the problem statement:
     * Example 1 should print 3.
     * Example 2 should print 6.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input size of the demonstrated examples)
     * Space complexity: O(number of unique signatures in the demonstrated examples)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String[] plates1 = {"A1B1", "1AB1", "AB12", "B2A1", "XYZ", "ZYX"};
        long result1 = solution.countMatchingPairs(plates1);
        System.out.println("Example 1 result: " + result1);
        System.out.println("Expected: 3");

        String[] plates2 = {"AA11", "1A1A", "A11A", "BB2", "2BB", "B2B", "C3"};
        long result2 = solution.countMatchingPairs(plates2);
        System.out.println("Example 2 result: " + result2);
        System.out.println("Expected: 6");

        // Additional quick checks for beginner-friendly understanding.

        // No matching pairs.
        String[] plates3 = {"A", "B", "1", "Z9"};
        long result3 = solution.countMatchingPairs(plates3);
        System.out.println("Additional test 1 result: " + result3);
        System.out.println("Expected: 0");

        // All four plates match each other: 4 choose 2 = 6.
        String[] plates4 = {"AB12", "21BA", "B1A2", "2AB1"};
        long result4 = solution.countMatchingPairs(plates4);
        System.out.println("Additional test 2 result: " + result4);
        System.out.println("Expected: 6");
    }
}