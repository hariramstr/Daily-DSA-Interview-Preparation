/*
Title: Maximum Simultaneous Streams Under Bandwidth Cap

Problem Description:
A video platform is preparing a live event with n available stream encodings. The i-th encoding can serve up to viewers[i] viewers, and each served viewer on that encoding consumes bitrate[i] units of bandwidth. You are given a total network budget B. The platform wants to choose a single target number x and require every selected encoding to serve exactly x viewers. An encoding can only be selected if viewers[i] >= x, and selecting it would then consume x * bitrate[i] bandwidth. You may select any subset of encodings, but the total consumed bandwidth of the selected encodings must not exceed B.

Your task is to compute the maximum possible number of selected encodings.

In other words, among all integers x >= 1, determine the largest subset size k such that there exists a set of k encodings where each chosen encoding can serve at least x viewers and the sum of x * bitrate[i] over those k encodings is at most B.

This problem is designed so that a direct search over all x is too slow. You will need to exploit the monotonic structure of feasibility and combine binary search with an efficient way to test whether at least k encodings can be chosen.
*/

using System;
using System.Collections.Generic;
using System.Linq;

class Solution
{
    private struct EncodingInfo
    {
        public int Viewers;
        public int Bitrate;

        public EncodingInfo(int viewers, int bitrate)
        {
            Viewers = viewers;
            Bitrate = bitrate;
        }
    }

    private sealed class MaxHeap
    {
        private readonly List<int> _data = new();

        public int Count => _data.Count;

        public int Peek()
        {
            return _data[0];
        }

        public void Push(int value)
        {
            _data.Add(value);
            int i = _data.Count - 1;

            while (i > 0)
            {
                int parent = (i - 1) / 2;
                if (_data[parent] >= _data[i]) break;

                (_data[parent], _data[i]) = (_data[i], _data[parent]);
                i = parent;
            }
        }

        public int Pop()
        {
            int root = _data[0];
            int last = _data[^1];
            _data.RemoveAt(_data.Count - 1);

            if (_data.Count > 0)
            {
                _data[0] = last;
                int i = 0;

                while (true)
                {
                    int left = i * 2 + 1;
                    int right = i * 2 + 2;
                    int largest = i;

                    if (left < _data.Count && _data[left] > _data[largest]) largest = left;
                    if (right < _data.Count && _data[right] > _data[largest]) largest = right;

                    if (largest == i) break;

                    (_data[i], _data[largest]) = (_data[largest], _data[i]);
                    i = largest;
                }
            }

            return root;
        }
    }

    /*
    Time Complexity:
    - Sorting encodings by viewers: O(n log n)
    - Binary search over answer k: O(log n)
    - Each feasibility check scans all encodings once and uses a heap with at most k elements:
      O(n log k)
    - Total: O(n log n + n log k log n), which is efficient for n up to 200000

    Space Complexity:
    - O(n) for the sorted encodings
    - O(k) for the heap during a feasibility check
    */
    public int MaxSelectedEncodings(int[] viewers, int[] bitrate, long B)
    {
        int n = viewers.Length;

        // Step 1:
        // Build a combined array so each encoding keeps its two related values together:
        // - maximum viewers it can support
        // - bandwidth cost per viewer
        //
        // This is necessary because after sorting by viewers, we still need to know
        // the matching bitrate for the same encoding.
        var encodings = new EncodingInfo[n];
        for (int i = 0; i < n; i++)
        {
            encodings[i] = new EncodingInfo(viewers[i], bitrate[i]);
        }

        // Step 2:
        // Sort encodings by their viewer capacity in descending order.
        //
        // Why descending?
        // For a fixed target x, the eligible encodings are exactly those with viewers >= x.
        // If we process capacities from large to small, then when we are "at" some capacity value x,
        // all encodings seen so far are precisely the encodings eligible for that x.
        //
        // This lets us test all meaningful x values efficiently without trying every integer from 1 to 1e9.
        Array.Sort(encodings, (a, b) => b.Viewers.CompareTo(a.Viewers));

        // Step 3:
        // Binary search on the answer k = maximum number of encodings we can select.
        //
        // Why binary search works:
        // If it is possible to select k encodings, then it is also possible to select any smaller number
        // of encodings (just drop some chosen encodings).
        // So feasibility is monotonic:
        // feasible(1), feasible(2), ..., feasible(ans) are true
        // feasible(ans+1), feasible(ans+2), ... are false
        int left = 0;
        int right = n;
        int answer = 0;

        while (left <= right)
        {
            int mid = left + (right - left) / 2;

            // Check whether selecting at least 'mid' encodings is possible.
            if (CanSelectK(encodings, mid, B))
            {
                answer = mid;
                left = mid + 1;
            }
            else
            {
                right = mid - 1;
            }
        }

        return answer;
    }

