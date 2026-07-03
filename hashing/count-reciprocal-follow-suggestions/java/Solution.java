import java.util.*;

/*
Problem Title: Count Reciprocal Follow Suggestions

Problem Description:
You are given a list of directed follow relationships in a social platform. Each relationship
is represented as a pair [a, b], meaning user a follows user b. The platform wants to suggest
users who should follow each other back. A reciprocal follow suggestion exists for a pair of
distinct users (u, v) if exactly one of the two directed relationships exists: either u follows
v or v follows u, but not both.

Your task is to count how many unordered user pairs have exactly one directed follow relationship
between them.

Each unordered pair of users should be counted at most once. Duplicate input relationships may
appear and should be treated as a single follow relationship. Self-follows of the form [x, x]
should be ignored completely, since a user cannot be suggested to follow themselves back.

Return the total number of reciprocal follow suggestions.

This problem is designed to test careful use of hashing to deduplicate edges and normalize
unordered pairs while tracking direction. An efficient solution should avoid comparing every
pair of users.

Constraints:
- 1 <= relationships.length <= 200000
- relationships[i].length == 2
- 1 <= a, b <= 10^9
- Duplicate relationships may appear many times
- Self-follows may appear in the input

Example 1:
Input: relationships = [[1,2],[2,1],[1,3],[4,5],[4,5],[6,7]]
Output: 3

Explanation:
- Users 1 and 2 follow each other, so they do not need a suggestion.
- Users 1 and 3 have only 1 -> 3, so this contributes 1.
- Users 4 and 5 have only 4 -> 5, duplicates do not change the count, so this contributes 1.
- Users 6 and 7 have only 6 -> 7, so this contributes 1.
Total = 3.

Example 2:
Input: relationships = [[10,20],[20,30],[30,20],[40,40],[50,60],[60,50],[70,80],[80,90]]
Under the clarified rule in the statement, after deduplication and ignoring self-follows:
- (10,20) has exactly one direction
- (20,30) has both directions
- (50,60) has both directions
- (70,80) has exactly one direction
- (80,90) has exactly one direction
So the correct result is 3.

Implementation note:
Deduplicate directed edges first, then group by unordered user pair and count whether that pair
has one or two directions present.
*/

public class Solution {

    /**
     * Counts how many unordered user pairs have exactly one unique directed follow relationship.
     *
     * The method follows the problem requirements exactly:
     * 1. Ignore self-follows [x, x].
     * 2. Deduplicate directed edges, so repeated [a, b] entries count only once.
     * 3. For each unordered pair {u, v}, determine whether:
     *    - only u -> v exists, or
     *    - only v -> u exists, or
     *    - both directions exist.
     * 4. Count the unordered pairs where exactly one direction exists.
     *
     * @param relationships the list of directed follow relationships, where each element is [a, b]
     * @return the number of unordered user pairs with exactly one directed edge after deduplication
     * Time complexity: O(n), where n is the number of input relationships on average with hashing
     * Space complexity: O(n), for storing unique directed edges and unordered pair direction states
     */
    public int countReciprocalFollowSuggestions(int[][] relationships) {
        /*
         * We use two hash-based structures:
         *
         * 1. directedEdges:
         *    Stores each unique directed edge (a -> b).
         *    This removes duplicates such as repeated [4,5].
         *
         * 2. pairState:
         *    Maps each unordered pair {min(a,b), max(a,b)} to a small bitmask describing
         *    which direction(s) exist.
         *
         *    For an unordered pair (small, large):
         *    - bit 1 means small -> large exists
         *    - bit 2 means large -> small exists
         *
         *    Therefore:
         *    - state 1 => only small -> large exists
         *    - state 2 => only large -> small exists
         *    - state 3 => both directions exist
         *
         * At the end, we count how many pairs have state 1 or state 2.
         */

        Set<Long> directedEdges = new HashSet<>();
        Map<Long, Integer> pairState = new HashMap<>();

        for (int[] relationship : relationships) {
            int from = relationship[0];
            int to = relationship[1];

            // Self-follows are ignored completely.
            if (from == to) {
                continue;
            }

            // Encode the directed edge uniquely as a long so it can be deduplicated in a HashSet.
            long directedKey = encodePair(from, to);

            // If this exact directed edge was already seen, skip it.
            if (!directedEdges.add(directedKey)) {
                continue;
            }

            // Normalize the unordered pair as (small, large).
            int small = Math.min(from, to);
            int large = Math.max(from, to);

            // Encode the unordered pair.
            long unorderedKey = encodePair(small, large);

            /*
             * Determine which direction this unique directed edge represents relative to (small, large):
             *
             * If from == small and to == large, then direction is small -> large => bit 1.
             * Otherwise, it must be large -> small => bit 2.
             */
            int directionBit = (from == small) ? 1 : 2;

            // Merge this direction into the existing state for the unordered pair.
            int currentState = pairState.getOrDefault(unorderedKey, 0);
            pairState.put(unorderedKey, currentState | directionBit);
        }

        int suggestions = 0;

        // Count unordered pairs where exactly one direction exists.
        for (int state : pairState.values()) {
            if (state == 1 || state == 2) {
                suggestions++;
            }
        }

        return suggestions;
    }

