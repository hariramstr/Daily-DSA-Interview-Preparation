/*
Title: Shortest Booth Segment Covering All Payment Methods
Difficulty: Medium
Topic: Two Pointers

Problem Description:
At a large outdoor festival, booths are arranged in a single row. Each booth supports exactly one payment method,
represented by a string in the array methods, where methods[i] is the payment method accepted by the i-th booth.
Festival organizers want to place a help desk so that visitors can walk to a contiguous segment of booths that
includes every distinct payment method used anywhere in the festival at least once.

Your task is to return the length of the shortest contiguous segment of booths that contains all distinct payment
methods present in the entire array. If the array is empty, return 0.

This is a practical optimization problem: the answer is not asking for all such segments, only the minimum possible
length. The payment methods are case-sensitive, so "Card" and "card" are different methods.

Constraints:
- 0 <= methods.length <= 100000
- 1 <= methods[i].length <= 20
- methods[i] consists of English letters, digits, underscores, or hyphens
- The expected solution should run in O(n) time using a two-pointers / sliding window approach

Example 1:
Input: methods = ["cash", "card", "voucher", "cash", "qr", "card", "qr"]
Output: 4
Explanation: The distinct payment methods are {"cash", "card", "voucher", "qr"}.
One shortest valid segment is ["voucher", "cash", "qr", "card"], which has length 4.

Example 2:
Input: methods = ["applepay", "applepay", "cash"]
Output: 2
Explanation: The distinct payment methods are {"applepay", "cash"}.
The shortest segment covering both is ["applepay", "cash"].
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We first scan the array once to find all distinct payment methods.
    - Then we use a sliding window where each pointer moves from left to right at most once.
    - Because each element is added to the window once and removed from the window once, the total work is linear.

    Space Complexity: O(k)
    - k is the number of distinct payment methods in the array.
    - We store:
      1) a HashSet of all distinct methods
      2) a Dictionary for counts of methods inside the current window
    */
    public int ShortestSegmentCoveringAllMethods(string[] methods)
    {
        // Step 1:
        // Handle the edge case where the input array is empty.
        //
        // Why this is necessary:
        // If there are no booths, then there is no segment to consider.
        // The problem explicitly says to return 0 in this case.
        if (methods == null || methods.Length == 0)
        {
            return 0;
        }

        // Step 2:
        // Find how many distinct payment methods exist in the entire festival.
        //
        // Why this is necessary:
        // Our sliding window must contain ALL distinct methods that appear anywhere in the array.
        // So before we can know whether a window is valid, we must know how many unique methods
        // we are trying to cover.
        //
        // Data structure choice:
        // HashSet<string> is ideal for collecting unique values efficiently.
        // It gives average O(1) insertion and lookup.
        HashSet<string> allDistinctMethods = new HashSet<string>(methods);

        // This is the target number of unique methods that every valid window must contain.
        int requiredDistinctCount = allDistinctMethods.Count;

        // Step 3:
        // Prepare the sliding window data structures.
        //
        // We will maintain a window [left, right].
        // As we expand right, we include more booths.
        // As we move left forward, we try to shrink the window while keeping it valid.
        //
        // Data structure choice:
        // Dictionary<string, int> windowCounts stores how many times each payment method
        // appears inside the current window.
        //
        // Why counts are needed instead of just a set:
        // When shrinking the window from the left, we need to know whether removing one booth
        // causes a payment method to disappear completely from the window.
        Dictionary<string, int> windowCounts = new Dictionary<string, int>();

        // left pointer of the sliding window
        int left = 0;

        // This tracks how many distinct payment methods are currently represented in the window.
        //
        // Important meaning:
        // "formedDistinctCount" is the number of payment methods whose count in the current window is at least 1.
        int formedDistinctCount = 0;

        // Initialize answer with the largest possible value.
        // We will minimize this whenever we find a valid window.
        int minLength = int.MaxValue;

        // Step 4:
        // Expand the window by moving the right pointer from left to right across the array.
        //
        // Why this is necessary:
        // We need to consider contiguous segments, and sliding window is the standard O(n) way
        // to explore them efficiently without checking every possible subarray.
        for (int right = 0; right < methods.Length; right++)
        {
            // The payment method at the booth we are adding to the window.
            string currentMethod = methods[right];

            // Step 4a:
            // Add the current method to the window count.
            //
            // Why this is necessary:
            // The window now includes booth "right", so we must update our bookkeeping.
            if (!windowCounts.ContainsKey(currentMethod))
            {
                windowCounts[currentMethod] = 0;
            }

            windowCounts[currentMethod]++;

            // Step 4b:
            // If this method's count became 1, then this method has just appeared in the window
            // for the first time.
            //
            // Why this matters:
            // That means the number of distinct methods currently covered by the window increases by 1.
            if (windowCounts[currentMethod] == 1)
            {
                formedDistinctCount++;
            }

            // Step 5:
            // If the current window contains all distinct payment methods,
            // then it is a valid candidate answer.
            //
            // We now try to shrink it from the left as much as possible while keeping it valid.
            //
            // Why this is necessary:
            // Expanding right helps us reach validity.
            // Shrinking left helps us find the shortest valid window ending at this right position.
            while (formedDistinctCount == requiredDistinctCount)
            {
                // Current window length is right - left + 1 because both ends are inclusive.
                int currentWindowLength = right - left + 1;

                // Update the best answer if this valid window is smaller than any previously found.
                if (currentWindowLength < minLength)
                {
                    minLength = currentWindowLength;
                }

                // Step 5a:
                // Prepare to remove the leftmost booth from the window.
                //
                // Why this is necessary:
                // We want to see if the window can remain valid after removing unnecessary booths
                // from the left side.
                string leftMethod = methods[left];

                // Decrease the count because booth "left" is leaving the window.
                windowCounts[leftMethod]--;

                // Step 5b:
                // If the count becomes 0, then this payment method is no longer present in the window.
                //
                // Why this matters:
                // The window stops covering all distinct methods, so it is no longer valid.
                if (windowCounts[leftMethod] == 0)
                {
                    formedDistinctCount--;
                }

                // Move left forward to actually shrink the window.
                left++;
            }
        }

        // Step 6:
        // Return the smallest valid window length found.
        //
        // Because the entire array always contains all distinct methods by definition,
        // and the array is non-empty here, there is always at least one valid window:
        // the whole array itself.
        return minLength;
    }
}

// Demo code

var solution = new Solution();

// Example 1
string[] methods1 = { "cash", "card", "voucher", "cash", "qr", "card", "qr" };
int result1 = solution.ShortestSegmentCoveringAllMethods(methods1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 4

// Example 2
string[] methods2 = { "applepay", "applepay", "cash" };
int result2 = solution.ShortestSegmentCoveringAllMethods(methods2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 2

// Additional demo: empty input
string[] methods3 = Array.Empty<string>();
int result3 = solution.ShortestSegmentCoveringAllMethods(methods3);
Console.WriteLine("Empty Input Result: " + result3); // Expected: 0

// Additional demo: already minimal
string[] methods4 = { "a", "b", "c" };
int result4 = solution.ShortestSegmentCoveringAllMethods(methods4);
Console.WriteLine("All Unique Result: " + result4); // Expected: 3

// Additional demo: repeated values
string[] methods5 = { "x", "x", "x", "y", "z", "y", "z" };
int result5 = solution.ShortestSegmentCoveringAllMethods(methods5);
Console.WriteLine("Repeated Values Result: " + result5); // Expected: 3