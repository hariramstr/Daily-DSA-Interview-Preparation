import java.util.*;

/*
 * Title: Longest Translation Draft With Limited Unknown Terms
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * A localization team is reviewing a draft translation represented by an array of lowercase words.
 * Some words are already approved and appear in a dictionary, while other words are currently unknown
 * and require manual review. The team wants to find the longest contiguous section of the draft that
 * can be sent to a reviewer, under the rule that the section may contain at most k unknown words.
 *
 * You are given a string array words, a string array approved containing all approved dictionary words,
 * and an integer k. Return the length of the longest contiguous subarray of words that contains at most
 * k words not present in approved.
 *
 * A word is considered unknown if it does not exist in the approved list. The approved list may contain
 * duplicates, but they should be treated as a set. Your solution should be efficient enough for large inputs.
 *
 * Constraints:
 * - 1 <= words.length <= 200000
 * - 1 <= approved.length <= 200000
 * - 0 <= k <= words.length
 * - 1 <= words[i].length, approved[i].length <= 20
 * - All words consist only of lowercase English letters
 *
 * Example 1:
 * Input: words = ["hola","mundo","api","seguro","beta","listo"],
 *        approved = ["hola","mundo","seguro","listo"],
 *        k = 1
 * Output: 4
 * Explanation: The longest valid window is ["hola","mundo","api","seguro"].
 * It contains only one unknown word: "api".
 *
 * Example 2:
 * Input: words = ["save","draft","tmp","publish","tmp","done"],
 *        approved = ["save","draft","publish","done"],
 *        k = 0
 * Output: 2
 * Explanation: With no unknown words allowed, the longest valid windows are
 * ["save","draft"], ["publish"], and ["done"]. The maximum length is 2.
 */

public class Solution {

    /**
     * Finds the length of the longest contiguous subarray of words that contains
     * at most k unknown words, where "unknown" means the word does not appear
     * in the approved dictionary.
     *
     * The method first converts the approved array into a HashSet so membership
     * checks are fast. Then it uses the classic sliding window technique:
     *
     * 1. Expand the right boundary one word at a time.
     * 2. Count how many unknown words are currently inside the window.
     * 3. If the count becomes greater than k, move the left boundary forward
     *    until the window becomes valid again.
     * 4. Track the maximum valid window length seen so far.
     *
     * @param words the draft translation as an array of lowercase words
     * @param approved the list of approved dictionary words; duplicates are allowed but treated as a set
     * @param k the maximum number of unknown words allowed in a valid window
     * @return the length of the longest contiguous subarray containing at most k unknown words
     *
     * Time complexity: O(n + m), where n = words.length and m = approved.length
     * because each word is processed at most a constant number of times.
     * Space complexity: O(m) for the HashSet storing approved words.
     */
    public int longestTranslationDraft(String[] words, String[] approved, int k) {
        // Convert the approved list into a set.
        // This removes duplicates automatically and gives us average O(1) lookup time.
        Set<String> approvedSet = buildApprovedSet(approved);

        // left points to the beginning of the current sliding window.
        int left = 0;

        // unknownCount stores how many words inside the current window are not approved.
        int unknownCount = 0;

        // best stores the maximum valid window length found so far.
        int best = 0;

        // Move right from 0 to words.length - 1, expanding the window one step at a time.
        for (int right = 0; right < words.length; right++) {
            // If the newly included word is not approved, it increases the unknown count.
            if (!approvedSet.contains(words[right])) {
                unknownCount++;
            }

            // If the window now has too many unknown words, it is invalid.
            // We must shrink it from the left until it becomes valid again.
            while (unknownCount > k) {
                // Before moving left forward, check whether the word leaving the window
                // was unknown. If yes, reduce unknownCount because it is no longer inside.
                if (!approvedSet.contains(words[left])) {
                    unknownCount--;
                }

                // Actually shrink the window by moving left one step to the right.
                left++;
            }

            // At this point, the window [left, right] is guaranteed to be valid:
            // it contains at most k unknown words.
            int currentLength = right - left + 1;

            // Update the best answer if this valid window is longer than any previous one.
            if (currentLength > best) {
                best = currentLength;
            }
        }

        // Return the maximum valid window length found.
        return best;
    }

    /**
     * Builds a HashSet from the approved array so duplicate approved words are ignored
     * and membership checks become efficient.
     *
     * @param approved the array of approved words, possibly containing duplicates
     * @return a set containing all unique approved words
     *
     * Time complexity: O(m), where m = approved.length
     * Space complexity: O(m) in the worst case if all approved words are unique
     */
    public Set<String> buildApprovedSet(String[] approved) {
        Set<String> approvedSet = new HashSet<>();

        // Add every approved word into the set.
        // If duplicates exist, HashSet keeps only one copy.
        for (String word : approved) {
            approvedSet.add(word);
        }

        return approvedSet;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     * It prints the computed results and the expected results for easy verification.
     *
     * @param args command-line arguments; not used in this program
     * @return nothing
     *
     * Time complexity: O(n + m) per demonstration call to the main algorithm
     * Space complexity: O(m) per demonstration call for the approved set
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        String[] words1 = {"hola", "mundo", "api", "seguro", "beta", "listo"};
        String[] approved1 = {"hola", "mundo", "seguro", "listo"};
        int k1 = 1;
        int result1 = solution.longestTranslationDraft(words1, approved1, k1);
        System.out.println("Example 1 Result: " + result1);
        System.out.println("Expected: 4");

        // Example 2
        String[] words2 = {"save", "draft", "tmp", "publish", "tmp", "done"};
        String[] approved2 = {"save", "draft", "publish", "done"};
        int k2 = 0;
        int result2 = solution.longestTranslationDraft(words2, approved2, k2);
        System.out.println("Example 2 Result: " + result2);
        System.out.println("Expected: 2");

        // Additional quick sanity checks
        String[] words3 = {"a", "b", "c"};
        String[] approved3 = {"a", "b", "c"};
        int k3 = 0;
        int result3 = solution.longestTranslationDraft(words3, approved3, k3);
        System.out.println("All approved Result: " + result3);
        System.out.println("Expected: 3");

        String[] words4 = {"x", "y", "z"};
        String[] approved4 = {"a", "b"};
        int k4 = 2;
        int result4 = solution.longestTranslationDraft(words4, approved4, k4);
        System.out.println("At most 2 unknowns Result: " + result4);
        System.out.println("Expected: 2");
    }
}