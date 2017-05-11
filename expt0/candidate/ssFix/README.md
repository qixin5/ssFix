# Candidate

Here you can find the candidates ssFix used to produce all the plausible patches.

In the directory of either *plausible* or *valid*, you can find the information
of a candidate from a file named *&ast;bug-id&ast;*. The information includes the
candidate file path in our code database, the chunk location in the file, and
the similarity score.

For example, the information of the candidate used for repairing the bug `Lang_21`
can be found in `./plausible/Lang_21`:
> file:///gpfs/data/people/qx5/merobase_split_100/87/srcs/5349.java,slc:344,2;slc:345,2;slc:346,2,7.225489

where `file:///gpfs/data/people/qx5/merobase_split_100/87/srcs/5349.java` is the
candidate file path, `slc:344,2;slc:345,2;slc:346,2` is the chunk location in the
candidate file, and `7.225489` is the similarity score.

You can find the *candidate* file that we copied from our code database in the same
directory named *&ast;bug-id&ast;.java*.

The chunk location string (`slc:xxx;slc:xxx`) consists of a sequence of statement
location strings connected by semi-colons. A statement location string starts with
`slc:` followed by two numbers connected by a comma. The first number is the starting
*line* number of the statement, and the second number is the starting *column* number
of the statement.

For the `Lang_21` example, you can find the three chunk statements from `./plausible/Lang_21.java` at lines `344`, `345`, and `346` respectively.