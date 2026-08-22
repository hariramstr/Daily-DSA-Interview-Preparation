import java.util.*;

/*
 * Title: Find the First Repeated Poll Vote
 * Difficulty: Easy
 * Topic: Hashing
 *
 * Problem Description:
 * You are given an array `votes` where each element is a string representing the option
 * selected by a user in the order the votes were received. Your task is to return the
 * first vote value that appears more than once in the stream.
 *
 * A vote is considered the first repeated vote if its second occurrence appears earlier
 * than the second occurrence of any other repeated vote. In other words, scan the array
 * from left to right and return the first value you have already seen before.
 *
 * If no vote is repeated, return an empty string `""`.
 *
 * This problem models a real-time polling system where duplicate selections may indicate
 * repeated submissions, and the system wants to detect the earliest duplicated option as
 * quickly as possible.
 *
 * You should aim for a solution that processes the votes in one pass. A hash set is a
 * natural fit because it allows you to check whether a vote has already appeared in
 * average O(1) time.
 *
 * Constraints:
 * - 1 <= votes.length <= 100000
 * - 1 <= votes[i].length <= 30
 * - votes[i] consists of lowercase English letters, digits, or underscores
 *
 * Example 1:
 * Input: votes = ["red", "blue", "green", "blue", "red"]
 * Output: "blue"
 * Explanation: `blue` is the first vote whose second appearance is encountered while
 * scanning from left to right.
 *
 * Example 2:
 * Input: votes = ["north", "south", "east", "west"]
 * Output: ""
 * Explanation: No vote appears more than once, so return an empty string.
 */

public class Solution {

    /**
     * Finds the first repeated vote while scanning from left to right.
     *
     * The idea is:
     * 1. Keep a set of votes we have already seen.
     * 2. For each vote in order:
     *    - If it is already in the set, then this is the first vote whose repeated
     *      occurrence we have encountered, so return it immediately.
     *    - Otherwise, add it to the set and continue.
     * 3. If we finish scanning the entire array without finding any repeated vote,
     *    return the empty string.
     *
     * @param votes the array of vote strings in the order they were received
     * @return the first vote whose second occurrence appears earliest; otherwise ""
     *
     * Time complexity: O(n) average, where n is the number of votes, because each
     * vote is checked/inserted into the hash set in average O(1) time.
     *
     * Space complexity: O(n) in the worst case, if all votes are distinct and all
     * of them must be stored in the hash set.
     */
    public String firstRepeatedVote(String[] votes) {
        // This set stores every vote value we have already encountered.
        // HashSet gives average O(1) lookup and insertion, which is exactly
        // what we want for a one-pass solution.
        Set<String> seenVotes = new HashSet<>();

        // We scan from left to right because the problem defines the answer
        // based on the earliest second occurrence encountered in the stream.
        for (String vote : votes) {
            // Step 1:
            // Check whether this vote has already been seen before.
            // If yes, then this current position is the second (or later) occurrence
            // of this vote, and because we are scanning in order, this is the first
            // repeated vote we should return.
            if (seenVotes.contains(vote)) {
                return vote;
            }

            // Step 2:
            // If the vote has not been seen before, record it in the set so that
            // if it appears again later, we can detect the repetition immediately.
            seenVotes.add(vote);
        }

        // If we reach this point, no vote ever appeared more than once.
        return "";
    }

    /**
     * Helper method to print an array of votes in a readable format.
     *
     * @param votes the array of vote strings to display
     * @return a string representation of the array
     *
     * Time complexity: O(n), where n is the number of votes.
     *
     * Space complexity: O(n), due to building the output string.
     */
    public String formatVotes(String[] votes) {
        return Arrays.toString(votes);
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and a few additional test cases.
     *
     * @param args command-line arguments; not used in this program
     * @return nothing
     *
     * Time complexity: O(1) for the fixed demonstration calls, excluding the cost
     * of each individual algorithm execution.
     *
     * Space complexity: O(1), excluding the space used by the algorithm itself.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample test case 1:
        // votes = ["red", "blue", "green", "blue", "red"]
        //
        // Detailed trace:
        // - See "red"   -> not seen before, add it
        // - See "blue"  -> not seen before, add it
        // - See "green" -> not seen before, add it
        // - See "blue"  -> already seen, so answer is "blue"
        //
        // This matches the expected output from the problem statement.
        String[] votes1 = {"red", "blue", "green", "blue", "red"};
        System.out.println("Input:  " + solution.formatVotes(votes1));
        System.out.println("Output: " + solution.firstRepeatedVote(votes1));
        System.out.println("Expected: blue");
        System.out.println();

        // Sample test case 2:
        // votes = ["north", "south", "east", "west"]
        //
        // Detailed trace:
        // - "north" -> add
        // - "south" -> add
        // - "east"  -> add
        // - "west"  -> add
        // No repeated vote is found, so answer is ""
        //
        // This matches the expected output from the problem statement.
        String[] votes2 = {"north", "south", "east", "west"};
        System.out.println("Input:  " + solution.formatVotes(votes2));
        System.out.println("Output: " + "\"" + solution.firstRepeatedVote(votes2) + "\"");
        System.out.println("Expected: \"\"");
        System.out.println();

        // Additional test case:
        // The first repeated vote is detected as soon as its second occurrence appears.
        String[] votes3 = {"a", "b", "c", "a", "b"};
        System.out.println("Input:  " + solution.formatVotes(votes3));
        System.out.println("Output: " + solution.firstRepeatedVote(votes3));
        System.out.println("Expected: a");
        System.out.println();

        // Additional test case:
        // Immediate repetition.
        String[] votes4 = {"yes", "yes", "no"};
        System.out.println("Input:  " + solution.formatVotes(votes4));
        System.out.println("Output: " + solution.firstRepeatedVote(votes4));
        System.out.println("Expected: yes");
        System.out.println();

        // Additional test case:
        // Only one element, so no repetition is possible.
        String[] votes5 = {"single"};
        System.out.println("Input:  " + solution.formatVotes(votes5));
        System.out.println("Output: " + "\"" + solution.firstRepeatedVote(votes5) + "\"");
        System.out.println("Expected: \"\"");
    }
}