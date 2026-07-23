import java.util.*;

/*
 * Title: First Repeated Hashtag in a Campaign Feed
 * Difficulty: Easy
 * Topic: Hashing
 *
 * Problem Description:
 * A marketing platform stores the hashtags used in a campaign feed as an array of strings,
 * in the exact order they were posted. Your task is to find the first hashtag that appears
 * more than once while scanning the feed from left to right.
 *
 * Return the first repeated hashtag based on the moment its second occurrence is seen.
 * If no hashtag is repeated, return an empty string.
 *
 * For example, if the feed is ["#launch", "#sale", "#launch", "#summer"], the answer is
 * "#launch" because it is the first hashtag whose second appearance occurs during the scan.
 * If multiple hashtags appear multiple times, you should not return the one with the smallest
 * total count or lexicographically smallest value; return the one that becomes repeated earliest.
 *
 * This problem is intended to be solved efficiently using a hash-based data structure to track
 * which hashtags have already been seen.
 *
 * Constraints:
 * - 1 <= hashtags.length <= 100000
 * - 1 <= hashtags[i].length <= 50
 * - hashtags[i] consists of letters, digits, underscores, and the '#' character
 * - Comparison is case-sensitive
 *
 * Example 1:
 * Input: hashtags = ["#launch", "#sale", "#launch", "#summer"]
 * Output: "#launch"
 * Explanation: "#launch" is seen at index 0 and repeats at index 2, which is the earliest second occurrence.
 *
 * Example 2:
 * Input: hashtags = ["#red", "#blue", "#green", "#blue", "#red"]
 * Output: "#blue"
 * Explanation: Although both "#blue" and "#red" repeat, "#blue" becomes repeated first when scanning from left to right.
 */

public class Solution {

    /**
     * Finds the first hashtag that becomes repeated while scanning from left to right.
     *
     * The key idea is:
     * 1. Keep a hash set of hashtags we have already seen.
     * 2. Visit each hashtag in order.
     * 3. If the current hashtag is already in the set, then this is the first moment
     *    we have encountered its second occurrence during the scan, so we return it immediately.
     * 4. Otherwise, add it to the set and continue.
     * 5. If the scan finishes without finding any repeated hashtag, return an empty string.
     *
     * @param hashtags the campaign feed hashtags in the exact order they were posted
     * @return the first hashtag whose second occurrence appears earliest during the left-to-right scan;
     *         returns an empty string if no hashtag repeats
     *
     * Time complexity: O(n), where n is the number of hashtags, because each hashtag is checked
     * and inserted into the hash set at most once on average.
     * Space complexity: O(n), in the worst case when all hashtags are distinct and stored in the set.
     */
    public String firstRepeatedHashtag(String[] hashtags) {
        // Create a HashSet to store every hashtag we have seen so far.
        // Why a HashSet?
        // Because it gives us very fast average-time lookup:
        // - contains(...) is O(1) on average
        // - add(...) is O(1) on average
        Set<String> seen = new HashSet<>();

        // Scan the array from left to right exactly once.
        // This order is extremely important because the problem asks for the hashtag
        // whose SECOND occurrence is encountered first during the scan.
        for (int i = 0; i < hashtags.length; i++) {
            // Read the current hashtag at index i.
            String currentHashtag = hashtags[i];

            // Step 1:
            // Check whether we have already seen this hashtag before.
            //
            // If yes:
            // That means the current position is at least the second occurrence of this hashtag.
            // Since we are scanning from left to right, the FIRST time this condition becomes true
            // is exactly the answer required by the problem.
            if (seen.contains(currentHashtag)) {
                return currentHashtag;
            }

            // Step 2:
            // If the hashtag has not been seen before, record it in the set
            // so that if it appears again later, we can detect the repetition.
            seen.add(currentHashtag);
        }

        // If we finish the entire scan and never find a repeated hashtag,
        // the correct result is an empty string.
        return "";
    }

    /**
     * Utility method to print an array of hashtags in a readable format.
     *
     * @param hashtags the array of hashtags to print
     * @return a string representation of the array
     *
     * Time complexity: O(n), where n is the number of hashtags.
     * Space complexity: O(n), due to building the output string.
     */
    public String arrayToString(String[] hashtags) {
        return Arrays.toString(hashtags);
    }

    /**
     * Demonstrates the solution using sample inputs from the problem statement
     * and a few additional beginner-friendly test cases.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(1) for the fixed demonstration size shown here,
     * though each individual call to the algorithm is O(n) for its input size.
     * Space complexity: O(1) extra for the driver logic, excluding the input arrays themselves.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement:
        // ["#launch", "#sale", "#launch", "#summer"]
        //
        // Trace:
        // - See "#launch" first time -> add to set
        // - See "#sale" first time   -> add to set
        // - See "#launch" again      -> already in set, so answer is "#launch"
        String[] hashtags1 = {"#launch", "#sale", "#launch", "#summer"};
        System.out.println("Input:  " + solution.arrayToString(hashtags1));
        System.out.println("Output: " + solution.firstRepeatedHashtag(hashtags1));
        System.out.println("Expected: #launch");
        System.out.println();

        // Example 2 from the problem statement:
        // ["#red", "#blue", "#green", "#blue", "#red"]
        //
        // Trace:
        // - "#red"   -> first time, add
        // - "#blue"  -> first time, add
        // - "#green" -> first time, add
        // - "#blue"  -> already seen, so answer is "#blue"
        // We return immediately here, even though "#red" also repeats later.
        String[] hashtags2 = {"#red", "#blue", "#green", "#blue", "#red"};
        System.out.println("Input:  " + solution.arrayToString(hashtags2));
        System.out.println("Output: " + solution.firstRepeatedHashtag(hashtags2));
        System.out.println("Expected: #blue");
        System.out.println();

        // Additional test case: no repeated hashtag
        String[] hashtags3 = {"#one", "#two", "#three"};
        System.out.println("Input:  " + solution.arrayToString(hashtags3));
        System.out.println("Output: " + solution.firstRepeatedHashtag(hashtags3));
        System.out.println("Expected: ");
        System.out.println();

        // Additional test case: immediate repetition
        String[] hashtags4 = {"#hot", "#hot", "#new"};
        System.out.println("Input:  " + solution.arrayToString(hashtags4));
        System.out.println("Output: " + solution.firstRepeatedHashtag(hashtags4));
        System.out.println("Expected: #hot");
        System.out.println();

        // Additional test case: case-sensitive comparison
        // "#Tag" and "#tag" are different strings in Java.
        String[] hashtags5 = {"#Tag", "#tag", "#Tag"};
        System.out.println("Input:  " + solution.arrayToString(hashtags5));
        System.out.println("Output: " + solution.firstRepeatedHashtag(hashtags5));
        System.out.println("Expected: #Tag");
    }
}