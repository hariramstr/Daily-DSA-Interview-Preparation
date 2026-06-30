/*
Title: Longest Typing Burst With Limited Hand Switches
Difficulty: Medium
Topic: Sliding Window

Problem Description:
You are given a string s representing a sequence of keys typed on a custom keyboard.
Each character belongs to either the left hand or the right hand. You are also given
a mapping string handMap of length 26, where handMap[i] is either 'L' or 'R',
indicating whether the lowercase letter ('a' + i) is typed with the left or right hand.

A contiguous substring of s is called a smooth typing burst if the number of times
the typist switches hands between adjacent characters in that substring is at most k.

For example, if the substring is "abca" and the hand sequence is L L R L, then the
number of hand switches is 2:
- between the second and third characters
- between the third and fourth characters

Return the length of the longest smooth typing burst.

A hand switch is counted only between neighboring characters inside the chosen substring.
A substring of length 0 has length 0, and a substring of length 1 always has 0 switches.

Constraints:
- 1 <= s.length <= 2 * 10^5
- 0 <= k < s.length
- s contains only lowercase English letters
- handMap.length == 26
- Every character in handMap is either 'L' or 'R'
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We move the right pointer from left to right exactly once.
    - We move the left pointer from left to right at most once per character.
    - Therefore, every character is processed a constant number of times.

    Space Complexity: O(n)
    - We store the hand ('L' or 'R') for each character in the string in an array.
    - This could be reduced to O(1) extra space with direct lookups, but this version
      is intentionally beginner-friendly and easier to trace.
    */
    public int LongestTypingBurst(string s, int k, string handMap)
    {
        int n = s.Length;

        // Convert every character in the input string into its corresponding hand.
        //
        // Why do this?
        // The actual letters do not matter once we know whether each one is typed
        // by the left hand or the right hand. The problem is really about counting
        // how many times neighboring hand values change inside a window.
        //
        // Example:
        // s = "abac"
        // hand sequence might become: L L R L
        //
        // Then the problem becomes:
        // "Find the longest contiguous segment with at most k adjacent changes."
        char[] hands = new char[n];
        for (int i = 0; i < n; i++)
        {
            hands[i] = handMap[s[i] - 'a'];
        }

        // left and right define our current sliding window [left, right].
        //
        // We will expand right one step at a time.
        // If the window becomes invalid (too many hand switches),
        // we will move left forward until the window becomes valid again.
        int left = 0;

        // This variable stores the number of hand switches currently inside the window.
        //
        // Important detail:
        // A hand switch is counted between adjacent positions only.
        // So for a window [left, right], the relevant adjacent pairs are:
        // (left, left+1), (left+1, left+2), ..., (right-1, right)
        int switches = 0;

        // This will store the best (maximum) valid window length found so far.
        int best = 0;

        // Expand the window by moving right from 0 to n - 1.
        for (int right = 0; right < n; right++)
        {
            // STEP 1: Add the new character at position 'right' into the window.
            //
            // When we extend the window from [left, right-1] to [left, right],
            // only ONE new adjacent pair is introduced:
            // (right - 1, right)
            //
            // If those two positions use different hands, then the number of
            // switches inside the window increases by 1.
            //
            // We do not need to recount the whole window. This is the key idea
            // that makes the algorithm efficient.
            if (right > 0 && hands[right] != hands[right - 1])
            {
                switches++;
            }

            // STEP 2: If the window has too many switches, shrink it from the left.
            //
            // Why is shrinking correct?
            // Because we want a contiguous substring, and the current window is invalid.
            // The only way to make it valid again while keeping 'right' fixed is to
            // move 'left' forward.
            //
            // What happens when we remove the character at 'left'?
            // The adjacent pair (left, left + 1) leaves the window.
            // If that pair was a switch, then the switch count decreases by 1.
            //
            // We repeat until switches <= k.
            while (switches > k)
            {
                // Before incrementing left, check whether the pair (left, left + 1)
                // contributes a switch inside the current window.
                //
                // If left < right, then such a pair exists.
                // If the hands differ, removing position 'left' removes one switch.
                if (left < right && hands[left] != hands[left + 1])
                {
                    switches--;
                }

                left++;
            }

            // STEP 3: Now the window [left, right] is valid.
            // Update the answer with its length.
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
string s1 = "abacabad";
int k1 = 2;
string handMap1 = "LLRLRRLLRLRLRLRLRLRLRLRLRL";
int result1 = solution.LongestTypingBurst(s1, k1, handMap1);
Console.WriteLine(result1); // Expected: 5

// Example 2
string s2 = "zzxyyx";
int k2 = 1;
string handMap2 = "LRLRLRLRLRLRLRLRLRLRLRLRLR";
int result2 = solution.LongestTypingBurst(s2, k2, handMap2);
Console.WriteLine(result2); // Based on the stated example: Expected: 4

// Additional quick checks
string s3 = "aaaaa";
int k3 = 0;
string handMap3 = "LLLLLLLLLLLLLLLLLLLLLLLLLL";
int result3 = solution.LongestTypingBurst(s3, k3, handMap3);
Console.WriteLine(result3); // Expected: 5

string s4 = "abcd";
int k4 = 0;
string handMap4 = "LRLRLLLLLLLLLLLLLLLLLLLLLL";
int result4 = solution.LongestTypingBurst(s4, k4, handMap4);
Console.WriteLine(result4); // Expected: 1