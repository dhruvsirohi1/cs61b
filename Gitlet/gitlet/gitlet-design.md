# Gitlet Design Document

**Name**: Dhruv Sirohi

##Overview
Gitlet is out very own version control system, which will be modeled after (and scaled down from) the real git.
All the basic implementation will be fulfilled.

## Top-level Layout
Once intialized (see init command), the directory that contains the gitlet class should contain a hidden directory with 
the name ".gitlet"
This folder will further contain the storage for commits, status log, staging area and versions of the files that are being saved.
There needs to be appropriate mapping from the commits to the respective file(s) version. These commits will be ordered 
primarily by the date and time of the "commit" command.
Handling of multiple branches (2 in this case) is enabled. What this means is that 2 separate version "histories" of a
 project
can be maintained. This is useful in events of multiple programmers working on the same project, or a divergence from 
the 
main project goal/ development of a side-project at some point during it's completion.

##Commands
###init
* Description: Initialize a new Gitlet, which should contain 1 empty commit with a time stamp "00:00:00 UTC, Thursday,
1 January 1970"
* Failure cases: Throw error and abort if a Gitlet version control system exists in the current directory. 
###add [file name]
* Description: Add the file to the staging area, replacing any existing versions of said file in the staging area.
* Failure cases: Throw error and exit if file does not exist.
###commit
* Description: Save a snapshot of the staged files, clearing the staging area thereafter. Update the head to this commit,
and create a new commit, which by default will always point to the files' version mapped by it's parent commit. 
* Failure Cases: Abort if no files have been staged (using add). If no commit msg, throw error asking for the same.
## Classes and Data Structures

### Main: 
The engine of gitlet. Used of intializing and processing every command argument, utilizing additional classes
as per command requirement.

### DumpObj:
Much like the dump command from LOA, this class is used to deserialize -> display useful contents of an object.

###GitletException:
Class used for throwing errors and displaying useful messages.

### Filing: 
A class that verifies the existence of an added file, and takes care of "committing" said file.

### Commits: 
A class that implements a tree (probably a BBST) for storing commits.

### Log: 
A log class/file that will store the textual representation of each commit id, needs to be an independent file/
class to allow for checking out commits.

### Status: 
A text file which will store the current state of the staging area. 

### Other data structures used:
* HashMap: Various hashmaps may be implemented, including <String, String> which will map a commit id to the file name
 that was saved with that commit. 
 * Trees: Each file name will represent a tree of the versions of that file.

## Serialization
Serialization will be implemented while saving file versions.
## Algorithms
### init
1. If .gitlet directory is already present in the folder, throw error and abort.
2. If not, initialize a new .gitlet directory.
3. Create a single branch: master. This will be the current branch.
4. Add an empty commit, with the message "initial commit" with timestamp 00:00:00 UTC, Thursday, 1 January 1970.
Master points to this commit.
5. All git repositories will have this same initial commit, with the same UID.

### add [file name]
1. Verify the the file exists in the directory .gitlet is in.
2. If an already commited version of this file exists, compare the state of the file to the latest version commited.
If the state is identical:
    * remove this file from the staging area (if present) and exit.
else: 
    * continue to step 3.
3. If this file is already in the staging area, overwrite it with it's current state. Else,
add this file to the staging area, in "as in" state. 
4. Update git status to "tracking" this file. 

### commit
1. Check if there are files in the staging area, if not abort and throw error message. Else if: 
2. If there is no commit message, throw an error asking for one. Else:
3. Create an SHA-1 hash value for the file(s) to be committed, and add this to the mapping structure, so the particular
file versions can be retrieved using this SHA-1 value.
4. Serialize the file(s) and add the file(s), i.e. their current versions, in the appropriate place.
5. Update the master pointer.
6. Clear the staging area.
7. Create a new commit, and add this to the commit tree. This is the current commit.

### rm [file name]
1. If the file is not staged, print "No reason to remove file" and exit.
2. Unstage the file if it is currently staged for addition.
3. Stage it for removal, check if the user has already deleted it, if not remove it from the directory.

###log
1. Display the commit tree chronologically. 
2. This data should be either:
    * persistently maintained, or
    * Each commit hashcode should map to a set of strings containing the respective data
    in the appropriate format.
   
### find [commit message]
1. Compare the commit message string to all commit messages.
2. Display the equivalent ones chronologically.
3. If no commit with the message exists, print appropriate error.

###status
1. Display the status file.

###checkout 
1. Obtain the number of command arguments, n.
2.  * if n == 1 :
        * Assert that the command argument is a branch name.
        * Put all the files in the commit at the head of this branch in the working directory.
        * Overwrite all these file with the commit version, restoring any deleted files, or deleting 
        any newly created files.
        * This branch now becomes the current branch (HEAD).
        * If this new branch is different from the previous current branch, clear the staging area.
    * else if n == 2 :
        * Assert that 1st argument (0th) is "--" and 2nd argument(1st) is a correct file name.
        * Obtain the version of this file that is in the head commit, and put it n the working directory.
        * Overwrite previous version, or restore if deleted.
    * else if n == 3 :
        * Assert that args[0] is a valid commit id, args[1] == "--", args[2] is a valid file name.
        * Put the version of the file from the commit with this commit id, in the working directory.
        * Restore this version of the file
    *else :
        * throw error.

###branch [branch name]
1. Create a new branch with branch name, and have it point to the current head (master).
## Persistence
* Every time an add command is run on a file, a clone file will be persistently created.
* If add is called again on the file before committing it, the previous persistent file will be replaced with
a new persistently created file.
* When the commit command is executed (and the staging area is not empty) the persistently created file will be inserted 
the commit tree mapping.

### Other files/folders that "persist"
* Commit log

## Current hierarchy of directories:

* .gitlet 
     * head
     * log
     * status
     * files
        * individual file trees
     

  

