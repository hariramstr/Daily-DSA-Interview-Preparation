"""
Title: Minimum Playback Speed for Museum Audio Guides

Problem Description:
A museum offers a fixed sequence of audio guide sections that must be listened to in order.
The i-th section has length guides[i] minutes at normal speed. Visitors may choose a
constant playback speed s, where s is a positive integer, and every section is played at
that same speed. A section that would take x minutes at normal speed takes ceil(x / s)
whole minutes to finish because the museum app only advances to the next section at the
start of the next minute.

Given an array guides and an integer limit, return the minimum integer playback speed s
such that the total listening time of all sections is at most limit minutes. If it is
impossible even at arbitrarily large speed, return -1.

Key observations:
- Total time at speed s is: sum(ceil(guides[i] / s)) for all i.
- As speed increases, total required time never increases.
- This monotonic behavior makes binary search the correct efficient approach.

Constraint note:
- If limit < len(guides), the answer is always -1 because each section takes at least
  1 whole minute no matter how large the speed is.
"""

from typing import List


class Solution:
    def _required_time(self, guides: List[int], speed: int) -> int:
        """
        Compute the total listening time needed at a given playback speed.

        Args:
            guides: List of section lengths in minutes at normal speed.
            speed: Chosen integer playback speed.

        Returns:
            The total whole-number minutes required to listen to all sections.

        Time complexity:
            O(n), where n is the number of sections.

        Space complexity:
            O(1), ignoring input storage.
        """
        # We accumulate the total number of minutes needed for all sections.
        total_time: int = 0

        # We process each section independently because the total time is simply
        # the sum of the rounded-up time for each section.
        for length in guides:
            # Instead of using math.ceil(length / speed), we use integer arithmetic:
            # ceil(a / b) == (a + b - 1) // b
            #
            # This is preferred because:
            # 1. It avoids floating-point operations.
            # 2. It is exact for integers.
            # 3. It is fast and common in interview problems.
            total_time += (length + speed - 1) // speed

        return total_time

    def minPlaybackSpeed(self, guides: List[int], limit: int) -> int:
        """
        Find the minimum integer playback speed so total listening time is at most limit.

        Args:
            guides: List of section lengths in minutes at normal speed.
            limit: Maximum allowed total listening time in whole minutes.

        Returns:
            The minimum valid integer playback speed, or -1 if impossible.

        Time complexity:
            O(n log m), where:
            - n is the number of sections
            - m is max(guides)
            This is because each binary search step scans the list once.

        Space complexity:
            O(1), ignoring input storage.
        """
        # Step 1: Handle the impossible case immediately.
        #
        # Every section takes at least 1 whole minute, even at extremely large speed,
        # because ceil(x / s) is always at least 1 for positive x and positive s.
        #
        # Therefore, the absolute minimum possible total time is exactly the number
        # of sections. If the allowed limit is smaller than that, no solution exists.
        number_of_sections: int = len(guides)
        if limit < number_of_sections:
            return -1

        # Step 2: Set up the binary search range.
        #
        # Lowest possible speed is 1.
        # Highest speed we ever need to test is max(guides).
        #
        # Why is max(guides) enough?
        # At speed = max(guides), every section length is <= speed, so each section
        # takes exactly 1 minute. Therefore total time becomes len(guides), which is
        # the minimum possible total time. Since we already checked that
        # limit >= len(guides), this upper bound is always sufficient.
        left: int = 1
        right: int = max(guides)

        # This variable will store the best valid speed found so far.
        # We initialize it to right because right is guaranteed to be valid
        # after the impossible-case check above.
        answer: int = right

        # Step 3: Standard binary search on the answer space.
        #
        # We are not searching inside the array; we are searching over possible speeds.
        # Because required time decreases (or stays the same) as speed increases,
        # the predicate "is this speed fast enough?" is monotonic:
        #
        # - If a speed works, any larger speed also works.
        # - If a speed does not work, any smaller speed also does not work.
        #
        # This is exactly the pattern binary search needs.
        while left <= right:
            # Choose the middle speed to test.
            mid: int = (left + right) // 2

            # Compute how many total minutes are needed at this speed.
            needed: int = self._required_time(guides, mid)

            # If the required time fits within the limit, then this speed is valid.
            if needed <= limit:
                # Since we want the MINIMUM valid speed, we record this candidate
                # and continue searching on the left half to see if an even smaller
                # valid speed exists.
                answer = mid
                right = mid - 1
            else:
                # If this speed is too slow, then we must increase the speed.
                # So we discard the left half including mid.
                left = mid + 1

        # After binary search finishes, answer holds the smallest valid speed.
        return answer


if __name__ == "__main__":
    # Create an instance of the solution class.
    solution = Solution()

    # Example 1:
    # guides = [7, 11, 5], limit = 8
    # Check expected answer:
    # speed 4 => ceil(7/4) + ceil(11/4) + ceil(5/4) = 2 + 3 + 2 = 7 <= 8
    # speed 3 => 3 + 4 + 2 = 9 > 8
    # Therefore answer should be 4.
    guides1: List[int] = [7, 11, 5]
    limit1: int = 8
    result1: int = solution.minPlaybackSpeed(guides1, limit1)
    print(f"Example 1 result: {result1}")  # Expected: 4

    # Example 2:
    # guides = [12, 3, 9, 6], limit = 4
    # There are 4 sections, so to finish in exactly 4 minutes, each section must
    # take exactly 1 minute.
    # speed 12 => 1 + 1 + 1 + 1 = 4
    # Any smaller speed makes the section of length 12 take at least 2 minutes.
    # Therefore answer should be 12.
    guides2: List[int] = [12, 3, 9, 6]
    limit2: int = 4
    result2: int = solution.minPlaybackSpeed(guides2, limit2)
    print(f"Example 2 result: {result2}")  # Expected: 12

    # Additional impossible-case demonstration:
    # 3 sections cannot be completed in fewer than 3 minutes.
    guides3: List[int] = [5, 10, 15]
    limit3: int = 2
    result3: int = solution.minPlaybackSpeed(guides3, limit3)
    print(f"Impossible case result: {result3}")  # Expected: -1