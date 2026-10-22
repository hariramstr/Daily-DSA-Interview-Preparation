/*
Title: Find the Last Passed Practice Exam
Difficulty: Easy
Topic: Binary Search

Problem Description:
You are given a sorted array `scores` where `scores[i]` is the score of the `i`-th practice exam taken by a student.
The array is sorted in non-decreasing order, so later values may be equal to earlier ones.
You are also given an integer `passMark`, representing the minimum score required to pass.

Your task is to return the index of the last exam whose score is less than or equal to `passMark`
if the goal is to find the final exam result that still counts as passing under a given cutoff.
If no exam score is less than or equal to `passMark`, return `-1`.

Because the scores are already sorted, the expected solution should use binary search instead of
scanning the entire array. This makes the solution efficient even when the number of exams is large.

Constraints:
- 1 <= scores.length <= 100000
- 0 <= scores[i] <= 10^9
- scores is sorted in non-decreasing order
- 0 <= passMark <= 10^9

Example 1:
Input: scores = [35, 42, 42, 58, 71, 88], passMark = 58
Output: 3
Explanation: The last score that is less than or equal to 58 is 58, which appears at index 3.

Example 2:
Input: scores = [60, 75, 80], passMark = 50
Output: -1
Explanation: Every score is greater than 50, so there is no valid passing exam under this cutoff.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(log n)
    Space Complexity: O(1)

    We use binary search because the input array is already sorted.
    That allows us to repeatedly cut the search range in half,
    which is much faster than checking every element one by one.
    */
    public int FindLastPassedPracticeExam(int[] scores, int passMark)
    {
        // `left` points to the beginning of the current search range.
        // We start at index 0 because that is the first valid position in the array.
        int left = 0;

        // `right` points to the end of the current search range.
        // We start at the last index in the array because that is the final valid position.
        int right = scores.Length - 1;

        // `answer` stores the best valid index we have found so far.
        // We initialize it to -1 because that represents "no valid exam found yet".
        // If every score is greater than passMark, this value will remain -1,
        // which is exactly what the problem asks us to return.
        int answer = -1;

        // Continue searching while there is still a valid range to inspect.
        // The condition `left <= right` means there is at least one candidate index left.
        while (left <= right)
        {
            // Compute the middle index of the current search range.
            // We use this form instead of `(left + right) / 2` to avoid integer overflow
            // in general binary search patterns, even though the current constraints are safe.
            int mid = left + (right - left) / 2;

            // Read the score at the middle position once and store it in a variable.
            // This makes the code easier to read and avoids repeating `scores[mid]`.
            int currentScore = scores[mid];

            // Case 1:
            // If the middle score is less than or equal to the passMark,
            // then this index is a VALID candidate answer.
            if (currentScore <= passMark)
            {
                // Since this index satisfies the condition, record it as the best answer so far.
                // However, it may not be the LAST such index.
                // There could be another valid score further to the right.
                answer = mid;

                // Because the array is sorted in non-decreasing order:
                // - everything to the left of `mid` is <= or possibly smaller
                // - but the LAST valid index, if it exists beyond `mid`, must be on the right side
                //
                // So we move `left` to `mid + 1` to continue searching the right half.
                left = mid + 1;
            }
            else
            {
                // Case 2:
                // If the middle score is greater than passMark,
                // then this index is NOT valid.
                //
                // Because the array is sorted, every element to the right of `mid`
                // is also greater than or equal to `currentScore`, which means those values
                // are also too large to be valid answers.
                //
                // Therefore, we can safely discard the entire right half including `mid`
                // and continue searching only in the left half.
                right = mid - 1;
            }
        }

        // When the loop ends, `answer` contains:
        // - the index of the last score <= passMark, if one exists
        // - or -1 if no such score exists
        return answer;
    }
}

// Demo code:
// Create a solution object so we can call the algorithm method.
var solution = new Solution();

// Example 1 from the problem description:
// scores = [35, 42, 42, 58, 71, 88], passMark = 58
// The last score <= 58 is 58 at index 3.
int[] scores1 = { 35, 42, 42, 58, 71, 88 };
int passMark1 = 58;
int result1 = solution.FindLastPassedPracticeExam(scores1, passMark1);
Console.WriteLine(result1); // Expected output: 3

// Example 2 from the problem description:
// scores = [60, 75, 80], passMark = 50
// No score is <= 50, so the answer is -1.
int[] scores2 = { 60, 75, 80 };
int passMark2 = 50;
int result2 = solution.FindLastPassedPracticeExam(scores2, passMark2);
Console.WriteLine(result2); // Expected output: -1

// Additional demo:
// scores = [10, 20, 20, 20, 30], passMark = 20
// The last score <= 20 is at index 3.
int[] scores3 = { 10, 20, 20, 20, 30 };
int passMark3 = 20;
int result3 = solution.FindLastPassedPracticeExam(scores3, passMark3);
Console.WriteLine(result3); // Expected output: 3