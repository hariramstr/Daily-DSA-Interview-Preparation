import java.util.*;

/*
 * Title: Longest Browsing Streak With Limited Tab Domains
 * Difficulty: Medium
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given an array `domains` where `domains[i]` is the website domain opened in the browser at minute `i`.
 * A user wants to study their browsing habits and find the longest contiguous time interval during which they
 * were focused on only a small set of websites.
 *
 * Define a browsing streak as any contiguous subarray of `domains`. Given an integer `k`, return the length of
 * the longest browsing streak that contains visits to at most `k` distinct domains.
 *
 * For example, if the streak is ["docs.com", "mail.com", "docs.com"] and k = 2, the streak is valid because it
 * contains only 2 distinct domains. However, ["docs.com", "mail.com", "video.com"] is invalid when k = 2 because
 * it contains 3 distinct domains.
 *
 * Your task is to compute the maximum possible length of a valid streak.
 *
 * Constraints:
 * - 1 <= domains.length <= 200000
 * - 1 <= domains[i].length <= 30
 * - domains[i] consists of lowercase English letters, digits, dots, and hyphens
 * - 1 <= k <= domains.length
 *
 * Example 1:
 * Input: domains = ["docs.com","mail.com","docs.com","video.com","mail.com","mail.com"], k = 2
 * Output: 3
 * Explanation:
 * The best valid streaks have length 3, such as ["docs.com","mail.com","docs.com"]
 * or ["video.com","mail.com","mail.com"].
 *
 * Example 2:
 * Input: domains = ["news.com","news.com","shop.com","music.com","shop.com","shop.com","news.com"], k = 2
 * Output: 4
 * Explanation:
 * Although the original text contains a contradictory intermediate statement, the correct answer is 4.
 * One optimal valid streak is ["shop.com","music.com","shop.com","shop.com"], which contains only
 * 2 distinct domains: "shop.com" and "music.com".
 */

public class Solution {

    /**
     * Computes the length of the longest contiguous subarray that contains at most k distinct domains.
     *
     * This method uses the classic sliding window technique:
     * 1. Expand the right side of the window one element at a time.
     * 2. Track how many times each domain appears inside the current window.
     * 3. If the window contains more than k distinct domains, shrink the left side
     *    until the window becomes valid again.
     * 4. After each expansion/shrinking step, record the maximum valid window length seen so far.
     *
     * @param domains the array of visited website domains, where domains[i] is the domain opened at minute i
     * @param k the maximum number of distinct domains allowed in a valid browsing streak
     * @return the maximum length of any contiguous browsing streak containing at most k distinct domains
     *
     * Time complexity: O(n), where n is domains.length, because each index moves into and out of the window at most once.
     * Space complexity: O(min(n, number of distinct domains)), due to the frequency map storing counts of domains in the current window.
     */
    public int longestBrowsingStreak(String[] domains, int k) {
        // Frequency map:
        // key   -> domain name
        // value -> how many times that domain appears in the current sliding window
        Map<String, Integer> frequencyMap = new HashMap<>();

        // left marks the beginning of the current window
        int left = 0;

        // best stores the maximum valid window length found so far
        int best = 0;

        // We move right from 0 to domains.length - 1, expanding the window one step at a time.
        for (int right = 0; right < domains.length; right++) {
            String currentDomain = domains[right];

            // Add the new domain at position right into the window.
            // If it is already present, increase its count.
            // If not, start its count at 1.
            frequencyMap.put(currentDomain, frequencyMap.getOrDefault(currentDomain, 0) + 1);

            // If the number of distinct domains is now greater than k,
            // the window is invalid and must be shrunk from the left.
            while (frequencyMap.size() > k) {
                String leftDomain = domains[left];

                // Decrease the count of the domain that is leaving the window.
                frequencyMap.put(leftDomain, frequencyMap.get(leftDomain) - 1);

                // If its count becomes 0, it no longer exists in the window,
                // so remove it from the map entirely.
                if (frequencyMap.get(leftDomain) == 0) {
                    frequencyMap.remove(leftDomain);
                }

                // Move the left boundary to the right, shrinking the window.
                left++;
            }

            // At this point, the window [left, right] is guaranteed to be valid:
            // it contains at most k distinct domains.
            int currentLength = right - left + 1;

            // Update the best answer if this valid window is longer than any previous one.
            if (currentLength > best) {
                best = currentLength;
            }
        }

        return best;
    }

    /**
     * Helper method to print an input array in a readable format and show the computed result.
     *
     * @param domains the array of visited domains to test
     * @param k the maximum number of distinct domains allowed
     * @return the computed longest valid browsing streak length for the given input
     *
     * Time complexity: O(n), where n is domains.length, because it calls the main algorithm once.
     * Space complexity: O(min(n, number of distinct domains)), due to the sliding window frequency map.
     */
    public int demonstrateCase(String[] domains, int k) {
        int result = longestBrowsingStreak(domains, k);
        System.out.println("Domains: " + Arrays.toString(domains));
        System.out.println("k = " + k);
        System.out.println("Longest valid browsing streak length = " + result);
        System.out.println();
        return result;
    }

    /**
     * Runs sample demonstrations for the problem.
     *
     * This main method verifies the examples from the prompt:
     * - Example 1 should produce 3
     * - Example 2 should produce 4
     *
     * It also includes one extra quick test for clarity.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     *
     * Time complexity: O(total number of elements across demonstrated test cases).
     * Space complexity: O(min(n, number of distinct domains)) for each individual test case.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String[] domains1 = {
            "docs.com", "mail.com", "docs.com", "video.com", "mail.com", "mail.com"
        };
        int k1 = 2;
        int result1 = solution.demonstrateCase(domains1, k1);
        System.out.println("Expected: 3, Actual: " + result1);
        System.out.println("Matches expected: " + (result1 == 3));
        System.out.println();

        String[] domains2 = {
            "news.com", "news.com", "shop.com", "music.com", "shop.com", "shop.com", "news.com"
        };
        int k2 = 2;
        int result2 = solution.demonstrateCase(domains2, k2);
        System.out.println("Expected: 4, Actual: " + result2);
        System.out.println("Matches expected: " + (result2 == 4));
        System.out.println();

        String[] domains3 = {
            "a.com", "b.com", "a.com", "a.com", "c.com", "b.com"
        };
        int k3 = 2;
        int result3 = solution.demonstrateCase(domains3, k3);
        System.out.println("Extra test result: " + result3);
    }
}