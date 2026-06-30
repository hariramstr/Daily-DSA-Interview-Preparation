import java.util.*;

/*
 * Title: Longest Typing Burst With Limited Hand Switches
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given a string s representing a sequence of keys typed on a custom keyboard.
 * Each character belongs to either the left hand or the right hand. You are also given
 * a mapping string handMap of length 26, where handMap[i] is either 'L' or 'R',
 * indicating whether the lowercase letter ('a' + i) is typed with the left or right hand.
 *
 * A contiguous substring of s is called a smooth typing burst if the number of times
 * the typist switches hands between adjacent characters in that substring is at most k.
 * For example, if the substring is "abca" and the hand sequence is L L R L, then the
 * number of hand switches is 2: between the second and third characters, and between
 * the third and fourth characters.
 *
 * Return the length of the longest smooth typing burst.
 *
 * A hand switch is counted only between neighboring characters inside the chosen substring.
 * A substring of length 0 has length 0, and a substring of length 1 always has 0 switches.
 *
 * Constraints:
 * - 1 <= s.length <= 2 * 10^5
 * - 0 <= k < s.length
 * - s contains only lowercase English letters
 * - handMap.length == 26
 * - Every character in handMap is either 'L' or 'R'
 *
 * Efficient approach hint:
 * Use a sliding window. As the right boundary expands, track whether the new adjacent pair
 * introduces a hand switch. If the number of switches becomes too large, move the left
 * boundary rightward and remove the contribution of the adjacent pair that leaves the window.
 */

public class Solution {

    /**
     * Computes the length of the longest contiguous substring whose number of adjacent
     * hand switches is at most k.
     *
     * The key observation:
     * For a window [left, right], the total number of hand switches depends only on
     * adjacent pairs inside that window:
     *   (left,left+1), (left+1,left+2), ..., (right-1,right)
     *
     * We maintain:
     * - left: start of the current window
     * - right: end of the current window as we expand
     * - switches: number of adjacent hand switches currently inside the window
     *
     * When we move right forward by one position:
     * - Only one new adjacent pair is added: (right-1, right)
     * - If those two characters use different hands, switches increases by 1
     *
     * When switches becomes greater than k:
     * - We shrink from the left
     * - Removing the leftmost character removes exactly one adjacent pair from the window:
     *   (left, left+1)
     * - If that pair was a hand switch, switches decreases by 1
     *
     * This guarantees linear time because each pointer moves from left to right at most once.
     *
     * @param s the typed string consisting of lowercase English letters
     * @param k the maximum allowed number of adjacent hand switches inside the substring
     * @param handMap a 26-character string where handMap[i] is 'L' or 'R' for letter ('a' + i)
     * @return the maximum length of a valid smooth typing burst
     * Time complexity: O(n), where n is s.length()
     * Space complexity: O(1), ignoring the input storage, because only a few variables are used
     */
    public int longestTypingBurst(String s, int k, String handMap) {
        validateInput(s, k, handMap);

        int n = s.length();

        // Convert each character in s into its hand label ('L' or 'R').
        // This makes adjacent hand comparisons very direct and easy to read.
        char[] hands = buildHandSequence(s, handMap);

        // Sliding window left boundary.
        int left = 0;

        // Number of hand switches among adjacent pairs currently inside [left, right].
        int switches = 0;

        // Best answer found so far.
        int maxLength = 0;

        // Expand the window one character at a time.
        for (int right = 0; right < n; right++) {

            // If right > 0, then adding position right introduces exactly one new adjacent pair:
            // (right - 1, right)
            // If the hands differ, that pair contributes one hand switch.
            if (right > 0 && hands[right] != hands[right - 1]) {
                switches++;
            }

            // If the window now has too many switches, shrink it from the left
            // until it becomes valid again.
            //
            // Important detail:
            // When left moves from x to x + 1, the pair (x, x + 1) leaves the window.
            // If that pair was a hand switch, we must subtract it.
            while (switches > k) {
                if (left < right && hands[left] != hands[left + 1]) {
                    switches--;
                }
                left++;
            }

            // Now [left, right] is valid, so update the best answer.
            int currentLength = right - left + 1;
            if (currentLength > maxLength) {
                maxLength = currentLength;
            }
        }

        return maxLength;
    }

