/*
 * Shopping Cart Checkout Queue
 * ============================
 * Problem: Given a singly linked list where each node contains the price of one item,
 * determine if the cart is "balanced" — meaning the total price of items in the first
 * half equals the total price of items in the second half.
 *
 * If the list has an odd number of nodes, the middle node is excluded from both halves.
 *
 * Example 1: [3, 7, 2, 7, 3] → true  (first half sum = 10, second half sum = 10)
 * Example 2: [1, 4, 9, 2]    → false (first half sum = 5,  second half sum = 11)
 *
 * Constraints:
 *   - Number of nodes: [1, 10^4]
 *   - Node values: [1, 1000]
 *   - Must solve in O(n) time and O(1) extra space
 */

// ─── Node Definition ────────────────────────────────────────────────────────

/// <summary>
/// Represents a single node in the singly linked list (one item in the cart).
/// </summary>
public class ListNode
{
    public int val;        // The price of this cart item
    public ListNode? next; // Reference to the next item in the cart (null if last)

    public ListNode(int val = 0, ListNode? next = null)
    {
        this.val = val;
        this.next = next;
    }
}

// ─── Solution Class ──────────────────────────────────────────────────────────

/// <summary>
/// Contains the algorithm to check whether a shopping cart is "balanced".
/// </summary>
public class Solution
{
    /// <summary>
    /// Determines if the sum of the first half of the linked list equals
    /// the sum of the second half. The middle node (for odd-length lists)
    /// is excluded from both halves.
    ///
    /// Time Complexity:  O(n) — we traverse the list a constant number of times.
    /// Space Complexity: O(1) — we only use a fixed number of extra variables
    ///                          (no arrays, stacks, or recursion that grow with n).
    /// </summary>
    /// <param name="head">The head node of the linked list.</param>
    /// <returns>True if both halves have equal sums; otherwise false.</returns>
    public bool IsBalancedCart(ListNode? head)
    {
        // ── Step 1: Handle edge cases ────────────────────────────────────────
        // A list with 0 or 1 node has no meaningful "two halves".
        // A single node is trivially balanced (both halves are empty, sum = 0).
        if (head == null || head.next == null)
            return true;

        // ── Step 2: Find the total length of the list ────────────────────────
        // We need to know the length so we can figure out:
        //   • How many nodes belong to the first half.
        //   • Where the second half starts.
        // For an even-length list of n nodes: each half has n/2 nodes.
        // For an odd-length  list of n nodes: each half has n/2 nodes (integer division),
        //   and the middle node at index n/2 is skipped.
        int length = 0;
        ListNode? current = head;
        while (current != null)
        {
            length++;
            current = current.next;
        }
        // After this loop, `length` holds the total number of nodes.
        // Example 1: [3,7,2,7,3] → length = 5
        // Example 2: [1,4,9,2]   → length = 4

        // ── Step 3: Determine the size of each half ──────────────────────────
        // Integer division automatically handles both even and odd lengths:
        //   length = 5 → halfSize = 2  (nodes 0,1 are first half; node 2 skipped; nodes 3,4 are second half)
        //   length = 4 → halfSize = 2  (nodes 0,1 are first half; nodes 2,3 are second half)
        int halfSize = length / 2;

        // ── Step 4: Sum the first half ───────────────────────────────────────
        // Walk through the first `halfSize` nodes and accumulate their values.
        int firstHalfSum = 0;
        current = head; // Reset pointer back to the beginning of the list
        for (int i = 0; i < halfSize; i++)
        {
            // We know current is not null here because i < halfSize <= length
            firstHalfSum += current!.val;
            current = current.next;
        }
        // After this loop:
        //   Example 1: firstHalfSum = 3 + 7 = 10,  current points to node with val=2 (the middle)
        //   Example 2: firstHalfSum = 1 + 4 = 5,   current points to node with val=9

        // ── Step 5: Skip the middle node (for odd-length lists) ──────────────
        // If the list has an odd number of nodes, `current` now points to the
        // middle node, which must be excluded from both halves.
        // We detect an odd-length list by checking if length is odd.
        // For even-length lists, this step does nothing — current already points
        // to the first node of the second half.
        if (length % 2 != 0)
        {
            // Advance past the middle node
            current = current?.next;
            // Example 1 (odd, length=5): current was at val=2, now moves to val=7
        }
        // After this step:
        //   Example 1: current points to node with val=7 (start of second half)
        //   Example 2: current points to node with val=9 (start of second half)

        // ── Step 6: Sum the second half ──────────────────────────────────────
        // Walk through the remaining `halfSize` nodes and accumulate their values.
        int secondHalfSum = 0;
        for (int i = 0; i < halfSize; i++)
        {
            // We know current is not null here because there are exactly halfSize nodes left
            secondHalfSum += current!.val;
            current = current.next;
        }
        // After this loop:
        //   Example 1: secondHalfSum = 7 + 3 = 10
        //   Example 2: secondHalfSum = 9 + 2 = 11

        // ── Step 7: Compare the two halves ───────────────────────────────────
        // The cart is "balanced" if and only if both sums are equal.
        // Example 1: 10 == 10 → true  ✓
        // Example 2:  5 == 11 → false ✓
        return firstHalfSum == secondHalfSum;
    }
}

// ─── Helper: Build a linked list from an array ───────────────────────────────

