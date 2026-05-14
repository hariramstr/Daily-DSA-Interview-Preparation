```python
"""
Title: Closest Patient Appointments
Difficulty: Easy
Topic: Heaps and Priority Queues

Problem Description:
A hospital scheduling system receives a list of patient appointment times (in minutes
from midnight) and a query time `t`. Your task is to find the `k` appointment times
closest to the query time `t`.

If two appointments are equally close to `t`, prefer the one with the smaller time value.
Return the result as a list sorted in ascending order.

Problem Statement:
Given an integer array `appointments` representing scheduled appointment times, an integer
`t` representing a query time, and an integer `k`, return the `k` appointment times closest
to `t`, sorted in ascending order.

Constraints:
- 1 <= appointments.length <= 10^4
- 0 <= appointments[i] <= 1440 (minutes in a day)
- 0 <= t <= 1440
- 1 <= k <= appointments.length
- All appointment times are distinct.

Example 1:
- Input: appointments = [30, 120, 200, 450, 800], t = 150, k = 2
- Output: [120, 200]
- Explanation: Distances from 150 are [120, 30, 50, 300, 650].
  The two smallest distances are 30 (time 120) and 50 (time 200).

Example 2:
- Input: appointments = [60, 180, 300, 420], t = 240, k = 3
- Output: [60, 180, 300]
- Explanation: Distances from 240 are [180, 60, 60, 180].
  The three closest: 60 (time 180), 60 (time 300), 180 (time 60 — prefer smaller, so 60 over 420).
  Result sorted: [60, 180, 300].
"""

import heapq
from typing import List


class Solution:
    def find_closest_appointments(
        self, appointments: List[int], t: int, k: int
    ) -> List[int]:
        """
        Find the k appointment times closest to query time t.

        Uses a max-heap of size k to efficiently track the k closest appointments.
        When two appointments are equally close to t, the one with the smaller
        time value is preferred.

        Args:
            appointments (List[int]): List of appointment times in minutes from midnight.
            t (int): The query time in minutes from midnight.
            k (int): The number of closest appointments to return.

        Returns:
            List[int]: The k closest appointment times, sorted in ascending order.

        Time Complexity: O(n log k) where n is the number of appointments.
            - We iterate through all n appointments: O(n)
            - Each heap push/pop operation is O(log k)
            - Final sort of k elements: O(k log k)
        Space Complexity: O(k) for the heap storing at most k elements.
        """

        # -----------------------------------------------------------------------
        # STEP 1: Understand the comparison key
        # -----------------------------------------------------------------------
        # For each appointment time `a`, we compute:
        #   - distance = abs(a - t)  → how far the appointment is from query time t
        #   - tie-break: if two appointments have the same distance, prefer smaller `a`
        #
        # So the sorting key is: (distance, a)
        # Example: t=240, a=180 → (60, 180); a=300 → (60, 300)
        # (60, 180) < (60, 300) because 180 < 300, so 180 is preferred. ✓

        # -----------------------------------------------------------------------
        # STEP 2: Why use a Max-Heap of size k?
        # -----------------------------------------------------------------------
        # We want to keep the k SMALLEST (distance, appointment) pairs.
        # A max-heap of size k lets us:
        #   - Quickly check if a new appointment is "better" than the worst in our set
        #   - If yes, remove the worst and add the new one
        #
        # Python's heapq is a MIN-heap, so to simulate a MAX-heap we negate values.
        # For the tuple (distance, appointment), we store (-distance, -appointment).
        # This way, the "largest" (distance, appointment) pair sits at the top of
        # the min-heap as the most negative value.

        # Initialize an empty max-heap (simulated via negation)
        max_heap: List[tuple] = []

        # -----------------------------------------------------------------------
        # STEP 3: Iterate through all appointments and maintain heap of size k
        # -----------------------------------------------------------------------
        for appt in appointments:
            # Compute the absolute distance from query time t
            distance = abs(appt - t)

            # We push (-distance, -appt) to simulate a max-heap.
            # The heap root will be the element with the LARGEST (distance, appt).
            # We negate both values because Python's heapq is a min-heap:
            #   - Negating distance: largest distance becomes most negative → floats to top
            #   - Negating appt: for equal distances, larger appt becomes more negative → floats to top
            #     This means when we pop, we remove the appointment with the largest time
            #     among those with equal (maximum) distance — effectively keeping smaller times. ✓
            heapq.heappush(max_heap, (-distance, -appt))

            # If the heap exceeds size k, remove the "worst" element
            # The "worst" element is the one at the top of our max-heap:
            # the appointment with the largest distance (or largest time if distances are equal)
            if len(max_heap) > k:
                # heappop removes the smallest element in the min-heap,
                # which corresponds to the largest (distance, appt) in our max-heap
                heapq.heappop(max_heap)

        # -----------------------------------------------------------------------
        # STEP 4: Extract the k closest appointment times from the heap
        # -----------------------------------------------------------------------
        # The heap now contains exactly k elements: the k closest appointments.
        # We negate back to get the original appointment times.
        result = [-appt for (_, appt) in max_heap]

        # -----------------------------------------------------------------------
        # STEP 5: Sort the result in ascending order as required by the problem
        # -----------------------------------------------------------------------
        result.sort()

        return result


# -------------------------------------------------------------------------------
# Verification / Trace-through of Examples
# -------------------------------------------------------------------------------
# Example 1: appointments = [30, 120, 200, 450, 800], t = 150, k = 2
#   Distances: 30→120, 120→30, 200→50, 450→300, 800→650
#   Sorted by (distance, appt): (30,120), (50,200), (120,30), (300,450), (650,800)
#   Top 2: (30,120) and (50,200) → times [120, 200]
#   Expected Output: [120, 200] ✓
#
# Example 2: appointments = [60, 180, 300, 420], t = 240, k = 3
#   Distances: 60→180, 180→60, 300→60, 420→180
#   Sorted by (distance, appt): (60,180), (60,300), (180,60), (180,420)
#   Top 3: (60,180), (60,300), (180,60) → times [180, 300, 60]
#   Sorted ascending: [60, 180, 300]
#   Expected Output: [60, 180, 300] ✓
#
# Heap trace for Example 2:
#   Process 60:  push (-180, -60)  → heap=[(-180,-60)]
#   Process 180: push (-60, -180)  → heap=[(-180,-60),(-60,-180)]
#   Process 300: push (-60, -300)  → heap=[(-180,-60),(-60,-180),(-60,-300)]
#                heap size=3=k, no pop
#   Process 420: push (-180, -420) → heap size=4 > k=3
#                pop smallest in min-heap = most negative = (-180, -420)
#                (because -420 < -60, so (-180,-420) < (-180,-60))
#                heap=[(-180,-60),(-60,-180),(-60,-300)]
#   Extract times: -(-60)=60, -(-180)=180, -(-300)=300
#   Sorted: [60, 180, 300] ✓
# -------------------------------------------------------------------------------


if __name__ == "__main__":
    solution = Solution()

    # ------------------------------------------------------------------
    # Test Case 1 (from problem description)
    # ------------------------------------------------------------------
    appointments1 = [30, 120, 200, 450, 800]
    t1 = 150
    k1 = 2
    result1 = solution.find_closest_appointments(appointments1, t1, k1)
    print("Test Case 1:")
    print(f"  appointments = {appointments1}")
    print(f"  t = {t1}, k = {k1}")
    print(f"  Output:   {result1}")
    print(f"  Expected: [120, 200]")
    print(f"  Pass: {result1 == [120, 200]}")
    print()

    # ------------------------------------------------------------------
    # Test Case 2 (from problem description)
    # ------------------------------------------------------------------
    appointments2 = [60, 180, 300, 420]
    t2 = 240
    k2 = 3
    result2 = solution.find_closest_appointments(appointments2, t2, k2)
    print("Test Case 2:")
    print(f"  appointments = {appointments2}")
    print(f"  t = {t2}, k = {k2}")
    print(f"  Output:   {result2}")
    print(f"  Expected: [60, 180, 300]")
    print(f"  Pass: {result2 == [60, 180, 300]}")
    print()

    # ------------------------------------------------------------------
    # Test Case 3: k equals the total number of appointments
    # ------------------------------------------------------------------
    appointments3 = [100, 200, 300]
    t3 = 150
    k3 = 3
    result3 = solution.find_closest_appointments(appointments3, t3, k3)
    print("Test Case 3 (k = len(appointments)):")
    print(f"  appointments = {appointments3}")
    print(f"  t = {t3}, k = {k3}")
    print(f"  Output:   {result3}")
    print(f"  Expected: [100, 200, 300]")
    print(f"  Pass: {result3 == [100, 200, 300]}")
    print()

    # ------------------------------------------------------------------
    # Test Case 4: Single appointment
    # ------------------------------------------------------------------
    appointments4 = [720]
    t4 = 0
    k4 = 1
    result4 = solution.find_closest_appointments(appointments4, t4, k4)
    print("Test Case 4 (single appointment):")
    print(f"  appointments = {appointments4}")
    print(f"  t = {t4}, k = {k4}")
    print(f"  Output:   {result4}")
    print(f"  Expected: [720]")
    print(f"  Pass: {result4 == [720]}")
    print()

    # ------------------------------------------------------------------
    # Test Case 5: Tie-breaking — prefer smaller time value
    # ------------------------------------------------------------------
    # t=100, appointments=[50, 150] → both have distance 50
    # Prefer smaller time: 50 over 150
    appointments5 = [50, 150]
    t5 = 100
    k5 = 1
    result5 = solution.find_closest_appointments(appointments5, t5, k5)
    print("Test Case 5 (tie-breaking, prefer smaller time):")
    print(f"  appointments = {appointments5}")
    print(f"  t = {t5}, k = {k5}")
    print(f"  Output:   {result5}")
    print(f"  Expected: [50]")
    print(f"  Pass: {result5 == [50]}")
    print()

    # ------------------------------------------------------------------
    # Test Case 6: Query time at boundary (t=0)
    # ------------------------------------------------------------------
    appointments6 = [0, 60, 120, 180, 1440]
    t6 = 0
    k6 = 2
    result6 = solution.find_closest_appointments(appointments6, t6, k6)
    print("Test Case 6 (t=0, boundary):")
    print(f"  appointments = {appointments6}")
    print(f"  t = {t6}, k = {k6}")
    print(f"  Output:   {result6}")
    print(f"  Expected: [0, 60]")
    print(f"  Pass: {result6 == [0, 60]}")
```