import java.util.*;

/*
Title: Count Mirror-Shifted Coupon Codes
Difficulty: Medium
Topic: Hashing

Problem Description:
An e-commerce platform stores promotional coupon codes as strings of lowercase English letters.
Two coupon codes are considered mirror-shifted if one can be transformed into the other by
applying the same cyclic alphabet shift to every character. For example, shifting each character
in "abc" by 2 produces "cde", and shifting each character in "xyz" by 3 produces "abc" because
the alphabet wraps around. Therefore, "abc", "bcd", and "xyz" all belong to the same
mirror-shifted group. However, codes of different lengths can never belong to the same group.

You are given an array codes where each element is a non-empty string. Count how many unordered
pairs of indices (i, j) with i < j belong to the same mirror-shifted group.

A common way to solve this is to normalize each string into a canonical signature based on the
differences between consecutive characters modulo 26, then use a hash map to count how many times
each signature appears.

Return the total number of valid pairs.

Constraints:
- 1 <= codes.length <= 100000
- 1 <= codes[i].length <= 100000
- codes[i] contains only lowercase English letters
- The sum of all string lengths does not exceed 200000

Example 1:
Input: codes = ["abc", "bcd", "ace", "xyz", "az", "ba", "a", "z"]
Output: 5
Explanation: The groups are:
- ["abc", "bcd", "xyz"] -> 3 pairs
- ["az", "ba"] -> 1 pair
- ["a", "z"] -> 1 pair
Total = 5.

Example 2:
Input: codes = ["aa", "bb", "ab", "za", "yx"]
Output: 4
Explanation:
- ["aa", "bb"] share the same signature, contributing 1 pair.
- ["ab", "za", "yx"] share the same signature, contributing 3 pairs.
Total = 4.
*/

public class Solution {

    /**
     * Counts how many unordered pairs of coupon codes belong to the same mirror-shifted group.
     *
     * The key idea is:
     * 1. Convert each string into a canonical signature.
     * 2. Two strings are in the same group if and only if their signatures are identical.
     * 3. Use a hash map to count how many times each signature appears.
     * 4. Every time we see a signature again, it forms new pairs with all previous strings
     *    that had the same signature.
     *
     * For example:
     * - "abc" -> differences: (1, 1)
     * - "bcd" -> differences: (1, 1)
     * - "xyz" -> differences: (1, 1)
     * So all three share the same signature and belong to the same group.
     *
     * @param codes the array of coupon code strings
     * @return the total number of unordered pairs (i, j) with i < j that are mirror-shifted
     * Time complexity: O(total length of all strings)
     * Space complexity: O(total length of all generated signatures)
     */
    public long countMirrorShiftedPairs(String[] codes) {
        // This map stores:
        // signature -> how many strings seen so far have this signature
        Map<String, Integer> frequencyBySignature = new HashMap<>();

        // We use long because the number of pairs can be large.
        // In the worst case, if all strings belong to one group and there are 100000 strings,
        // the number of pairs is 100000 * 99999 / 2, which does not fit in int.
        long totalPairs = 0L;

        // Process each code one by one.
        for (String code : codes) {
            // Build a canonical representation of the current string.
            String signature = buildSignature(code);

            // Find how many previous strings had the same signature.
            // Each such previous string forms one new valid pair with the current string.
            int seenSoFar = frequencyBySignature.getOrDefault(signature, 0);

            // Add those newly formed pairs to the answer.
            totalPairs += seenSoFar;

            // Record that we have now seen one more string with this signature.
            frequencyBySignature.put(signature, seenSoFar + 1);
        }

        return totalPairs;
    }

