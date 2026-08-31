import java.util.*;

/*
 * Title: Find Players With Reordered Card Histories
 * Difficulty: Medium
 * Topic: Hashing
 *
 * Problem Description:
 * You are given the game histories of several players in an online card platform.
 * Each player history is represented by a list of card IDs in the exact order they
 * were drawn during a match. Two players are considered to have a reordered-equivalent
 * history if their histories contain the same multiset of card IDs, even if the draw
 * order is different.
 *
 * For example:
 * [4, 9, 4, 2] and [2, 4, 9, 4] are equivalent,
 * but [4, 9, 2] is not equivalent to [4, 9, 4, 2] because the frequencies differ.
 *
 * Your task is to return all player IDs that belong to at least one reordered-equivalent
 * group. The result should be sorted in increasing order of player ID.
 *
 * Player IDs are 0-indexed based on their position in the input array. Each history may
 * have a different length. A player belongs to a reordered-equivalent group if there exists
 * at least one other player with exactly the same card-frequency profile.
 *
 * A good solution should avoid comparing every pair of histories directly. Think about how
 * to build a canonical signature for each history and use hashing to group equivalent players
 * efficiently.
 *
 * Constraints:
 * - 1 <= histories.length <= 100000
 * - 0 <= histories[i].length <= 100000
 * - 0 <= card IDs <= 1000000000
 * - The sum of all history lengths across all players does not exceed 200000
 * - Return the player IDs in sorted ascending order
 *
 * Example 1:
 * Input: histories = [[4,9,4,2],[2,4,9,4],[7,7,1],[1,7,7],[3,5]]
 * Output: [0,1,2,3]
 * Explanation:
 * Players 0 and 1 have the same card counts.
 * Players 2 and 3 also have the same card counts.
 * Player 4 does not match anyone.
 *
 * Example 2:
 * Input: histories = [[1,2,3],[3,2,1,1],[],[5,5],[]]
 * Output: [2,4]
 * Explanation:
 * Player 2 and player 4 both have empty histories, so they form a reordered-equivalent group.
 * The other players do not share the same card-frequency profile with any other player.
 */

public class Solution {

    /**
     * Finds all player IDs whose histories belong to at least one reordered-equivalent group.
     *
     * The key idea is:
     * 1. For each player's history, count the frequency of each card ID.
     * 2. Convert that frequency map into a canonical string signature.
     *    - Two histories that have the same multiset of values will produce the same signature.
     *    - Histories with different frequencies or different card IDs will produce different signatures.
     * 3. Group player IDs by this signature using a hash map.
     * 4. Any signature with at least two player IDs represents a reordered-equivalent group.
     * 5. Collect all such player IDs and return them in ascending order.
     *
     * @param histories a 2D array where histories[i] is the draw history of player i
     * @return a list of player IDs that belong to at least one reordered-equivalent group, sorted ascending
     * Time complexity: O(T log T) in total, where T is the total number of card entries across all histories.
     * More precisely, each history of length k is processed in O(k + u log u), where u is the number of distinct cards in that history.
     * Space complexity: O(T) in the worst case for frequency maps, signatures, and grouping storage.
     */
    public List<Integer> findPlayersWithReorderedCardHistories(int[][] histories) {
        // This map groups player IDs by their canonical frequency signature.
        // Key   -> signature representing the multiset of card IDs in a history
        // Value -> list of player IDs that share that exact signature
        Map<String, List<Integer>> groups = new HashMap<>();

        // Process each player's history one by one.
        for (int playerId = 0; playerId < histories.length; playerId++) {
            int[] history = histories[playerId];

            // Build a canonical signature for this player's history.
            String signature = buildSignature(history);

            // Insert the player into the group for this signature.
            groups.computeIfAbsent(signature, k -> new ArrayList<>()).add(playerId);
        }

        // This list will store all player IDs that belong to a group of size >= 2.
        List<Integer> result = new ArrayList<>();

        // Go through every signature group.
        for (List<Integer> playerIds : groups.values()) {
            // If at least two players share the same signature,
            // then every player in this group should be included.
            if (playerIds.size() >= 2) {
                result.addAll(playerIds);
            }
        }

        // The problem requires the final result in increasing order of player ID.
        Collections.sort(result);
        return result;
    }

    /**
     * Builds a canonical signature for one history based on card frequencies.
     *
     * Why this works:
     * - Order does not matter for equivalence.
     * - Frequency does matter.
     * Therefore, we count how many times each card appears, then serialize the
     * sorted (card, count) pairs into a unique string.
     *
     * Example:
     * history = [4, 9, 4, 2]
     * frequency map = {4=2, 9=1, 2=1}
     * sorted keys = [2, 4, 9]
     * signature = "2#1|4#2|9#1|"
     *
     * Another history [2, 4, 9, 4] produces the exact same signature.
     *
     * Empty history:
     * - It produces an empty signature "".
     * - Therefore, all empty histories are grouped together correctly.
     *
     * @param history the card history of one player
     * @return a canonical string signature representing the multiset of card IDs
     * Time complexity: O(k + u log u), where k is history length and u is number of distinct card IDs
     * Space complexity: O(u) for the frequency map and sorted key list
     */
    public String buildSignature(int[] history) {
        // First, count how many times each card ID appears.
        Map<Integer, Integer> frequency = new HashMap<>();
        for (int card : history) {
            frequency.put(card, frequency.getOrDefault(card, 0) + 1);
        }

        // To make the signature canonical, we must output entries in a fixed order.
        // Sorting the distinct card IDs guarantees that equivalent histories produce
        // exactly the same serialized representation.
        List<Integer> cards = new ArrayList<>(frequency.keySet());
        Collections.sort(cards);

        // Build the signature carefully.
        // We use separators to avoid ambiguity.
        // Example:
        // card=12, count=3 becomes "12#3|"
        StringBuilder signature = new StringBuilder();
        for (int card : cards) {
            signature.append(card)
                     .append('#')
                     .append(frequency.get(card))
                     .append('|');
        }

        return signature.toString();
    }

    /**
     * Utility method to print a list of integers in a clean format.
     *
     * @param values the list to print
     * @return a string representation of the list
     * Time complexity: O(n)
     * Space complexity: O(n) due to string construction
     */
    public String listToString(List<Integer> values) {
        return values.toString();
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * It also prints the expected outputs so the behavior can be visually verified.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input size for the demo cases)
     * Space complexity: O(total input size for the demo cases)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[][] histories1 = {
            {4, 9, 4, 2},
            {2, 4, 9, 4},
            {7, 7, 1},
            {1, 7, 7},
            {3, 5}
        };

        List<Integer> result1 = solution.findPlayersWithReorderedCardHistories(histories1);
        System.out.println("Example 1 Output:   " + solution.listToString(result1));
        System.out.println("Example 1 Expected: [0, 1, 2, 3]");

        // Example 2
        int[][] histories2 = {
            {1, 2, 3},
            {3, 2, 1, 1},
            {},
            {5, 5},
            {}
        };

        List<Integer> result2 = solution.findPlayersWithReorderedCardHistories(histories2);
        System.out.println("Example 2 Output:   " + solution.listToString(result2));
        System.out.println("Example 2 Expected: [2, 4]");

        // Additional quick sanity check:
        // Players 0 and 2 are equivalent, player 1 is not.
        int[][] histories3 = {
            {10, 20, 10},
            {10, 20},
            {20, 10, 10}
        };

        List<Integer> result3 = solution.findPlayersWithReorderedCardHistories(histories3);
        System.out.println("Extra Example Output:   " + solution.listToString(result3));
        System.out.println("Extra Example Expected: [0, 2]");
    }
}