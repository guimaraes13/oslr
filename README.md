#Online Structure Learner by Revision (OSLR)

OSLR is an online relational learning algorithm that can handle continuous, open-ended streams of relational examples as
they arrive. We employ techniques from theory revision to take advantage of the already acquired knowledge as a starting
point, find where it should be modified to cope with the new examples, and automatically update it. We rely on the
Hoeffding's bound statistical theory to decide if the model must in fact be updated according to the new examples.
The system is built upon ProPPR statistical relational language to describe the induced models, aiming at contemplating
the uncertainty inherent to real data.

This system was developed as a product of a master theses at the Program of Systems Engendering and Computer Science,
COPPE, Federal University of Rio de Janeiro (UFRJ). The research was funded by Conselho Nacional de Desenvolvimento
Científico e Tecnológico (CNPq). The work was supervised by Gerson Zaverucha (UFRJ) and Aline Paes (UFF).

This project is under the GPL version 3.0, see the [file](LICENSE.md) for more details.

For further details, feel free to contact me at \<my first name\> \<my github id\> at gmail dot com.

##ProPPR
ProPPR is a system for probabilistic logic inference first presented at

> Wang, W. Y., Mazaitis, K., Lao, N., & Cohen, W. W. (2015). Efficient inference and learning in a large knowledge base.
<br> Machine Learning, 100(1), 101-126.