    /**
     * Encodes two non-negative int values into one long value.
     *
     * This is a common hashing trick for pairs of integers:
     * - the first integer is stored in the high 32 bits
     * - the second integer is stored in the low 32 bits
     *
     * Because user IDs are within int range here, this encoding is collision-free for ordered pairs.
     *
     * @param first the first integer in the pair
     * @param second the second integer in the pair
     * @return a unique long encoding of the ordered pair (first, second)
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long encodePair(int first, int second) {
        return (((long) first) << 32) | (second & 0xffffffffL);
    }

    /**
     * Helper method to print a 2D array in a readable format.
     *
     * @param relationships the 2D array of relationships
     * @return a string representation of the relationships array
     * Time complexity: O(n)
     * Space complexity: O(n), due to string construction
     */
    public String relationshipsToString(int[][] relationships) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < relationships.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(Arrays.toString(relationships[i]));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Demonstrates the solution on sample inputs from the problem statement and prints results.
     *
     * This main method also verifies the clarified interpretation for Example 2:
     * after deduplication and ignoring self-follows, the correct answer is 3.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(total input size of demonstrated examples)
     * Space complexity: O(total unique relationships in demonstrated examples)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[][] example1 = {
            {1, 2},
            {2, 1},
            {1, 3},
            {4, 5},
            {4, 5},
            {6, 7}
        };

        int[][] example2 = {
            {10, 20},
            {20, 30},
            {30, 20},
            {40, 40},
            {50, 60},
            {60, 50},
            {70, 80},
            {80, 90}
        };

        int[][] additionalExample = {
            {1, 1},   // ignored
            {1, 2},   // unique
            {1, 2},   // duplicate ignored
            {2, 1},   // now both directions exist for pair (1,2)
            {3, 4},   // unique
            {5, 6},   // unique
            {6, 5},   // both directions exist for pair (5,6)
            {7, 8}    // unique
        };

        System.out.println("Example 1 relationships: " + solution.relationshipsToString(example1));
        System.out.println("Example 1 result: " + solution.countReciprocalFollowSuggestions(example1));
        System.out.println("Expected: 3");
        System.out.println();

        System.out.println("Example 2 relationships: " + solution.relationshipsToString(example2));
        System.out.println("Example 2 result: " + solution.countReciprocalFollowSuggestions(example2));
        System.out.println("Expected: 3");
        System.out.println();

        System.out.println("Additional example relationships: " + solution.relationshipsToString(additionalExample));
        System.out.println("Additional example result: " + solution.countReciprocalFollowSuggestions(additionalExample));
        System.out.println("Expected: 2");
    }
}