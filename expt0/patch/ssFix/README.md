# Patch

**Chart 1 (Valid)**
> The faulty program works incorrectly both when `dataset` is and is not `null`. The patched program by developer works correctly both when `dataset` is and is not `null`. The generated patch deletes the body of the faulty if-statement (which essentially deletes the faulty if-statement). Now it works fine when `dataset` is not `null`, but still works incorrectly when `dataset` is `null`. Since the patched program makes the test suite pass and does not introduce regressions, we consider the patch as **valid**.

**Chart 20 (Correct)**
> The generated patch is both syntactically and semantically identical to the developer patch.

**Chart 24 (Correct)**
> The generated patch is syntactically transformable from (and is thus syntactically identical to) the developer patch. The patch is also semantically identical to the developer patch.

**Closure 115 (Valid)**
> The generated patch correctly repairs one of the two modification places in the developer patch by deleting `return CanInlineResult.NO;` (note that this is semantically equivalent to deleting `if (hasSideEffects && NodeUtil.canBeSideEffected(cArg)) { return CanInlineResult.NO; }` since the if-condition only checks and does not modify any program states), and passes the test suite.

**Closure 14 (Correct)**
> The generated patch is both syntactically and semantically identical to the developer patch.

**Lang 21 (Correct)**
> The generated patch is both syntactically and semantically identical to the developer patch.

**Lang 33 (Correct)**
> The generated patch is both syntactically and semantically identical to the developer patch.

**Lang 43 (Correct)**
> The generated patch is both syntactically and semantically identical to the developer patch.

**Lang 59 (Correct)**
> The generated patch is both syntactically and semantically identical to the developer patch.

**Lang 6 (Correct)**
> The generated patch is both syntactically and semantically identical to the developer patch.

**Math 30 (Valid)**
> The developer patch changes the declared type of `n1n2prod` from `int` to `double` to avoid the precision loss happened at `n1n2prod * (n1 + n2 + 1)`. The generated patch avoids the precision loss by casting `n1n2prod` to `double` when computing the multiplification and passes the test suite.

**Math 33 (Correct)**
> The generated patch is both syntactically and semantically identical to the developer patch.

**Math 41 (Correct)**
> The generated patch is both syntactically and semantically identical to the developer patch.

**Math 50 (Correct)**
> The generated patch deletes the faulty statement `x0 = 0.5 * (x0 + x1 - delta);` and is semantically equivalent to the deletion of the enclosing if-statement as the developer patch does. This is because after deleting `x0 = 0.5 * (x0 + x1 - delta);`, `delta` would never be used, so it makes no difference that the declaration statement for `delta` is not deleted. It also makes no difference that the assignment `f0 = computeObjectiveValue(x0);` is not deleted since there is a break at the end under the case `REGULA_FALSI`, and the local variable `f0` is never used afterwards.

**Math 53 (Correct)**
> The generated patch is both syntactically and semantically identical to the developer patch.

**Math 57 (Valid)**
> The developer patch changes the declared type of `sum` from `int` to `double` to avoid precision loss. The generated patch changes the type from `int` to `float`, and passes the test suite.

**Math 59 (Correct)**
> The generated patch is both syntactically and semantically identical to the developer patch.

**Math 70 (Correct)**
> The generated patch is both syntactically and semantically identical to the developer patch.

**Math 79 (Valid)**
> The generated patch correctly repairs one of the two modification places in the developer patch to avoid precision loss, and passes the test suite.

**Math 80 (Correct)**
> The generated patch is syntactically transformable from (and is thus syntactically identical to) the developer patch. The patch is also semantically identical to the developer patch.



