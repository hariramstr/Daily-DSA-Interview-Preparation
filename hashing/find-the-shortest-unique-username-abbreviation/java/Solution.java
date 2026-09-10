import java.util.*;

/*
Problem Title: Find the Shortest Unique Username Abbreviation

Problem Description:
You are given an array of distinct lowercase usernames and an integer index p pointing to one target username.
A valid abbreviation of a username is formed by keeping a non-empty prefix of the username and replacing the
remaining suffix with '*'. For example, the username "marina" can produce "m*", "ma*", "mar*", "mari*",
and "marin*". The full username without '*' is also allowed and is considered an abbreviation of length equal
to the whole word.

Your task is to return the shortest valid abbreviation of usernames[p] that does not match the abbreviation of
any other username in the array. Two abbreviations match if their resulting strings are exactly equal. If multiple
shortest answers exist, return the lexicographically smallest one, although for this abbreviation rule the answer
is usually unique.

This problem models generating compact but unambiguous user labels in a system. An efficient solution should avoid
comparing the target against every possible prefix of every string repeatedly, and instead use hashing or frequency
counting over prefixes.

Constraints:
- 1 <= usernames.length <= 2 * 10^5
- 1 <= usernames[i].length <= 10^5
- Sum of all username lengths does not exceed 2 * 10^5
- All usernames contain only lowercase English letters
- All usernames are distinct
- 0 <= p < usernames.length

Example 1:
Input: usernames = ["marina","mark","mason","mila"], p = 0
Output: "mari*"
Explanation: "m*" matches several usernames, "ma*" matches "marina", "mark", and "mason", "mar*" matches
"marina" and "mark", but "mari*" matches only "marina".

Example 2:
Input: usernames = ["zoe","zora","zack","amy"], p = 3
Output: "a*"
Explanation: No other username starts with "a", so the shortest unique abbreviation is "a*".
*/

public class Solution {

    /**
     * Finds the shortest valid abbreviation of usernames[p] that is unique among all usernames.
     *
     * Core idea:
     * 1. Count how many usernames share each possible prefix.
     * 2. For the target username, scan its prefixes from shortest to longest.
     * 3. The first prefix whose count is exactly 1 is the shortest unique prefix.
     * 4. Convert that prefix into the required abbreviation form:
     *    - If the prefix is shorter than the full word, return prefix + "*"
     *    - Otherwise, return the full word itself
     *
     * Why this works:
     * - An abbreviation based on prefix length k is unique exactly when no other username has the same first k characters.
     * - Therefore, the shortest unique abbreviation corresponds to the shortest prefix of the target that appears in exactly one username.
     * - If every proper prefix is shared by some other username, then the full username is always unique because all usernames are distinct.
     *
     * @param usernames the array of distinct lowercase usernames
     * @param p the index of the target username
     * @return the shortest unique abbreviation for usernames[p]
     *
     * Time complexity:
     * O(S), where S is the sum of lengths of all usernames.
     *
     * Space complexity:
     * O(S), for storing all prefix counts.
     */
    public String shortestUniqueAbbreviation(String[] usernames, int p) {
        // This map stores:
        // key   = a prefix string such as "m", "ma", "mar"
        // value = how many usernames contain that exact prefix
        //
        // Example:
        // usernames = ["marina", "mark", "mason"]
        // Then:
        // "m"   -> 3
        // "ma"  -> 3
        // "mar" -> 2
        // "mari"-> 1
        // "mas" -> 1
        Map<String, Integer> prefixCount = buildPrefixFrequencyMap(usernames);

        // The target username for which we need the shortest unique abbreviation.
        String target = usernames[p];

        // We now test prefixes of the target from shortest to longest.
        //
        // If prefixCount.get(prefix) == 1, then only the target has that prefix,
        // so prefix + "*" is a unique abbreviation (unless prefix is the full word).
        //
        // Because we scan from shortest to longest, the first such prefix gives
        // the shortest valid unique abbreviation.
        for (int len = 1; len <= target.length(); len++) {
            String prefix = target.substring(0, len);
            int count = prefixCount.getOrDefault(prefix, 0);

            // If exactly one username has this prefix, it must be the target itself.
            if (count == 1) {
                // If the prefix is the entire word, the allowed abbreviation is the full word.
                if (len == target.length()) {
                    return target;
                }

                // Otherwise, replace the remaining suffix with '*'.
                return prefix + "*";
            }
        }

        // This line should never be reached because:
        // - all usernames are distinct
        // - therefore the full username itself must be unique
        //
        // Still, returning the full target is a safe fallback.
        return target;
    }

