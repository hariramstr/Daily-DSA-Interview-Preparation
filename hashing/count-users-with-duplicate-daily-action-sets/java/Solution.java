import java.util.*;

/*
Problem Title: Count Users With Duplicate Daily Action Sets

Problem Description:
A product analytics team stores user activity for a single day as a list of events.
Each event is represented by a pair [userId, action], where userId is a string and
action is a lowercase string such as "click", "view", or "share".

A user's daily action set is the set of distinct actions they performed that day.
Repeated occurrences of the same action by the same user should count only once.

Your task is to return the number of users whose daily action set is identical to at
least one other user's daily action set. Two users are considered matching if the set
of distinct actions they performed is exactly the same, regardless of the order of
events or how many times each action appears.

For example, if user A performed ["view", "click", "click"] and user B performed
["click", "view"], then they have the same daily action set: {"click", "view"}.
If user C performed only ["view"], then C does not match A or B.

Return the count of distinct users that belong to any matching group. A group may
contain more than two users, and every user in that group should be counted.

Constraints:
- 1 <= events.length <= 200000
- Each event has exactly 2 values: [userId, action]
- 1 <= userId.length, action.length <= 20
- userId and action contain only lowercase English letters and digits
- There are at most 100000 distinct users

Example 1:
Input:
events = [
  ["u1","view"],
  ["u2","click"],
  ["u1","click"],
  ["u2","view"],
  ["u3","view"],
  ["u3","view"]
]
Output: 2

Explanation:
u1 has set {view, click}
u2 has set {click, view}
u3 has set {view}
Only u1 and u2 share an identical daily action set, so the answer is 2.

Example 2:
Input:
events = [
  ["a","login"],
  ["a","upload"],
  ["b","upload"],
  ["b","login"],
  ["c","login"],
  ["d","pay"],
  ["d","refund"],
  ["e","refund"],
  ["e","pay"]
]
Output: 4

Explanation:
Users a and b share the set {login, upload}.
Users d and e share the set {pay, refund}.
User c has {login} alone.
Therefore, 4 users belong to matching groups.

Expected approach:
Use hashing to normalize each user's distinct-action set into a canonical representation,
then count how many users share the same representation.
*/

public class Solution {

    /**
     * Counts how many distinct users belong to any group of users that share the exact same
     * set of distinct actions.
     *
     * Algorithm overview:
     * 1. Build a mapping from userId -> set of distinct actions.
     * 2. Convert each user's action set into a canonical string representation:
     *    - sort the actions
     *    - join them with a separator
     * 3. Count how many users have each canonical representation.
     * 4. Sum the sizes of all groups whose frequency is at least 2.
     *
     * Why sorting works:
     * If two users have the same set of actions, sorting those actions produces the same order,
     * so both users get the exact same canonical key.
     *
     * @param events the list of events, where each event is a 2-element list:
     *               [userId, action]
     * @return the number of distinct users that belong to any matching group
     * Time complexity: O(E + U * A log A), where:
     *                  E = number of events,
     *                  U = number of distinct users,
     *                  A = average number of distinct actions per user
     * Space complexity: O(E) in the worst case, due to storing user action sets and hash maps
     */
    public int countUsersWithDuplicateDailyActionSets(List<List<String>> events) {
        // Step 1:
        // Build a map from each user to the set of distinct actions they performed.
        //
        // We use:
        // - HashMap<String, Set<String>> for fast user lookup
        // - HashSet<String> for each user's actions so duplicates are automatically removed
        //
        // Example:
        // If events contain:
        // ["u1","view"], ["u1","click"], ["u1","click"]
        // then userToActions.get("u1") becomes {"view", "click"}
        Map<String, Set<String>> userToActions = new HashMap<>();

        for (List<String> event : events) {
            // Each event must contain exactly two strings:
            // index 0 -> userId
            // index 1 -> action
            String userId = event.get(0);
            String action = event.get(1);

            // If this is the first time we see the user, create an empty action set.
            userToActions.computeIfAbsent(userId, ignored -> new HashSet<>()).add(action);
        }

        // Step 2:
        // For each user, convert their set of actions into a canonical representation.
        //
        // Why do we need a canonical representation?
        // Because sets do not preserve order.
        // For example:
        // {"view", "click"} and {"click", "view"} are the same set,
        // but if we iterate over a HashSet directly, the order is not guaranteed.
        //
        // So we:
        // - copy the set into a list
        // - sort the list
        // - join the sorted actions into one string key
        //
        // Then users with identical action sets will produce identical keys.
        Map<String, Integer> canonicalSetFrequency = new HashMap<>();

        for (Set<String> actions : userToActions.values()) {
            String canonicalKey = buildCanonicalKey(actions);

            // Count how many users have this exact canonical action-set key.
            canonicalSetFrequency.put(canonicalKey, canonicalSetFrequency.getOrDefault(canonicalKey, 0) + 1);
        }

        // Step 3:
        // Every canonical key with frequency >= 2 represents a matching group.
        // We must count ALL users in such groups.
        //
        // Example:
        // key "{click,view}" -> 2 users  => add 2
        // key "{login}"      -> 1 user   => add 0
        // key "{pay,refund}" -> 2 users  => add 2
        //
        // Final answer = 2 + 2 = 4
        int result = 0;

        for (int frequency : canonicalSetFrequency.values()) {
            if (frequency >= 2) {
                result += frequency;
            }
        }

        return result;
    }

