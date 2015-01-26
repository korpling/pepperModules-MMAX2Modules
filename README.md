![SaltNPepper project](./gh-site/img/SaltNPepper_logo2010.png)
# MMAX2Modules
This project provides an importer and an exporter to support the [MMAX2](http://mmax2.sourceforge.net/) format in the linguistic converter framework Pepper (see https://u.hu-berlin.de/saltnpepper). A description of the exporter can be found in section [MMAX2Importer](#details).

Pepper is a pluggable framework to convert a variety of linguistic formats (like [TigerXML](http://www.ims.uni-stuttgart.de/forschung/ressourcen/werkzeuge/TIGERSearch/doc/html/TigerXML.html), the [EXMARaLDA format](http://www.exmaralda.org/), [PAULA](http://www.sfb632.uni-potsdam.de/paula.html) etc.) into each other. Furthermore Pepper uses Salt (see https://github.com/korpling/salt), the graph-based meta model for linguistic data, which acts as an intermediate model to reduce the number of mappings to be implemented. That means converting data from a format _A_ to format _B_ consists of two steps. First the data is mapped from format _A_ to Salt and second from Salt to format _B_. This detour reduces the number of Pepper modules from _n<sup>2</sup>-n_ (in the case of a direct mapping) to _2n_ to handle a number of n formats.

![n:n mappings via SaltNPepper](./gh-site/img/puzzle.png)

In Pepper there are three different types of modules:
* importers (to map a format _A_ to a Salt model)
* manipulators (to map a Salt model to a Salt model, e.g. to add additional annotations, to rename things to merge data etc.)
* exporters (to map a Salt model to a format _B_).

For a simple Pepper workflow you need at least one importer and one exporter.

## Requirements
Since the here provided module is a plugin for Pepper, you need an instance of the Pepper framework. If you do not already have a running Pepper instance, click on the link below and download the latest stable version (not a SNAPSHOT):

> Note:
> Pepper is a Java based program, therefore you need to have at least Java 7 (JRE or JDK) on your system. You can download Java from https://www.oracle.com/java/index.html or http://openjdk.java.net/ .


## Install module
If this Pepper module is not yet contained in your Pepper distribution, you can easily install it. Just open a command line and enter one of the following program calls:

**Windows**
```
pepperStart.bat 
```

**Linux/Unix**
```
bash pepperStart.sh 
```

Then type in command *is* and the path from where to install the module:
```
pepper> update de.hu_berlin.german.korpling.saltnpepper::pepperModules-MMAX2Modules::https://korpling.german.hu-berlin.de/maven2/
```

## Usage
To use this module in your Pepper workflow, put the following lines into the workflow description file. Note the fixed order of xml elements in the workflow description file: &lt;importer/>, &lt;manipulator/>, &lt;exporter/>. The MMAX2Importer is an importer module, which can be addressed by one of the following alternatives.
A detailed description of the Pepper workflow can be found on the [Pepper project site](https://u.hu-berlin.de/saltnpepper). 

### a) Identify the module by name

```xml
<importer name="MMAX2Importer" path="PATH_TO_CORPUS"/>
```

or

```xml
<importer name="MMAX2Exporter" path="PATH_TO_CORPUS"/>
```

### b) Identify the module by formats
```xml
<importer formatName="mmax2" formatVersion="1.0" path="PATH_TO_CORPUS"/>
```

or

```xml
<exporter formatName="mmax2" formatVersion="1.0" path="PATH_TO_CORPUS"/>
```

### c) Use properties

```xml
<exporter name="MMAX2Exporter" path="PATH_TO_CORPUS">
  <property key="PROPERTY_NAME">PROPERTY_VALUE</key>
</exporter>
```

## Contribute
Since this Pepper module is under a free license, please feel free to fork it from github and improve the module. If you even think that others can benefit from your improvements, don't hesitate to make a pull request, so that your changes can be merged.
If you have found any bugs, or have some feature request, please open an issue on github. If you need any help, please write an e-mail to saltnpepper@lists.hu-berlin.de .

## Funders
This project has been funded by the [Accademia Europea Bolzano](http://www.eurac.edu/en/), the [department of corpus linguistics and morphology](https://www.linguistik.hu-berlin.de/institut/professuren/korpuslinguistik/) of the Humboldt-Universität zu Berlin, the Institut national de recherche en informatique et en automatique ([INRIA](www.inria.fr/en/)). 

## License
  Copyright 2012 Accademia Europea Bolzano, Humboldt-Universität zu Berlin and INRIA.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.


# <a name="details">MMAX2Exporter</a>

##Properties

### MMAX2Exporter.matchingConditionsFilePath
This property allows to customize the mapping of SAnnotation and SMetaAnnotation objects to MMAX2. This property must point to an xml File having the following structure: 

```xml
<conditions>
  <condition stype="XXX"  namespace_regexp="YYY"   
    name_regexp="ZZZ" value_regexp="AAAA" slayer_name_regexp="BBB"   
    dest_scheme="CCC" dest_attr="DDD"/>
  ...
</conditions>
```

the stype, dest_scheme and dest_attr are mandatory, where as the others are optional regexp expressions. They map the SAnnotation or SMetaAnnotations of something of type stype on the attribute dest_attr of a "container" markable belonging to the scheme dest_scheme. On the Re-import, all the SDocumentGraph is recreated and the value (changed or not) of the attribute of the "container" markable is copied as SAnnotation of the recreated SElement. 
```xml
<conditions>
  <condition salt_relation_type="XXX" stype_regexp="YYY"  
    slayer_name_regexp="DDD" source_scheme="ZZZ" target_scheme="AAAA"   
    source_attr="BBB" />
  ...
</conditions>
```
 A similar reasoning applies to the mapping of the SRelations, only the second and third arguments are optional. When matching a certain SRelation of type salt_relation_type, a "container" markable is created for each the source and the target SNodes. A pointer source_attr is then created on the source "container" markable pointing at the target "container" markable. When re-importing a SRelation of type "salt_relation_type" is created between the SNode of the source container markable and the SNode of the target container markable. 

### MMAX2Exporter.pointersMatchingConditionsFilePath
This property allows to customize the mapping of SAnnotation and SMetaAnnotation objects to MMAX2. This property must point to an xml File having the structure given in .
