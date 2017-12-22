First attempt at a really simple Android app, written in Kotlin. The purpose is to allow tracking 
time for long distance swims 400m/500y/800m/1000y/1650y/1500m, estimating the time for the swimmer
based on their split times.

Initially, you can select a target time, and the app calculates and shows a target split time. 
This time assumes splits at the start end, every two laps.

When the race starts, hitting start will start the app timing. You can then use either the Split 
button or the volume controls to mark the end of each pair of laps. The screen will show the card 
that would be displayed for the swimmer at the turn end. 

The race screen shows the current elapsed time, the elapsed time for the current split, an
estimate of the distance the swimmer has swum in total, based on the average time for splits, 
and their expected finish time based on the current pace.

If you accidentally hit the volume button, or tap the screen twice, you can undo it by hitting the
'oops' button on the left hand side. There's another oops button on the right hand screen, that I 
intend to implement to allow the user to mark a missed split, without significantly impacting the 
estimate times (basically, assume the average split occurred, unless it hasn't happened yet, in which
case take the current time).

Ideas for future functionality:

 * support multiple swimmers, and tracking last best time
 * support split history for previous races
 * show graph of splits vs time
 * use previous historic split times to improve predictions  - for instance, the initial and final
   laps are usually about 10% faster than other laps, and the laps tend to slow down slightly from
   the in the middle.

In this iteration, I tried to create very basic Android app, just using kotlin without a lot of 
additional features. On the technical side, I'd like to change the app up a little to try some of
the additional features available from kotlin libraries like anko and the coroutine support, to see
how this changes the app development experience. Also, I want to try to add some basic unit tests 
for the race tracking functionality, as this is where I've seen the most bugs while developing, and
work out how to get some simple UI tests going (probably using espresso?).