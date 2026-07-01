/*
Title: Count Customers with a Unique Favorite Product
Difficulty: Easy
Topic: Hashing

Problem Description:
You are given a list of customer purchase preferences. Each element in the list represents
the favorite product chosen by one customer, identified by a string product code.

A product is considered unique if exactly one customer selected it as their favorite.

Your task is to return the number of customers whose favorite product is unique across
the entire list.

In other words, count how many entries in the array belong to product codes that appear
exactly once.

Example 1:
Input: favorites = ["phone_case", "charger", "phone_case", "notebook", "pen"]
Output: 3
Explanation:
- "phone_case" appears 2 times, so it is not unique.
- "charger" appears 1 time, so it is unique.
- "notebook" appears 1 time, so it is unique.
- "pen" appears 1 time, so it is unique.
Therefore, 3 customers have a unique favorite product.

Example 2:
Input: favorites = ["mouse", "mouse", "keyboard", "keyboard", "monitor"]
Output: 1
Explanation:
- "mouse" appears 2 times, so it is not unique.
- "keyboard" appears 2 times, so it is not unique.
- "monitor" appears 1 time, so it is unique.
Therefore, 1 customer has a unique favorite product.

Approach:
Use a hash map (Dictionary<string, int>) to count how many times each product code appears.
Then count how many product codes have frequency exactly 1.
Because each unique product appears for exactly one customer, the number of unique products
is also the number of customers whose favorite product is unique.
*/

using System;
using System.Collections.Generic;

public class Solution
{
    /*
    Time Complexity: O(n) on average
    - We make one pass to count frequencies.
    - We make another pass over the array to count entries whose frequency is 1.
    - Dictionary operations are O(1) on average.

    Space Complexity: O(n)
    - In the worst case, every product code is different, so the dictionary stores n entries.
    */
    public int CountUniqueFavoriteCustomers(string[] favorites)
    {
        // This dictionary will store:
        // key   = product code
        // value = how many times that product code appears in the input array
        //
        // Why use a Dictionary?
        // Because we need fast lookups and updates by product code.
        // A dictionary gives average O(1) time for checking whether a key exists,
        // reading its current count, and updating that count.
        var frequency = new Dictionary<string, int>();

        // STEP 1: Count how many times each favorite product appears.
        //
        // This is necessary because we cannot know whether a product is unique
        // until we know its total number of appearances in the full array.
        //
        // Example:
        // favorites = ["phone_case", "charger", "phone_case", "notebook", "pen"]
        //
        // After this loop, frequency will contain:
        // "phone_case" -> 2
        // "charger"    -> 1
        // "notebook"   -> 1
        // "pen"        -> 1
        foreach (string product in favorites)
        {
            // If the product has already been seen before,
            // increase its count by 1.
            if (frequency.ContainsKey(product))
            {
                frequency[product]++;
            }
            else
            {
                // If this is the first time we see this product,
                // start its count at 1.
                frequency[product] = 1;
            }
        }

        // This variable will store the final answer:
        // the number of customers whose favorite product is unique.
        int uniqueCustomerCount = 0;

        // STEP 2: Go through the original array again.
        //
        // Why scan the original array instead of only the dictionary?
        // Because the problem asks for the number of customers whose favorite product is unique.
        // Each array element represents one customer.
        //
        // If a customer's product has frequency 1, then that customer should be counted.
        //
        // In this specific problem, counting dictionary entries with value 1 would also work,
        // because each unique product contributes exactly one customer.
        // But scanning the original array makes the "count customers" interpretation very explicit.
        foreach (string product in favorites)
        {
            // Look up how many times this customer's favorite product appears overall.
            //
            // If the frequency is exactly 1, that means:
            // - this product appears only once in the entire list
            // - therefore this customer's favorite product is unique
            // - so we add 1 to the answer
            if (frequency[product] == 1)
            {
                uniqueCustomerCount++;
            }
        }

        // Return the total number of customers whose favorite product is unique.
        return uniqueCustomerCount;
    }
}

// Demo code

var solution = new Solution();

// Example 1
string[] favorites1 = { "phone_case", "charger", "phone_case", "notebook", "pen" };
int result1 = solution.CountUniqueFavoriteCustomers(favorites1);
Console.WriteLine("Example 1 Result: " + result1); // Expected: 3

// Example 2
string[] favorites2 = { "mouse", "mouse", "keyboard", "keyboard", "monitor" };
int result2 = solution.CountUniqueFavoriteCustomers(favorites2);
Console.WriteLine("Example 2 Result: " + result2); // Expected: 1

// Additional quick demo
string[] favorites3 = { "a", "b", "c", "a", "d", "e", "e" };
int result3 = solution.CountUniqueFavoriteCustomers(favorites3);
Console.WriteLine("Additional Demo Result: " + result3); // Expected: 3 ("b", "c", "d")