/// <summary>
/// Utility class for building and displaying linked lists in demos.
/// </summary>
static class LinkedListHelper
{
    /// <summary>
    /// Builds a singly linked list from an integer array.
    /// The first element of the array becomes the head of the list.
    /// </summary>
    public static ListNode? Build(int[] values)
    {
        if (values == null || values.Length == 0)
            return null;

        // Create a dummy head node to simplify building the list
        ListNode dummy = new ListNode(0);
        ListNode tail = dummy;

        foreach (int v in values)
        {
            tail.next = new ListNode(v);
            tail = tail.next;
        }

        return dummy.next; // Return the actual first node (skip dummy)
    }

    /// <summary>
    /// Converts a linked list to a readable string like "[3 → 7 → 2 → 7 → 3]".
    /// </summary>
    public static string ToString(ListNode? head)
    {
        if (head == null) return "[]";

        var parts = new System.Text.StringBuilder("[");
        ListNode? current = head;
        while (current != null)
        {
            parts.Append(current.val);
            if (current.next != null) parts.Append(" → ");
            current = current.next;
        }
        parts.Append("]");
        return parts.ToString();
    }
}

// ─── Demo / Test Code ────────────────────────────────────────────────────────

Console.WriteLine("=== Shopping Cart Checkout Queue ===");
Console.WriteLine("Checking if cart item prices are balanced between first and second halves.\n");

Solution solution = new Solution();

// ── Test Case 1: Odd-length list, balanced ───────────────────────────────────
// [3, 7, 2, 7, 3]
// First half: [3, 7] → sum = 10
// Middle (excluded): [2]
// Second half: [7, 3] → sum = 10
// Expected: true
int[] cart1Values = { 3, 7, 2, 7, 3 };
ListNode? cart1 = LinkedListHelper.Build(cart1Values);
bool result1 = solution.IsBalancedCart(cart1);
Console.WriteLine($"Test 1 — Cart: {LinkedListHelper.ToString(cart1)}");
Console.WriteLine($"         First half sum = 10, Middle node (excluded) = 2, Second half sum = 10");
Console.WriteLine($"         Result: {result1}  (Expected: True)\n");

// ── Test Case 2: Even-length list, not balanced ──────────────────────────────
// [1, 4, 9, 2]
// First half: [1, 4] → sum = 5
// Second half: [9, 2] → sum = 11
// Expected: false
int[] cart2Values = { 1, 4, 9, 2 };
ListNode? cart2 = LinkedListHelper.Build(cart2Values);
bool result2 = solution.IsBalancedCart(cart2);
Console.WriteLine($"Test 2 — Cart: {LinkedListHelper.ToString(cart2)}");
Console.WriteLine($"         First half sum = 5, Second half sum = 11");
Console.WriteLine($"         Result: {result2}  (Expected: False)\n");

// ── Test Case 3: Single node ─────────────────────────────────────────────────
// [42]
// Only one node — trivially balanced (both halves are empty)
// Expected: true
int[] cart3Values = { 42 };
ListNode? cart3 = LinkedListHelper.Build(cart3Values);
bool result3 = solution.IsBalancedCart(cart3);
Console.WriteLine($"Test 3 — Cart: {LinkedListHelper.ToString(cart3)}");
Console.WriteLine($"         Single node — both halves are empty (sum = 0 each)");
Console.WriteLine($"         Result: {result3}  (Expected: True)\n");

// ── Test Case 4: Even-length list, balanced ──────────────────────────────────
// [5, 10, 8, 7]
// First half: [5, 10] → sum = 15
// Second half: [8, 7] → sum = 15
// Expected: true
int[] cart4Values = { 5, 10, 8, 7 };
ListNode? cart4 = LinkedListHelper.Build(cart4Values);
bool result4 = solution.IsBalancedCart(cart4);
Console.WriteLine($"Test 4 — Cart: {LinkedListHelper.ToString(cart4)}");
Console.WriteLine($"         First half sum = 15, Second half sum = 15");
Console.WriteLine($"         Result: {result4}  (Expected: True)\n");

// ── Test Case 5: Two nodes, not balanced ─────────────────────────────────────
// [3, 7]
// First half: [3] → sum = 3
// Second half: [7] → sum = 7
// Expected: false
int[] cart5Values = { 3, 7 };
ListNode? cart5 = LinkedListHelper.Build(cart5Values);
bool result5 = solution.IsBalancedCart(cart5);
Console.WriteLine($"Test 5 — Cart: {LinkedListHelper.ToString(cart5)}");
Console.WriteLine($"         First half sum = 3, Second half sum = 7");
Console.WriteLine($"         Result: {result5}  (Expected: False)\n");

// ── Test Case 6: Odd-length list, not balanced ───────────────────────────────
// [1, 2, 99, 5, 6]
// First half: [1, 2] → sum = 3
// Middle (excluded): [99]
// Second half: [5, 6] → sum = 11
// Expected: false
int[] cart6Values = { 1, 2, 99, 5, 6 };
ListNode? cart6 = LinkedListHelper.Build(cart6Values);
bool result6 = solution.IsBalancedCart(cart6);
Console.WriteLine($"Test 6 — Cart: {LinkedListHelper.ToString(cart6)}");
Console.WriteLine($"         First half sum = 3, Middle node (excluded) = 99, Second half sum = 11");
Console.WriteLine($"         Result: {result6}  (Expected: False)\n");

Console.WriteLine("=== All tests complete ===");