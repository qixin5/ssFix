# Experiments

All the generated patches by all the tools (ssFix, jGenProg, jKali, NoPol, HDRepair,
and ACS) can be found under `./patch`.

All the candidates that ssFix used for producing all the plausible patches can be
found under `./candidate`.

The repair information for each bug for which ssFix produced a plausible patch
can be found in `./rslt`. At each line, from left to right, it shows the bug id,
the running time (in minutes) for find the patch, whether it is plausible (since
we only show all the bugs for which ssFix generated plausible patches, all the
rows in this column are TRUE), whether it is valid/correct (TRUE_STAR or TRUE_STAR_HW
for a valid patch, TRUE for a correct patch, FALSE for a defective patch, and UKN for
a correctness-unknown patch), the rank of the candidate used for producing the patch,
whether the candidate is local or non-local, and the number of patches that ssFix
tested before finding the patch.
