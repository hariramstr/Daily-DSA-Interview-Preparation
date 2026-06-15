import java.util.*;

/*
 * Title: Longest Purchase Streak With Category Quotas and Spend Cap
 * Difficulty: Hard
 * Topic: Sliding Window
 *
 * Problem Description:
 * You are given a chronological list of purchases made by a customer during a promotional campaign.
 * Each purchase is represented by two arrays of equal length: cost[i] is the amount spent on the
 * i-th purchase, and category[i] is the category label of that purchase. You are also given an
 * integer budget, and a dictionary quota where quota[c] specifies the minimum number of purchases
 * from category c that must appear inside a valid streak.
 *
 * A purchase streak is any contiguous subarray of purchases. A streak is considered eligible if:
 * 1. The total cost of all purchases in the streak is at most budget.
 * 2. For every category c listed in quota, the streak contains at least quota[c] purchases whose
 *    category is c.
 *
 * Return the length of the longest eligible streak. If no streak satisfies all quotas under the
 * budget, return 0.
 *
 * Constraints:
 * - 1 <= n == cost.length == category.length <= 2 * 10^5
 * - 1 <= cost[i] <= 10^9
 * - 1 <= budget <= 10^15
 * - 1 <= number of distinct categories in quota <= 2 * 10^5
 * - category[i] is a lowercase string of length between 1 and 20
 * - 1 <= quota[c] <= n
 * - Categories appearing in quota may or may not appear in the purchase list
 *
 * Key idea:
 * Because all costs are positive, a classic sliding window can maintain the budget constraint.
 * However, the quota condition is "at least" for several categories, so we also maintain counts
 * for only the categories that matter. We track how many quota categories are currently satisfied.
 *
 * For each right endpoint:
 * - Expand the window by including purchase[right].
 * - While total cost exceeds budget, move left forward to restore the budget.
 * - If all quotas are satisfied, then the current window is valid.
 *
 * Why this works:
 * Since all costs are positive, once a window exceeds budget, the only way to fix it is to move
 * the left pointer forward. Each index enters and leaves the window at most once, so the algorithm
 * is linear.
 *
 * Important note about the examples in the prompt:
 * The written explanations contain inconsistencies. For example 1, the window [0, 3] has length 4
 * and is valid, while no length-5 valid window exists under budget, so the correct answer is 4.
 * For example 2, no window can satisfy both the budget and the quotas, so the correct answer is 0.
 */

public class Solution {

    /**
     * Computes the maximum length of a contiguous purchase streak whose total cost is at most
     * the given budget and which satisfies all category quotas.
     *
     * Algorithm:
     * 1. Use a sliding window [left, right].
     * 2. Maintain:
     *    - currentSum: total cost inside the current window
     *    - windowCount: counts of quota-relevant categories inside the current window
     *    - satisfiedCategories: how many quota categories currently meet their required minimum
     * 3. Expand right one step at a time.
     * 4. If currentSum exceeds budget, shrink from the left until the budget is restored.
     * 5. Whenever all quota categories are satisfied, update the answer with the current length.
     *
     * Because all costs are positive, shrinking the window is monotonic and safe.
     *
     * @param cost the cost of each purchase in chronological order
     * @param category the category label of each purchase in chronological order
     * @param budget the maximum allowed total cost for a valid streak
     * @param quota a map from category name to required minimum count inside the streak
     * @return the maximum length of any valid contiguous streak; returns 0 if none exists
     *
     * Time complexity: O(n + q), where n is the number of purchases and q is the number of quota categories.
     * Each purchase is added to and removed from the window at most once.
     *
     * Space complexity: O(q), for tracking counts of quota-relevant categories.
     */
    public int longestEligibleStreak(int[] cost, String[] category, long budget, Map<String, Integer> quota) {
        if (cost == null || category == null || quota == null) {
            throw new IllegalArgumentException("Input arrays and quota map must not be null.");
        }
        if (cost.length != category.length) {
            throw new IllegalArgumentException("cost and category must have the same length.");
        }

        int n = cost.length;
        if (n == 0) {
            return 0;
        }

        // If there are no quota requirements, then the problem reduces to:
        // "Find the longest subarray with sum <= budget."
        // The same sliding window logic below already handles that case naturally because:
        // - requiredCategories = 0
        // - satisfiedCategories = 0
        // so every budget-valid window is considered valid.

        // This map stores the current counts of only the categories that appear in quota.
        Map<String, Integer> windowCount = new HashMap<>();

        // Total number of distinct categories that must be satisfied.
        int requiredCategories = quota.size();

        // Number of distinct quota categories whose current count in the window
        // is already >= required count.
        int satisfiedCategories = 0;

        int left = 0;
        long currentSum = 0L;
        int best = 0;

        // Move the right boundary from left to right across the array.
        for (int right = 0; right < n; right++) {
            // Step 1: include the new purchase at index right into the window.
            currentSum += cost[right];

            // Step 2: if this purchase belongs to a category that matters for quotas,
            // update its count and possibly mark that category as newly satisfied.
            String rightCategory = category[right];
            Integer requiredForRightCategory = quota.get(rightCategory);

            if (requiredForRightCategory != null) {
                int newCount = windowCount.getOrDefault(rightCategory, 0) + 1;
                windowCount.put(rightCategory, newCount);

                // If the count just reached the required threshold exactly now,
                // then one more category has become satisfied.
                if (newCount == requiredForRightCategory) {
                    satisfiedCategories++;
                }
            }

            // Step 3: if the budget is violated, shrink the window from the left
            // until the total cost is back within the allowed budget.
            //
            // This is valid because all costs are positive:
            // removing elements from the left can only decrease the sum.
            while (left <= right && currentSum > budget) {
                currentSum -= cost[left];

                String leftCategory = category[left];
                Integer requiredForLeftCategory = quota.get(leftCategory);

                if (requiredForLeftCategory != null) {
                    int oldCount = windowCount.get(leftCategory);

                    // If this category was satisfied before removal, and removing this element
                    // causes the count to drop below the required threshold, then it is no longer satisfied.
                    if (oldCount == requiredForLeftCategory) {
                        satisfiedCategories--;
                    }

                    int newCount = oldCount - 1;
                    if (newCount == 0) {
                        windowCount.remove(leftCategory);
                    } else {
                        windowCount.put(leftCategory, newCount);
                    }
                }

                left++;
            }

            // Step 4: after restoring the budget, check whether all quotas are satisfied.
            // If yes, the current window [left, right] is fully valid.
            if (satisfiedCategories == requiredCategories) {
                best = Math.max(best, right - left + 1);
            }
        }

        return best;
    }