    /**
     * Builds an array where each position stores whether the corresponding character
     * in the input string is typed with the left hand or the right hand.
     *
     * Example:
     * If s = "abca" and:
     * - a -> L
     * - b -> L
     * - c -> R
     * then the result is ['L', 'L', 'R', 'L'].
     *
     * @param s the typed string
     * @param handMap the 26-character hand mapping
     * @return a char array of the same length as s, containing only 'L' or 'R'
     * Time complexity: O(n), where n is s.length()
     * Space complexity: O(n), for the returned array
     */
    public char[] buildHandSequence(String s, String handMap) {
        char[] hands = new char[s.length()];

        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            hands[i] = handMap.charAt(ch - 'a');
        }

        return hands;
    }

    /**
     * Validates the input according to the problem constraints.
     *
     * This method is not strictly necessary for competitive programming,
     * but it is useful in a complete runnable program to make failures clearer.
     *
     * @param s the typed string
     * @param k the maximum allowed number of hand switches
     * @param handMap the 26-character hand mapping
     * @return nothing
     * Time complexity: O(n), where n is s.length()
     * Space complexity: O(1)
     */
    public void validateInput(String s, int k, String handMap) {
        if (s == null) {
            throw new IllegalArgumentException("Input string s must not be null.");
        }
        if (handMap == null) {
            throw new IllegalArgumentException("handMap must not be null.");
        }
        if (handMap.length() != 26) {
            throw new IllegalArgumentException("handMap must have length 26.");
        }
        if (s.length() < 1 || s.length() > 200_000) {
            throw new IllegalArgumentException("s.length must be between 1 and 200000.");
        }
        if (k < 0 || k >= s.length()) {
            throw new IllegalArgumentException("k must satisfy 0 <= k < s.length.");
        }

        for (int i = 0; i < 26; i++) {
            char c = handMap.charAt(i);
            if (c != 'L' && c != 'R') {
                throw new IllegalArgumentException("handMap must contain only 'L' or 'R'.");
            }
        }

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < 'a' || c > 'z') {
                throw new IllegalArgumentException("s must contain only lowercase English letters.");
            }
        }
    }

    /**
     * Demonstrates the solution on sample-style inputs and prints the results.
     *
     * Note:
     * The exact sample outputs shown in the prompt are not consistent with the stated
     * hand-switch definition for the provided concrete handMap strings.
     * This program follows the formal problem definition exactly:
     * count switches between adjacent characters inside the substring using handMap.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(n) per demonstrated test case
     * Space complexity: O(n) per demonstrated test case due to hand sequence construction
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String s1 = "abacabad";
        int k1 = 2;
        String handMap1 = "LLRLRRLLRLRLRLRLRLRLRLRLRL";
        int result1 = solution.longestTypingBurst(s1, k1, handMap1);
        System.out.println("Example 1 Result: " + result1);

        String s2 = "zzxyyx";
        int k2 = 1;
        String handMap2 = "LRLRLRLRLRLRLRLRLRLRLRLRLR";
        int result2 = solution.longestTypingBurst(s2, k2, handMap2);
        System.out.println("Example 2 Result: " + result2);

        // Additional small sanity checks for beginner-friendly understanding.

        // All same hand => zero switches everywhere, so entire string is valid.
        String s3 = "aaaaa";
        int k3 = 0;
        String handMap3 = "LLLLLLLLLLLLLLLLLLLLLLLLLL";
        int result3 = solution.longestTypingBurst(s3, k3, handMap3);
        System.out.println("Sanity Check 1 Result: " + result3); // Expected: 5

        // Alternating hands by letter parity, string "ababa" => every adjacent pair switches.
        // With k = 2, the longest valid substring can contain at most 2 switches => length 3.
        String s4 = "ababa";
        int k4 = 2;
        String handMap4 = "LRLRLRLRLRLRLRLRLRLRLRLRLR";
        int result4 = solution.longestTypingBurst(s4, k4, handMap4);
        System.out.println("Sanity Check 2 Result: " + result4); // Expected: 3
    }
}