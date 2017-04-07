# Patch

**Chart 1**
> The faulty program works incorrect both when `dataset` is and is not `null`. The patched program by developer works correct both when `dataset` is and is not `null`. The generated patch deletes the faulty if-statement. Now it works fine when `dataset` is not `null`, but still works incorrectly when `dataset` is `null`. Since the patched program makes the test suite pass and does not introduce regressions, we consider the patch as **valid** .

**Chart 20**
> The generated patch is both syntactically and semantically identical to the developer patch.

**Chart 24**
> The generated patch is syntactically rewrittable and semantically identical to the developer patch.

