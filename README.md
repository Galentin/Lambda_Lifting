# Lambda_Lifting

Solution for the Lambda Lifting game (ICFPC 2012)

### Description
The task is to go through the maze, collect all the lambdas and get into the elevator.

Scoring system:
* For each step 1 point is deducted
* For each collected lambda, 25 points are added
* For each collected lambda at the moment of command A execution, 25 points are added;
* For each collected lambda at the moment of reaching the outputs, 50 points are added

### Legend
+ R - robot
+ \* - a rock
+ L - closed exit
+ O - open exit
+ . - earth
+ \# - wall
+ ! - razor
+ \ - lambda
+ “” - space, empty cell
+ W - beard
+ @ - a higher order stone
+ A..I - springboard entry
+ 1..9 - springboard exit