    /**
     * Builds a canonical signature for a string based on cyclic differences between
     * consecutive characters.
     *
     * Why this works:
     * - If two strings differ only by a uniform cyclic shift, then every consecutive
     *   difference modulo 26 stays the same.
     * - Therefore, the sequence of differences uniquely identifies the mirror-shifted group.
     *
     * Examples:
     * - "abc" -> differences: 1,1 -> signature "3#1,1"
     * - "bcd" -> differences: 1,1 -> signature "3#1,1"
     * - "xyz" -> differences: 1,1 -> signature "3#1,1"
     * - "az"  -> difference from 'a' to 'z' is 25 -> signature "2#25"
     * - "ba"  -> difference from 'b' to 'a' is (0 - 1 + 26) % 26 = 25 -> signature "2#25"
     *
     * Single-character strings:
     * - Any single character can be shifted to any other single character.
     * - So all length-1 strings must share the same signature.
     * - We simply return "1#" for all of them.
     *
     * We also include the string length in the signature to make the grouping explicit and safe.
     * Although the difference sequence length already implies the original length, including the
     * length makes the signature easier to understand and avoids ambiguity.
     *
     * @param code the input coupon code
     * @return a canonical signature string representing the mirror-shifted group
     * Time complexity: O(code.length())
     * Space complexity: O(code.length())
     */
    public String buildSignature(String code) {
        int n = code.length();

        // All single-character strings belong to the same group.
        // Example: "a", "z", "m" can all be shifted into each other.
        if (n == 1) {
            return "1#";
        }

        // We build a compact textual signature.
        // Format:
        // length#diff1,diff2,diff3,...
        //
        // Example:
        // "abc" => 3#1,1
        // "ace" => 3#2,2
        //
        // The separators (# and ,) ensure there is no accidental ambiguity.
        StringBuilder signature = new StringBuilder();
        signature.append(n).append('#');

        // Walk through each adjacent pair of characters.
        for (int i = 1; i < n; i++) {
            int previous = code.charAt(i - 1) - 'a';
            int current = code.charAt(i) - 'a';

            // Compute cyclic difference modulo 26.
            // We add 26 before taking % 26 to avoid negative values.
            int diff = (current - previous + 26) % 26;

            // Add a separator before every difference except the first one.
            if (i > 1) {
                signature.append(',');
            }

            signature.append(diff);
        }

        return signature.toString();
    }

    /**
     * Runs a demonstration using the sample inputs from the problem statement.
     *
     * This method prints:
     * - the input arrays
     * - the computed answers
     * - the expected answers
     *
     * It is intended to make the solution easy to test and understand.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total length of demo inputs)
     * Space complexity: O(total length of generated signatures for demo inputs)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String[] codes1 = {"abc", "bcd", "ace", "xyz", "az", "ba", "a", "z"};
        long result1 = solution.countMirrorShiftedPairs(codes1);
        System.out.println("Example 1 input: " + Arrays.toString(codes1));
        System.out.println("Example 1 output: " + result1);
        System.out.println("Example 1 expected: 5");
        System.out.println();

        String[] codes2 = {"aa", "bb", "ab", "za", "yx"};
        long result2 = solution.countMirrorShiftedPairs(codes2);
        System.out.println("Example 2 input: " + Arrays.toString(codes2));
        System.out.println("Example 2 output: " + result2);
        System.out.println("Example 2 expected: 4");
        System.out.println();

        // Additional quick sanity checks for beginners:
        // 1) All single-character strings belong to the same group.
        String[] codes3 = {"a", "b", "z", "m"};
        long result3 = solution.countMirrorShiftedPairs(codes3);
        System.out.println("Single-character test input: " + Arrays.toString(codes3));
        System.out.println("Single-character test output: " + result3);
        System.out.println("Single-character test expected: 6");
        System.out.println();

        // 2) Different lengths can never match.
        String[] codes4 = {"a", "ab", "bc", "z"};
        long result4 = solution.countMirrorShiftedPairs(codes4);
        System.out.println("Different-length test input: " + Arrays.toString(codes4));
        System.out.println("Different-length test output: " + result4);
        System.out.println("Different-length test expected: 2");
    }
}