    /**
     * Builds a frequency map of all non-empty prefixes across all usernames.
     *
     * Example:
     * For "marina", we add:
     * "m", "ma", "mar", "mari", "marin", "marina"
     *
     * For every username, we generate all of its prefixes and count them.
     * This allows us to later answer:
     * "How many usernames share this prefix?"
     *
     * @param usernames the array of usernames
     * @return a map from prefix string to the number of usernames having that prefix
     *
     * Time complexity:
     * O(S), where S is the sum of lengths of all usernames.
     *
     * Space complexity:
     * O(S), because the total number of prefixes stored is proportional to the total input size.
     */
    public Map<String, Integer> buildPrefixFrequencyMap(String[] usernames) {
        Map<String, Integer> prefixCount = new HashMap<>();

        // Process each username independently.
        for (String username : usernames) {
            // We build prefixes incrementally using StringBuilder.
            // This is beginner-friendly and avoids repeatedly recomputing characters manually.
            StringBuilder prefixBuilder = new StringBuilder();

            // Add prefixes of lengths 1, 2, 3, ..., username.length()
            for (int i = 0; i < username.length(); i++) {
                prefixBuilder.append(username.charAt(i));
                String prefix = prefixBuilder.toString();

                // Increase the count for this prefix.
                prefixCount.put(prefix, prefixCount.getOrDefault(prefix, 0) + 1);
            }
        }

        return prefixCount;
    }

    /**
     * A helper method that runs one demonstration case and prints the result.
     *
     * @param usernames the array of usernames
     * @param p the target index
     * @return the computed abbreviation so callers can also reuse the result if desired
     *
     * Time complexity:
     * O(S), where S is the sum of lengths of all usernames.
     *
     * Space complexity:
     * O(S), for the prefix frequency map.
     */
    public String demonstrateCase(String[] usernames, int p) {
        String result = shortestUniqueAbbreviation(usernames, p);
        System.out.println("Usernames: " + Arrays.toString(usernames));
        System.out.println("Target index p: " + p);
        System.out.println("Target username: " + usernames[p]);
        System.out.println("Shortest unique abbreviation: " + result);
        System.out.println();
        return result;
    }

    /**
     * Main method demonstrating the solution on the sample inputs from the problem statement.
     *
     * Verified examples:
     * 1) ["marina","mark","mason","mila"], p = 0
     *    Prefix counts for "marina":
     *    "m"    -> shared
     *    "ma"   -> shared
     *    "mar"  -> shared with "mark"
     *    "mari" -> unique
     *    Answer = "mari*"
     *
     * 2) ["zoe","zora","zack","amy"], p = 3
     *    Prefix "a" is unique immediately
     *    Answer = "a*"
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity:
     * O(S) per demonstration case.
     *
     * Space complexity:
     * O(S) per demonstration case.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Sample 1 from the problem statement.
        String[] usernames1 = {"marina", "mark", "mason", "mila"};
        solution.demonstrateCase(usernames1, 0); // Expected: mari*

        // Sample 2 from the problem statement.
        String[] usernames2 = {"zoe", "zora", "zack", "amy"};
        solution.demonstrateCase(usernames2, 3); // Expected: a*

        // Additional small sanity checks.

        // If the first character is already unique, answer is that character plus '*'.
        String[] usernames3 = {"bob", "alice", "carol"};
        solution.demonstrateCase(usernames3, 1); // Expected: a*

        // If all proper prefixes are shared, the full word may be required.
        String[] usernames4 = {"abc", "abcd"};
        solution.demonstrateCase(usernames4, 0); // Expected: abc

        // Another case where a middle-length prefix becomes unique.
        String[] usernames5 = {"stone", "storm", "stack", "apple"};
        solution.demonstrateCase(usernames5, 1); // "storm" -> "stor*"
    }
}