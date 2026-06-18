/*
Title: K Closest Delivery Bots to Charging Stations
Difficulty: Medium
Topic: Heaps and Priority Queues

Problem Description:
A robotics warehouse tracks the current positions of delivery bots and needs to quickly identify which bots should be recalled for charging. You are given two arrays: bots, where each element is [id, x, y] representing a unique bot ID and its 2D position, and stations, where each element is [x, y] representing a charging station. For every bot, define its charging distance as the Manhattan distance to its nearest charging station: |x1 - x2| + |y1 - y2|. Your task is to return the IDs of the k bots with the smallest charging distances. If two bots have the same charging distance, the bot with the smaller ID should come first. The returned list must be sorted by increasing charging distance, and then by increasing ID.

A straightforward solution may compare every bot against every station, but the interview challenge is to combine efficient nearest-station evaluation with a heap or priority queue to keep only the best k candidates seen so far.

Constraints:
- 1 <= bots.length <= 10^5
- 1 <= stations.length <= 10^5
- 1 <= k <= bots.length
- 0 <= id <= 10^9
- -10^6 <= x, y <= 10^6
- All bot IDs are unique.

Example 1:
Input: bots = [[101,1,2],[205,3,5],[150,-1,4]], stations = [[0,0],[2,3]], k = 2
Output: [101,150]
Explanation: Bot 101 has nearest distance 2 (to station [2,3]), bot 205 has nearest distance 3, and bot 150 has nearest distance 2. The two smallest distances are bots 101 and 150, and the tie is broken by smaller ID.

Example 2:
Input: bots = [[7,10,10],[3,1,1],[9,4,0],[12,2,2]], stations = [[0,0]], k = 3
Output: [3,9,12]
Explanation: Distances are: bot 7 -> 20, bot 3 -> 2, bot 9 -> 4, bot 12 -> 4. The closest three bots are 3, 9, and 12. Bots 9 and 12 tie on distance, so the final ordered result by distance then ID is [3,9,12].
*/

using System;
using System.Collections.Generic;
using System.Linq;

class Solution
{
    private struct Candidate
    {
        public long Distance;
        public int Id;

        public Candidate(long distance, int id)
        {
            Distance = distance;
            Id = id;
        }
    }

    private sealed class WorseCandidateComparer : IComparer<Candidate>
    {
        public int Compare(Candidate a, Candidate b)
        {
            int distanceCompare = a.Distance.CompareTo(b.Distance);
            if (distanceCompare != 0)
            {
                return distanceCompare;
            }

            return a.Id.CompareTo(b.Id);
        }
    }

    private sealed class BetterCandidateComparer : IComparer<Candidate>
    {
        public int Compare(Candidate a, Candidate b)
        {
            int distanceCompare = a.Distance.CompareTo(b.Distance);
            if (distanceCompare != 0)
            {
                return distanceCompare;
            }

            return a.Id.CompareTo(b.Id);
        }
    }

    private sealed class ManhattanNearestStation
    {
        private readonly List<(int X, int Y)> _points;
        private readonly Node? _root;

        private sealed class Node
        {
            public int PointIndex;
            public int Axis;
            public Node? Left;
            public Node? Right;

            public Node(int pointIndex, int axis)
            {
                PointIndex = pointIndex;
                Axis = axis;
            }
        }

        public ManhattanNearestStation(int[][] stations)
        {
            _points = new List<(int X, int Y)>(stations.Length);
            for (int i = 0; i < stations.Length; i++)
            {
                _points.Add((stations[i][0], stations[i][1]));
            }

            int[] indices = Enumerable.Range(0, _points.Count).ToArray();
            _root = Build(indices, 0, indices.Length - 1, 0);
        }

        public long QueryNearestDistance(int x, int y)
        {
            long best = long.MaxValue;
            Search(_root, x, y, ref best);
            return best;
        }

