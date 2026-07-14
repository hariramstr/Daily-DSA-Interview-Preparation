/*
Title: Longest Event Span With Matching Endpoint Signature

Problem Description:
You are given an integer array events of length n, where events[i] represents the type of event that occurred at time i.
A contiguous span events[l..r] is called signature-balanced if the event type at the left endpoint and the event type
at the right endpoint have the same total frequency inside that span.

In other words, if:
    x = events[l]
    y = events[r]
then the span is valid when:
    count(x in events[l..r]) == count(y in events[l..r])

Note:
- x and y may be the same value; in that case the condition is automatically satisfied.
- We must return the maximum possible length of a signature-balanced contiguous span.

Constraints:
- 1 <= n <= 200000
- -10^9 <= events[i] <= 10^9
- The answer fits in a 32-bit signed integer.

Key observation:
For a span [l..r], let pref[v][i] be the number of occurrences of value v in events[0..i-1].
Then:
    count(v in events[l..r]) = pref[v][r+1] - pref[v][l]

The condition for endpoints x = events[l], y = events[r] is:
    pref[x][r+1] - pref[x][l] == pref[y][r+1] - pref[y][l]

Rearrange:
    pref[x][r+1] - pref[y][r+1] == pref[x][l] - pref[y][l]

So for a fixed ordered pair of endpoint values (x, y), we want:
    D_xy(position) = pref[x][position] - pref[y][position]
to have the same value at position l and position r+1.

That means:
- when we are at right endpoint r with value y,
- for every possible left endpoint value x,
- if we know the earliest index l where events[l] = x and D_xy(l) equals D_xy(r+1),
  then [l..r] is valid.

The challenge is doing this efficiently for n up to 200000.

Efficient strategy:
- Compress values to ids 0..m-1.
- Let total frequency of each value be freq[id].
- Split values into:
    * heavy values: freq > B
    * light values: freq <= B
  where B is about sqrt(n).
- For spans whose left endpoint value equals right endpoint value, the answer is simply the maximum distance
  between first and last occurrence of the same value, plus 1.
- For pairs involving a heavy value, we can process all right endpoints in O(n) per heavy value.
- For pairs of two light values, only values that actually appear matter, and each light value has at most B occurrences,
  so we can enumerate occurrence pairs efficiently enough.

This gives an overall near O(n * sqrt(n)) solution, which is suitable for n = 200000.
*/

using System;
using System.Collections.Generic;
using System.Linq;

