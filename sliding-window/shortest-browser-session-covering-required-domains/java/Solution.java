import java.util.*;

/*
 * Title: Shortest Browser Session Covering Required Domains
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * A security team analyzes a user's browsing history as an array visits, where visits[i]
 * is the domain opened at minute i. For an investigation, the team is given a requirement
 * map need describing how many times each important domain must appear inside a single
 * contiguous session. Your task is to find the length of the shortest contiguous subarray
 * of visits that satisfies all domain requirements.
 *
 * A window is valid if for every domain d in need, the window contains at least need[d]
 * occurrences of d. Domains not listed in need may appear any number of times and do not
 * affect validity. If no valid session exists, return -1.
 *
 * This is not just a basic coverage problem: the input size is large, domain names may
 * repeat heavily, and the solution must scale close to linear time. An O(n^2) solution
 * will time out.
 *
 * Return the minimum possible length of a valid contiguous session.
 *
 * Constraints:
 * - 1 <= visits.length <= 200000
 * - 1 <= need.size <= 50000
 * - Sum of all required counts in need <= visits.length
 * - Each domain name consists of lowercase English letters, digits, dots, and hyphens
 * - 1 <= domain name length <= 30
 * - 1 <= need[d] <= 100000
 *
 * Example 1:
 * visits = ["news.com","mail.com","shop.com","news.com","video.com","mail.com","news.com"]
 * need = {"news.com": 2, "mail.com": 1}
 * Output: 4
 *
 * Example 2:
 * visits = ["a.com","b.com","a.com","c.com","b.com"]
 * need = {"a.com": 2, "b.com": 2, "d.com": 1}
 * Output: -1
 */

public class Solution {

    /**
     * Finds the length of the shortest contiguous subarray of visits that satisfies
     * all required domain counts in the need map.
     *
     * The algorithm uses the classic sliding window technique:
     * 1. Expand the right boundary to include more visits.
     * 2. Track how many required domains currently meet their needed counts.
     * 3. Once all requirements are satisfied, shrink the left boundary as much as possible
     *    while keeping the window valid.
     * 4. Record the smallest valid window length seen.
     *
     * @param visits the browsing history array where visits[i] is the domain visited at minute i
     * @param need a map from domain name to the minimum number of times it must appear in a valid window
     * @return the minimum length of a valid contiguous session, or -1 if no such session exists
     *
     * Time complexity: O(n + m), where n is visits.length and m is need.size(),
     * because each index enters and leaves the sliding window at most once.
     * Space complexity: O(m), for storing counts of only the required domains.
     */
    public int shortestSession(String[] visits, Map<String, Integer> need) {
        if (visits == null || visits.length == 0 || need == null || need.isEmpty()) {
            return -1;
        }

        // Optional but useful early impossibility check:
        // Count total occurrences in the full visits array only for domains that matter.
        // If any required domain does not appear enough times overall, we can immediately return -1.
        if (!canPossiblySatisfy(visits, need)) {
            return -1;
        }

        // This map stores how many times each required domain appears in the current window.
        Map<String, Integer> windowCount = new HashMap<>();

        // Number of distinct required domains that currently satisfy:
        // windowCount[domain] >= need[domain]
        int satisfiedDomains = 0;

        // Total number of distinct domains that must be satisfied.
        int requiredDomains = need.size();

        // Left boundary of the sliding window.
        int left = 0;

        // Best answer found so far. Start with a very large value.
        int minLength = Integer.MAX_VALUE;

        // Expand the window by moving the right boundary one step at a time.
        for (int right = 0; right < visits.length; right++) {
            String currentDomain = visits[right];

            // Only domains listed in need affect validity.
            if (need.containsKey(currentDomain)) {
                int newCount = windowCount.getOrDefault(currentDomain, 0) + 1;
                windowCount.put(currentDomain, newCount);

                // Very important detail:
                // We increase satisfiedDomains only when this domain count becomes exactly equal
                // to the required count for the first time.
                //
                // Example:
                // need["news.com"] = 2
                // window count goes 0 -> 1 : not satisfied yet
                // window count goes 1 -> 2 : now satisfied, increment satisfiedDomains
                // window count goes 2 -> 3 : still satisfied, do NOT increment again
                if (newCount == need.get(currentDomain)) {
                    satisfiedDomains++;
                }
            }

            // If all distinct required domains are satisfied, the current window is valid.
            // Now try to shrink it from the left to make it as short as possible.
            while (satisfiedDomains == requiredDomains) {
                // Update the best answer using the current valid window [left, right].
                int currentLength = right - left + 1;
                if (currentLength < minLength) {
                    minLength = currentLength;
                }

                String leftDomain = visits[left];

                // We are about to remove visits[left] from the window by moving left forward.
                // If that domain is required, we must update its count.
                if (need.containsKey(leftDomain)) {
                    int countBeforeRemoval = windowCount.get(leftDomain);

                    // If the count is exactly equal to the required amount before removal,
                    // then removing one occurrence will make this domain no longer satisfied.
                    if (countBeforeRemoval == need.get(leftDomain)) {
                        satisfiedDomains--;
                    }

                    // Decrease the count in the window.
                    int countAfterRemoval = countBeforeRemoval - 1;
                    if (countAfterRemoval == 0) {
                        windowCount.remove(leftDomain);
                    } else {
                        windowCount.put(leftDomain, countAfterRemoval);
                    }
                }

                // Actually shrink the window.
                left++;
            }
        }

        return minLength == Integer.MAX_VALUE ? -1 : minLength;
    }

