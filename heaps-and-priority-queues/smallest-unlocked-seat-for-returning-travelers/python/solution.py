"""
Title: Smallest Unlocked Seat for Returning Travelers

Problem Description:
An airport lounge has numbered seats starting from 0. Travelers arrive and leave
throughout the day. Whenever a traveler arrives, they must be assigned the
smallest-numbered seat that is currently unoccupied. If no previously used seat
is available, the lounge opens a new seat with the next unused number.

You are given two integer arrays, arrivals and departures, where arrivals[i] and
departures[i] are the arrival and departure times of traveler i. All arrival
times are distinct, but multiple travelers may leave at the same time. If a
traveler leaves at time t, that seat becomes available immediately, so another
traveler arriving at time t may use it.

Return the seat number assigned to traveler targetTraveler.

Your task is to simulate the seating process efficiently. A brute-force scan of
all seats on every arrival will be too slow for large inputs. Think carefully
about how to track both currently occupied seats and the pool of reusable seat
numbers.

Constraints:
- 1 <= n == arrivals.length == departures.length <= 100000
- 1 <= arrivals[i] < departures[i] <= 1000000000
- All values in arrivals are distinct
- 0 <= targetTraveler < n

Example 1:
Input: arrivals = [1,4,2], departures = [5,6,3], targetTraveler = 1
Output: 1

Example 2:
Input: arrivals = [3,8,5,6], departures = [10,9,7,11], targetTraveler = 3
Output: 2
"""

from heapq import heappop, heappush
from typing import List, Tuple


class Solution:
    def smallest_unlocked_seat(
        self,
        arrivals: List[int],
        departures: List[int],
        targetTraveler: int,
    ) -> int:
        """
        Simulate traveler arrivals and departures to find the seat assigned to
        the target traveler.

        Args:
            arrivals: List where arrivals[i] is the arrival time of traveler i.
            departures: List where departures[i] is the departure time of traveler i.
            targetTraveler: Index of the traveler whose seat number we must return.

        Returns:
            The seat number assigned to targetTraveler.

        Time complexity:
            O(n log n), because we sort travelers by arrival time and each heap
            operation costs O(log n).

        Space complexity:
            O(n), for the sorted traveler list and the heaps.
        """
        n: int = len(arrivals)

        # Build a list of travelers as tuples:
        # (arrival_time, departure_time, traveler_index)
        #
        # Why do this?
        # We need to process travelers in the exact order they arrive.
        # Since arrival times are distinct, sorting by arrival time gives a clear,
        # unambiguous simulation order.
        travelers: List[Tuple[int, int, int]] = [
            (arrivals[i], departures[i], i) for i in range(n)
        ]
        travelers.sort(key=lambda item: item[0])

        # This min-heap stores currently occupied seats.
        # Each entry is:
        # (departure_time, seat_number)
        #
        # Why this structure?
        # Before seating a newly arriving traveler, we must free every seat whose
        # traveler has already departed. The earliest departures should be checked
        # first, so a min-heap by departure time is ideal.
        occupied: List[Tuple[int, int]] = []

        # This min-heap stores seat numbers that are currently free and can be reused.
        #
        # Why a min-heap?
        # The problem requires assigning the smallest-numbered available seat.
        # A min-heap lets us retrieve that smallest free seat in O(log n) time.
        available_seats: List[int] = []

        # This tracks the next brand-new seat number that has never been used before.
        #
        # If there are no reusable seats available, we assign this seat number and
        # then increment it for future use.
        next_new_seat: int = 0

        # Process travelers in arrival order.
        for arrival_time, departure_time, traveler_index in travelers:
            # Step 1: Free all seats whose travelers have already left by the current arrival time.
            #
            # Important detail:
            # If someone leaves at time t, and another traveler arrives at time t,
            # that seat is immediately available. Therefore, we must free seats with
            # departure_time <= arrival_time.
            while occupied and occupied[0][0] <= arrival_time:
                freed_departure_time, freed_seat = heappop(occupied)

                # The traveler using freed_seat has left, so this seat becomes reusable.
                heappush(available_seats, freed_seat)

            # Step 2: Assign the smallest available seat.
            #
            # If we have any previously used seats that are now free, reuse the
            # smallest-numbered one.
            if available_seats:
                assigned_seat: int = heappop(available_seats)
            else:
                # Otherwise, no old seat is free, so we must open a brand-new seat.
                assigned_seat = next_new_seat
                next_new_seat += 1

            # Step 3: If this is the target traveler, we can return immediately.
            #
            # Why is this safe?
            # The seat assignment for the target traveler is determined exactly at
            # their arrival moment. Future events cannot change which seat they were
            # assigned, so we do not need to continue the simulation.
            if traveler_index == targetTraveler:
                return assigned_seat

            # Step 4: Mark this seat as occupied until this traveler's departure time.
            #
            # We push (departure_time, assigned_seat) so that future arrivals can
            # efficiently free seats in the correct order.
            heappush(occupied, (departure_time, assigned_seat))

        # Given valid input, we should always return inside the loop when we reach
        # the target traveler. This fallback is only here for completeness.
        return -1


if __name__ == "__main__":
    solution = Solution()

    arrivals1 = [1, 4, 2]
    departures1 = [5, 6, 3]
    targetTraveler1 = 1
    result1 = solution.smallest_unlocked_seat(arrivals1, departures1, targetTraveler1)
    print(result1)  # Expected: 1

    arrivals2 = [3, 8, 5, 6]
    departures2 = [10, 9, 7, 11]
    targetTraveler2 = 3
    result2 = solution.smallest_unlocked_seat(arrivals2, departures2, targetTraveler2)
    print(result2)  # Expected: 2