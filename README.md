<p align="center">
  <img width="60%" src="https://raw.githubusercontent.com/ExplorViz/Docs/master/images/explorviz-logo.png">
</p>

___

[![Build Status](https://travis-ci.org/ExplorViz/explorviz-discovery-agent.svg?branch=master)](https://travis-ci.org/ExplorViz/explorviz-discovery-agent)

## Project Description
The ExplorViz Discovery Agent facilitates the monitoring setup and monitoring configuration of applications for ExplorViz.
Therefore, users do not need to manually start applications with monitoring parameters or handle monitoring configuration 
files. This Agent also uses a rulebased engine to discovery processes.

## Requirements
- Linux- or Windows-based operating system
- Java 8
- Accessible (network) [ExplorViz Backend](https://github.com/ExplorViz/explorviz-backend)

## Setup
1. Modify `backendIP`, `updateIP` and`server.ip` in the contained `explorviz.properties` file.
2. Run the `thesis-data/Relṕlikation/Agenten/explorviz-discover-agent-RBE.jar` from [here](10.5281/zenodo.3460626).
## Usage
If agent and backend are connected, data will be continuously exchanged in the modifiable time interval of 30 seconds.
The visualized configuration dialogs of the overall discovery and monitoring management mechanic are 
accessible via the `Discovery` button in the [ExplorViz Frontend](https://github.com/ExplorViz/explorviz-frontend). 

## Attention
The [sampleApplication](https://github.com/ExplorViz/sampleApplication) will be used for the tests. Therefore its important, to check that this application does not run, when you start the tests. 