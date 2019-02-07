<p align="center">
  <img width="60%" src="https://raw.githubusercontent.com/ExplorViz/Docs/master/images/explorviz-logo.png">
</p>

___

[![Build Status](https://travis-ci.org/ExplorViz/explorviz-discovery-agent.svg?branch=master)](https://travis-ci.org/ExplorViz/explorviz-discovery-agent)

## Project Description
The ExplorViz Discovery Agent facilitates the monitoring setup and monitoring configuration of applications for ExplorViz.
Therefore, users do not need to manually start applications with monitoring parameters or handle monitoring configuration 
files.

## Requirements
- Linux-based operating system
- Java 8
- Accessible (network) [ExplorViz Backend](https://github.com/ExplorViz/explorviz-backend)

## Setup
1. Download the `explorviz-discovery-agent.jar` from the [release page](https://github.com/ExplorViz/explorviz-discovery-agent/releases) 
2. Modify `backendIP` and `server.ip` in the contained `explorviz.properties` file

## Usage
If agent and backend are connected, data will be continuously exchanged in the modifiable time interval of 30 seconds.
The visualized configuration dialogs of the overall discovery and monitoring management mechanic are 
accessible via the `Discovery` button in the [ExplorViz Frontend](https://github.com/ExplorViz/explorviz-frontend). 
