# Patch

**Lang 51 (Correct)**
> The generated patches are all syntactically and semantically identical to the developer patch.

**Math 70 (Correct)**
> The generated patches are all syntactically and semantically identical to the developer patch.

**Math 75 (Correct)**
> The generated patches (two out of the three trials) are both syntactically and semantically identical to the developer patch.

**Math 79 (Valid)**
> Two of the generated patches correctly repair one of the two modification places in the developer patch through changing `fp*fp` to `(float) fp*fp` and `(double) fp*fp` respectively to avoid precision loss.

**Math_94 (Correct)**
> Two of the generated patches change `u*v` to `(double) u*v` and `(long) u*v` respectively to avoid precision loss. The developer patch does not use `u*v` to avoid such precision loss. We believe such generated patches are correct.