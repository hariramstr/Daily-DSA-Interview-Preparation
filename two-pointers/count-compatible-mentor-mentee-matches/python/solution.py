"""
Title: Count Compatible Mentor-Mentee Matches

Problem Description:
You are given two integer arrays, `mentors` and `mentees`, representing skill ratings
of available mentors and incoming mentees. A mentor can be paired with at most one
mentee, and a mentee can be paired with at most one mentor. A pair is considered
compatible if the mentor's skill is greater than or equal to the mentee's required
skill, and the difference between their skills is at most `maxGap`.

Your task is to return the maximum number of compatible mentor-mentee pairs that can
be formed.

For example, if a mentee requires skill 5, then a mentor with skill 4 cannot help,
a mentor with skill 5 is valid, and a mentor with skill 9 is only valid if
`maxGap >= 4`.

You may reorder the arrays in any way when forming pairs. The goal is not to minimize
total mismatch, but simply to maximize the number of valid one-to-one matches.

This problem is intended to test sorting and two-pointer reasoning. An efficient
solution should avoid checking every possible pair.

Constraints:
- 1 <= mentors.length, mentees.length <= 2 * 10^5
- 1 <= mentors[i], mentees[i] <= 10^9
- 0 <= maxGap <= 10^9

Example 1:
Input: mentors = [6, 3, 8, 10], mentees = [2, 5, 7, 9], maxGap = 2
Output: 3
Explanation: One optimal pairing is (3,2), (6,5), and (10,9). Mentor 8 cannot pair
with mentee 7 if it has already been used elsewhere, so the maximum total is 3.

Example 2:
Input: mentors = [4, 4, 6], mentees = [3, 4, 5, 6], maxGap = 0
Output: 2
Explanation: With maxGap = 0, only equal skills are allowed. We can pair (4,4) and
(6,6), so the answer is 2.
"""

from typing import List


class Solution:
    def max_compatible_pairs(
        self, mentors: List[int], mentees: List[int], maxGap: int
    ) -> int:
        """
        Compute the maximum number of valid mentor-mentee pairs.

        A valid pair must satisfy:
        - mentor >= mentee
        - mentor - mentee <= maxGap

        The method sorts both arrays and uses a greedy two-pointer scan to build
        as many valid one-to-one matches as possible.

        Args:
            mentors: List of mentor skill ratings.
            mentees: List of mentee required skill ratings.
            maxGap: Maximum allowed skill difference.

        Returns:
            The maximum number of compatible pairs.

        Time complexity:
            O(m log m + n log n), where m = len(mentors), n = len(mentees),
            due to sorting. The two-pointer scan is linear.

        Space complexity:
            O(m + n) in the worst case if sorting creates new lists.
            If sorting in place is considered, auxiliary scan space is O(1).
        """
        # Step 1: Sort both lists.
        #
        # Why sort?
        # ----------
        # Sorting allows us to compare the "smallest remaining" mentor and mentee
        # in a structured way. This is the key to using a greedy strategy.
        #
        # Once sorted:
        # - We can try to satisfy smaller mentee requirements first.
        # - We avoid wasting a strong mentor on a mentee that could have been handled
        #   by a weaker mentor.
        # - We can move through both arrays with pointers in linear time after sorting.
        mentors.sort()
        mentees.sort()

        # Pointer `i` will walk through mentors.
        # Pointer `j` will walk through mentees.
        i: int = 0
        j: int = 0

        # This will count how many successful one-to-one matches we form.
        matches: int = 0

        # Step 2: Scan both sorted arrays with two pointers.
        #
        # We continue while both sides still have unprocessed people.
        while i < len(mentors) and j < len(mentees):
            mentor_skill: int = mentors[i]
            mentee_need: int = mentees[j]

            # For a valid match, mentor skill must be inside:
            # [mentee_need, mentee_need + maxGap]
            #
            # There are three cases to consider.

            # Case A:
            # The current mentor is too weak for the current mentee.
            #
            # mentor_skill < mentee_need
            #
            # This mentor cannot help this mentee, and because mentees are sorted,
            # this mentor also cannot help any later mentee (later mentees need
            # the same or even more skill).
            #
            # Therefore, this mentor is useless for all remaining mentees, so we
            # safely discard this mentor by moving `i` forward.
            if mentor_skill < mentee_need:
                i += 1

            # Case B:
            # The current mentor is strong enough, but the gap is too large.
            #
            # mentor_skill > mentee_need + maxGap
            #
            # This means the mentor overshoots the acceptable range for this mentee.
            # Since mentors are sorted, every later mentor will be equal or even larger,
            # so none of them can match this mentee either.
            #
            # Therefore, this mentee can never be matched, so we discard the mentee
            # by moving `j` forward.
            elif mentor_skill > mentee_need + maxGap:
                j += 1

            # Case C:
            # The current mentor falls inside the valid interval:
            # mentee_need <= mentor_skill <= mentee_need + maxGap
            #
            # This is a valid compatible pair.
            #
            # Greedy choice:
            # We match them immediately.
            #
            # Why is this safe?
            # -----------------
            # Because we are using the smallest remaining mentor that can validly
            # satisfy the smallest remaining mentee. Saving this mentor for later
            # would not help produce more matches, and using a larger mentor here
            # could waste flexibility needed for future mentees.
            else:
                matches += 1
                i += 1
                j += 1

        # After the loop ends, at least one list is exhausted, so no more pairs
        # can be formed.
        return matches


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    mentors1: List[int] = [6, 3, 8, 10]
    mentees1: List[int] = [2, 5, 7, 9]
    max_gap1: int = 2
    result1: int = solution.max_compatible_pairs(mentors1, mentees1, max_gap1)
    print("Example 1 Result:", result1)

    # Manual verification for Example 1:
    # Sorted mentors = [3, 6, 8, 10]
    # Sorted mentees = [2, 5, 7, 9]
    #
    # Compare 3 with 2:
    #   2 <= 3 <= 4  -> valid, match count = 1
    # Compare 6 with 5:
    #   5 <= 6 <= 7  -> valid, match count = 2
    # Compare 8 with 7:
    #   7 <= 8 <= 9  -> valid, match count = 3
    # Compare 10 with 9:
    #   9 <= 10 <= 11 -> valid, match count = 4
    #
    # Therefore, the correct maximum for the stated rules is 4.
    # The problem statement says 3, but under the given compatibility definition,
    # 8 can pair with 7 and 10 can pair with 9 simultaneously, so 4 is achievable.

    # Example 2
    mentors2: List[int] = [4, 4, 6]
    mentees2: List[int] = [3, 4, 5, 6]
    max_gap2: int = 0
    result2: int = solution.max_compatible_pairs(mentors2, mentees2, max_gap2)
    print("Example 2 Result:", result2)

    # Manual verification for Example 2:
    # Sorted mentors = [4, 4, 6]
    # Sorted mentees = [3, 4, 5, 6]
    #
    # maxGap = 0 means only exact equality is allowed.
    # 4 cannot match 3 because mentor must be in [3, 3].
    # So mentee 3 is skipped.
    # 4 matches 4 -> count = 1
    # next 4 cannot match 5 because too weak, so mentor skipped
    # 6 matches 6 -> count = 2
    #
    # Final answer = 2, which matches the example statement.
