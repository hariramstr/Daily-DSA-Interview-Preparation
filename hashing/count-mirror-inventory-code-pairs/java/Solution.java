import java.util.*;

/*
 * Title: Count Mirror Inventory Code Pairs
 * Difficulty: Medium
 * Topic: Hashing
 *
 * Problem Description:
 * You are given an array of product codes used in a warehouse system. Each code is a non-empty
 * lowercase string. Two codes form a mirror pair if one code can be transformed into the other
 * by reversing the order of its characters and then rotating the result by any number of positions,
 * including zero. For example, the reverse of "abca" is "acba", and its rotations are "acba",
 * "cbaa", "baac", and "aacb". Any code equal to one of those strings forms a mirror pair with "abca".
 *
 * Your task is to count how many unordered index pairs (i, j) with i < j are mirror pairs.
 *
 * A straightforward O(n^2 * m) comparison is too slow when the input is large. You should design
 * a solution that uses hashing to build a canonical signature for each code so that equivalent
 * codes under this mirror rule are grouped together efficiently.
 *
 * Return the total number of valid pairs.
 *
 * Constraints:
 * - 1 <= codes.length <= 100000
 * - 1 <= codes[i].length <= 50
 * - codes[i] contains only lowercase English letters
 * - Only codes of the same length can form a mirror pair
 *
 * Key Insight:
 * Two strings a and b form a mirror pair iff b is a rotation of reverse(a).
 *
 * This relation can be rewritten in a more grouping-friendly way:
 * b is a rotation of reverse(a)
 * <=> reverse(b) is a rotation of a
 *
 * Therefore, a and b are mirror-pair-compatible exactly when:
 * the rotation class of a is the same as the rotation class of reverse(b).
 *
 * So for each code s:
 * 1. Compute a canonical representative of the rotation class of s.
 * 2. Compute a canonical representative of the rotation class of reverse(s).
 *
 * While scanning from left to right:
 * - Let normalKey = canonical rotation of s
 * - Let reversedKey = canonical rotation of reverse(s)
 * - Any previous string t forms a valid pair with s exactly when canonical rotation of t == reversedKey
 *
 * We store counts of normalKey values seen so far.
 * Then:
 *   answer += countSeenNormal[reversedKey]
 *   countSeenNormal[normalKey]++
 *
 * This counts each unordered pair exactly once.
 */
public class Solution {

    /**
     * Counts how many unordered index pairs (i, j), with i < j, are mirror pairs.
     *
     * A pair is valid when one string can be obtained from the other by:
     * 1) reversing it
     * 2) rotating the reversed string by any number of positions
     *
     * We avoid O(n^2) comparison by assigning each string a canonical signature for its
     * rotation class. Then we use hashing to count compatible previously seen strings.
     *
     * @param codes the array of product codes
     * @return the total number of valid mirror pairs
     * Time complexity: O(n * m^2) in this implementation, where n is the number of strings
     * and m is the maximum string length. Since m <= 50, this is efficient in practice.
     * Space complexity: O(n * m) for the hash map storing canonical signatures
     */
    public long countMirrorPairs(String[] codes) {
        // This map stores:
        // key   -> canonical rotation signature of an ORIGINAL string seen earlier
        // value -> how many earlier strings had that signature
        Map<String, Integer> seenNormalRotationClassCount = new HashMap<>();

        long totalPairs = 0L;

        // Process strings from left to right so that every valid pair is counted exactly once.
        for (String code : codes) {
            // Canonical signature of the current string's own rotation class.
            String normalKey = canonicalRotation(code);

            // Reverse the current string.
            String reversed = reverse(code);

            // Canonical signature of the reversed string's rotation class.
            String reversedKey = canonicalRotation(reversed);

            // Every earlier string t forms a valid pair with current code iff:
            // canonicalRotation(t) == canonicalRotation(reverse(current))
            //
            // Why?
            // current is a rotation of reverse(t)
            // <=> reverse(current) is a rotation of t
            // <=> t and reverse(current) belong to the same rotation class
            totalPairs += seenNormalRotationClassCount.getOrDefault(reversedKey, 0);

            // Now add the current string to the "seen" structure for future strings.
            seenNormalRotationClassCount.put(
                normalKey,
                seenNormalRotationClassCount.getOrDefault(normalKey, 0) + 1
            );
        }

        return totalPairs;
    }

    /**
     * Builds a canonical representative for the rotation class of a string.
     *
     * All cyclic rotations of a string belong to the same class.
     * We choose the lexicographically smallest rotation as the canonical signature.
     *
     * Example:
     * rotations of "cbaa" are:
     * "cbaa", "baac", "aacb", "acba"
     * canonical representative = "aacb"
     *
     * Any two strings have the same canonical rotation signature iff one is a rotation of the other.
     *
     * @param s the input string
     * @return the lexicographically smallest cyclic rotation of s
     * Time complexity: O(m^2), where m is the length of s
     * Space complexity: O(m)
     */
    public String canonicalRotation(String s) {
        int n = s.length();

        // A length-1 string has only one rotation: itself.
        if (n == 1) {
            return s;
        }

        // Concatenating the string to itself allows every rotation to appear
        // as a contiguous substring of length n.
        //
        // Example:
        // s = "acba"
        // doubled = "acbaacba"
        //
        // Rotations:
        // start at 0 -> "acba"
        // start at 1 -> "cbaa"
        // start at 2 -> "baac"
        // start at 3 -> "aacb"
        String doubled = s + s;

        // Start by assuming the original string is the smallest rotation.
        String best = doubled.substring(0, n);

        // Check every possible rotation start position from 1 to n - 1.
        for (int start = 1; start < n; start++) {
            String candidate = doubled.substring(start, start + n);

            // Keep the lexicographically smallest rotation.
            if (candidate.compareTo(best) < 0) {
                best = candidate;
            }
        }

        return best;
    }

    /**
     * Reverses a string.
     *
     * @param s the input string
     * @return the reversed string
     * Time complexity: O(m), where m is the length of s
     * Space complexity: O(m)
     */
    public String reverse(String s) {
        return new StringBuilder(s).reverse().toString();
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * It also prints the expected values so the output can be visually verified.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input size * m^2) for the demonstrated examples
     * Space complexity: O(total input size)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String[] codes1 = {"abca", "cbaa", "zz", "zz", "aacb"};
        long result1 = solution.countMirrorPairs(codes1);
        System.out.println("Example 1 result: " + result1);
        System.out.println("Expected: 3");

        String[] codes2 = {"abc", "cab", "bca", "xy", "yx", "aa"};
        long result2 = solution.countMirrorPairs(codes2);
        System.out.println("Example 2 result: " + result2);
        System.out.println("Expected: 2");

        // Additional quick sanity checks.

        // Single string cannot form any pair.
        String[] codes3 = {"a"};
        System.out.println("Sanity check 1: " + solution.countMirrorPairs(codes3));
        System.out.println("Expected: 0");

        // "abc" reverse is "cba"; rotations are "cba", "bac", "acb"
        // So "acb" is a valid partner.
        String[] codes4 = {"abc", "acb"};
        System.out.println("Sanity check 2: " + solution.countMirrorPairs(codes4));
        System.out.println("Expected: 1");

        // All "zz" pair with each other.
        String[] codes5 = {"zz", "zz", "zz"};
        System.out.println("Sanity check 3: " + solution.countMirrorPairs(codes5));
        System.out.println("Expected: 3");
    }
}