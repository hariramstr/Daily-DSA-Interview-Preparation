/*
Title: Longest Translation Draft With Limited Unknown Terms
Difficulty: Medium
Topic: Sliding Window

Problem Description:
A localization team is reviewing a draft translation represented by an array of lowercase words.
Some words are already approved and appear in a dictionary, while other words are currently unknown
and require manual review. The team wants to find the longest contiguous section of the draft that
can be sent to a reviewer, under the rule that the section may contain at most k unknown words.

You are given a string array words, a string array approved containing all approved dictionary words,
and an integer k. Return the length of the longest contiguous subarray of words that contains at most
k words not present in approved.

A word is considered unknown if it does not exist in the approved list. The approved list may contain
duplicates, but they should be treated as a set. Your solution should be efficient enough for large inputs.

Constraints:
- 1 <= words.length <= 200000
- 1 <= approved.length <= 200000
- 0 <= k <= words.length
- 1 <= words[i].length, approved[i].length <= 20
- All words consist only of lowercase English letters

Example 1:
Input: words = ["hola","mundo","api","seguro","beta","listo"], approved = ["hola","mundo","seguro","listo"], k = 1
Output: 4
Explanation: The longest valid window is ["hola","mundo","api","seguro"]. It contains only one unknown word: "api".

Example 2:
Input: words = ["save","draft","tmp","publish","tmp","done"], approved = ["save","draft","publish","done"], k = 0
Output: 2
Explanation: With no unknown words allowed, the longest valid windows are ["save","draft"] and ["publish"] and ["done"].
The maximum length is 2.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    - Building the approved-word set takes O(m), where m = approved.Length.
    - The sliding window scans the words array once, and each index moves at most once as left/right pointers advance.
    - Total: O(n + m), where n = words.Length.

    Space Complexity:
    - O(m) for the HashSet that stores approved words.
    */
    public int LongestApprovedDraft(string[] words, string[] approved, int k)
    {
        // Step 1:
        // Convert the approved list into a HashSet.
        //
        // Why this is necessary:
        // We need to quickly answer the question:
        // "Is this word approved?"
        //
        // A HashSet gives average O(1) lookup time, which is much faster than scanning
        // the approved array every time. This is especially important because the input
        // size can be as large as 200,000.
        //
        // Also, the problem says the approved list may contain duplicates, but duplicates
        // should be treated as a set. A HashSet naturally removes duplicates for us.
        var approvedSet = new HashSet<string>(approved);

        // Step 2:
        // Prepare the sliding window variables.
        //
        // We will maintain a window [left, right] that always represents a contiguous
        // subarray of words.
        //
        // The goal:
        // Keep the window valid, meaning it contains at most k unknown words.
        //
        // left:
        // The starting index of the current window.
        //
        // unknownCount:
        // How many words inside the current window are NOT in the approved set.
        //
        // bestLength:
        // The maximum valid window length we have seen so far.
        int left = 0;
        int unknownCount = 0;
        int bestLength = 0;

        // Step 3:
        // Expand the window one word at a time by moving the right pointer.
        //
        // For each position right:
        // - Include words[right] into the current window
        // - Update unknownCount if needed
        // - If the window becomes invalid (too many unknown words), shrink it from the left
        // - Record the best valid length
        for (int right = 0; right < words.Length; right++)
        {
            // Step 3a:
            // Add the new word at index right into the window.
            //
            // If this word is not approved, then it is unknown, so we increase unknownCount.
            //
            // Why this matters:
            // The problem limits how many unknown words may appear in the chosen subarray.
            // So we must track this count accurately as the window changes.
            if (!approvedSet.Contains(words[right]))
            {
                unknownCount++;
            }

            // Step 3b:
            // If the window now contains too many unknown words, it is invalid.
            //
            // We must shrink the window from the left until it becomes valid again.
            //
            // Why shrinking works:
            // The sliding window technique relies on the fact that we are looking for a
            // contiguous subarray and the validity condition depends on counts inside the window.
            // If the window is invalid, removing elements from the left is the correct way
            // to restore validity while still considering all possible windows efficiently.
            while (unknownCount > k)
            {
                // Before moving left forward, check whether the word leaving the window
                // is unknown. If it is, we must decrease unknownCount because that word
                // will no longer be inside the current window.
                if (!approvedSet.Contains(words[left]))
                {
                    unknownCount--;
                }

                // Move the left boundary one step to the right.
                left++;
            }

            // Step 3c:
            // At this point, the window [left, right] is guaranteed to be valid:
            // it contains at most k unknown words.
            //
            // So we compute its length and update the best answer if this window is larger.
            int currentLength = right - left + 1;
            if (currentLength > bestLength)
            {
                bestLength = currentLength;
            }
        }

        // Step 4:
        // Return the maximum valid window length found during the scan.
        return bestLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
string[] words1 = { "hola", "mundo", "api", "seguro", "beta", "listo" };
string[] approved1 = { "hola", "mundo", "seguro", "listo" };
int k1 = 1;
int result1 = solution.LongestApprovedDraft(words1, approved1, k1);
Console.WriteLine(result1); // Expected: 4

// Example 2
string[] words2 = { "save", "draft", "tmp", "publish", "tmp", "done" };
string[] approved2 = { "save", "draft", "publish", "done" };
int k2 = 0;
int result2 = solution.LongestApprovedDraft(words2, approved2, k2);
Console.WriteLine(result2); // Expected: 2

// Additional quick sanity check
string[] words3 = { "a", "x", "b", "y", "c", "z" };
string[] approved3 = { "a", "b", "c" };
int k3 = 2;
int result3 = solution.LongestApprovedDraft(words3, approved3, k3);
Console.WriteLine(result3); // Expected: 5 ("a","x","b","y","c")