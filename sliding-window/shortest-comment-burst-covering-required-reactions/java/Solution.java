import java.util.*;

/*
 * Title: Shortest Comment Burst Covering Required Reactions
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given a chronological stream of reactions on a live video. Each reaction is represented
 * by a string in the array reactions, where reactions[i] is the reaction type posted at second i.
 * You are also given a list required where each element is a pair [type, count], meaning the chosen
 * contiguous time window must contain at least count occurrences of that reaction type. A reaction
 * type may appear many times in the stream, and reaction types not mentioned in required may also
 * appear inside the window.
 *
 * Return the length of the shortest contiguous subarray of reactions that satisfies all required
 * reaction counts. If no such window exists, return -1. If multiple valid windows have the same
 * minimum length, any one of them would be acceptable conceptually, but your function should return
 * only the minimum length.
 *
 * This is a hard sliding window problem because the target is not just to include a set of labels,
 * but to satisfy per-label multiplicity requirements while processing a long stream efficiently.
 * A brute-force check over all subarrays will time out.
 *
 * Constraints:
 * - 1 <= reactions.length <= 200000
 * - 1 <= required.length <= 100000
 * - 1 <= reactions[i].length <= 20
 * - 1 <= required[j][1] <= 10^9
 * - Sum of distinct reaction types in required is at most 100000
 * - Reaction strings consist of lowercase English letters, digits, and underscore
 * - If a reaction type appears multiple times in required, their counts should be added together
 *   before evaluating the answer
 *
 * Example 1:
 * Input:
 * reactions = ["like","wow","sad","like","heart","wow","like","heart"]
 * required = [["like",2],["heart",1],["wow",1]]
 * Output:
 * 4
 * Explanation:
 * The shortest valid window is ["like","heart","wow","like"] from indices 3 to 6.
 * It contains like twice, heart once, and wow once.
 *
 * Example 2:
 * Input:
 * reactions = ["fire","clap","fire","laugh","clap","fire"]
 * required = [["fire",2],["clap",2],["boo",1]]
 * Output:
 * -1
 * Explanation:
 * No window can satisfy the requirement because reaction type "boo" never appears in the stream.
 */

public class Solution {

    /**
     * Finds the length of the shortest contiguous subarray whose reaction counts satisfy
     * all required reaction multiplicities.
     *
     * This overload accepts the required list as a 2D string array, where each row is:
     * required[i][0] = reaction type
     * required[i][1] = required count as a string
     *
     * Example row: {"like", "2"}
     *
     * @param reactions the chronological stream of reactions
     * @param required the required reaction/count pairs, with count stored as a string
     * @return the minimum valid window length, or -1 if no valid window exists
     * Time complexity: O(n + m), where n = reactions.length and m = required.length,
     * plus hash map operations that are O(1) average-case
     * Space complexity: O(k), where k is the number of distinct reaction types appearing
     * in the merged requirement map and/or current window map
     */
    public int shortestCommentBurst(String[] reactions, String[][] required) {
        Map<String, Long> needed = new HashMap<>();

        // Merge duplicate requirement entries by summing their counts.
        // The problem explicitly states that if a reaction type appears multiple times
        // in "required", their counts should be added together before evaluating.
        for (String[] pair : required) {
            String type = pair[0];
            long count = Long.parseLong(pair[1]);
            needed.put(type, needed.getOrDefault(type, 0L) + count);
        }

        return shortestCommentBurstInternal(reactions, needed);
    }

    /**
     * Finds the length of the shortest contiguous subarray whose reaction counts satisfy
     * all required reaction multiplicities.
     *
     * This overload accepts the required list as a list of pairs, where each pair is:
     * pair.get(0) = reaction type
     * pair.get(1) = required count as a string
     *
     * @param reactions the chronological stream of reactions
     * @param requiredPairs list of [type, count] pairs, with count stored as a string
     * @return the minimum valid window length, or -1 if no valid window exists
     * Time complexity: O(n + m), where n = reactions.length and m = requiredPairs.size(),
     * plus hash map operations that are O(1) average-case
     * Space complexity: O(k), where k is the number of distinct reaction types appearing
     * in the merged requirement map and/or current window map
     */
    public int shortestCommentBurst(String[] reactions, List<List<String>> requiredPairs) {
        Map<String, Long> needed = new HashMap<>();

        // Merge duplicate requirement entries by summing their counts.
        for (List<String> pair : requiredPairs) {
            String type = pair.get(0);
            long count = Long.parseLong(pair.get(1));
            needed.put(type, needed.getOrDefault(type, 0L) + count);
        }

        return shortestCommentBurstInternal(reactions, needed);
    }

