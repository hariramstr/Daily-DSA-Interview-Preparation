/*
Title: Minimum Dock Bays for Delayed Cargo Unloading

Problem Description:
A shipping terminal receives cargo vessels, each with a scheduled arrival time and a fixed unloading duration.
However, the terminal follows a strict operational rule: if multiple ships are waiting, the next available dock bay
must always be assigned to the waiting ship with the smallest original arrival time; if there is still a tie,
assign the ship with the smaller input index. Once a ship starts unloading, it occupies its dock bay continuously
for its full duration. If no dock bay is free when a ship arrives, that ship waits until some bay becomes available.

You are given two integer arrays arrival and unload, where arrival[i] is the scheduled arrival time of ship i and
unload[i] is the time required to unload it. You are also given an integer T. Determine the minimum number of dock
bays needed so that every ship can begin unloading no later than T time units after its scheduled arrival time.

Formally, if ship i starts at time start[i], then start[i] must satisfy start[i] - arrival[i] <= T.
Ships cannot be reordered arbitrarily; whenever a dock becomes free, the terminal must choose the eligible waiting
ship according to the rule above. Your task is to compute the smallest number of dock bays that makes the schedule
feasible, or return -1 if it is impossible under the dispatch rule.

Constraints:
- 1 <= n <= 200000
- 0 <= arrival[i] <= 10^9
- 1 <= unload[i] <= 10^9
- 0 <= T <= 10^9
- arrival is not guaranteed to be sorted
*/

using System;
using System.Collections.Generic;

public class Solution
{
    private struct Ship
    {
        public int Arrival;
        public int Duration;
        public int Index;
    }

    private struct RunningShip
    {
        public long FinishTime;
        public int ShipIndex;
    }

    private sealed class RunningComparer : IComparer<RunningShip>
    {
        public int Compare(RunningShip x, RunningShip y)
        {
            int cmp = x.FinishTime.CompareTo(y.FinishTime);
            if (cmp != 0) return cmp;
            return x.ShipIndex.CompareTo(y.ShipIndex);
        }
    }

    private sealed class WaitingComparer : IComparer<int>
    {
        private readonly Ship[] _ships;

        public WaitingComparer(Ship[] ships)
        {
            _ships = ships;
        }

        public int Compare(int a, int b)
        {
            int cmp = _ships[a].Arrival.CompareTo(_ships[b].Arrival);
            if (cmp != 0) return cmp;
            return _ships[a].Index.CompareTo(_ships[b].Index);
        }
    }

    /*
    Time Complexity:
    - Sorting ships once: O(n log n)
    - Feasibility check for a fixed number of bays: O(n log n)
    - Binary search over number of bays: O(log n) checks
    - Total: O(n log n log n)

    Space Complexity:
    - O(n) for the sorted ships and priority queues / sets used during simulation
    */
    public int MinimumDockBays(int[] arrival, int[] unload, int T)
    {
        int n = arrival.Length;

        // Step 1:
        // Build a single array of ship records so we can sort by arrival time while still remembering:
        // - the original arrival time
        // - the unload duration
        // - the original input index
        //
        // The original input index matters because the dispatch rule says:
        // if two waiting ships have the same arrival time, the smaller input index must go first.
        var ships = new Ship[n];
        for (int i = 0; i < n; i++)
        {
            ships[i] = new Ship
            {
                Arrival = arrival[i],
                Duration = unload[i],
                Index = i
            };
        }

        // Step 2:
        // Sort ships by:
        //   1) arrival time ascending
        //   2) original index ascending
        //
        // Why this is useful:
        // - It lets us process "new arrivals" in chronological order.
        // - It also matches the waiting priority rule naturally.
        Array.Sort(ships, (a, b) =>
        {
            int cmp = a.Arrival.CompareTo(b.Arrival);
            if (cmp != 0) return cmp;
            return a.Index.CompareTo(b.Index);
        });

        // Step 3:
        // Binary search for the minimum number of bays.
        //
        // Why binary search works:
        // If k bays are enough, then any larger number of bays is also enough.
        // More bays can only help, never hurt.
        int left = 1;
        int right = n;
        int answer = -1;

        while (left <= right)
        {
            int mid = left + (right - left) / 2;

            if (CanScheduleWithBays(ships, T, mid))
            {
                answer = mid;
                right = mid - 1;
            }
            else
            {
                left = mid + 1;
            }
        }

        return answer;
    }

