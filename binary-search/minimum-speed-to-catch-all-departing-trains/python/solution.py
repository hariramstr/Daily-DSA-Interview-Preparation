```python
"""
Title: Minimum Speed to Catch All Departing Trains
Difficulty: Medium
Topic: Binary Search

Problem Description:
A traveler needs to board a sequence of n trains at a station. Each train i departs
exactly at minute schedule[i], and the traveler arrives at the station at minute 0.
To board train i, the traveler must finish boarding train i-1 and then walk to the
next platform, which takes exactly 1 minute per unit of distance dist[i] (the distance
to platform i+1). However, the traveler can move at a chosen speed s (a positive integer),
meaning crossing distance dist[i] takes ceil(dist[i] / s) minutes.

The traveler starts at platform 1 at time 0 and must board every train in order.
To board train i, the traveler must arrive at platform i no later than schedule[i] minutes.
The traveler always boards the train the moment they arrive (or waits if early).

Given arrays schedule and dist of length n, where dist[i] is the distance from platform i
to platform i+1 (there are n-1 such distances), find the minimum integer speed s such that
the traveler can board all n trains on time. If it is impossible even at very high speed,
return -1.

Constraints:
- 2 <= n <= 10^5
- 1 <= schedule[i] <= 10^9 (schedule is strictly increasing)
- 1 <= dist[i] <= 10^9, dist.length == n - 1
- The answer speed, if it exists, will not exceed 10^7
"""

import math
from typing import List


class Solution:
    def minSpeed(self, schedule: List[int], dist: List[int]) -> int:
        """
        Find the minimum integer speed to catch all trains on time using binary search.

        The key insight is: if speed s works, then any speed > s also works.
        This monotonic property makes binary search applicable.

        We binary search on the speed value in range [1, 10^7].
        For each candidate speed, we simulate the journey and check feasibility.

        Args:
            schedule: List of departure times for each train (strictly increasing).
            dist: List of distances between consecutive platforms (length = n-1).

        Returns:
            Minimum integer speed to catch all trains, or -1 if impossible.

        Time Complexity: O(n * log(MAX_SPEED)) where MAX_SPEED = 10^7
                         = O(n * log(10^7)) ≈ O(n * 23) = O(n)
        Space Complexity: O(1) — only a constant amount of extra space used.
        """

        n = len(schedule)  # Number of trains to catch

        # -----------------------------------------------------------------------
        # FEASIBILITY CHECK FUNCTION
        # Given a speed s, determine if the traveler can catch all trains.
        # -----------------------------------------------------------------------
        def can_catch_all(speed: int) -> bool:
            """
            Simulate the journey at the given speed and check if all trains are caught.

            Args:
                speed: The integer speed to test.

            Returns:
                True if all trains can be caught at this speed, False otherwise.
            """
            # current_time tracks when the traveler is ready to depart from current platform
            # Initially at platform 1 at time 0, and train 0 departs at schedule[0].
            # The traveler boards train 0 at time 0 (since schedule[0] >= 0 always,
            # as schedule[0] >= 1 per constraints). After boarding, they wait until
            # the train departs at schedule[0].
            # So departure time from platform 1 = schedule[0].
            current_time = 0  # Time when traveler arrives at current platform

            # We need to check that the traveler arrives at each platform on time.
            # For platform 1 (train 0): traveler is already there at time 0.
            # schedule[0] >= 1 >= 0, so train 0 is always catchable.

            # Check if we can even board train 0:
            # Traveler is at platform 1 at time 0, train 0 departs at schedule[0] >= 1.
            # This is always fine.

            # Now simulate traveling between platforms:
            # After boarding train i at platform i+1, the traveler departs at schedule[i].
            # Then they travel dist[i] to platform i+2, taking ceil(dist[i]/speed) minutes.
            # They must arrive at platform i+2 by schedule[i+1].

            # We iterate over each inter-platform journey:
            # dist[i] connects platform i+1 to platform i+2 (0-indexed: dist[0] to dist[n-2])
            for i in range(n - 1):
                # The traveler is at platform i+1 (0-indexed: platform i).
                # They board train i and depart at max(current_time, schedule[i]).
                # Wait: current_time is when they ARRIVE at platform i+1.
                # They must arrive by schedule[i], so if current_time > schedule[i], impossible.

                # First, check if traveler arrived at this platform on time:
                if current_time > schedule[i]:
                    return False  # Missed train i

                # The traveler boards train i and waits until it departs at schedule[i].
                # Departure time from platform i+1 = schedule[i].
                departure_time = schedule[i]

                # Travel time to next platform = ceil(dist[i] / speed)
                travel_time = math.ceil(dist[i] / speed)

                # Arrival time at next platform:
                current_time = departure_time + travel_time

            # After the loop, current_time is the arrival time at the last platform (platform n).
            # Check if traveler arrives at platform n on time for train n-1:
            if current_time > schedule[n - 1]:
                return False

            return True

        # -----------------------------------------------------------------------
        # EARLY IMPOSSIBILITY CHECK
        # -----------------------------------------------------------------------
        # The traveler must board train 0 at platform 1 (always possible since they
        # start there at time 0 and schedule[0] >= 1).
        #
        # For intermediate trains (trains 1 to n-2), the traveler departs platform i+1
        # at schedule[i] and must arrive at platform i+2 by schedule[i+1].
        # Even at infinite speed (travel time = 1 minute minimum since ceil(d/inf) -> 0
        # but practically ceil(d/s) >= 1 for any finite s... wait, actually at very high
        # speed, ceil(dist/speed) can be 1 if dist <= speed, or even approach 0 conceptually.
        # But since speed is an integer and dist >= 1, ceil(dist/speed) >= 1 when speed < dist,
        # and = 1 when speed >= dist.
        # Actually ceil(dist/speed) = 1 when speed >= dist (since dist/speed <= 1, ceil = 1).
        # Wait: ceil(1/10^7) = 1. So minimum travel time is always 1 minute.
        #
        # Therefore, for each intermediate leg i (0 to n-2):
        # minimum arrival at platform i+2 = schedule[i] + 1
        # This must be <= schedule[i+1]
        # i.e., schedule[i+1] - schedule[i] >= 1 (always true since schedule is strictly increasing)
        #
        # But wait — for the LAST leg (i = n-2):
        # arrival at platform n = schedule[n-2] + ceil(dist[n-2]/speed)
        # This must be <= schedule[n-1]
        # At max speed (10^7), ceil(dist[n-2]/10^7) could be 1 if dist[n-2] <= 10^7.
        # The problem says answer won't exceed 10^7, so if it's impossible, return -1.
        #
        # The problem guarantees schedule is strictly increasing, so schedule[i+1] > schedule[i].
        # The only way it's impossible is if even at speed 10^7 it doesn't work.
        # We handle this by checking if can_catch_all(10^7) is False -> return -1.

        MAX_SPEED = 10**7

        # Quick check: if even maximum speed doesn't work, return -1
        if not can_catch_all(MAX_SPEED):
            return -1

        # -----------------------------------------------------------------------
        # BINARY SEARCH FOR MINIMUM SPEED
        # -----------------------------------------------------------------------
        # We know:
        # - Speed 1 might or might not work.
        # - Speed MAX_SPEED works (checked above).
        # - The feasibility function is monotone: if speed s works, s+1 also works.
        #
        # We binary search for the smallest speed that works.
        # Search range: [1, MAX_SPEED]

        left = 1          # Minimum possible speed
        right = MAX_SPEED  # Maximum possible speed (guaranteed to work)

        # Binary search loop:
        # Invariant: right always satisfies can_catch_all(right) = True
        #            left is the current lower bound to explore
        while left < right:
            # Calculate midpoint (avoid overflow — not an issue in Python, but good practice)
            mid = (left + right) // 2

            if can_catch_all(mid):
                # Speed mid works! Try to find something smaller.
                # Move right boundary down to mid (mid might be the answer).
                right = mid
            else:
                # Speed mid doesn't work. We need higher speed.
                # Move left boundary up past mid.
                left = mid + 1

        # When left == right, we've found the minimum speed.
        # Since we verified MAX_SPEED works, left will be a valid speed.
        return left


# -----------------------------------------------------------------------
# MAIN BLOCK: Test with provided examples
# -----------------------------------------------------------------------
if __name__ == "__main__":
    solution = Solution()

    # -----------------------------------------------------------------------
    # Example 1:
    # schedule = [2, 5, 9], dist = [3, 4]
    # Expected Output: 2
    #
    # Trace at speed 2:
    # - Platform 1: arrive at time 0, board train 0 (schedule[0]=2), depart at 2.
    # - Travel dist[0]=3: ceil(3/2)=2 min. Arrive at platform 2 at time 2+2=4.
    # - Platform 2: arrive at 4, must be <= schedule[1]=5. OK! Board train 1, depart at 5.
    # - Travel dist[1]=4: ceil(4/2)=2 min. Arrive at platform 3 at time 5+2=7.
    # - Platform 3: arrive at 7, must be <= schedule[2]=9. OK!
    # All trains caught at speed 2. ✓
    #
    # Trace at speed 1:
    # - Depart platform 1 at time 2.
    # - Travel dist[0]=3: ceil(3/1)=3 min. Arrive at platform 2 at 2+3=5.
    # - Platform 2: arrive at 5, must be <= schedule[1]=5. OK! Depart at 5.
    # - Travel dist[1]=4: ceil(4/1)=4 min. Arrive at platform 3 at 5+4=9.
    # - Platform 3: arrive at 9, must be <= schedule[2]=9. OK!
    # Wait — speed 1 also works? Let me re-check...
    # Actually yes, speed 1 works for this example too!
    # Hmm, but expected output is 2. Let me re-read the problem.
    #
    # Wait, re-reading: "dist[i] is the distance from platform i to platform i+1"
    # and "dist.length == n-1". So for n=3 trains, dist has 2 elements.
    # schedule = [2, 5, 9], dist = [3, 4].
    #
    # At speed 1:
    # - At platform 1 at time 0. Train 0 departs at schedule[0]=2. Depart at 2.
    # - Travel dist[0]=3 at speed 1: ceil(3/1)=3. Arrive platform 2 at 2+3=5.
    # - Train 1 departs at schedule[1]=5. Arrive at 5 <= 5. OK! Depart at 5.
    # - Travel dist[1]=4 at speed 1: ceil(4/1)=4. Arrive platform 3 at 5+4=9.
    # - Train 2 departs at schedule[2]=9. Arrive at 9 <= 9. OK!
    # So speed 1 works! But expected output is 2...
    #
    # Hmm, let me re-read the problem more carefully.
    # "dist[i] is the distance from platform i to platform i+1 (there are n-1 such distances)"
    # Wait, maybe I'm misunderstanding the indexing.
    # Actually, looking at the example explanation again:
    # "traveling dist[0]=3 takes ceil(3/2)=2 min, arriving at platform 2 at time 2"
    # So at speed 2, arrive at platform 2 at time 2 (not 2+2=4).
    # That means the traveler starts traveling immediately at time 0, not at schedule[0]!
    #
    # Re-reading: "The traveler starts at platform 1 at time 0 and must board every train in order."
    # "To board train i, the traveler must arrive at platform i no later than schedule[i] minutes."
    #
    # So the traveler starts at platform 1 at time 0. Train 0 is AT platform 1 (schedule[0]=2).
    # The traveler boards train 0 immediately (they're already there). Then they travel to platform 2.
    # But when do they START traveling? After boarding train 0, which departs at schedule[0]=2?
    # OR do they travel immediately?
    #
    # From the example: "arriving at platform 2 at time 2 (on time for schedule[1]=5)"
    # At speed 2, dist[0]=3, ceil(3/2)=2. If they start at time 0, arrive at 0+2=2. ✓
    # If they start at schedule[0]=2, arrive at 2+2=4.
    # The example says arrive at time 2, so they start traveling at time 0!
    #
    # So the model is:
    # - Traveler starts at platform 1 at time 0.
    # - They must be at platform i by schedule[i-1] (0-indexed: schedule[i]).
    # - After arriving at platform i, they immediately start traveling to platform i+1.
    # - Travel time = ceil(dist[i] / speed).
    # - They must arrive at platform i+1 by schedule[i+1].
    #
    # Wait but then when do they "board" the train? The train departs at schedule[i].
    # If they arrive early, do they wait for the train? The problem says "The traveler always
    # boards the train the moment they arrive (or waits if early)."
    # So if they arrive early, they wait. The train departs at schedule[i].
    # But then they can't leave for the next platform until the train departs!
    #
    # From the example: "Then traveling dist[1]=4 takes ceil(4/2)=2 min, arriving at platform 3
    # at time max(2,5)+2=7". The max(2,5) means they wait until schedule[1]=5 before departing!
    #
    # So the correct model:
    # - Arrive at platform i at time arrival_i.
    # - Must have arrival_i <= schedule[i].
    # - Depart at time max(arrival_i, schedule[i]) = schedule[i] (since arrival_i <= schedule[i]).
    # - Actually departure = schedule[i] always (since they wait for the train).
    # - Travel to platform i+1: ceil(dist[i] / speed) minutes.
    # - arrival_{i+1} = schedule[i] + ceil(dist[i] / speed).
    #
    # For the first platform: arrival_0 = 0 (traveler starts there