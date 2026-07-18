import java.util.*;

/*
 * Title: Count Distinct User Sets per Alert Pattern
 * Difficulty: Hard
 * Topic: Hashing
 *
 * Problem Description:
 * A monitoring system records security alerts as pairs (userId, alertCode).
 * Multiple records may exist for the same pair because the same user can trigger
 * the same alert many times. For each alertCode, define its user set as the set
 * of distinct users who triggered that alert at least once. Two alert codes are
 * considered equivalent if their user sets are exactly the same.
 *
 * Your task is to return the number of unordered pairs of distinct alert codes
 * that are equivalent.
 *
 * For example, if alert A1 was triggered by users {2, 5, 9} and alert B7 was
 * also triggered by users {2, 5, 9}, then (A1, B7) contributes 1 to the answer.
 * Duplicate records must not change a user set. Alert codes triggered by no users
 * do not appear in the input and therefore are not considered.
 *
 * Design an algorithm that works efficiently for large inputs. A naive comparison
 * of every pair of alert codes and every user in their sets will be too slow.
 * You will likely need hashing to build a canonical representation of each alert's
 * distinct user set and then count how many alerts share the same representation.
 *
 * Constraints:
 * - 1 <= records.length <= 2 * 10^5
 * - Each record is a pair [userId, alertCode]
 * - 1 <= userId <= 10^9
 * - 1 <= alertCode <= 10^9
 * - The same (userId, alertCode) pair may appear many times
 * - The answer can be as large as n * (n - 1) / 2, so use 64-bit arithmetic
 *
 * Example 1:
 * Input: records = [[1,10],[2,10],[2,10],[1,20],[2,20],[3,30],[4,30]]
 * Output: 1
 * Explanation: alert 10 has users {1,2}, alert 20 has users {1,2}, and alert 30
 * has users {3,4}. Only alerts 10 and 20 have identical user sets.
 *
 * Example 2:
 * Input: records = [[5,100],[7,100],[5,200],[7,200],[8,300],[8,300],[9,400],[10,400],[9,500],[10,500]]
 * Output: 2
 * Explanation: alert 100 and 200 share the user set {5,7}. Alert 400 and 500
 * share the user set {9,10}. Alert 300 has user set {8}. Therefore there are
 * 2 equivalent unordered pairs.
 */

public class Solution {

