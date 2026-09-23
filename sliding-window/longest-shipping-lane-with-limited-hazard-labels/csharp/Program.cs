/*
Title: Longest Shipping Lane With Limited Hazard Labels
Difficulty: Medium
Topic: Sliding Window

Problem Description:
A logistics company records the hazard label attached to each package loaded onto a conveyor belt.
The labels are given as an array of strings `labels`, where `labels[i]` is the hazard category
of the `i`-th package in loading order.

For safety, a supervisor wants to inspect the longest contiguous block of packages such that
the block contains at most `k` distinct hazard categories.

Your task is to return the length of the longest contiguous subarray of `labels` that contains
no more than `k` distinct values.

This models a real monitoring problem: when too many hazard categories appear together,
the inspection procedure becomes too complex, so the company wants the largest continuous
stretch that still stays within the allowed variety.

A contiguous block means you may only choose packages that appear next to each other
in the original order.

Constraints:
- 1 <= labels.length <= 100000
- 1 <= labels[i].length <= 20
- labels[i] consists of uppercase English letters, digits, or underscores
- 1 <= k <= labels.length

Example 1:
Input: labels = ["FLAMMABLE", "CORROSIVE", "FLAMMABLE", "TOXIC", "CORROSIVE", "CORROSIVE"], k = 2
Output: 3
Explanation:
The longest valid block is ["FLAMMABLE", "CORROSIVE", "FLAMMABLE"].
Another block like ["FLAMMABLE", "TOXIC", "CORROSIVE"] would have 3 distinct labels, so it is invalid.
Any valid block with at most 2 distinct labels has maximum length 3.

Example 2:
Input: labels = ["A", "A", "B", "B", "C", "B", "B", "A"], k = 2
Output: 5
Explanation:
One longest valid block is ["B", "B", "C", "B", "B"], which contains only the distinct labels "B" and "C".
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - Each package label is added to the sliding window once by the right pointer.
    - Each package label is removed from the sliding window at most once by the left pointer.
    - Because both pointers only move forward, the total work is linear.

    Space Complexity: O(k) in the typical sliding-window sense, or more precisely O(m)
    - The dictionary stores counts for the distinct labels currently inside the window.
    - At any valid moment, the window contains at most k distinct labels.
    - During shrinking, it may temporarily hold slightly more before being reduced, but still bounded by
      the number of distinct labels encountered in the current active window.
    */
    public int LongestShippingLane(string[] labels, int k)
    {
        // This dictionary will store:
        // key   = a hazard label currently inside the window
        // value = how many times that label appears in the current window
        //
        // Why do we need counts instead of just a set?
        // Because when we move the left side of the window forward, we need to know whether
        // removing one occurrence should completely remove that label from the window
        // or whether the label still remains due to other occurrences.
        var frequency = new Dictionary<string, int>();

        // `left` marks the beginning of the current sliding window.
        int left = 0;

        // `maxLength` stores the best answer found so far.
        int maxLength = 0;

        // We expand the window by moving `right` from left to right across the array.
        for (int right = 0; right < labels.Length; right++)
        {
            // Step 1: Include labels[right] in the current window.
            //
            // The current window is always labels[left..right].
            // By moving `right` one step forward, we are trying to grow the window
            // and see whether this larger window is still valid.
            string currentLabel = labels[right];

            if (!frequency.ContainsKey(currentLabel))
            {
                // If this label is not yet in the window, start its count at 0 first.
                frequency[currentLabel] = 0;
            }

            // Increase the count because currentLabel is now part of the window.
            frequency[currentLabel]++;

            // Step 2: If the window has too many distinct labels, shrink it from the left.
            //
            // The problem requires AT MOST k distinct labels.
            // So while the number of distinct labels is greater than k,
            // the window is invalid and must be reduced.
            while (frequency.Count > k)
            {
                // Identify the label that is leaving the window.
                string leftLabel = labels[left];

                // Decrease its count because we are moving the left boundary forward,
                // which removes one occurrence of leftLabel from the window.
                frequency[leftLabel]--;

                // If the count becomes 0, that means this label no longer exists
                // anywhere in the current window, so we must remove it from the dictionary.
                //
                // This is very important because frequency.Count is our measure of
                // how many distinct labels are currently inside the window.
                if (frequency[leftLabel] == 0)
                {
                    frequency.Remove(leftLabel);
                }

                // Move the left boundary forward by one position.
                left++;
            }

            // Step 3: At this point, the window is guaranteed to be valid.
            //
            // Why?
            // Because the while-loop above keeps shrinking until frequency.Count <= k.
            //
            // So now labels[left..right] is a valid contiguous block with at most k distinct labels.
            int currentWindowLength = right - left + 1;

            // Step 4: Update the best answer if this valid window is larger than any previous one.
            if (currentWindowLength > maxLength)
            {
                maxLength = currentWindowLength;
            }
        }

        // After checking all possible windows formed during the scan,
        // maxLength contains the length of the longest valid contiguous block.
        return maxLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
string[] labels1 =
{
    "FLAMMABLE", "CORROSIVE", "FLAMMABLE", "TOXIC", "CORROSIVE", "CORROSIVE"
};
int k1 = 2;
int result1 = solution.LongestShippingLane(labels1, k1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 3

// Example 2
string[] labels2 =
{
    "A", "A", "B", "B", "C", "B", "B", "A"
};
int k2 = 2;
int result2 = solution.LongestShippingLane(labels2, k2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 5

// Additional quick sanity check
string[] labels3 =
{
    "X", "Y", "Z"
};
int k3 = 3;
int result3 = solution.LongestShippingLane(labels3, k3);
Console.WriteLine("Additional Test Result: " + result3); // Expected: 3