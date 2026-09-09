import java.util.*;

/*
Title: Count User Pairs With Matching Distinct Login Hours
Difficulty: Medium
Topic: Hashing

Problem Description:
You are given a list of login records from an application. Each record is a pair [userId, hour], where userId is a string and hour is an integer from 0 to 23 representing the hour of day when that user logged in. A user may appear many times, and the same user may log in multiple times during the same hour.

Two users are considered hour-equivalent if the set of distinct hours in which they logged in is exactly the same. For example, if one user logged in at hours [1, 3, 3, 8] and another logged in at hours [8, 1, 3], they are hour-equivalent because both users have the distinct hour set {1, 3, 8}.

Return the number of unordered pairs of different users that are hour-equivalent.

Your solution should be efficient for large inputs. A common approach is to build a compact signature for each user's distinct login hours, then count how many users share the same signature.

Constraints:
- 1 <= records.length <= 200000
- records[i].length == 2
- 1 <= userId.length <= 20
- userId consists of lowercase English letters, digits, or underscores
- 0 <= hour <= 23
- There may be up to 100000 distinct users

Example 1:
Input: records = [["alice",1],["alice",3],["alice",3],["bob",3],["bob",1],["cara",2],["cara",5]]
Output: 1
Explanation: alice has distinct hours {1,3}, bob has distinct hours {1,3}, and cara has {2,5}. Only the pair (alice, bob) matches.

Example 2:
Input: records = [["u1",0],["u1",23],["u2",23],["u2",0],["u3",0],["u3",0],["u4",5]]
Output: 1
Explanation: u1 and u2 both have the distinct hour set {0,23}. u3 has {0}, and u4 has {5}. Therefore exactly one unordered pair is hour-equivalent.
*/

public class Solution {

    /**
     * Counts how many unordered pairs of different users have exactly the same set
     * of distinct login hours.
     *
     * The key idea is:
     * 1. For each user, build a compact 24-bit signature using an integer bitmask.
     *    - Bit h is set to 1 if the user logged in during hour h.
     * 2. Users with the same bitmask have the same distinct login-hour set.
     * 3. If a particular bitmask appears for k users, then it contributes
     *    k * (k - 1) / 2 unordered matching pairs.
     *
     * Why a bitmask works well here:
     * - There are only 24 possible hours: 0 through 23.
     * - An int has 32 bits, so we can store all hours inside one integer.
     * - Duplicate logins in the same hour do not matter, because setting the same bit
     *   again leaves the bitmask unchanged.
     *
     * @param records a 2D array where each element is [userId, hourAsString];
     *                records[i][0] is the user ID and records[i][1] is the hour
     * @return the number of unordered pairs of different users that are hour-equivalent
     * Time complexity: O(n + u), where n is the number of records and u is the number of distinct users
     * Space complexity: O(u), where u is the number of distinct users
     */
    public long countHourEquivalentPairs(String[][] records) {
        // This map stores, for each user, a 24-bit signature representing
        // the set of distinct hours in which that user logged in.
        //
        // Example:
        // If a user logged in at hours 1 and 3, then:
        // mask = (1 << 1) | (1 << 3)
        //      = 2 | 8
        //      = 10
        //
        // Duplicate hours do not change the mask.
        Map<String, Integer> userToMask = new HashMap<>();

        // Step 1: Build each user's distinct-hour signature.
        for (String[] record : records) {
            // Read the user ID.
            String userId = record[0];

            // Parse the hour from string to integer.
            int hour = Integer.parseInt(record[1]);

            // Create a bit with only the "hour" position set.
            // For example, if hour = 5, then bit = 1 << 5.
            int bit = 1 << hour;

            // Get the user's current mask, or 0 if this is the first time we see the user.
            int currentMask = userToMask.getOrDefault(userId, 0);

            // Set the bit for this hour.
            // If the hour was already present, OR keeps it set and changes nothing.
            int updatedMask = currentMask | bit;

            // Save the updated mask back into the map.
            userToMask.put(userId, updatedMask);
        }

        // This map counts how many users share the same final hour signature.
        Map<Integer, Integer> maskFrequency = new HashMap<>();

        // Step 2: Count how many users have each signature.
        for (int mask : userToMask.values()) {
            maskFrequency.put(mask, maskFrequency.getOrDefault(mask, 0) + 1);
        }

        // Step 3: For each signature that appears k times, add the number of
        // unordered pairs among those k users.
        //
        // Number of unordered pairs from k items = k choose 2 = k * (k - 1) / 2
        long pairs = 0L;
        for (int frequency : maskFrequency.values()) {
            if (frequency >= 2) {
                pairs += (long) frequency * (frequency - 1) / 2;
            }
        }

        return pairs;
    }