    /**
     * Convenience overload that accepts a 2D string array.
     * This is useful for simple testing in main.
     *
     * @param events a 2D array where each row is [userId, action]
     * @return the number of distinct users that belong to any matching group
     * Time complexity: O(E + U * A log A), same as the main algorithm
     * Space complexity: O(E), same as the main algorithm
     */
    public int countUsersWithDuplicateDailyActionSets(String[][] events) {
        List<List<String>> eventList = new ArrayList<>(events.length);

        for (String[] event : events) {
            List<String> pair = new ArrayList<>(2);
            pair.add(event[0]);
            pair.add(event[1]);
            eventList.add(pair);
        }

        return countUsersWithDuplicateDailyActionSets(eventList);
    }

    /**
     * Builds a canonical string key for a set of actions.
     *
     * Detailed idea:
     * - A set has no guaranteed order.
     * - To compare sets using hashing, we need a stable representation.
     * - We copy the set into a list and sort it alphabetically.
     * - Then we join the sorted actions using a delimiter that separates values clearly.
     *
     * Example:
     * actions = {"view", "click"}
     * sorted  = ["click", "view"]
     * key     = "click#view"
     *
     * Another user with actions {"click", "view"} will produce the same key.
     *
     * @param actions the set of distinct actions for one user
     * @return a canonical string representation of the action set
     * Time complexity: O(A log A), where A is the number of distinct actions in the set
     * Space complexity: O(A), for the temporary sorted list and string builder
     */
    public String buildCanonicalKey(Set<String> actions) {
        // Copy the set into a list so we can sort it.
        List<String> sortedActions = new ArrayList<>(actions);

        // Sorting ensures that equal sets produce equal ordered lists.
        Collections.sort(sortedActions);

        // Build the final key.
        //
        // We use a separator between actions so that different combinations do not
        // accidentally produce the same string.
        //
        // For example, without a separator:
        // ["ab", "c"] -> "abc"
        // ["a", "bc"] -> "abc"
        // That would be incorrect.
        //
        // With a separator:
        // ["ab", "c"] -> "ab#c"
        // ["a", "bc"] -> "a#bc"
        // Now they are clearly different.
        StringBuilder keyBuilder = new StringBuilder();

        for (int i = 0; i < sortedActions.size(); i++) {
            if (i > 0) {
                keyBuilder.append('#');
            }
            keyBuilder.append(sortedActions.get(i));
        }

        return keyBuilder.toString();
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(1) for the driver itself, excluding the called algorithm
     * Space complexity: O(1) for the driver itself, excluding the called algorithm
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        String[][] events1 = {
            {"u1", "view"},
            {"u2", "click"},
            {"u1", "click"},
            {"u2", "view"},
            {"u3", "view"},
            {"u3", "view"}
        };

        int result1 = solution.countUsersWithDuplicateDailyActionSets(events1);
        System.out.println(result1); // Expected: 2

        // Example 2
        String[][] events2 = {
            {"a", "login"},
            {"a", "upload"},
            {"b", "upload"},
            {"b", "login"},
            {"c", "login"},
            {"d", "pay"},
            {"d", "refund"},
            {"e", "refund"},
            {"e", "pay"}
        };

        int result2 = solution.countUsersWithDuplicateDailyActionSets(events2);
        System.out.println(result2); // Expected: 4

        // Additional quick sanity check:
        // Three users all share the same set {"x", "y"}.
        // All three should be counted.
        String[][] events3 = {
            {"p1", "x"},
            {"p1", "y"},
            {"p2", "y"},
            {"p2", "x"},
            {"p3", "x"},
            {"p3", "y"},
            {"solo", "z"}
        };

        int result3 = solution.countUsersWithDuplicateDailyActionSets(events3);
        System.out.println(result3); // Expected: 3
    }
}