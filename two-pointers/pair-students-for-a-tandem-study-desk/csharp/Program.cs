/*
Title: Pair Students for a Tandem Study Desk

Problem Description:
A library has a row of students waiting to use tandem study desks. Each desk can seat exactly two students, and the combined backpack sizes of the two students assigned to the same desk must not exceed a given limit. You are given an integer array backpackSizes where backpackSizes[i] is the size of the i-th student's backpack, and an integer deskLimit representing the maximum total size allowed at one desk.

Return the minimum number of tandem desks needed to seat all students.

Each desk may hold either one student or two students. If two students are paired together, their combined backpack sizes must be less than or equal to deskLimit. You may rearrange students in any way before assigning desks.

Your goal is to use as few desks as possible.

This is an interview-friendly problem that rewards sorting and a two-pointer strategy. After sorting the backpack sizes, you can try to pair the student with the smallest backpack with the student with the largest backpack still unassigned. If they fit together, place them at one desk. Otherwise, the largest backpack must sit alone.

Constraints:
- 1 <= backpackSizes.length <= 50000
- 1 <= backpackSizes[i] <= deskLimit <= 100000
- Every student must be assigned to exactly one desk

Examples:
1) backpackSizes = [2, 3, 4, 5], deskLimit = 6
   Output: 3

2) backpackSizes = [1, 2, 2, 3], deskLimit = 3
   Correct Output: 3
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n log n)
    - We sort the array once, which costs O(n log n).
    - Then we scan it with two pointers in O(n).

    Space Complexity: O(1) extra space (ignoring the sorting implementation details)
    - We sort the input array in place.
    - We only use a few integer variables after that.
    */
    public int MinTandemDesks(int[] backpackSizes, int deskLimit)
    {
        // Step 1: Sort the backpack sizes in non-decreasing order.
        //
        // What this does:
        // - It arranges students from smallest backpack to largest backpack.
        //
        // Why this is necessary:
        // - Sorting allows us to make a greedy decision efficiently.
        // - Once sorted, we can try to pair the lightest remaining student
        //   with the heaviest remaining student.
        // - If even the lightest student cannot pair with the heaviest one,
        //   then the heaviest student cannot pair with anyone at all,
        //   because everyone else is at least as large as the lightest.
        //
        // Data structure choice:
        // - We use the input array itself and sort it in place.
        // - This is memory-efficient and ideal for this problem.
        Array.Sort(backpackSizes);

        // Step 2: Create two pointers.
        //
        // left:
        // - Points to the smallest backpack size not yet assigned to a desk.
        //
        // right:
        // - Points to the largest backpack size not yet assigned to a desk.
        //
        // Why this is necessary:
        // - This lets us consider the best possible pairing for the largest student.
        // - We want to use as few desks as possible, so whenever possible,
        //   we try to place two students together.
        int left = 0;
        int right = backpackSizes.Length - 1;

        // Step 3: Track how many desks we use.
        //
        // Every loop iteration will assign at least the student at "right"
        // to a desk, either:
        // - alone, or
        // - paired with the student at "left".
        int desksNeeded = 0;

        // Step 4: Process students until all have been assigned.
        //
        // The condition left <= right means:
        // - There is at least one unassigned student remaining.
        while (left <= right)
        {
            // Step 4a: Check whether the smallest and largest remaining students
            // can share one desk.
            //
            // What this does:
            // - We test the sum of the two extreme values.
            //
            // Why this is the right greedy choice:
            // - The largest student is the hardest one to pair.
            // - If we can pair them with the smallest student, that is always safe
            //   and helps save a desk.
            // - If we cannot pair the largest with the smallest, then the largest
            //   cannot pair with anyone, so they must sit alone.
            if (backpackSizes[left] + backpackSizes[right] <= deskLimit)
            {
                // The pair fits within the desk limit.
                //
                // So we assign both students to one desk.
                // We move both pointers inward because both students are now seated.
                left++;
                right--;
            }
            else
            {
                // The pair does NOT fit.
                //
                // This means the largest student at "right" cannot pair with the
                // smallest remaining student. Since all other remaining students
                // are larger than or equal to backpackSizes[left], the largest
                // student cannot pair with any of them either.
                //
                // Therefore, the largest student must sit alone.
                // We only move the right pointer because only that student is assigned.
                right--;
            }

            // Step 4b: In both cases above, we used exactly one desk:
            // - either for a pair, or
            // - for one student sitting alone.
            desksNeeded++;
        }

        // Step 5: Return the total number of desks used.
        return desksNeeded;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] backpackSizes1 = { 2, 3, 4, 5 };
int deskLimit1 = 6;
int result1 = solution.MinTandemDesks((int[])backpackSizes1.Clone(), deskLimit1);
Console.WriteLine("Example 1:");
Console.WriteLine($"Input: [{string.Join(", ", backpackSizes1)}], deskLimit = {deskLimit1}");
Console.WriteLine($"Output: {result1}");
Console.WriteLine("Expected: 3");
Console.WriteLine();

// Example 2
int[] backpackSizes2 = { 1, 2, 2, 3 };
int deskLimit2 = 3;
int result2 = solution.MinTandemDesks((int[])backpackSizes2.Clone(), deskLimit2);
Console.WriteLine("Example 2:");
Console.WriteLine($"Input: [{string.Join(", ", backpackSizes2)}], deskLimit = {deskLimit2}");
Console.WriteLine($"Output: {result2}");
Console.WriteLine("Expected: 3");
Console.WriteLine();

// Additional demo
int[] backpackSizes3 = { 3, 5, 3, 4 };
int deskLimit3 = 5;
int result3 = solution.MinTandemDesks((int[])backpackSizes3.Clone(), deskLimit3);
Console.WriteLine("Additional Demo:");
Console.WriteLine($"Input: [{string.Join(", ", backpackSizes3)}], deskLimit = {deskLimit3}");
Console.WriteLine($"Output: {result3}");
Console.WriteLine("Expected: 4");