    /**
     * Core sliding window implementation.
     *
     * The key idea:
     * 1. Build a map of required counts for each reaction type.
     * 2. Expand the right end of the window one reaction at a time.
     * 3. Track counts only for reaction types that matter.
     * 4. Once the current window satisfies every required type, try to shrink it from the left
     *    as much as possible while still remaining valid.
     * 5. Record the smallest valid window length seen.
     *
     * Important correctness detail:
     * We do NOT need to compare the entire window map against the requirement map every time.
     * Instead, we maintain a number called "formed":
     * - formed = how many distinct required reaction types are currently satisfied
     * - A type becomes satisfied exactly when windowCount[type] reaches needed[type]
     * - A type becomes unsatisfied exactly when windowCount[type] drops below needed[type]
     *
     * Then the window is valid if and only if:
     * formed == needed.size()
     *
     * @param reactions the chronological stream of reactions
     * @param needed merged requirement map: reaction type -> required count
     * @return the minimum valid window length, or -1 if no valid window exists
     * Time complexity: O(n), because each index moves into and out of the sliding window at most once
     * Space complexity: O(k), where k is the number of distinct required reaction types
     */
    public int shortestCommentBurstInternal(String[] reactions, Map<String, Long> needed) {
        // Edge case:
        // If there are no requirements, then conceptually the empty window satisfies everything.
        // The problem constraints imply required.length >= 1, but handling this makes the method robust.
        if (needed.isEmpty()) {
            return 0;
        }

        // Optional but very useful early impossibility check:
        // Count total occurrences in the entire stream for only the required reaction types.
        // If the full stream itself does not contain enough of some required type,
        // then no subarray can possibly satisfy the requirement.
        Map<String, Long> totalAvailable = new HashMap<>();
        for (String reaction : reactions) {
            if (needed.containsKey(reaction)) {
                totalAvailable.put(reaction, totalAvailable.getOrDefault(reaction, 0L) + 1L);
            }
        }

        for (Map.Entry<String, Long> entry : needed.entrySet()) {
            String type = entry.getKey();
            long requiredCount = entry.getValue();
            long availableCount = totalAvailable.getOrDefault(type, 0L);

            if (availableCount < requiredCount) {
                return -1;
            }
        }

        // Sliding window count map:
        // stores counts of required reaction types currently inside [left, right].
        Map<String, Long> windowCounts = new HashMap<>();

        // Number of distinct required types currently satisfied.
        int formed = 0;

        // Total number of distinct required types that must be satisfied.
        int requiredDistinctTypes = needed.size();

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far.
        int minLength = Integer.MAX_VALUE;

        // Expand the window by moving "right" from left to right across the array.
        for (int right = 0; right < reactions.length; right++) {
            String currentReaction = reactions[right];

            // We only care about counts for reaction types that appear in the requirement map.
            // Reactions not in "needed" can still be inside the window, but they do not affect validity.
            if (needed.containsKey(currentReaction)) {
                long newCount = windowCounts.getOrDefault(currentReaction, 0L) + 1L;
                windowCounts.put(currentReaction, newCount);

                // If this increment causes the current reaction type to reach exactly the required count,
                // then one more required type is now satisfied.
                if (newCount == needed.get(currentReaction)) {
                    formed++;
                }
            }

            // While the current window satisfies all required types,
            // try to shrink it from the left to find the smallest valid window ending at "right".
            while (formed == requiredDistinctTypes && left <= right) {
                // Current window [left, right] is valid.
                int currentLength = right - left + 1;
                if (currentLength < minLength) {
                    minLength = currentLength;
                }

                String leftReaction = reactions[left];

                // We are about to remove reactions[left] from the window by moving left forward.
                // If that reaction type matters, update its count.
                if (needed.containsKey(leftReaction)) {
                    long updatedCount = windowCounts.get(leftReaction) - 1L;
                    windowCounts.put(leftReaction, updatedCount);

                    // If removing this reaction causes the count to drop below the required threshold,
                    // then the window is no longer valid after this removal.
                    if (updatedCount < needed.get(leftReaction)) {
                        formed--;
                    }
                }

                // Actually shrink the window.
                left++;
            }
        }

        return minLength == Integer.MAX_VALUE ? -1 : minLength;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the fixed demo inputs shown here
     * Space complexity: O(1) excluding the sample data structures
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String[] reactions1 = {
            "like", "wow", "sad", "like", "heart", "wow", "like", "heart"
        };
        String[][] required1 = {
            {"like", "2"},
            {"heart", "1"},
            {"wow", "1"}
        };

        int result1 = solution.shortestCommentBurst(reactions1, required1);
        System.out.println(result1); // Expected: 4

        String[] reactions2 = {
            "fire", "clap", "fire", "laugh", "clap", "fire"
        };
        String[][] required2 = {
            {"fire", "2"},
            {"clap", "2"},
            {"boo", "1"}
        };

        int result2 = solution.shortestCommentBurst(reactions2, required2);
        System.out.println(result2); // Expected: -1

        // Extra demonstration: duplicate required entries should be merged.
        // Need like total 3 times and wow 1 time.
        String[] reactions3 = {
            "like", "wow", "like", "sad", "like", "heart"
        };
        String[][] required3 = {
            {"like", "1"},
            {"like", "2"},
            {"wow", "1"}
        };

        int result3 = solution.shortestCommentBurst(reactions3, required3);
        System.out.println(result3); // Expected: 5 -> ["like","wow","like","sad","like"]
    }
}