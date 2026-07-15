import java.util.*;

/*
Title: Count Mirror Username Pairs
Difficulty: Medium
Topic: Hashing

Problem Description:
A social platform stores a list of usernames in the order they were created. Two usernames form a mirror pair if one of them is exactly the reverse of the other, and the two usernames appear at different indices. For example, "stressed" and "desserts" form a mirror pair, while a single username does not pair with itself unless the same string appears again at another index. Your task is to count how many distinct index pairs (i, j) with i < j form a mirror pair.

You are given an array usernames of length n. Return the total number of mirror pairs.

A pair is counted by indices, not by unique string values. This means duplicate usernames can create multiple valid pairs. For instance, if usernames contains two copies of "abc" and three copies of "cba", then they contribute 2 * 3 = 6 mirror pairs. Usernames that are palindromes, such as "level", can also form mirror pairs with other equal copies of the same palindrome.

Constraints:
- 1 <= n <= 200000
- 1 <= usernames[i].length <= 30
- usernames[i] consists only of lowercase English letters

Example 1:
Input: usernames = ["abc", "cba", "xy", "yx", "abc"]
Output: 2
Explanation: Valid pairs are (0,1) because "abc" reversed is "cba", and (2,3) because "xy" reversed is "yx". The last "abc" has no later matching reverse.

Example 2:
Input: usernames = ["aa", "aa", "aa", "ab", "ba"]
Output: 4
Explanation: The three "aa" usernames are palindromes, so every pair among them is valid: 3 pairs. In addition, "ab" and "ba" form 1 pair. Total = 4.

An efficient solution should avoid checking all O(n^2) pairs. Consider using a hash map to track how many reversed usernames have already appeared as you scan through the array.
*/

public class Solution {

    /**
     * Counts how many index pairs (i, j), with i < j, form a mirror pair.
     *
     * Core idea:
     * We scan the array from left to right. For the current username at index j,
     * we compute its reversed string. Any earlier username equal to that reversed
     * string can pair with the current username, because:
     *   usernames[i] == reverse(usernames[j]) for some i < j
     *
     * So:
     * 1. Maintain a frequency map of usernames already seen.
     * 2. For each current username:
     *    - reverse it
     *    - add the number of previously seen copies of that reversed string
     *      to the answer
     *    - then record the current username as seen
     *
     * This correctly handles:
     * - normal reverse pairs like "abc" and "cba"
     * - duplicates, because each prior matching index contributes one pair
     * - palindromes like "aa", where reverse("aa") == "aa", so earlier equal
     *   copies are counted naturally
     *
     * @param usernames the array of usernames in creation order
     * @return the total number of mirror index pairs
     * Time complexity: O(n * m), where n is the number of usernames and m is the maximum username length
     * Space complexity: O(n * m) in the worst case for storing distinct usernames in the hash map
     */
    public long countMirrorPairs(String[] usernames) {
        // This map stores how many times each username has appeared so far
        // while scanning from left to right.
        Map<String, Integer> seenCount = new HashMap<>();

        // We use long because the number of valid pairs can be large.
        // For example, if many usernames are identical palindromes,
        // the number of pairs can exceed the range of int.
        long pairs = 0L;

        // Process each username in order.
        for (String current : usernames) {
            // Compute the reverse of the current username.
            String reversed = reverse(current);

            // If we have already seen some usernames equal to this reversed string,
            // then each of those earlier indices forms a valid pair with the current index.
            //
            // Example:
            // seenCount["abc"] = 2
            // current = "cba"
            // reversed = "abc"
            // Then both earlier "abc" entries pair with this "cba".
            pairs += seenCount.getOrDefault(reversed, 0);

            // Now record that the current username has been seen.
            // This ensures it can contribute to future pairs, but not to itself.
            seenCount.put(current, seenCount.getOrDefault(current, 0) + 1);
        }

        return pairs;
    }

    /**
     * Reverses a string.
     *
     * @param s the input string
     * @return the reversed string
     * Time complexity: O(m), where m is the length of the string
     * Space complexity: O(m)
     */
    public String reverse(String s) {
        // StringBuilder provides an easy and efficient way to reverse a string.
        return new StringBuilder(s).reverse().toString();
    }

    /**
     * Utility method to print an array in a readable format.
     *
     * @param usernames the array to print
     * @return a string representation of the array
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public String arrayToString(String[] usernames) {
        return Arrays.toString(usernames);
    }

    /**
     * Demonstrates the solution on sample inputs from the problem statement
     * and prints the results.
     *
     * Verified examples:
     *
     * Example 1:
     * ["abc", "cba", "xy", "yx", "abc"]
     * Pairs:
     * - (0,1): "abc" <-> "cba"
     * - (2,3): "xy" <-> "yx"
     * Total = 2
     *
     * Example 2:
     * ["aa", "aa", "aa", "ab", "ba"]
     * Pairs:
     * - Among the three "aa": choose any 2 => 3 pairs
     * - (3,4): "ab" <-> "ba"
     * Total = 4
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demo setup, excluding the called algorithm
     * Space complexity: O(1), excluding the called algorithm
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String[] usernames1 = {"abc", "cba", "xy", "yx", "abc"};
        long result1 = solution.countMirrorPairs(usernames1);
        System.out.println("Input: " + solution.arrayToString(usernames1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 2");
        System.out.println();

        String[] usernames2 = {"aa", "aa", "aa", "ab", "ba"};
        long result2 = solution.countMirrorPairs(usernames2);
        System.out.println("Input: " + solution.arrayToString(usernames2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 4");
        System.out.println();

        // Additional demonstration:
        // Two copies of "abc" and three copies of "cba" contribute 2 * 3 = 6 pairs.
        String[] usernames3 = {"abc", "cba", "abc", "cba", "cba"};
        long result3 = solution.countMirrorPairs(usernames3);
        System.out.println("Input: " + solution.arrayToString(usernames3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 6");
        System.out.println();

        // Additional palindrome demonstration:
        // Four copies of "level" produce C(4,2) = 6 pairs.
        String[] usernames4 = {"level", "level", "level", "level"};
        long result4 = solution.countMirrorPairs(usernames4);
        System.out.println("Input: " + solution.arrayToString(usernames4));
        System.out.println("Output: " + result4);
        System.out.println("Expected: 6");
    }
}