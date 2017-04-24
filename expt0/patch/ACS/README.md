# Patch

Note that you can find the patch in a source file between something like `patch begin` and `patch end` surrounded by many *star*s.

**Lang 24 (Correct)**
> The patch is syntactically transformable and semantically identical to the developer patch.

**Math 61 (Correct)**
> The patch correctly creates the if-statement to throw an exception `NotStrictlyPositiveException` under the condition `p<=0`. Although the exceptional message is not as that in the developer patch, we still consider this patch to be semantically identical to the developer patch.

**Math 85 (Correct)**
> The patch is syntactically transformable and semantically identical to the developer patch.

**Time 15 (Correct)**
> We consider the patch which inserts the if-statement `if (val1==Long.MIN_VALUE){throw new ArithmeticException();}` to be semantically identical to the developer patch, though the exceptional message (empty here) is not as that in the developer patch.