        private Node? Build(int[] indices, int left, int right, int depth)
        {
            if (left > right)
            {
                return null;
            }

            int axis = depth % 2;
            Array.Sort(indices, left, right - left + 1, Comparer<int>.Create((a, b) =>
            {
                if (axis == 0)
                {
                    int cmp = _points[a].X.CompareTo(_points[b].X);
                    if (cmp != 0) return cmp;
                    return _points[a].Y.CompareTo(_points[b].Y);
                }
                else
                {
                    int cmp = _points[a].Y.CompareTo(_points[b].Y);
                    if (cmp != 0) return cmp;
                    return _points[a].X.CompareTo(_points[b].X);
                }
            }));

            int mid = left + (right - left) / 2;
            Node node = new Node(indices[mid], axis);

            node.Left = Build(indices, left, mid - 1, depth + 1);
            node.Right = Build(indices, mid + 1, right, depth + 1);

            return node;
        }

        private void Search(Node? node, int x, int y, ref long best)
        {
            if (node == null)
            {
                return;
            }

            var point = _points[node.PointIndex];

            long currentDistance = Math.Abs((long)x - point.X) + Math.Abs((long)y - point.Y);
            if (currentDistance < best)
            {
                best = currentDistance;
            }

            long delta = node.Axis == 0 ? Math.Abs((long)x - point.X) : Math.Abs((long)y - point.Y);

            Node? first;
            Node? second;

            if (node.Axis == 0)
            {
                if (x <= point.X)
                {
                    first = node.Left;
                    second = node.Right;
                }
                else
                {
                    first = node.Right;
                    second = node.Left;
                }
            }
            else
            {
                if (y <= point.Y)
                {
                    first = node.Left;
                    second = node.Right;
                }
                else
                {
                    first = node.Right;
                    second = node.Left;
                }
            }

            Search(first, x, y, ref best);

            if (delta <= best)
            {
                Search(second, x, y, ref best);
            }
        }
    }

