File structure
=================
The file structure for the repos are here:

### Repo create

  Looks like this:
  ```
  REPO ROOT
  |--> db <== Stands for database
    |--> current <== The current revision number
    |--> UUID <== The identification number of this repo
    |--> version <== The current version of the
  |--> master <== `master` folder, where the code resides
    |--> leaf <== The place where the latest revision resides
      |--> current.zip <== The zip archive of the current revision
      |--> diff <== The folder for the diff
        |--> 0 <== Diff for revision 0
        |--> ... (etc, etc...)
      |--> logs <== The revision logs.
    |--> working <== The working branch
        |--> current <== Folder where all the code exists. Use this for comparing
          |-->xxx.txt <== The files in the repo
        |--> diff <== The total diff from the latest revision
        |--> pushes <== The diff for each individual push. Scrapped after each revision
          |--> 0 <== Diff for push 0
          |--> ... (etc, etc...)
```

### Checked out Repo
```
REPO ROOT
|--> .scs
  |--> HEAD <== The file that stores the repo path
  |--> UUID <== The UUID of the repo
  |--> commit <== The commit of the repo
```