    private bool CanScheduleWithBays(Ship[] ships, int T, int bays)
    {
        int n = ships.Length;

        // This sorted set stores currently running ships.
        // We need to know which ship finishes first, because that is the next time a bay becomes free.
        //
        // Key = (finish time, ship index)
        // We include ship index to make every key unique and to break ties deterministically.
        var running = new SortedSet<RunningShip>(new RunningComparer());

        // This sorted set stores waiting ships that have arrived but have not started yet.
        // The dispatch rule says we must always choose:
        //   - smallest original arrival time
        //   - if tied, smallest input index
        //
        // Since our ships are already sorted by arrival then index, we can store the position in the sorted array.
        // The comparer uses the ship data to enforce exactly that priority.
        var waiting = new SortedSet<int>(new WaitingComparer(ships));

        // Pointer over the sorted ships array.
        // It tells us which ships have not yet been "introduced" into the simulation.
        int i = 0;

        // Current simulation time.
        // We will jump this forward to the next meaningful event:
        // - a ship arrival
        // - a ship completion
        long currentTime = 0;

        // Main event-driven simulation loop.
        //
        // We continue until:
        // - all ships have been introduced, and
        // - no ships are waiting, and
        // - no ships are running.
        while (i < n || waiting.Count > 0 || running.Count > 0)
        {
            // Step A:
            // If nobody is waiting and there is at least one free bay opportunity in the future,
            // we may need to jump time forward.
            //
            // This avoids simulating every single time unit, which would be far too slow.
            if (waiting.Count == 0)
            {
                long nextArrival = i < n ? ships[i].Arrival : long.MaxValue;
                long nextFinish = running.Count > 0 ? running.Min.FinishTime : long.MaxValue;

                currentTime = Math.Max(currentTime, Math.Min(nextArrival, nextFinish));
            }

            // Step B:
            // First, release every running ship whose finish time is <= currentTime.
            //
            // Why first?
            // Because if a ship finishes exactly at time t, that bay is available at time t.
            // Ships arriving at time t are allowed to use it immediately.
            while (running.Count > 0 && running.Min.FinishTime <= currentTime)
            {
                running.Remove(running.Min);
            }

            // Step C:
            // Add every ship whose arrival time is <= currentTime into the waiting set.
            //
            // These ships are now eligible to be scheduled if a bay is free.
            while (i < n && ships[i].Arrival <= currentTime)
            {
                waiting.Add(i);
                i++;
            }

            // Step D:
            // While we still have free bays and waiting ships, start ships immediately at currentTime.
            //
            // This is necessary because idle bays should never remain unused if there are waiting ships.
            // Also, the dispatch rule forces us to always choose the highest-priority waiting ship.
            while (running.Count < bays && waiting.Count > 0)
            {
                int shipPos = waiting.Min;
                waiting.Remove(shipPos);

                Ship ship = ships[shipPos];

                // Check the waiting-time constraint:
                // start time = currentTime
                // must satisfy currentTime - arrival <= T
                if (currentTime - ship.Arrival > T)
                {
                    return false;
                }

                long finish = currentTime + ship.Duration;
                running.Add(new RunningShip
                {
                    FinishTime = finish,
                    ShipIndex = ship.Index
                });
            }

            // Step E:
            // Early failure check for the waiting queue.
            //
            // Suppose the highest-priority waiting ship has already waited too long.
            // Then the schedule is impossible.
            //
            // Why can we fail immediately?
            // Because that ship is the one that MUST be chosen next whenever a bay becomes free.
            // If it is already late now, no future action can make its start time earlier.
            if (waiting.Count > 0)
            {
                int top = waiting.Min;
                if (currentTime - ships[top].Arrival > T)
                {
                    return false;
                }
            }

            // Step F:
            // Decide what the next event time should be.
            //
            // There are two kinds of future events:
            // - next ship arrival
            // - next running ship completion
            //
            // We move to the earlier one.
            long nextArrivalTime = i < n ? ships[i].Arrival : long.MaxValue;
            long nextFinishTime = running.Count > 0 ? running.Min.FinishTime : long.MaxValue;

            // If there are waiting ships but all bays are full, the only thing that can help is a completion.
            // If there are no waiting ships, either an arrival or a completion can matter.
            long nextTime = Math.Min(nextArrivalTime, nextFinishTime);

            // If there is no next event, we are done.
            if (nextTime == long.MaxValue)
            {
                break;
            }

            // Important subtlety:
            // If nextTime == currentTime, the next loop iteration will still make progress because
            // we will process arrivals/finishes at that exact time and/or start more ships.
            currentTime = Math.Max(currentTime, nextTime);
        }

        return true;
    }
}

// Demo code
var solution = new Solution();

int[] arrival1 = { 1, 2, 4 };
int[] unload1 = { 5, 2, 3 };
int T1 = 2;
Console.WriteLine(solution.MinimumDockBays(arrival1, unload1, T1)); // Expected: 2

int[] arrival2 = { 0, 1, 1, 3 };
int[] unload2 = { 4, 2, 5, 1 };
int T2 = 1;
Console.WriteLine(solution.MinimumDockBays(arrival2, unload2, T2)); // Expected: 3