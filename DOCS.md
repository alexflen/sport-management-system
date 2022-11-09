# A system for conducting sports competitions

## Description of the subject area

Suppose you need to implement an organization system for sports competitions
in one of the cyclic sports: running, cross-country skiing, swimming, cycling, orienteering, etc.

In the simplest case, competitions have some name, date of holding, and imply the passage
of each athlete of some one distance.

All athletes perform in different groups depending on gender and age. The list of groups is determined
by the competition rules and published in advance.

Each group has its own distance, while some groups may have the same distances.

Athletes compete for different teams, each team submits application lists in which
it indicates for athletes the surname, first name, year of birth, sports category, desired group.
Also, the application lists indicate data on medical examination and accident insurance for each athlete.

Based on all the application lists, a start protocol is formed for each group.
The protocol is formed as a result of the draw. Each athlete receives an individual chest number and start time.
The start can be shared (at one time) or separate. In the simplest case, the draw places all the athletes in the group in a random order.
However, there may be more complex types of draw, for example, when it is necessary to take into account the rank, take into account the race within the group,
take into account the simultaneous start of different groups.

After passing the competition, a protocol of results is formed, as well as a protocol with intermediate results of passing the distance.
The distance may consist of several checkpoints, at each of which the time of passage is recorded.
The result is recorded either manually or using one or more electronic marking systems.
Accordingly, the result is transmitted to the system either by manual input or by receiving data from electronic marking systems.
Usually it is either a list of the type <number> - <time> for a given checkpoint, or
a list of the type <checkpoint> - <time> for a given number (athlete).

In the protocols of the start, finish, etc. for each participant, it is necessary to specify the number, first name, last name, year of birth, sports category, team.
The start time is additionally indicated in the start protocol.
The results report indicates the final place, the result (time spent on completing the distance), Lagging behind the leader and (optionally) the sports category performed.
The completed sports category is calculated according to some formula, which depends on the type of sport, group and regulations of specific competitions.
In addition to the results protocol, a results protocol for teams is formed for each of the groups.
At the same time, according to a certain formula, depending on the regulations of specific competitions, the result of each athlete in his group
gives a certain number of points, which in total give the result of the team.

Example of the application list (CSV):

```csv
Vyborg Secondary School No. 10,,,,,,,
Ivanov,Ivan,2002,KMS,M21,,,
Petrov,Peter,1978,I,M40,,,
Pupkin, Vasily,2011,3rd,M10,,
``

Example of a start protocol for a group (CSV):

```csv
M10,,,,,,
241,Pupkin,Vasily,2011,3rd,12:01:00,
242,Pirogov,Grigory,2011,3rd,12:02:00
243,Smirnov,Sergey,2012,,12:03:00
```

Example of a participant's distance protocol (CSV):

```csv
243,,
1km,12:06:15
2km,12:10:36
Finish,12:14:51
```

Example of a checkpoint protocol (CSV):

```csv
1km,,
241,12:04:17
242,12:05:11
243,12:06:15
```

Example of the results protocol (CSV):

```csv
M10,,,,,,
1,242,Pirogov,Grigory,2011,3rd,00:12:51,
2,243,Smirnov,Sergey,2012,,00:12:57,
3,241,Pupkin,Vasily,2011,3rd,00:13:15
```

## Task

The program should:

1. To form the starting protocols according to the application lists. Use a simple draw with an interval of 1 minute and the start at 12:00:00.
2. According to the starting protocols and protocols of passing checkpoints, form protocols of results.
3. According to the protocols of results, form a protocol of results for teams. The points are calculated using the formula max(0, 100 * (2 - <result>/<winner's result>)).
4. Check the correctness of the application lists.
5. Check the correctness of passing checkpoints by each of the participants.
6. Write a log

In addition, you need:
1. Create a file DOCS.md with instructions for using the program
2. Write tests (you will need generators of applications, results, etc.)

## General remarks

1. Libraries are convenient to search on https://kotlin.link . You may need libraries for logging, parsing command-line arguments,
working with configuration files (for example, https://github.com/sksamuel/hoplite ), reading/writing csv (for example, https://github.com/doyaaaaaken/kotlin-csv
2. It is assumed that the description of the competition and the distances are defined in advance in some configuration file.
3. The system works with files, think about how to structure their location on disk - it's not worth storing everything in one random folder.
4. The system will evolve and change. Try to build an object model that will be convenient to expand. At the same time, do not get too carried away,
because you do not yet know where the development will go. Search for balance ⚖ !
