Distributed Real Time Stream Monitoring System
=================

This project is an on going project. The goal of this project is to design and implement a tool to monitor the computers as well as the distributed systems in real time. This tool is designed to be distributed, so it is able to be scaled out to handle the monitoring of a large number of computers. To make the tool scalable, it is designed as the master/slave mode with two types of slaves: the monitor and the collector. 

The monitor is used to be deployed into the computers that need to be monitored. It would continuously collect a variety of utilization information such as the CPU utilization, memory utilization, disk I/O utilization, network I/O utilization and so on. At the same time, it would send the collected meta-data to the collector that is assigned. The monitor is a lightweight program. Its resource consumption is very little so it would not affect performance of the computer it monitors.

The collector is responsible for gathering the computer trace data from the monitors. It also exposes the interface to enable the external systems to pull the meta-data it collects.

The master is in charge of assigning the monitors to collectors. Once a monitor is assigned, it would directly communicate with the collector, and would not bother the manager until the collector cannot be connected. 

The wiki of this project is available [here](https://github.com/yxjiang/system-monitoring/wiki).
