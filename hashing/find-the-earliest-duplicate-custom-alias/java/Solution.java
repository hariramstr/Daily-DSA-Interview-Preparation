import java.util.*;

/*
Problem Title: Find the Earliest Duplicate Custom Alias

Problem Description:
A messaging platform lets users define custom aliases for channels. Two aliases are considered equivalent if, after normalizing them, they become identical. Normalization follows these rules: convert all uppercase letters to lowercase, remove every hyphen '-' and underscore '_', and keep all other characters unchanged. Given a list of aliases in the order they were created, return the index of the first alias that is equivalent to any earlier alias after normalization. If no such alias exists, return -1.

Your task is to detect the earliest duplicate by creation time, not the earliest original alias it matches. In other words, scan the list from left to right and return the first position i such that the normalized form of aliases[i] has already appeared among aliases[0...i-1].

Implement a function that solves this efficiently for large inputs.

Constraints:
- 1 <= aliases.length <= 200000
- 1 <= aliases[i].length <= 100
- aliases[i] consists of English letters, digits, hyphens '-', underscores '_', and periods '.'
- The answer should be computed in O(total input size) expected time using hashing

Example 1:
Input: aliases = ["Team-Chat", "alerts", "team_chat", "team.chat"]
Output: 2
Explanation: "Team-Chat" normalizes to "teamchat". "team_chat" also normalizes to "teamchat", so index 2 is the first duplicate.

Example 2:
Input: aliases = ["build.v1", "build_v1", "BUILD-V2", "buildv2"]
Output: 3
Explanation: "build.v1" normalizes to "build.v1" because periods are kept. "build_v1" normalizes to "buildv1", so it is not a duplicate. "BUILD-V2" normalizes to "buildv2", and "buildv2" also normalizes to "buildv2", making index 3 the first duplicate.
*/

public class Solution {

    /**
     * Finds the earliest index i such that the normalized form of aliases[i]
     * has already appeared among aliases[0..i-1].
     *
     * The algorithm scans from left to right exactly once:
     * 1. Normalize the current alias.
     * 2. Check whether that normalized form has already been seen.
     * 3. If yes, return the current index immediately because this is the earliest
     *    duplicate by creation time.
     * 4. Otherwise, store the normalized form and continue.
     *
     * @param aliases the array of aliases in creation order
     * @return the earliest index whose normalized alias matches an earlier one; -1 if none exists
     * Time complexity: O(total input size) expected, because each character is processed once during normalization and each hash set operation is expected O(1)
     * Space complexity: O(total normalized input size) in the worst case for storing distinct normalized aliases
     */
    public int findEarliestDuplicateAlias(String[] aliases) {
        // A HashSet stores every normalized alias we have seen so far.
        // Why a set?
        // Because we only need to know whether a normalized alias has appeared before.
        // We do NOT need counts, and we do NOT need the exact earlier index.
        Set<String> seen = new HashSet<>();

        // We scan from left to right because the problem asks for the earliest duplicate
        // by creation time. The first time we detect a repeated normalized alias,
        // that index is the correct answer.
        for (int i = 0; i < aliases.length; i++) {
            // Normalize the current alias according to the rules:
            // - uppercase letters become lowercase
            // - '-' and '_' are removed
            // - all other characters remain unchanged
            String normalized = normalizeAlias(aliases[i]);

            // If this normalized form is already in the set, then some earlier alias
            // normalized to the same value. Since we are scanning in order, this is
            // the earliest duplicate index, so we return immediately.
            if (seen.contains(normalized)) {
                return i;
            }

            // Otherwise, remember this normalized alias for future comparisons.
            seen.add(normalized);
        }

        // If we finish the loop without finding any repeated normalized alias,
        // then no duplicate exists.
        return -1;
    }

    /**
     * Normalizes a single alias using the problem rules:
     * - convert uppercase English letters to lowercase
     * - remove every hyphen '-' and underscore '_'
     * - keep all other characters unchanged
     *
     * Examples:
     * - "Team-Chat" -> "teamchat"
     * - "team_chat" -> "teamchat"
     * - "build.v1" -> "build.v1"
     * - "BUILD-V2" -> "buildv2"
     *
     * @param alias the original alias string
     * @return the normalized alias string
     * Time complexity: O(m), where m is the length of the alias
     * Space complexity: O(m) for the output builder
     */
    public String normalizeAlias(String alias) {
        // StringBuilder is efficient for building strings character by character.
        StringBuilder sb = new StringBuilder(alias.length());

        // Process each character exactly once.
        for (int i = 0; i < alias.length(); i++) {
            char ch = alias.charAt(i);

            // Rule 1: remove hyphens and underscores completely.
            // That means we simply skip them and do not append anything.
            if (ch == '-' || ch == '_') {
                continue;
            }

            // Rule 2: convert uppercase letters to lowercase.
            // We use Character.toLowerCase for clarity and correctness.
            // Digits, periods, and lowercase letters remain unchanged by this call.
            sb.append(Character.toLowerCase(ch));
        }

        // Convert the builder into the final normalized string.
        return sb.toString();
    }

    /**
     * Runs a demonstration of the solution using the sample inputs from the problem statement
     * and prints the results.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total size of demonstrated inputs)
     * Space complexity: O(total distinct normalized aliases in the demonstrated inputs)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1:
        // "Team-Chat" -> "teamchat"
        // "alerts" -> "alerts"
        // "team_chat" -> "teamchat"  => duplicate of index 0, so answer is 2
        // "team.chat" -> "team.chat" => not reached as earliest duplicate already found at 2
        String[] aliases1 = {"Team-Chat", "alerts", "team_chat", "team.chat"};
        int result1 = solution.findEarliestDuplicateAlias(aliases1);
        System.out.println(result1); // Expected: 2

        // Sample 2:
        // "build.v1" -> "build.v1"
        // "build_v1" -> "buildv1"    => not duplicate of "build.v1" because period is kept
        // "BUILD-V2" -> "buildv2"
        // "buildv2" -> "buildv2"     => duplicate of index 2, so answer is 3
        String[] aliases2 = {"build.v1", "build_v1", "BUILD-V2", "buildv2"};
        int result2 = solution.findEarliestDuplicateAlias(aliases2);
        System.out.println(result2); // Expected: 3

        // Additional quick sanity checks for beginners:
        String[] aliases3 = {"alpha", "beta", "gamma"};
        System.out.println(solution.findEarliestDuplicateAlias(aliases3)); // Expected: -1

        String[] aliases4 = {"A_B-C", "abc"};
        System.out.println(solution.findEarliestDuplicateAlias(aliases4)); // Expected: 1
    }
}