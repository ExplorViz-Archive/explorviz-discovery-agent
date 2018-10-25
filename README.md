# ExplorViz Discovery Agent

The ExploViz Discovery Agent facilitates the monitoring setup and monitoring configuration of applications for ExplorViz.
Therefore, users do not need to manually start applications with monitoring parameters or handle monitoring configuration 
files.

## Requirements
- Linux-based operating system
- Java 8
- Accessible (network) [ExplorViz Backend](https://github.com/ExplorViz/explorviz-backend)

## Setup
1. Download or clone repository
2. Modify `src/main/webapp/WEB-INF/classes/explorviz.properties` file
3. Build war file with `gradlew build` and deploy in e.g. [Apache Tomcat](http://tomcat.apache.org/) **OR** 
use the embedded [Gretty web server](http://akhikhl.github.io/gretty-doc/Feature-overview.html) with 
`gradlew appStart` / `gradlew appStop`

## Usage
If agent and backend are connected, data will be continuously exchanged in the modifiable time interval of 30 seconds.
The visualized configuration dialogs of the overall discovery and monitoring management mechanic are 
accessible via the `Discovery` button in the [ExplorViz Frontend](https://github.com/ExplorViz/explorviz-frontend). 
