/*
Title: Longest Code Review Window With Per-Language Bug Caps
Difficulty: Hard
Topic: Sliding Window

Problem Description:
You are given a stream of code review findings in chronological order. Each finding belongs to a programming language and has a bug severity score. The i-th finding is represented by two arrays: languages[i] is a string such as "cpp", "java", or "py", and severities[i] is a positive integer. You are also given an integer limit and an integer budget.

A contiguous review window is considered valid if it satisfies both rules:
1. For every language, the number of findings from that language inside the window is at most limit.
2. Let maxSeverity be the maximum severity in the window and minSeverity be the minimum severity in the window. Then maxSeverity - minSeverity must be at most budget.

Return the length of the longest valid contiguous window.

This problem models a review dashboard that should not become dominated by too many findings from the same language, while also keeping the severity spread manageable enough for a single triage session.

Design an algorithm efficient enough for large inputs. A brute-force O(n^2) or O(n^3) scan will not pass.

Constraints:
- 1 <= languages.length == severities.length <= 200000
- 1 <= severities[i] <= 10^9
- 1 <= limit <= languages.length
- 0 <= budget <= 10^9
- languages[i] consists of lowercase English letters and has length between 1 and 10

Example 1:
Input:
languages = ["py","java","py","cpp","java","py"]
severities = [4,7,5,6,8,5]
limit = 2
budget = 3
Output: 4

Example 2:
Input:
languages = ["go","go","rs","go","rs","ts","rs"]
severities = [10,3,6,4,7,5,8]
limit = 2
budget = 4
Output: 5
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity:
    O(n), where n is the number of findings.
    Explanation:
    - Each index is added to and removed from each deque at most once.
    - The left and right pointers each move from 0 to n - 1 only once.
    - Dictionary updates are O(1) average time.

    Space Complexity:
    O(n + k), where:
    - n is for the monotonic deques in the worst case,
    - k is the number of distinct languages currently tracked in the dictionary.
    */
    public int LongestValidWindow(string[] languages, int[] severities, int limit, int budget)
    {
        int n = languages.Length;

        // This dictionary stores how many times each language appears inside the current window.
        // Example:
        // if the current window contains ["py", "java", "py"], then:
        // counts["py"] = 2
        // counts["java"] = 1
        //
        // We need this because one of the rules says:
        // "For every language, the count inside the window must be at most limit."
        var counts = new Dictionary<string, int>();

        // This integer tracks how many languages are currently violating the "at most limit" rule.
        //
        // Why do we need this?
        // We could check the whole dictionary every time, but that would be too slow.
        // Instead, we maintain this number incrementally:
        // - when a language count becomes limit + 1, it starts violating, so we increment badLanguageKinds
        // - when a language count drops back to limit, it stops violating, so we decrement badLanguageKinds
        //
        // Then the language-count rule is satisfied exactly when badLanguageKinds == 0.
        int badLanguageKinds = 0;

        // We use two monotonic deques, but we store indices instead of values.
        //
        // maxDeque:
        // - severities at these indices are kept in decreasing order
        // - the front always points to the maximum severity in the current window
        //
        // minDeque:
        // - severities at these indices are kept in increasing order
        // - the front always points to the minimum severity in the current window
        //
        // Why deques?
        // We need to know max and min in the current sliding window efficiently.
        // A brute-force scan of the window each time would be too slow.
        var maxDeque = new LinkedList<int>();
        var minDeque = new LinkedList<int>();

        int left = 0;
        int best = 0;

        // We expand the window by moving "right" from left to right across the array.
        for (int right = 0; right < n; right++)
        {
            string currentLanguage = languages[right];
            int currentSeverity = severities[right];

            // ------------------------------------------------------------
            // STEP 1: Add the new language occurrence into the frequency map.
            // ------------------------------------------------------------
            //
            // We are extending the window to include index "right".
            // So we must update the count of that language.
            if (!counts.TryGetValue(currentLanguage, out int oldCount))
            {
                oldCount = 0;
            }

            int newCount = oldCount + 1;
            counts[currentLanguage] = newCount;

            // If the count just crossed from "limit" to "limit + 1",
            // this language has started violating the per-language cap.
            if (newCount == limit + 1)
            {
                badLanguageKinds++;
            }

            // ------------------------------------------------------------
            // STEP 2: Insert the new severity into the max deque.
            // ------------------------------------------------------------
            //
            // maxDeque must remain decreasing by severity.
            // That means:
            // while the last element has severity smaller than the new severity,
            // it can never become the maximum for any future window that also contains
            // the new element, so we remove it.
            //
            // Example:
            // existing severities in deque tail: [7, 5]
            // new severity: 6
            // remove 5 because 6 is newer and larger, so 5 is useless for max queries
            while (maxDeque.Count > 0 && severities[maxDeque.Last!.Value] < currentSeverity)
            {
                maxDeque.RemoveLast();
            }

            // Now append the current index.
            maxDeque.AddLast(right);

            // ------------------------------------------------------------
            // STEP 3: Insert the new severity into the min deque.
            // ------------------------------------------------------------
            //
            // minDeque must remain increasing by severity.
            // That means:
            // while the last element has severity larger than the new severity,
            // it can never become the minimum for any future window that also contains
            // the new element, so we remove it.
            while (minDeque.Count > 0 && severities[minDeque.Last!.Value] > currentSeverity)
            {
                minDeque.RemoveLast();
            }

            // Now append the current index.
            minDeque.AddLast(right);

            // ------------------------------------------------------------
            // STEP 4: Shrink the window from the left until it becomes valid.
            // ------------------------------------------------------------
            //
            // The window is invalid if either:
            // 1) some language count exceeds limit  => badLanguageKinds > 0
            // 2) maxSeverity - minSeverity > budget
            //
            // We keep moving "left" forward until both conditions are satisfied.
            while (badLanguageKinds > 0 || (long)severities[maxDeque.First!.Value] - severities[minDeque.First!.Value] > budget)
            {
                string leftLanguage = languages[left];
                int leftSeverity = severities[left];

                // --------------------------------------------------------
                // STEP 4a: Remove the leftmost language occurrence.
                // --------------------------------------------------------
                //
                // Since index "left" is leaving the window, its language count must decrease.
                int beforeRemoval = counts[leftLanguage];
                int afterRemoval = beforeRemoval - 1;
                counts[leftLanguage] = afterRemoval;

                // If the count just dropped from limit + 1 down to limit,
                // that language is no longer violating the cap.
                if (beforeRemoval == limit + 1)
                {
                    badLanguageKinds--;
                }

                // Optional cleanup:
                // removing zero-count entries is not required for correctness,
                // but it keeps the dictionary a little cleaner.
                if (afterRemoval == 0)
                {
                    counts.Remove(leftLanguage);
                }

                // --------------------------------------------------------
                // STEP 4b: Remove expired indices from the fronts of the deques.
                // --------------------------------------------------------
                //
                // If the front index equals "left", it is leaving the window now,
                // so it must be removed.
                if (maxDeque.Count > 0 && maxDeque.First!.Value == left)
                {
                    maxDeque.RemoveFirst();
                }

                if (minDeque.Count > 0 && minDeque.First!.Value == left)
                {
                    minDeque.RemoveFirst();
                }

                // Finally move the left boundary forward.
                left++;
            }

            // ------------------------------------------------------------
            // STEP 5: At this point, the window [left..right] is valid.
            // ------------------------------------------------------------
            //
            // So we can safely update the best answer.
            int currentLength = right - left + 1;
            if (currentLength > best)
            {
                best = currentLength;
            }
        }

        return best;
    }
}

// Demo code

var solution = new Solution();

// Example 1
string[] languages1 = { "py", "java", "py", "cpp", "java", "py" };
int[] severities1 = { 4, 7, 5, 6, 8, 5 };
int limit1 = 2;
int budget1 = 3;
int result1 = solution.LongestValidWindow(languages1, severities1, limit1, budget1);
Console.WriteLine(result1); // Expected: 4

// Example 2
string[] languages2 = { "go", "go", "rs", "go", "rs", "ts", "rs" };
int[] severities2 = { 10, 3, 6, 4, 7, 5, 8 };
int limit2 = 2;
int budget2 = 4;
int result2 = solution.LongestValidWindow(languages2, severities2, limit2, budget2);
Console.WriteLine(result2); // Expected: 5

// Additional quick sanity check
string[] languages3 = { "cpp" };
int[] severities3 = { 42 };
int limit3 = 1;
int budget3 = 0;
int result3 = solution.LongestValidWindow(languages3, severities3, limit3, budget3);
Console.WriteLine(result3); // Expected: 1