    /*
    Time Complexity:
    - Building the station search structure: approximately O(m log^2 m) with this simple KD-tree construction,
      where m = number of stations.
    - Querying nearest station for each bot: average-case often close to O(log m), worst-case O(m).
    - Maintaining the heap of size k for n bots: O(n log k).
    - Final sorting of the selected k bots: O(k log k).

    Overall:
    - Practical average-case: about O(m log^2 m + n log m + n log k + k log k)
    - Worst-case for KD-tree queries can degrade, but this is still far better in practice than O(n * m).

    Space Complexity:
    - O(m) for the station search structure
    - O(k) for the heap
    - O(k) for final result storage
    */
    public IList<int> KClosestBots(int[][] bots, int[][] stations, int k)
    {
        // Step 1:
        // Build a search structure over all charging stations.
        //
        // Why do we need this?
        // If we compare every bot to every station directly, the work becomes:
        // bots.length * stations.length
        // which can be 10^5 * 10^5 = 10^10 comparisons in the worst case.
        // That is far too slow.
        //
        // So instead, we preprocess the stations into a structure that helps us
        // answer this question faster:
        // "For this bot position (x, y), what is the Manhattan distance to the nearest station?"
        //
        // Here we use a simple 2D KD-tree style structure. While KD-trees are more commonly
        // associated with Euclidean distance, they still provide a practical way to prune
        // many branches for nearest-neighbor style searching in 2D.
        ManhattanNearestStation stationSearcher = new ManhattanNearestStation(stations);

        // Step 2:
        // Create a priority queue that will keep only the best k bots seen so far.
        //
        // Important idea:
        // We want the k smallest candidates by:
        //   1) smaller charging distance is better
        //   2) if distance ties, smaller bot ID is better
        //
        // A common heap trick:
        // Keep a heap of size at most k, where the TOP is the "worst" among the currently selected bots.
        // Then when a new bot is better than the current worst, we remove the worst and insert the new one.
        //
        // In .NET PriorityQueue, the smallest priority comes out first.
        // So to make the "worst" candidate come out first, we store a reversed priority:
        //   larger distance => worse
        //   if same distance, larger ID => worse
        //
        // We achieve that by using a custom comparer for the priority type.
        PriorityQueue<Candidate, Candidate> heap = new PriorityQueue<Candidate, Candidate>(
            comparer: Comparer<Candidate>.Create((a, b) =>
            {
                int distanceCompare = b.Distance.CompareTo(a.Distance);
                if (distanceCompare != 0)
                {
                    return distanceCompare;
                }

                return b.Id.CompareTo(a.Id);
            }));

        // Step 3:
        // Process each bot one by one.
        //
        // For each bot:
        //   - compute its nearest charging distance
        //   - package it as a candidate
        //   - decide whether it belongs in the best k
        for (int i = 0; i < bots.Length; i++)
        {
            int id = bots[i][0];
            int x = bots[i][1];
            int y = bots[i][2];

            // Step 3a:
            // Find the Manhattan distance from this bot to its nearest station.
            //
            // This is the key geometric query of the problem.
            long nearestDistance = stationSearcher.QueryNearestDistance(x, y);

            Candidate current = new Candidate(nearestDistance, id);

            // Step 3b:
            // If we have not yet collected k bots, simply add this one.
            //
            // Why?
            // Because until the heap reaches size k, every candidate is provisionally part
            // of the best k seen so far.
            if (heap.Count < k)
            {
                heap.Enqueue(current, current);
            }
            else
            {
                // Step 3c:
                // The heap already contains k candidates.
                // The top of the heap is the current "worst" among those selected.
                Candidate worstSelected = heap.Peek();

                // Step 3d:
                // Compare the current bot with the worst selected bot.
                //
                // If current is better, it deserves a place in the top k.
                // Then we remove the worst and insert current.
                //
                // "Better" means:
                //   - smaller distance
                //   - or same distance but smaller ID
                if (IsBetter(current, worstSelected))
                {
                    heap.Dequeue();
                    heap.Enqueue(current, current);
                }
            }
        }

        // Step 4:
        // Extract all selected candidates from the heap.
        //
        // Important:
        // The heap is NOT guaranteed to return them in the final required order.
        // It only guarantees that the worst selected item is at the top according to our heap priority.
        // So after extraction, we must sort the final k candidates correctly.
        List<Candidate> selected = new List<Candidate>(k);
        while (heap.Count > 0)
        {
            selected.Add(heap.Dequeue());
        }

        // Step 5:
        // Sort the selected bots by the exact output rule:
        //   1) increasing charging distance
        //   2) increasing ID
        //
        // This final sort is necessary for correctness.
        // Even if the heap maintained the right set of k bots, the output order still matters.
        selected.Sort((a, b) =>
        {
            int distanceCompare = a.Distance.CompareTo(b.Distance);
            if (distanceCompare != 0)
            {
                return distanceCompare;
            }

            return a.Id.CompareTo(b.Id);
        });

        // Step 6:
        // Build the final answer containing only bot IDs.
        List<int> result = new List<int>(selected.Count);
        foreach (Candidate candidate in selected)
        {
            result.Add(candidate.Id);
        }

        return result;
    }

    private bool IsBetter(Candidate a, Candidate b)
    {
        if (a.Distance != b.Distance)
        {
            return a.Distance < b.Distance;
        }

        return a.Id < b.Id;
    }
}

// Demo code

Solution solution = new Solution();

// Example 1
int[][] bots1 =
{
    new[] { 101, 1, 2 },
    new[] { 205, 3, 5 },
    new[] { 150, -1, 4 }
};

int[][] stations1 =
{
    new[] { 0, 0 },
    new[] { 2, 3 }
};

int k1 = 2;
IList<int> result1 = solution.KClosestBots(bots1, stations1, k1);
Console.WriteLine("Example 1 Output: [" + string.Join(",", result1) + "]");

// Example 2
int[][] bots2 =
{
    new[] { 7, 10, 10 },
    new[] { 3, 1, 1 },
    new[] { 9, 4, 0 },
    new[] { 12, 2, 2 }
};

int[][] stations2 =
{
    new[] { 0, 0 }
};

int k2 = 3;
IList<int> result2 = solution.KClosestBots(bots2, stations2, k2);
Console.WriteLine("Example 2 Output: [" + string.Join(",", result2) + "]");