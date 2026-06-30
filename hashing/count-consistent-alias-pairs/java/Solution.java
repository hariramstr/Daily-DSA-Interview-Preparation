import java.util.*;

/*
 * Title: Count Consistent Alias Pairs
 * Difficulty: Medium
 * Topic: Hashing
 *
 * Problem Description:
 * A messaging platform stores user aliases as lowercase strings. Two aliases are considered
 * consistent if they use exactly the same set of distinct characters, regardless of order
 * or how many times each character appears.
 *
 * For example:
 * - "abbca" and "cba" are consistent because both contain the character set {a, b, c}
 * - "aabc" and "abdd" are not consistent
 *
 * Given an array aliases, return the number of index pairs (i, j) such that:
 * 0 <= i < j < aliases.length
 * and aliases[i] is consistent with aliases[j].
 *
 * The goal is to design an efficient solution for large inputs. Instead of comparing every
 * pair of strings directly, convert each alias into a canonical hashed representation of its
 * distinct characters, then count how many previous aliases share the same representation.
 *
 * Constraints:
 * - 1 <= aliases.length <= 100000
 * - 1 <= aliases[i].length <= 100
 * - aliases[i] consists only of lowercase English letters
 *
 * Example 1:
 * Input: aliases = ["abbca", "cba", "aaaa", "a", "bac", "xy", "yx"]
 * Output: 5
 * Explanation:
 * - "abbca", "cba", and "bac" all map to {a,b,c}, producing 3 pairs.
 * - "aaaa" and "a" both map to {a}, producing 1 pair.
 * - "xy" and "yx" both map to {x,y}, producing 1 pair.
 * Total = 3 + 1 + 1 = 5.
 *
 * Example 2:
 * Input: aliases = ["abc", "de", "eed", "fff", "fed", "cab", "xyz"]
 * Output: 2
 * Explanation:
 * - "abc" and "cab" are consistent.
 * - "de" and "eed" are consistent.
 * - "fed" is not consistent with either because its set is {d,e,f}.
 * So the answer is 2.
 */

public class Solution {

    /**
     * Counts how many index pairs (i, j) have aliases with exactly the same set of distinct characters.
     *
     * The key idea:
     * Each alias is converted into a compact canonical representation using a 26-bit bitmask.
     * - Bit 0 represents 'a'
     * - Bit 1 represents 'b'
     * - ...
     * - Bit 25 represents 'z'
     *
     * If a character appears at least once in the alias, its bit is turned on.
     * Because repeated characters do not matter, this bitmask perfectly represents the set
     * of distinct characters in the alias.
     *
     * Then:
     * - We keep a frequency map from bitmask -> how many previous aliases had this same bitmask
     * - For each new alias, if its bitmask has already appeared k times, then it forms exactly
     *   k new valid pairs with those previous aliases
     * - Add k to the answer, then increment the frequency of this bitmask
     *
     * @param aliases the array of lowercase alias strings
     * @return the number of consistent alias pairs
     * Time complexity: O(n * m), where n is the number of aliases and m is the maximum alias length.
     * Since each alias is scanned once, this is efficient for the given constraints.
     * Space complexity: O(u), where u is the number of distinct bitmasks encountered.
     */
    public long countConsistentPairs(String[] aliases) {
        // This map stores:
        // key   = bitmask representing the set of distinct letters in an alias
        // value = how many aliases seen so far have exactly that same bitmask
        Map<Integer, Integer> frequencyMap = new HashMap<>();

        // We use long because the number of pairs can be large.
        // In the worst case, if all aliases are consistent with each other,
        // the number of pairs is n * (n - 1) / 2, which can exceed int range.
        long pairCount = 0L;

        // Process aliases from left to right.
        // For each alias:
        // 1. Build its bitmask
        // 2. See how many previous aliases had the same bitmask
        // 3. Add that count to the answer
        // 4. Record this alias in the map
        for (String alias : aliases) {
            int mask = buildCharacterSetMask(alias);

            // If this mask has appeared before, every previous occurrence forms
            // one valid pair with the current alias.
            int previousCount = frequencyMap.getOrDefault(mask, 0);
            pairCount += previousCount;

            // Now include the current alias in the frequency map for future aliases.
            frequencyMap.put(mask, previousCount + 1);
        }

        return pairCount;
    }