    /**
     * Convenience overload that accepts records as a list of pairs, where each pair
     * is represented by a String array of length 2: [userId, hourAsString].
     *
     * @param records list of login records; each record is [userId, hourAsString]
     * @return the number of unordered hour-equivalent user pairs
     * Time complexity: O(n + u), where n is the number of records and u is the number of distinct users
     * Space complexity: O(u), where u is the number of distinct users
     */
    public long countHourEquivalentPairs(List<String[]> records) {
        String[][] array = new String[records.size()][2];
        for (int i = 0; i < records.size(); i++) {
            array[i] = records.get(i);
        }
        return countHourEquivalentPairs(array);
    }

    /**
     * Utility method to print a 2D String array in a readable format.
     *
     * @param records the records to print
     * @return a readable string representation of the records
     * Time complexity: O(n)
     * Space complexity: O(n) for the constructed output string
     */
    public String recordsToString(String[][] records) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < records.length; i++) {
            sb.append(Arrays.toString(records[i]));
            if (i + 1 < records.length) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * It also prints the expected outputs so the results can be visually verified.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n) across the demonstrated examples
     * Space complexity: O(u) for the internal maps used by the algorithm
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1 from the problem statement:
        // alice -> hours {1, 3}
        // bob   -> hours {1, 3}
        // cara  -> hours {2, 5}
        // Matching unordered pairs: only (alice, bob)
        String[][] records1 = {
                {"alice", "1"},
                {"alice", "3"},
                {"alice", "3"},
                {"bob", "3"},
                {"bob", "1"},
                {"cara", "2"},
                {"cara", "5"}
        };

        long result1 = solution.countHourEquivalentPairs(records1);
        System.out.println("Example 1 records: " + solution.recordsToString(records1));
        System.out.println("Output: " + result1);
        System.out.println("Expected: 1");
        System.out.println();

        // Example 2 from the problem statement:
        // u1 -> hours {0, 23}
        // u2 -> hours {0, 23}
        // u3 -> hours {0}
        // u4 -> hours {5}
        // Matching unordered pairs: only (u1, u2)
        String[][] records2 = {
                {"u1", "0"},
                {"u1", "23"},
                {"u2", "23"},
                {"u2", "0"},
                {"u3", "0"},
                {"u3", "0"},
                {"u4", "5"}
        };

        long result2 = solution.countHourEquivalentPairs(records2);
        System.out.println("Example 2 records: " + solution.recordsToString(records2));
        System.out.println("Output: " + result2);
        System.out.println("Expected: 1");
        System.out.println();

        // Additional small demonstration:
        // a -> {1,2}
        // b -> {1,2}
        // c -> {1,2}
        // d -> {4}
        // Three users share the same signature, so the number of unordered pairs is:
        // 3 choose 2 = 3
        String[][] records3 = {
                {"a", "1"},
                {"a", "2"},
                {"b", "2"},
                {"b", "1"},
                {"c", "1"},
                {"c", "2"},
                {"d", "4"}
        };

        long result3 = solution.countHourEquivalentPairs(records3);
        System.out.println("Additional Example records: " + solution.recordsToString(records3));
        System.out.println("Output: " + result3);
        System.out.println("Expected: 3");
    }
}