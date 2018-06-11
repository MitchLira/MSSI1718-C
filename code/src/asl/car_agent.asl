// Agent sample_agent in project bdi

/* Initial beliefs and rules */

state(initial).

actions([]).

/* Initial goals */

!start.

/* Plans */

+!start : true <- actions.chooseRoute(Route); createAvatar(Route).

+intersection : true <- actions.chooseRoute(Current, New).

+arrived(_): true <- actions.updateQValues(Values, Temperature); signalArrival(Values, Temperature).