    /**
     * Builds a 26-bit integer mask representing the set of distinct lowercase letters in the alias.
     *
     * Example:
     * - "abbca"
     *   distinct letters are {a, b, c}
     *   mask will have bits for a, b, c set to 1
     *
     * Repeated letters do not change the result because setting the same bit again
     * leaves it unchanged.
     *
     * Detailed process:
     * - Start with mask = 0
     * - For each character ch in the string:
     *   - Compute its zero-based position: ch - 'a'
     *   - Create a bit value: 1 << position
     *   - Turn that bit on in mask using bitwise OR
     *
     * @param alias the input alias consisting only of lowercase English letters
     * @return an integer bitmask representing the set of distinct characters in the alias
     * Time complexity: O(m), where m is the length of the alias
     * Space complexity: O(1)
     */
    public int buildCharacterSetMask(String alias) {
        // Start with no letters present.
        int mask = 0;

        // Scan every character in the alias.
        for (int i = 0; i < alias.length(); i++) {
            char ch = alias.charAt(i);

            // Convert character to an index from 0 to 25.
            // Example:
            // 'a' -> 0
            // 'b' -> 1
            // 'z' -> 25
            int bitIndex = ch - 'a';

            // Create a number with only that bit turned on.
            // Example:
            // if bitIndex = 0, then bit = 000...0001
            // if bitIndex = 1, then bit = 000...0010
            // if bitIndex = 2, then bit = 000...0100
            int bit = 1 << bitIndex;

            // Turn on this bit in the mask.
            // If it was already on because the character appeared before,
            // the mask remains unchanged.
            mask |= bit;
        }

        return mask;
    }

    /**
     * Runs a sample test, prints the aliases, the expected result, and the actual result.
     *
     * @param aliases the sample input array
     * @param expected the expected number of consistent pairs
     * @return nothing
     * Time complexity: O(n * m), because it calls the main counting method once
     * Space complexity: O(u), where u is the number of distinct masks
     */
    public void runDemo(String[] aliases, long expected) {
        long actual = countConsistentPairs(aliases);
        System.out.println("Aliases: " + Arrays.toString(aliases));
        System.out.println("Expected: " + expected);
        System.out.println("Actual:   " + actual);
        System.out.println();
    }

    /**
     * Main method to demonstrate the solution using the sample inputs from the problem statement.
     *
     * It also verifies the outputs by printing both expected and actual values.
     *
     * Example 1 trace:
     * - "abbca" -> {a,b,c}
     * - "cba"   -> {a,b,c} => 1 pair
     * - "aaaa"  -> {a}
     * - "a"     -> {a}     => 1 more pair
     * - "bac"   -> {a,b,c} => forms 2 more pairs with earlier {a,b,c} aliases
     * - "xy"    -> {x,y}
     * - "yx"    -> {x,y}   => 1 more pair
     * Total = 5
     *
     * Example 2 trace:
     * - "abc" -> {a,b,c}
     * - "de"  -> {d,e}
     * - "eed" -> {d,e}     => 1 pair
     * - "fff" -> {f}
     * - "fed" -> {d,e,f}
     * - "cab" -> {a,b,c}   => 1 pair
     * - "xyz" -> {x,y,z}
     * Total = 2
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n * m) for each demonstration call
     * Space complexity: O(u)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String[] aliases1 = {"abbca", "cba", "aaaa", "a", "bac", "xy", "yx"};
        long expected1 = 5L;

        String[] aliases2 = {"abc", "de", "eed", "fff", "fed", "cab", "xyz"};
        long expected2 = 2L;

        solution.runDemo(aliases1, expected1);
        solution.runDemo(aliases2, expected2);
    }
}