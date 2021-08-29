# Gitlet Design Document

**Name**: Irvin Tancioco

## Classes and Data Structures
___
### Class 1: Main
    DESCRIPTION: This is the entry point for executing gitlet commands

    COMMANDS: init, commit, checkout, log



### Class 2: Commit  
    ADT: Tree
    Description: Commit object tracks the path 
        from current commits to previous commits. Each instance
        contains message, a parent reference sha1 ID, timestamp, and
        list of files.

#### Instance Variables
| Variables                  | DESCRIPTION   |
|----------------------------|---------------------------------------------------------|
| private String message     | - contains the commit message                           |
| private Date timestamp     | - time of commit creation, assigned in the constructor  |
| private String parent[]    | - points to the parent commit of Commit object          |
| private String uid         | - unique ID for this commit object
| Map<String, String> blobs  | - contains all the filenames as keys and their current version



#### Methods
| Methods                    | DESCRIPTION                                             |
|----------------------------|---------------------------------------------------------|
| public Commit(String message) | - constructor intended for initial commit
| public Commit(String message, String parent) |
| public Commit(String message, String parent1, String parent2) | - a constructor that initializes the following: MESSAGE: "initial commit", TIMESTAMP
| public getMessage()        | - get this commit's MESSAGE                             |
| public getTimestamp()      | - get this commit's TIMESTAMP                           |
| public getFirstParent()    | - get this commit's first PARENT
| public getSecondParent()   | - get this commit's second PARENT
| public static Commit fromFile(String filename) | - reads in and deserializes a commit
| public void writeCommit()  | - writes the commit object to a file with sha1 as filename



___
### Class 3: Repository
    ADT: 
    Description: This class handles the COMMANDS functionaly

| Methods                    | DESCRIPTION                                             |
|----------------------------|---------------------------------------------------------|
| public static void init() | - initializes .gitlet directory, maps, and other files
| public static void add(String filename | - adds a copy of the file as it currently exists in the STAGING AREA
| public static void commit(String msg) | - saves a snapshot of certain files
| public static void checkout(String[] args | - 

___
### Class 4: Pointer
    ADT: Queue
    Description:



### Class 5: Validate
#### Methods
| Methods                    | DESCRIPTION                                             |
|----------------------------|---------------------------------------------------------|
| <div> public static void numArgs(String cmd, String[] args, int n)  | validate the number of arguments being passed in to our gitlet commands





## Algorithms

## Persistence