    /**
     * Convenience overload that accepts the budget as an int.
     *
     * @param cost the cost of each purchase in chronological order
     * @param category the category label of each purchase in chronological order
     * @param budget the maximum allowed total cost for a valid streak
     * @param quota a map from category name to required minimum count inside the streak
     * @return the maximum length of any valid contiguous streak; returns 0 if none exists
     *
     * Time complexity: O(n + q), where n is the number of purchases and q is the number of quota categories.
     * Space complexity: O(q), for tracking counts of quota-relevant categories.
     */
    public int longestEligibleStreak(int[] cost, String[] category, int budget, Map<String, Integer> quota) {
        return longestEligibleStreak(cost, category, (long) budget, quota);
    }

    /**
     * Builds a quota map from alternating key/value pairs.
     * Example:
     * buildQuota("grocery", 2, "book", 1)
     *
     * This helper is only for easy demonstration in main.
     *
     * @param entries alternating category name (String) and required count (Integer)
     * @return a map representing the quota requirements
     *
     * Time complexity: O(k), where k is the number of provided objects.
     * Space complexity: O(m), where m is the number of category/count pairs.
     */
    public static Map<String, Integer> buildQuota(Object... entries) {
        if (entries.length % 2 != 0) {
            throw new IllegalArgumentException("Entries must come in category/count pairs.");
        }

        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            if (!(entries[i] instanceof String) || !(entries[i + 1] instanceof Integer)) {
                throw new IllegalArgumentException("Expected alternating String and Integer values.");
            }
            String key = (String) entries[i];
            Integer value = (Integer) entries[i + 1];
            map.put(key, value);
        }
        return map;
    }

    /**
     * Demonstrates the solution on the sample-style inputs from the prompt and prints the results.
     *
     * @param args command-line arguments (unused)
     *
     * Time complexity: O(n + q) per demonstration call.
     * Space complexity: O(q) per demonstration call.
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1
        int[] cost1 = {4, 2, 3, 1, 2, 5, 1};
        String[] category1 = {"grocery", "book", "grocery", "toy", "book", "grocery", "toy"};
        long budget1 = 10L;
        Map<String, Integer> quota1 = buildQuota("grocery", 2, "book", 1);

        int result1 = solution.longestEligibleStreak(cost1, category1, budget1, quota1);
        System.out.println("Example 1 result: " + result1);
        System.out.println("Expected: 4");

        // Example 2
        int[] cost2 = {2, 2, 2, 2, 2};
        String[] category2 = {"a", "b", "a", "c", "b"};
        long budget2 = 6L;
        Map<String, Integer> quota2 = buildQuota("a", 1, "b", 2);

        int result2 = solution.longestEligibleStreak(cost2, category2, budget2, quota2);
        System.out.println("Example 2 result: " + result2);
        System.out.println("Expected: 0");

        // Additional quick sanity check:
        // No quotas -> longest subarray with sum <= budget.
        int[] cost3 = {1, 2, 3, 4};
        String[] category3 = {"x", "y", "z", "w"};
        long budget3 = 6L;
        Map<String, Integer> quota3 = new HashMap<>();

        int result3 = solution.longestEligibleStreak(cost3, category3, budget3, quota3);
        System.out.println("No-quota example result: " + result3);
        System.out.println("Expected: 3");
    }
}