# Patch

**Math 50 (Correct)**
> The generated patch deletes the faulty statement `x0 = 0.5 * (x0 + x1 - delta);` and is semantically equivalent to the deletion of the enclosing if-statement as the developer patch does. This is because after deleting `x0 = 0.5 * (x0 + x1 - delta);`, `delta` would never be used, so it makes no difference that the declaration statement for `delta` is not deleted. It also makes no difference that the assignment `f0 = computeObjectiveValue(x0);` is not deleted since there is a break at the end under the case `REGULA_FALSI`, and the local variable `f0` is never used afterwards.
