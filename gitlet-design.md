# Gitlet Design Document

**Name**:

## Classes and Data Structures

### Class 1

#### Fields

1. Field 1
2. Field 2


### Class 2

#### Fields

1. Field 1
2. Field 2


## Algorithms

## Persistence
The directory structure looks like this:
```
CWD                                                      <==== Whatever the current working directory is.
└── .gitlet                                              <==== All persistant data is stored within here.
    ├── HEAD                                             <==== The name of the current branch.
    ├── STAGE                                            <==== A hash pointer to the serialized staging area Tree.
    ├── objects                                          <==== The object database.
    │   ├── d991f6cad12cc1bfb64791e893fa01ac5bf8358e    <==== A saved HashObject.
    │   └── ...                                         
    └── branches                                         <==== Store all the branch references.
        ├── master                                       <==== The default branch. Contains a hash pointer to a commit.
        └── ...
```
