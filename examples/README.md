# Examples

_Keep in mind_: Currently boats are considered to go with a speed of 1m/s, meaning they are really slow.
We should probably upgrade this to something closer to 20kn which is the maximum speed a sea taxi can go.

This directory contains a list of examples that can be used to test the recommendation service.

The list contains the following examples:

## Simple example

A simple demonstration of how the weight selection affects results. Boats are way slower than 
scooters, so when selecting time as weight, the recommendation is to take the scooter for the whole trip,
while when selecting distance, the recommendation changes to using the boat. 


## Long distance

An example where all modes of transprotation are used. Since the range of other vehicles is limited, 
the longest part of the trip happens in the car.

## Long distance 2 

Very similar to the above, but now the scooter is further away so it's never used.

## Boat Trip

This is also a very long distance trip that depending on wether we chose distance or time as weight
we end up using different means of transportation.