    /**
     * Counts the number of unordered pairs of distinct alert codes whose sets of distinct users
     * are exactly equal.
     *
     * The algorithm works in three major phases:
     * 1. Group all userIds by alertCode.
     * 2. For each alertCode, sort its collected userIds and remove duplicates so that we obtain
     *    the exact distinct user set in canonical sorted order.
     * 3. Use that canonical representation as a key in a hash map. If the same canonical user set
     *    appears for multiple alert codes, and it appears k times, then it contributes
     *    k * (k - 1) / 2 unordered equivalent pairs.
     *
     * This approach is correct because:
     * - Sorting + deduplication removes repeated records of the same (userId, alertCode) pair.
     * - Two alert codes are equivalent if and only if their sorted distinct user lists are identical.
     * - Hashing the canonical representation lets us count equal user sets efficiently.
     *
     * @param records the input records where each element is a pair [userId, alertCode]
     * @return the number of unordered pairs of distinct alert codes with identical distinct user sets
     * @implNote Time complexity: O(n + sum over alerts of m_i log m_i)), where n is the number of records
     *           and m_i is the number of records associated with alert i before deduplication.
     *           In the worst case this is O(n log n).
     * @implNote Space complexity: O(n), for storing grouped users and canonical keys.
     */
    public long countEquivalentAlertPairs(int[][] records) {
        // Step 1:
        // Build a mapping from each alertCode to the list of userIds that triggered it.
        //
        // Important detail:
        // We intentionally keep duplicates for now because removing duplicates immediately with a
        // HashSet per alert would also work, but using a list and then sorting + deduplicating is
        // simple, deterministic, and beginner-friendly.
        Map<Integer, List<Integer>> alertToUsers = new HashMap<>();

        for (int[] record : records) {
            int userId = record[0];
            int alertCode = record[1];

            alertToUsers.computeIfAbsent(alertCode, k -> new ArrayList<>()).add(userId);
        }

        // Step 2:
        // For each alertCode, convert its user list into a canonical representation:
        // - sort the list
        // - remove duplicates
        // - wrap the distinct sorted users into a key object
        //
        // Then count how many alert codes share the same canonical user set.
        Map<UserSetKey, Integer> patternCount = new HashMap<>();

        for (List<Integer> users : alertToUsers.values()) {
            // Sort so that equal sets become equal ordered sequences.
            Collections.sort(users);

            // Deduplicate the sorted list in linear time.
            int uniqueCount = 0;
            int previous = Integer.MIN_VALUE;
            boolean hasPrevious = false;

            for (int user : users) {
                // Because the list is sorted, duplicates are adjacent.
                // We only keep the first occurrence of each distinct userId.
                if (!hasPrevious || user != previous) {
                    users.set(uniqueCount, user);
                    uniqueCount++;
                    previous = user;
                    hasPrevious = true;
                }
            }

            // Copy only the distinct prefix into an int[].
            // This array is the exact canonical representation of the alert's user set.
            int[] distinctUsers = new int[uniqueCount];
            for (int i = 0; i < uniqueCount; i++) {
                distinctUsers[i] = users.get(i);
            }

            UserSetKey key = new UserSetKey(distinctUsers);
            patternCount.put(key, patternCount.getOrDefault(key, 0) + 1);
        }

        // Step 3:
        // If a particular user-set pattern appears c times among alert codes,
        // then the number of unordered pairs among those c alert codes is:
        // c choose 2 = c * (c - 1) / 2
        long answer = 0L;
        for (int count : patternCount.values()) {
            answer += (long) count * (count - 1) / 2;
        }

        return answer;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * This method also effectively verifies the examples:
     * - Example 1 should print 1
     * - Example 2 should print 2
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * @implNote Time complexity: O(1) for the fixed demo size, excluding the called algorithm.
     * @implNote Space complexity: O(1), excluding the called algorithm.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[][] records1 = {
            {1, 10},
            {2, 10},
            {2, 10},
            {1, 20},
            {2, 20},
            {3, 30},
            {4, 30}
        };

        int[][] records2 = {
            {5, 100},
            {7, 100},
            {5, 200},
            {7, 200},
            {8, 300},
            {8, 300},
            {9, 400},
            {10, 400},
            {9, 500},
            {10, 500}
        };

        long result1 = solution.countEquivalentAlertPairs(records1);
        long result2 = solution.countEquivalentAlertPairs(records2);

        System.out.println(result1); // Expected: 1
        System.out.println(result2); // Expected: 2
    }

    /**
     * A hashable wrapper around a sorted distinct int[] representing one alert's user set.
     *
     * Why this class exists:
     * Java arrays do not compare by contents when used directly as hash map keys.
     * Therefore, we wrap the array and define content-based equals() and hashCode().
     *
     * Because the array is already sorted and deduplicated before construction,
     * two equivalent user sets produce identical arrays and therefore identical keys.
     */
    private static final class UserSetKey {
        private final int[] users;
        private final int hash;

        /**
         * Creates a key from a sorted distinct user array.
         *
         * @param users sorted array of distinct userIds
         */
        UserSetKey(int[] users) {
            this.users = users;
            this.hash = Arrays.hashCode(users);
        }

        /**
         * Compares this key with another object by array contents.
         *
         * @param obj the object to compare against
         * @return true if the other object is a UserSetKey with the same user array contents
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof UserSetKey)) {
                return false;
            }
            UserSetKey other = (UserSetKey) obj;
            return Arrays.equals(this.users, other.users);
        }

        /**
         * Returns the precomputed hash code of the user array.
         *
         * @return content-based hash code
         */
        @Override
        public int hashCode() {
            return hash;
        }
    }
}