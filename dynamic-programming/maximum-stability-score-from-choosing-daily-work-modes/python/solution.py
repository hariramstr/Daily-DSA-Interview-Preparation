"""
Title: Maximum Stability Score from Choosing Daily Work Modes

Problem Description:
A product team plans work over n days. On each day i, the team may operate in exactly
one of three modes: Focus, Meeting, or Recovery. If the team chooses mode m on day i,
it earns score[i][m] stability points. However, using the same mode on consecutive
days causes burnout, so the same mode cannot be chosen on two adjacent days.

Your task is to compute the maximum total stability score the team can earn over all
n days while respecting this rule.

Formally, you are given a 2D integer array score of size n x 3, where score[i][0],
score[i][1], and score[i][2] represent the points earned on day i for choosing Focus,
Meeting, and Recovery respectively. Choose exactly one mode per day such that for every
i > 0, the chosen mode on day i is different from the chosen mode on day i - 1.
Return the maximum possible sum of points.

This problem is a classic dynamic programming problem because the best choice for the
current day depends only on the best results from the previous day.
"""

from typing import List


class Solution:
    def maximum_stability_score(self, score: List[List[int]]) -> int:
        """
        Compute the maximum total stability score over all days without choosing
        the same mode on two consecutive days.

        Args:
            score: A 2D list where score[i][0], score[i][1], and score[i][2]
                are the points for Focus, Meeting, and Recovery on day i.

        Returns:
            The maximum total stability score achievable.

        Time Complexity:
            O(n), where n is the number of days. We process each day once.

        Space Complexity:
            O(1) extra space, because we only keep track of the previous day's
            three dynamic programming values.
        """
        # If there is only one day, the answer is simply the best mode on that day.
        # This also naturally works with the general DP initialization below,
        # but stating it here helps beginners understand the base case.
        if not score:
            return 0

        # -----------------------------
        # Dynamic Programming Idea
        # -----------------------------
        # Let:
        #   prev_focus    = best total score up to the previous day
        #                   if the previous day ended in Focus
        #   prev_meeting  = best total score up to the previous day
        #                   if the previous day ended in Meeting
        #   prev_recovery = best total score up to the previous day
        #                   if the previous day ended in Recovery
        #
        # For the current day:
        # - If we choose Focus today, then yesterday cannot also be Focus.
        #   So we must come from max(prev_meeting, prev_recovery).
        #
        # - If we choose Meeting today, then yesterday cannot also be Meeting.
        #   So we must come from max(prev_focus, prev_recovery).
        #
        # - If we choose Recovery today, then yesterday cannot also be Recovery.
        #   So we must come from max(prev_focus, prev_meeting).
        #
        # This is exactly why dynamic programming works well here:
        # each day's best values depend only on the previous day's best values.

        # -----------------------------
        # Base Case: Day 0
        # -----------------------------
        # On the first day, there is no previous day, so we can freely choose
        # any of the three modes. Therefore, the best totals ending in each mode
        # are simply the scores of day 0 itself.
        prev_focus: int = score[0][0]
        prev_meeting: int = score[0][1]
        prev_recovery: int = score[0][2]

        # -----------------------------
        # Process Days 1 to n - 1
        # -----------------------------
        # We iterate through each remaining day and compute the best possible
        # total if we end that day in each of the three modes.
        for day in range(1, len(score)):
            # Read today's scores into clearly named variables.
            # This improves readability and makes the transition formulas easier
            # to understand.
            focus_points: int = score[day][0]
            meeting_points: int = score[day][1]
            recovery_points: int = score[day][2]

            # If we choose Focus today:
            # yesterday must have been either Meeting or Recovery.
            current_focus: int = focus_points + max(prev_meeting, prev_recovery)

            # If we choose Meeting today:
            # yesterday must have been either Focus or Recovery.
            current_meeting: int = meeting_points + max(prev_focus, prev_recovery)

            # If we choose Recovery today:
            # yesterday must have been either Focus or Meeting.
            current_recovery: int = recovery_points + max(prev_focus, prev_meeting)

            # Move the "current day" results into the "previous day" variables
            # for the next iteration.
            prev_focus = current_focus
            prev_meeting = current_meeting
            prev_recovery = current_recovery

        # -----------------------------
        # Final Answer
        # -----------------------------
        # After processing all days, the schedule may end in any of the three modes.
        # So the answer is the maximum among the three best totals for the last day.
        return max(prev_focus, prev_meeting, prev_recovery)


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    score1: List[List[int]] = [[8, 5, 6], [7, 9, 4], [3, 8, 10], [9, 2, 7]]
    result1: int = solution.maximum_stability_score(score1)
    print("Example 1 Output:", result1)  # Expected: 36

    # Example 2
    score2: List[List[int]] = [[4, 4, 7], [6, 1, 5], [2, 9, 3]]
    result2: int = solution.maximum_stability_score(score2)
    print("Example 2 Output:", result2)  # Expected: 22

    # Additional quick check
    score3: List[List[int]] = [[10, 1, 1]]
    result3: int = solution.maximum_stability_score(score3)
    print("Single Day Output:", result3)  # Expected: 10