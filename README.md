The Eclipse Macker Plugin displays warnings and errors about architectural violations discovered by Macker right in Eclipse.

To use the Macker Eclipse Plugin, you need Eclipse 3.4 or later, and JDK/JRE 1.5 or later.

What is Macker?
-

Macker is a Java developers' utility which checks classes against user-defined structural rules. It's meant to model the architectural ideals programmers always dream up for their projects, and then break -- it helps keep code clean and consistent. You can use it to check general "good practice" rules, and you can also tailor a rules file to suit a specific project's structure.

The Macker Eclipse Plugin allows Macker to be used within the Eclipse IDE.

Read more about what it does and what it's for here: http://innig.net/macker/faq.html .

How to Install
-

* In Eclipse got to Help -> Install new Software...
* Add a new installation site and provide "[http://eclipse-macker.sourceforge.net/update](http://eclipse-macker.sourceforge.net/update)" as location URL.
* Mark "Macker Plugin" then press "Next >".
* Review and confirm the Licenses to install.
* Press "Finish".
* Restart Eclipse.

Getting Started
-

To get started, use the "Project->Clean.." command. The Plugin will run, and problem markers will point to locations in your code which have been identified as architectural violations.

You may customize how the plugin runs by opening the Properties dialog for a Java project

* Select the "Macker Property Page" section within the properties dialog
* Options you may choose include:
* Enable or disable the "Run on Incremental Build" checkbox. When enabled the Plugin will run every time you modify a Java class within the project.
* "Show Macker Events As": This option will choose how warnings are shown.
* Enable or disable the "Check Content" checkbox. When enabled, the whole Class-content is analyzed and fitted with markers.
* "Macker Rules Directory": A relative path to the Java project which includes the Macker rules.
* "Source/Classpath Filter": the Project Paths which are defined there, will be explored by the Plugin.
