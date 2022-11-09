## A system for conducting sports competitions

### Usage
The program is an interface in English.

The *Teams* tab contains application protocols, the *Groups* tab contains starting protocols, the *Distances* tab
contains the names of checkpoints for each group, the *Marks on check points* tab contains marks on the passage
of checkpoints by participants, the *Results* tab contains results.

You can download information from csv files, you can drive
it in manually (and also edit it after downloading). The input fields for each tab are described in the *header* field on this tab.
The input file must either have the exact format (each line matches the *header* template), or
it can have ad lines (on the first line of the ad of any data group, all fields are empty except for the first one) and
information lines that do not have the first field from *header* (this format is not allowed use for *Distances*).

Example of the first format:
``text
Vyborg Secondary School No. 10,,,,
Ivanov,Ivan,2002,KMS,M21
Petrov,Peter,1978,I,M40
Pupkin, Vasily,2011,3rd,M10
DPSH No. 10,,,,
Meow,Miu,2010,MS,M10
Pyam,pym,1998,II,M40
Aoao,AO,2000,3rd,M10
```

Example of the second format:
``text
3.3km,12:10:00
3,finish,12:20:09
4,1.5km,12:00:58
4,2.5km,12:01:09
4,3.5km,12:02:51
4,finish,12:05:07
```

On the right there are buttons for working with data: sorting in ascending, descending order, filtering and clearing the applied effects.
The top button *Check&Generate* checks the data you entered for correctness and, if they are correct, remembers them, and
also generates tabs depending on the current one (if possible). The *Groups* tab is automatically generated via *Teams*,
as well as the *Results* tab via *Groups*, *Distances*, *Marks on check points*. **IMPORTANT: SORTING AND FILTERING WORK
ONLY WITH PROCESSED DATA (THOSE THAT WERE SAVED BY THE CHECK&GENERATE BUTTON). IF THE DATA IS GENERATED OR
DOWNLOADED FROM A FILE, THE DATA IS CHECKED AND WRITTEN TO MEMORY AUTOMATICALLY**

What is displayed on the screen is exported to a file. That is, if you apply a filter, the export will be based on its results.

The program checks the data for correctness, checks the data connection between tabs,
displays an error pop-up window if something is wrong, and also supports the warning text under the CSV field for manual input (mostly).

[Example of working in the program (video)](https://drive.google.com/file/d/1jCVFtmtxWrmMVkq4k_LVRoRtU5qqpLAv/view?usp=sharing)