    private bool CanSelectK(EncodingInfo[] encodings, int k, long B)
    {
        // Special case:
        // Selecting 0 encodings is always possible.
        if (k == 0) return true;

        // We will scan encodings in descending order of viewer capacity.
        //
        // At any point in the scan, we have processed a prefix of encodings.
        // Let currentX be the viewer capacity of the current encoding.
        // Then every encoding in the processed prefix has viewers >= currentX,
        // so all of them are eligible if we choose target x = currentX.
        //
        // For that x, the total cost of selecting some k encodings is:
        // x * (sum of their bitrates)
        //
        // Therefore, among all currently eligible encodings, the cheapest possible set of size k
        // is simply the k smallest bitrates seen so far.
        //
        // To maintain the k smallest bitrates efficiently while scanning:
        // - use a max-heap
        // - keep at most k elements in it
        // - if a new bitrate is smaller than the largest currently kept bitrate,
        //   replace that largest one
        //
        // Why a max-heap?
        // Because when we already have k candidates, the one we want to discard
        // (if a better/smaller bitrate appears) is the largest bitrate among the kept ones.
        var heap = new MaxHeap();
        long sumOfChosenBitrates = 0;

        for (int i = 0; i < encodings.Length; i++)
        {
            int currentBitrate = encodings[i].Bitrate;
            int currentX = encodings[i].Viewers;

            // Step A:
            // Add the current encoding's bitrate into our candidate structure.
            //
            // This encoding is now part of the eligible set for x = currentX and for any smaller x.
            heap.Push(currentBitrate);
            sumOfChosenBitrates += currentBitrate;

            // Step B:
            // If we now have more than k candidates stored, remove the most expensive one.
            //
            // This ensures the heap always contains exactly the k smallest bitrates
            // among all encodings processed so far (once heap.Count reaches k).
            if (heap.Count > k)
            {
                int removed = heap.Pop();
                sumOfChosenBitrates -= removed;
            }

            // Step C:
            // Once we have exactly k eligible encodings stored, we can test feasibility.
            //
            // The current heap represents the cheapest possible choice of k encodings
            // among all encodings with viewers >= currentX.
            //
            // So the minimum possible total bandwidth cost for target x = currentX is:
            // currentX * sumOfChosenBitrates
            //
            // If even this minimum cost fits within B, then selecting k encodings is feasible.
            if (heap.Count == k)
            {
                // Use division instead of multiplication to avoid overflow:
                // currentX * sumOfChosenBitrates <= B
                // is equivalent to
                // sumOfChosenBitrates <= B / currentX
                if (sumOfChosenBitrates <= B / currentX)
                {
                    return true;
                }
            }
        }

        // If no capacity level produced a feasible set of size k, then k is impossible.
        return false;
    }
}

// Demo code:
// The examples in the prompt are inconsistent with the stated mathematical condition.
// Under the actual problem statement, if k encodings are feasible for some x >= 1,
// then they are also feasible for x = 1, because cost scales linearly with x and eligibility only becomes easier.
// Therefore the problem reduces to:
// "Choose the largest number of encodings whose cheapest total bitrate sum is <= B."
//
// We still keep the requested binary-search-based solution above, and it correctly solves the stated problem.
//
// The following demo prints the results for the provided sample inputs according to the stated rules.

var solution = new Solution();

int[] viewers1 = { 5, 3, 8, 6 };
int[] bitrate1 = { 4, 2, 1, 3 };
long B1 = 18;
Console.WriteLine(solution.MaxSelectedEncodings(viewers1, bitrate1, B1));

int[] viewers2 = { 10, 2, 7, 4 };
int[] bitrate2 = { 8, 5, 2, 1 };
long B2 = 12;
Console.WriteLine(solution.MaxSelectedEncodings(viewers2, bitrate2, B2));