    /**
     * Performs an early feasibility check by counting total occurrences of required domains
     * across the entire visits array.
     *
     * If any required domain appears fewer times in the whole array than needed, then no
     * contiguous subarray can ever satisfy the requirement.
     *
     * @param visits the browsing history array
     * @param need the required domain counts
     * @return true if it is possible in principle to satisfy all requirements, false otherwise
     *
     * Time complexity: O(n + m), where n is visits.length and m is need.size().
     * Space complexity: O(m), for counting only required domains.
     */
    public boolean canPossiblySatisfy(String[] visits, Map<String, Integer> need) {
        Map<String, Integer> total = new HashMap<>();

        // Count only domains that are actually required.
        for (String domain : visits) {
            if (need.containsKey(domain)) {
                total.put(domain, total.getOrDefault(domain, 0) + 1);
            }
        }

        // Verify every required domain appears enough times overall.
        for (Map.Entry<String, Integer> entry : need.entrySet()) {
            String domain = entry.getKey();
            int requiredCount = entry.getValue();
            int availableCount = total.getOrDefault(domain, 0);

            if (availableCount < requiredCount) {
                return false;
            }
        }

        return true;
    }

    /**
     * Helper method to build a map from alternating key/value pairs.
     * This is used only in the demo code inside main to keep examples readable.
     *
     * Example:
     * mapOf("news.com", 2, "mail.com", 1)
     *
     * @param pairs alternating domain name (String) and required count (Integer)
     * @return a map containing the provided pairs
     *
     * Time complexity: O(k), where k is the number of provided pairs.
     * Space complexity: O(k), for the resulting map.
     */
    public static Map<String, Integer> mapOf(Object... pairs) {
        Map<String, Integer> map = new HashMap<>();

        for (int i = 0; i < pairs.length; i += 2) {
            String key = (String) pairs[i];
            Integer value = (Integer) pairs[i + 1];
            map.put(key, value);
        }

        return map;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (not used)
     *
     * Time complexity: O(1) for the fixed demo setup, excluding the called algorithm.
     * Space complexity: O(1) for the fixed demo setup, excluding the called algorithm.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        String[] visits1 = {
            "news.com", "mail.com", "shop.com", "news.com",
            "video.com", "mail.com", "news.com"
        };
        Map<String, Integer> need1 = mapOf(
            "news.com", 2,
            "mail.com", 1
        );

        int result1 = solution.shortestSession(visits1, need1);
        System.out.println("Example 1 Result: " + result1); // Expected: 4

        // Example 2
        String[] visits2 = {
            "a.com", "b.com", "a.com", "c.com", "b.com"
        };
        Map<String, Integer> need2 = mapOf(
            "a.com", 2,
            "b.com", 2,
            "d.com", 1
        );

        int result2 = solution.shortestSession(visits2, need2);
        System.out.println("Example 2 Result: " + result2); // Expected: -1

        // Additional quick sanity check
        String[] visits3 = {
            "x.com", "y.com", "x.com", "z.com", "y.com", "x.com"
        };
        Map<String, Integer> need3 = mapOf(
            "x.com", 2,
            "y.com", 1
        );

        int result3 = solution.shortestSession(visits3, need3);
        System.out.println("Additional Example Result: " + result3); // One valid shortest answer is 3: ["y.com","x.com","z.com"] is invalid, but ["x.com","y.com","x.com"] is valid
    }
}