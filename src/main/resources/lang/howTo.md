## Translations ##

If someone wants to contribute a new translation to Digital,
the problem is not only to create the new translation.
There must be a way of maintaining the translation as the
program evolves.

There are three situation which can occur if the program
evolves after the new translation is completed:

1. There are language keys which are no longer needed.
   This is the simplest situation: I can remove the keys
   from all language files by myself.
2. A new language key is added.
   In this case I can add the translation for English and
   German. In all other language files the keys are
   missing and are replaced by the English version at runtime.
   But it is easy to find such keys: Simply look for
   keys which are available is English and are missing
   in other language files.
3. The most complex case occurs when a key remains unchanged
   but the corresponding text changes. It is difficult to
   see which keys had to be revised in other languages.
   To keep track of such keys the reference file comes into
   play.

If a new translation is added not only the translated language file
is added (e.g. lang_pt.xml) but also a file (lang_pt_ref.xml).
The latter one is the reference file and is a copy of the original
English translation (lang_en.xml).
When it is necessary to change a language text, it is possible to
detect that afterwards: It is possible to compare the strings
in the english translation with the content of the reference file
which is also English.
If the strings are not identical, the translation needs to be revised.
To do so, one has to update the translation and the English text in
the reference file.

## How to add a new Language ##

Add the new language to the file "Digital/src/main/resources/lang/lang.xml"
Then start the language test by running 

```
mvn -Dtest=TestLang test
```

Now all necessary files are created. In the target folder you can 
find a diff file that contains all missing keys. Now you can start 
adding your translation. **Copy this file to another location! The file 
will be overwritten every time Digital is built!** 
Use the Language Importer
"Digital/src/test/java/de/neemann/digital/lang/LanguageUpdater.java" to 
import your work. It is possible to import only partially translated files. 
This helps you to test your translation step by step.   

## How to find keys that need to be revised later on ##

Digital contains some test cases that check the consistency of
the translations. The test class "TestLang" checks the consistency
of the language files.

The easiest way to run the test case is to install maven.
Then you can start the test case by typing:

```
mvn -Dtest=TestLang test
```

This gives you a diff file for each language. This file contains all 
keys that have been added or modified. You can now add or update the 
language fragments contained in this file. After that start the class
"Digital/src/test/java/de/neemann/digital/lang/LanguageUpdater.java".
This allows you to select the reworked diff file and import it.
