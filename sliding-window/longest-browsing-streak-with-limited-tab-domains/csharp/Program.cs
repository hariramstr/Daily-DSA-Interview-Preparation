/*
Title: Longest Browsing Streak With Limited Tab Domains
Difficulty: Medium
Topic: Sliding Window

Problem Description:
You are given an array `domains` where `domains[i]` is the website domain opened in the browser at minute `i`.
A user wants to study their browsing habits and find the longest contiguous time interval during which they
were focused on only a small set of websites.

Define a browsing streak as any contiguous subarray of `domains`. Given an integer `k`, return the length of
the longest browsing streak that contains visits to at most `k` distinct domains.

For example, if the streak is `["docs.com", "mail.com", "docs.com"]` and `k = 2`, the streak is valid because
it contains only 2 distinct domains. However, `["docs.com", "mail.com", "video.com"]` is invalid when `k = 2`
because it contains 3 distinct domains.

Your task is to compute the maximum possible length of a valid streak.

Constraints:
- 1 <= domains.length <= 200000
- 1 <= domains[i].length <= 30
- domains[i] consists of lowercase English letters, digits, dots, and hyphens
- 1 <= k <= domains.length

Examples:
1)
Input: domains = ["docs.com","mail.com","docs.com","video.com","mail.com","mail.com"], k = 2
Output: 3

Reason:
Valid longest windows of length 3 include:
- ["docs.com","mail.com","docs.com"]
- ["video.com","mail.com","mail.com"]

2)
Input: domains = ["news.com","news.com","shop.com","music.com","shop.com","shop.com","news.com"], k = 2
Output: 4

Reason:
A correct longest valid streak is:
- ["shop.com","music.com","shop.com","shop.com"]
This contains only 2 distinct domains: "shop.com" and "music.com".

Approach:
Use the classic sliding window technique:
- Expand the right side of the window one domain at a time.
- Track how many times each domain appears in the current window.
- If the window ever contains more than k distinct domains, shrink from the left
  until it becomes valid again.
- Record the maximum valid window length seen.

This works because:
- The window always represents a contiguous subarray.
- Each element enters the window once and leaves the window once.
- Therefore the algorithm is efficient for large inputs.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each domain is added to the sliding window once when the right pointer moves.
    - Each domain is removed from the sliding window at most once when the left pointer moves.
    - Dictionary operations are O(1) on average.

    Space Complexity: O(k) in the typical valid-window sense, and O(min(n, number of unique domains overall))
    in the dictionary over time.
    - The dictionary stores counts of domains currently in the window.
    - At any moment, after adjustment, the valid window contains at most k distinct domains.
    */
    public int LengthOfLongestStreakWithAtMostKDistinct(string[] domains, int k)
    {
        // This dictionary maps:
        // domain string -> how many times that domain appears inside the current window.
        //
        // Why do we need counts instead of just a set?
        // Because when we move the left side of the window forward, we need to know
        // whether removing one occurrence of a domain makes that domain disappear
        // completely from the current window.
        //
        // Example:
        // Window = ["docs.com", "mail.com", "docs.com"]
        // Counts:
        // docs.com -> 2
        // mail.com -> 1
        //
        // If we remove the leftmost "docs.com", docs.com is still inside the window once,
        // so it should remain counted as a distinct domain.
        var frequency = new Dictionary<string, int>();

        // Left boundary of the sliding window.
        // The current window is always domains[left..right].
        int left = 0;

        // Best answer found so far.
        int maxLength = 0;

        // Move the right boundary one step at a time.
        // At each step, we include domains[right] into the current window.
        for (int right = 0; right < domains.Length; right++)
        {
            string currentDomain = domains[right];

            // Step 1: Add the new rightmost domain into the window.
            //
            // Why is this necessary?
            // Because we are exploring all contiguous windows that end at index "right".
            // So before checking validity, we must first include this new element.
            if (!frequency.ContainsKey(currentDomain))
            {
                frequency[currentDomain] = 0;
            }

            frequency[currentDomain]++;

            // Step 2: If the window has become invalid (more than k distinct domains),
            // shrink it from the left until it becomes valid again.
            //
            // Why do we shrink in a while loop?
            // Because adding one new domain at the right may cause the number of distinct
            // domains to exceed k, and we may need to remove multiple elements from the left
            // before the window becomes valid again.
            while (frequency.Count > k)
            {
                string leftDomain = domains[left];

                // Remove one occurrence of the leftmost domain because we are moving
                // the left boundary one step to the right.
                frequency[leftDomain]--;

                // If its count becomes zero, that means this domain no longer exists
                // anywhere in the current window.
                //
                // Why remove it from the dictionary?
                // Because the dictionary's Count is being used to represent the number
                // of distinct domains in the current window.
                if (frequency[leftDomain] == 0)
                {
                    frequency.Remove(leftDomain);
                }

                // Actually move the left boundary forward.
                left++;
            }

            // Step 3: At this point, the window is guaranteed to be valid:
            // it contains at most k distinct domains.
            //
            // So we can safely compute its length and compare it with the best answer.
            int currentLength = right - left + 1;

            if (currentLength > maxLength)
            {
                maxLength = currentLength;
            }
        }

        // After processing all possible right boundaries, maxLength stores the answer.
        return maxLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
string[] domains1 =
{
    "docs.com", "mail.com", "docs.com", "video.com", "mail.com", "mail.com"
};
int k1 = 2;
int result1 = solution.LengthOfLongestStreakWithAtMostKDistinct(domains1, k1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 3

// Example 2
string[] domains2 =
{
    "news.com", "news.com", "shop.com", "music.com", "shop.com", "shop.com", "news.com"
};
int k2 = 2;
int result2 = solution.LengthOfLongestStreakWithAtMostKDistinct(domains2, k2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 4

// Additional quick sanity check
string[] domains3 =
{
    "a.com", "b.com", "a.com", "a.com", "c.com"
};
int k3 = 2;
int result3 = solution.LengthOfLongestStreakWithAtMostKDistinct(domains3, k3);
Console.WriteLine("Additional Test Result: " + result3); // Expected: 4 ("a.com","b.com","a.com","a.com" or "b.com","a.com","a.com")