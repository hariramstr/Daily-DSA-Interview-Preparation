"""
Title: Minimum Cost to Plan Study Sessions with Topic Fatigue

Problem Description:
You are preparing a study plan for an upcoming exam. There are n chapters to study
in a fixed order, where chapter i has a study time time[i] and belongs to a
topic topic[i]. You must partition the chapters into contiguous study sessions.
Each session is completed in one sitting.

The cost of a single session is defined as:

    session_cost = max(time in the session) + fatigue_penalty

The fatigue_penalty for a session is the number of times the topic changes
between consecutive chapters inside that session. For example, if the topics
in a session are [2, 2, 5, 5, 3], then the fatigue_penalty is 2 because the
topic changes at 2->5 and 5->3.

Your task is to compute the minimum total cost to finish all chapters by choosing
where to split the sessions.

Since the chapters must be studied in order, every session must be a contiguous
subarray, and every chapter must belong to exactly one session.

Return the minimum possible total cost.

Constraints:
- 1 <= n <= 1000
- 1 <= time[i] <= 10^6
- 1 <= topic[i] <= 10^6
- You may create as many sessions as you want, including one chapter per session
  or one session containing all chapters.
"""

from typing import List


class Solution:
    def min_study_plan_cost(self, time: List[int], topic: List[int]) -> int:
        """
        Compute the minimum total cost to partition chapters into contiguous sessions.

        We use dynamic programming over prefixes:
        - dp[i] = minimum total cost to cover the first i chapters
        - To compute dp[i], we try every possible last session [j..i-1]
          and combine:
              dp[j] + cost_of_session(j, i-1)

        The session cost is:
            max(time[j..i-1]) + number_of_topic_changes_in(topic[j..i-1])

        Args:
            time: Study times for chapters.
            topic: Topic identifiers for chapters.

        Returns:
            Minimum possible total cost.

        Time complexity:
            O(n^2), because for each ending position we scan all possible starts.

        Space complexity:
            O(n), for the DP array.
        """
        n: int = len(time)

        # dp[i] means:
        # "the minimum cost needed to study chapters 0 through i-1"
        #
        # So:
        # - dp[0] = 0 because studying zero chapters costs nothing
        # - dp[n] will be the final answer
        #
        # We initialize with a very large number so that taking minimums works naturally.
        inf: int = 10**18
        dp: List[int] = [inf] * (n + 1)
        dp[0] = 0

        # Outer loop:
        # We compute dp[end] for every prefix length from 1 to n.
        #
        # "end" is the number of chapters covered.
        # Therefore the last chapter index in that prefix is end - 1.
        for end in range(1, n + 1):
            # We will build the last session by extending backwards:
            # possible last session = [start .. end-1]
            #
            # While moving start from end-1 down to 0, we maintain:
            # 1) current_max_time = max(time[start..end-1])
            # 2) topic_changes = number of topic changes inside topic[start..end-1]
            #
            # This lets us evaluate each candidate session in O(1) incremental time,
            # making the whole algorithm O(n^2) instead of O(n^3).
            current_max_time: int = 0
            topic_changes: int = 0

            # We scan start backward so we can update the session statistics
            # as we add one new chapter to the front of the current session.
            for start in range(end - 1, -1, -1):
                # Update the maximum study time in the current session.
                #
                # Since we are adding chapter "start" to the front of the session,
                # the new maximum is simply the max of the old maximum and time[start].
                current_max_time = max(current_max_time, time[start])

                # Update the number of topic changes inside the session.
                #
                # Important idea:
                # When we extend the session from [start+1 .. end-1] to [start .. end-1],
                # the only NEW adjacent pair introduced is:
                #     (start, start+1)
                #
                # Therefore:
                # - if topic[start] != topic[start+1], topic_changes increases by 1
                # - otherwise it stays the same
                #
                # We only do this when start < end - 1, because if start == end - 1,
                # the session has only one chapter and therefore no adjacent pair.
                if start < end - 1 and topic[start] != topic[start + 1]:
                    topic_changes += 1

                # Cost of choosing [start .. end-1] as the final session.
                session_cost: int = current_max_time + topic_changes

                # Total cost if we split right before "start":
                # - dp[start] covers chapters [0 .. start-1]
                # - session_cost covers chapters [start .. end-1]
                candidate_total: int = dp[start] + session_cost

                # Keep the best possible partition for the first "end" chapters.
                if candidate_total < dp[end]:
                    dp[end] = candidate_total

        return dp[n]


if __name__ == "__main__":
    solution = Solution()

    # Sample 1
    # Note:
    # The written explanation in the prompt is internally inconsistent.
    # Under the stated definition:
    # one session [3,1,4,2] has max = 4 and topic changes = 1, so cost = 5.
    # Therefore the mathematically correct answer for this input is 5.
    time1: List[int] = [3, 1, 4, 2]
    topic1: List[int] = [1, 1, 2, 2]
    result1: int = solution.min_study_plan_cost(time1, topic1)
    print("Example 1 result:", result1)

    # Sample 2
    # One session [5,2,6,1,3]:
    # max = 6, topic changes = 2 => total = 8
    time2: List[int] = [5, 2, 6, 1, 3]
    topic2: List[int] = [7, 7, 7, 8, 7]
    result2: int = solution.min_study_plan_cost(time2, topic2)
    print("Example 2 result:", result2)

    # Additional small sanity checks
    time3: List[int] = [10]
    topic3: List[int] = [42]
    result3: int = solution.min_study_plan_cost(time3, topic3)
    print("Single chapter result:", result3)

    time4: List[int] = [2, 2, 2]
    topic4: List[int] = [1, 2, 3]
    result4: int = solution.min_study_plan_cost(time4, topic4)
    print("All topic changes result:", result4)