This system is under the Apache License version 2.0 and is publicly available at
[ProPPR](https://github.com/TeamCohen/ProPPR).

OSLR uses a slightly modified version of ProPPR, as such, its source has been included in this repository.

##Dependencies
- Java 8;
- Maven;

##Compile

To compile the project, follow the steps below: <br>
- Download or clone the repository;
- Enter the downloaded or cloned folder;
- Run `mvn clean compile assembly:single`

This command will create a jar file `oslr-1.0-SNAPSHOT-jar-with-dependencies.jar`,
with the correspondent dependencies, in the `target` directory.

##Run

There are two ways of running the system: the iteration mode (default), which learns by a set of iterations; and the
batch mode, which learns from a set of files. Next we will describe the files syntax, the examples bases and show how
to learn in **iteration** and **batch** mode.

###File Syntax

There are two main syntax for the input files of the system. A logic syntax for the background knowledge and
theories, both initial theories and output theories; and an example syntax for the examples. We will describe both of
them below.

####Logic Syntax

The logic syntax is simple, it is based on a function-free logic similar to Prolog. The elements are defined as follows:
- A **variable** is represented by a string of letters, digits or underscores, starting with an upper case letter;
- A **constant** is represented like a variable, but starting with a lower case letter;
- A **term** is either a constant or a variable;
- An **atom** is represented by a string of letters, digits or underscores, starting with a lower case letter followed by
a n-tuple of terms between brackets; thus, we say the predicate has arity n.

With these elements we can have facts and rules. A fact is represented by an **atom** ending with period. For instance:

`isMarried(john, mary).`

And a rule is represented by an **atom** followed by an implication symbol (:-) and a set of atoms separated by commas,
forming its body, ending with a period, for instance:

`isParent(A, B) :- isMarried(A, C), isParent(C, B).`

Additionally, a rule may have ProPPR's features at its end (before the period), represented by a list of **atoms**
separated by commas between curly braces, for instance:

`isParent(A, B) :- isMarried(A, C), isParent(C, B) {weight(A)}.`

See the [ProPPR system](https://github.com/TeamCohen/ProPPR) for more details about the language.

####Example Syntax (ProPPR)

ProPPR defines an example as composed of a query of the type *p(X1,...,Xn)*, where *p* is a predicate, *n >= 1*, and
*Xi in X1, ... ,Xn* is either a constant or a variable, and at least one term *Xi in X1, ... , Xn* must be a variable.

The query is followed by a set of its possible instantiations (where all the variables are replaced by constants)
separated by a tabulation. Each possible instantiation is preceded by a *+* or a *−* sign, indicating whether it is a
positive or a negative answer, respectively. An example is illustrated below:

`about(a,X) +about(a,sport) +about(a,politics) −about(a,food)`

The first atom is the query and the others represent possible answers by grounding *X*.

See the [ProPPR system](https://github.com/TeamCohen/ProPPR) for more details about the language.

####Other Syntax

Other syntax that might be useful for advanced configuration of the system are the yaml syntax and the Log4j 2 xml
configuration syntax.

#####Yaml

For the yaml, we use the [YamlBeans](https://github.com/EsotericSoftware/yamlbeans) library, and their syntax can be
found at their repository page.

#####Log4j 2

For the [Log4j 2](https://logging.apache.org/log4j/2.x/) from Apache, the configuration file is the xml file called
[log4j2.xml](src/main/resources/log4j2.xml)
placed at `src/main/resources`. The configuration syntax can be found at their page.

###Example Base

In the [example folder](src/main/resources/examples) there are some examples based on the UWCSE dataset.

> Richardson, M., & Domingos, P. (2006). Markov logic networks. Machine learning, 62(1-2), 107-136.

###Iteration Mode

The iteration mode is the default learning mode of the system. This mode simulate the online environment by passing
the examples splitted by iterations.

The iteration mode is called through the class
[LearningFromIterationsCLI](src/main/java/br/ufrj/cos/cli/LearningFromIterationsCLI.java).
Alternatively, since it is the default mode, it can be called by simply running the jar file.

####Input Files

To run the iteration learning mode, we need to create a folder with the following structure in it:
- A set of folders whose name starts with a **prefix** followed by a positive int number, the *iteration folders*;
- For each *iteration folders*:
    - A set of logic files ending with a **positive extension**, as the background knowledge;
    - A file containing the examples in ProPPR's format
    (see [ProPPR](https://github.com/TeamCohen/ProPPR) or [example](src/main/resources/examples/uwcse/batch/train.data),
    for more details) with a fixed **name** and the **example extension**;

The iteration folders will be sorted by the number after the prefix, and each iteration will be passed at a time for
training. After training on iteration *i*, the iteration *i+1* is used to test the current model, before training on
iteration *i+1*, at the end, the test results for each iteration are logged.

####Output Files

The output of the iteration mode is saved into a folder specified by the user. In this folder are saved the following
files:
- Running details:
    - **configuration_LFI.yaml**, it contains all the configuration of the run;
    - **arguments_LFI.txt**, it starts with the name of the class, followed by the arguments to repeat the run;
    - **output.txt**, it is the log file of the run, containing the log that was printed on the screen;
- Saved model:
    - **theory.pl**, it contains the logic part of the final ProPPR theory;
    - **savedFeatureTheory.pl**, it contains the features part of the final ProPPR theory;
    - **savedParameters.wts**, it contains the final weights learned by ProPPR;
- Others:
    - **statistics.yaml**, it is a machine-friendly file containing statistics about the runtime and evaluation measures;

In addition to the general files of the run, it generates a folder for each iteration (with the same name) and in
each folder it saves the following files:
- **inference.train.tsv**, a tab-separated file contains the real value and the prediction of each example in the
training set of the iteration;
- **inference.test.tsv**, analogous to the **inference.train.tsv** but for the test examples;
- **theory.pl**, the logic part of the learned ProPPR theory at the end of the current iteration;
- **savedFeatureTheory.pl**, the features part of the learned ProPPR theory until at the end of the current iteration;
- **savedParameters.wts**, the weights learned by ProPPR at the end of the current iteration;

####Running

The simplest way to call the learning from iterations mode is to run the command bellow, assuming the current
directory is the base folder:

```
java -jar target/oslr-1.0-SNAPSHOT-jar-with-dependencies.jar -d src/main/resources/examples/uwcse/iterations
```

This will call the `LearningFromIterationsCLI` class passing the `src/main/resources/examples/uwcse/iterations` as
input folder.

This command is the short version for:

```
java -cp target/oslr-1.0-SNAPSHOT-jar-with-dependencies.jar br.ufrj.cos.cli.LearningFromIterationsCLI \
--dataDirectory src/main/resources/examples/uwcse/iterations \
--iterationPrefix ITERATION_ \
--positiveExtension .f \
--targetRelation examples \
--examplesExtension .data \
--outputDirectory . \
--yaml src/main/resources/default_it.yml
```

Each one of the command line options is explained as follows:
- --dataDirectory (or -d), is the input folder structured as explained in the input files;
- --iterationPrefix (or -p), is the **prefix** of the name of the iteration folders, the default value is *ITERATION_*;
- --positiveExtension (or -pos), is the **positive extension** of the background files, the default value is *.f*;
- --targetRelation (or -tr), is the **name** of the example file, inside the iteration folder, the default value is
*examples*;
- --examplesExtension (or -ext), is the **example extension**, inside the iteration folder, the default value is *.data*;
- --outputDirectory (or -o), is the directory to save the output files, the default value is current directory;
- --yaml (or -y), is the yaml configuration file, the default is *src/main/resources/default_it.yml*;

Since the structure of the input files, from the example we use, follows all the default names, we do not need to
pass them as arguments, we only need to pass the path for the input directory.

The output files will be saved in a directory named *\<targetRelation\>\_RUN\_\<date and time\>* inside the directory
specified by the *outputDirectory* option. To place the output files strictly in the *outputDirectory*, use the flag
*--strictOutput* in the command line arguments.

###Batch Mode

Another way of running the system is the batch mode. This mode assumes a fixed training set and optionally, a test set.
It passes *n* examples (the default *n* is *10*) at a time to train the model, until no more examples are available in
the training set, finally, it tests the learned model (from the whole training set) on the test set.

The batch mode is called through the class
[LearningFromBatchCLI](src/main/java/br/ufrj/cos/cli/LearningFromBatchCLI.java).

####Input Files

To run the batch learning mode, we need to provide the following:
- A set of positive facts, background knowledge;
- A train set of examples;
- A test set of examples (optional);

####Output Files

The output of the iteration mode is saved into a folder specified by the user. In this folder are saved the following
files:
- Running details:
    - **configuration_LFB.yaml**, it contains all the configuration of the run;
    - **arguments_LFB.txt**, it starts with the name of the class, followed by the arguments to repeat the run;
    - **STD_OUT**, a directory to place the output log from the run:
        - **LFB_output_\<date and time\>.txt**, it is the log file of the run, containing the log that was printed
        on the screen;
- Saved model:
    - **theory.pl**, it contains the logic part of the final ProPPR theory;
    - **savedFeatureTheory.pl**, it contains the features part of the final ProPPR theory;
    - **savedParameters.wts**, it contains the final weights learned by ProPPR;
- Results:
    - **inference.train.tsv**, a tab-separated file contains the real value and the prediction of each example in the
training set;
    - **inference.test.tsv**, analogous to the **inference.train.tsv** but for the test set;
- Others:
    - **statistics.yaml**, it is a machine-friendly file containing statistics about the runtime and evaluation measures;

####Running

The simplest way to call the learning from batch mode is to run the command bellow, assuming the current
directory is the base folder:

```
java -cp target/oslr-1.0-SNAPSHOT-jar-with-dependencies.jar br.ufrj.cos.cli.LearningFromBatchCLI \
-k src/main/resources/examples/uwcse/batch/facts.f \
-e src/main/resources/examples/uwcse/batch/train.data
```

Optionally, a test set can be passed, by using the following command:

```
java -cp target/oslr-1.0-SNAPSHOT-jar-with-dependencies.jar br.ufrj.cos.cli.LearningFromBatchCLI \
-k src/main/resources/examples/uwcse/batch/facts.f \
-e src/main/resources/examples/uwcse/batch/train.data \
-test src/main/resources/examples/uwcse/batch/test.data
```

The long form of the command is:

```
java -cp target/oslr-1.0-SNAPSHOT-jar-with-dependencies.jar br.ufrj.cos.cli.LearningFromBatchCLI \
--knowledgeBase src/main/resources/examples/uwcse/batch/facts.f \
--example src/main/resources/examples/uwcse/batch/train.data \
--test src/main/resources/examples/uwcse/batch/test.data \
--yaml src/main/resources/default.yml
```

Each one of the command line options is explained as follows:
- --knowledgeBase (or -k), is for the input logic file(s) for the background knowledge;
- --example (or -e), is for the input example file(s) to train;
- --test (or -test), is for the input example file(s) to test;
- --yaml (or -y), is the yaml configuration file, the default is *src/main/resources/default.yml*;

Optionally, the *--outputDirectory (or -o)* can be specified for the directory to save the output files in.

The output files will be saved inside the directory specified by the *outputDirectory* option. If this options is
omitted, the files will be saved inside a directory called *LFBCLI\_\<hash of the configuration\>*, in the current
directory.

###Advanced Options

There are two ways of configuring the learning system:
- By command line arguments, this is an easier but limited way.
- By yaml configuration file, a more complex but expressive way.

The yaml allows the user to configure all parts of the learning system, including which implementation will be used
for each part of the learning method. It is a very complex configuration but also very powerful. The default
configuration files provided for each mode (**iteration** and **batch**) will provide a good implementation of the
methods and parameters for the learning system.

All the options provided by the command line interface are also presented in the yaml file. However, if an option is
specified in both ways, the command line arguments will overwrite the yaml file configuration.

We suggest the user to place the configurations of the learning system in the yaml file and use the command line
arguments to provide the input and output files/directories.

Despite the default yaml file provide a reasonable configuration, the user might want to change these parameters. The
yaml file is used, in the low-level, to specify the values of the properties of the main class and their values,
recursively, in the Java language. By opening the default yaml file, it is easy to identify the properties of the
java objects and their values, the names are, usually, self explainable and should be easy to change the desired
values. On the other hand, to change the structure of the system and the classes that are being instantiated, a
careful look at the source code might be required.

It is important to notice that the real default yaml file is inside the generated jar, in order to make the changes into
the yaml file to be considered, it has to be explicitly passed by the *--yaml (or -y)* command line option, or the jar
must be remade. If the user want to change the default yaml, we suggest to copy the default file to another location,
make the desired modifications and pass the new file in the command line option.

The command line option allows other configurations beside the ones present in this examples. For instance,
the *--theory (or -t)* options allows one to pass an initial theory to the learning system.

Furthermore, use the *--help (or -h)* flag to see the configurations of each class. Since the
LearningFromIterationsCLI is a subclass of the LearningFromBatchCLI it has inherited some options from its parent,
that might be ignored by it.

###Log
This system uses the Apache Log4j 2 framework. The log configuration file is [log4j2.xml](src/main/resources/log4j2.xml)
and is already a very verbose log.

By default, the log is printed on the screen and saved into a file, the precise location of the log file depends on the
learning mode (**iteration** or **batch**).

For a less verbose log, set the root level log to INFO, by change the line:
<br>
`<Root level="DEBUG">` to `<Root level="INFO">`

For a more verbose log, set the root level log to INFO, by change the line:
<br>
`<Root level="DEBUG">` to `<Root level="ALL">`

This log mechanism is very powerful and allows a very refined log settings. For more information about it, please see
its page [Log4j 2](https://logging.apache.org/log4j/2.x/).