class Solution
{
    /*
    Time Complexity:
    - Coordinate compression: O(n log n)
    - Same-endpoint-value spans: O(n)
    - Heavy processing: O(H * n), where H <= sqrt(n)
    - Light-light processing: O(sum over light pairs of (freq[a] + freq[b])) in an amortized near O(n * sqrt(n))
    Overall: approximately O(n * sqrt(n)) for the given constraints.

    Space Complexity:
    - O(n) for compressed array and occurrence lists
    - O(m) for frequencies and helper arrays
    - O(n) temporary arrays during heavy processing
    Overall: O(n)
    */
    public int LongestSignatureBalancedSpan(int[] events)
    {
        int n = events.Length;
        if (n == 0) return 0;

        // ------------------------------------------------------------
        // STEP 1: Coordinate compression
        // ------------------------------------------------------------
        // Why do we do this?
        // The event values can be as small as -1e9 and as large as 1e9.
        // Using them directly as array indices is impossible.
        //
        // Coordinate compression maps each distinct event value to a compact id:
        //     original values -> 0, 1, 2, ..., m-1
        //
        // This lets us store frequencies, occurrence lists, and helper arrays efficiently.
        // ------------------------------------------------------------
        int[] sorted = events.Distinct().ToArray();
        Array.Sort(sorted);

        int m = sorted.Length;
        int[] a = new int[n];
        for (int i = 0; i < n; i++)
        {
            a[i] = Array.BinarySearch(sorted, events[i]);
        }

        // ------------------------------------------------------------
        // STEP 2: Build frequency counts and occurrence lists
        // ------------------------------------------------------------
        // For each compressed value id:
        // - freq[id] tells us how many times it appears in the whole array
        // - positions[id] stores all indices where it appears
        //
        // These occurrence lists are extremely useful:
        // - same-value endpoints can be solved immediately from first/last occurrence
        // - light-light processing will iterate through these lists
        // ------------------------------------------------------------
        int[] freq = new int[m];
        for (int i = 0; i < n; i++) freq[a[i]]++;

        List<int>[] positions = new List<int>[m];
        for (int i = 0; i < m; i++) positions[i] = new List<int>(freq[i]);
        for (int i = 0; i < n; i++) positions[a[i]].Add(i);

        int answer = 1;

        // ------------------------------------------------------------
        // STEP 3: Handle spans whose endpoint values are the same
        // ------------------------------------------------------------
        // If events[l] == events[r], then the condition is automatically satisfied,
        // because we are comparing the frequency of the same value with itself.
        //
        // Therefore, for each distinct value, the longest valid span with equal endpoints
        // is simply from its first occurrence to its last occurrence.
        // ------------------------------------------------------------
        for (int id = 0; id < m; id++)
        {
            if (positions[id].Count > 0)
            {
                int len = positions[id][positions[id].Count - 1] - positions[id][0] + 1;
                if (len > answer) answer = len;
            }
        }

        // ------------------------------------------------------------
        // STEP 4: Heavy / light split
        // ------------------------------------------------------------
        // A classic optimization:
        // - Heavy values appear many times.
        // - Light values appear only a few times.
        //
        // Why split?
        // Because different processing strategies are efficient for each case.
        //
        // Let B be around sqrt(n).
        // Then:
        // - Number of heavy values is at most n / B = O(sqrt(n))
        // - Each light value appears at most B times
        //
        // This balance is what makes the whole algorithm fast.
        // ------------------------------------------------------------
        int B = (int)Math.Sqrt(n) + 1;

        List<int> heavyIds = new List<int>();
        bool[] isHeavy = new bool[m];
        for (int id = 0; id < m; id++)
        {
            if (freq[id] > B)
            {
                isHeavy[id] = true;
                heavyIds.Add(id);
            }
        }

        // ------------------------------------------------------------
        // STEP 5: Process all pairs where the LEFT endpoint value x is heavy
        // ------------------------------------------------------------
        // We fix a heavy value x.
        //
        // Recall the transformed condition:
        // For a valid span [l..r] with left value x and right value y:
        //     pref[x][l] - pref[y][l] == pref[x][r+1] - pref[y][r+1]
        //
        // For fixed x, define:
        //     state_y(pos) = pref[x][pos] - pref[y][pos]
        //
        // As we scan positions from left to right, we want to know:
        // for each possible right endpoint value y at index r,
        // what is the earliest index l with value x such that:
        //     state_y(l) == state_y(r+1)
        //
        // To support this efficiently for all y at once:
        // - maintain countX = pref[x][current position]
        // - maintain count[id] = pref[id][current position]
        // - for each y, the current state is countX - count[y]
        //
        // We store, for each y and each state value, the earliest index l where:
        // - events[l] == x
        // - state_y(l) had that value
        //
        // Since x is heavy, there are only O(sqrt(n)) such x values,
        // so O(n) work per heavy x is acceptable.
        // ------------------------------------------------------------
        foreach (int x in heavyIds)
        {
            // count[id] = how many times value id has appeared in the prefix processed so far
            int[] count = new int[m];

            // For each y, we need a map:
            //     state value -> earliest left index l where events[l] == x and state_y(l) = that value
            //
            // Instead of creating m dictionaries of arbitrary size in a complicated structure,
            // we create them once per heavy x.
            Dictionary<int, int>[] earliest = new Dictionary<int, int>[m];
            for (int id = 0; id < m; id++)
            {
                earliest[id] = new Dictionary<int, int>();
            }

            // We scan positions i from 0 to n-1.
            // At the start of iteration i, count[] represents prefix counts up to i-1,
            // which is exactly the "position l" form needed when considering l = i.
            for (int i = 0; i < n; i++)
            {
                // If current index i can serve as a left endpoint with value x,
                // record its state for every possible y.
                //
                // Why before updating count[a[i]]?
                // Because the formula uses pref[*][l], i.e. counts BEFORE index l is included.
                if (a[i] == x)
                {
                    int countXBefore = count[x];

                    for (int y = 0; y < m; y++)
                    {
                        int state = countXBefore - count[y];

                        // We only care about the earliest such left endpoint,
                        // because earlier l gives a longer span.
                        if (!earliest[y].ContainsKey(state))
                        {
                            earliest[y][state] = i;
                        }
                    }
                }

                // Now include events[i] into the prefix counts.
                count[a[i]]++;

                // If current index i is used as a right endpoint with value y = a[i],
                // then we need state_y(r+1), which is based on counts AFTER including i.
                int yRight = a[i];
                int stateAtRightPlusOne = count[x] - count[yRight];

                if (earliest[yRight].TryGetValue(stateAtRightPlusOne, out int leftIndex))
                {
                    int len = i - leftIndex + 1;
                    if (len > answer) answer = len;
                }
            }
        }

        // ------------------------------------------------------------
        // STEP 6: Process pairs where both endpoint values are light
        // ------------------------------------------------------------
        // Now we only need to consider pairs (x, y) where:
        // - x is light
        // - y is light
        // - x != y
        //
        // For a fixed pair (x, y), the condition depends only on the relative order
        // of occurrences of x and y.
        //
        // Important simplification:
        // If a span [l..r] has endpoints x and y, then only x and y affect:
        //     pref[x][pos] - pref[y][pos]
        // All other values are irrelevant to this difference.
        //
        // So for a pair (x, y), we can merge the occurrence lists of x and y into one sequence.
        // Treat:
        // - occurrence of x as +1
        // - occurrence of y as -1
        //
        // Let balance be pref[x] - pref[y] restricted to this merged sequence.
        // Then a span from an occurrence of x at left to an occurrence of y at right is valid
        // exactly when the balance before the left occurrence equals the balance after the right occurrence.
        //
        // We can process each light-light pair in O(freq[x] + freq[y]).
        // Since each light frequency is at most B, this is efficient enough overall.
        // ------------------------------------------------------------
        List<int> lightIds = new List<int>();
        for (int id = 0; id < m; id++)
        {
            if (!isHeavy[id]) lightIds.Add(id);
        }

        // To avoid processing both (x, y) and (y, x) redundantly in a confusing way,
        // we process ordered pairs because left endpoint value and right endpoint value
        // are not symmetric in the scan logic.
        //
        // However, each ordered pair still costs only O(freq[x] + freq[y]),
        // and with light frequencies bounded by B, this remains manageable.
        foreach (int x in lightIds)
        {
            foreach (int y in lightIds)
            {
                if (x == y) continue;

                var px = positions[x];
                var py = positions[y];

                // Merge the occurrence lists of x and y in sorted index order.
                //
                // mergedPos[k] = actual array index
                // mergedType[k] = +1 if x occurred there, -1 if y occurred there
                int ix = 0, iy = 0;
                int total = px.Count + py.Count;
                int[] mergedPos = new int[total];
                int[] mergedType = new int[total];
                int t = 0;

                while (ix < px.Count || iy < py.Count)
                {
                    if (iy == py.Count || (ix < px.Count && px[ix] < py[iy]))
                    {
                        mergedPos[t] = px[ix];
                        mergedType[t] = +1;
                        ix++;
                    }
                    else
                    {
                        mergedPos[t] = py[iy];
                        mergedType[t] = -1;
                        iy++;
                    }
                    t++;
                }

                // earliestBalance[balance] = earliest actual array index l
                // such that:
                // - events[l] == x
                // - balance BEFORE that x occurrence equals "balance"
                //
                // We use a dictionary because balances can be negative.
                Dictionary<int, int> earliestBalance = new Dictionary<int, int>();

                int balance = 0;

                for (int k = 0; k < total; k++)
                {
                    if (mergedType[k] == +1)
                    {
                        // This position is an occurrence of x.
                        // It can serve as a left endpoint.
                        //
                        // The needed state is the balance BEFORE consuming this x,
                        // because that corresponds to pref[x][l] - pref[y][l].
                        if (!earliestBalance.ContainsKey(balance))
                        {
                            earliestBalance[balance] = mergedPos[k];
                        }

                        // Now consume this x occurrence.
                        balance++;
                    }
                    else
                    {
                        // This position is an occurrence of y.
                        // It can serve as a right endpoint.
                        //
                        // For the right endpoint, we need the balance AFTER consuming this y,
                        // because that corresponds to pref[x][r+1] - pref[y][r+1].
                        balance--;

                        if (earliestBalance.TryGetValue(balance, out int leftIndex))
                        {
                            int len = mergedPos[k] - leftIndex + 1;
                            if (len > answer) answer = len;
                        }
                    }
                }
            }
        }

        return answer;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

int[] events1 = { 4, 1, 2, 1, 4, 2, 1 };
int result1 = solution.LongestSignatureBalancedSpan(events1);
Console.WriteLine(result1); // Expected: 6

int[] events2 = { 7, 3, 7, 5, 3, 5, 7 };
int result2 = solution.LongestSignatureBalancedSpan(events2);
Console.WriteLine(result2); // Expected: 5

// Additional quick sanity checks
int[] events3 = { 1 };
Console.WriteLine(solution.LongestSignatureBalancedSpan(events3)); // Expected: 1

int[] events4 = { 1, 2 };
Console.WriteLine(solution.LongestSignatureBalancedSpan(events4)); // Expected: 1

int[] events5 = { 1, 2, 1, 2 };
Console.WriteLine(solution.LongestSignatureBalancedSpan(events5)); // One valid longest span is [1,2,1,2], expected: 4