#!/bin/bash
make classes
make jarfile
scp CPUSchedulingSimulator.jar coffeebot:/mnt/ics143/www/project
cd ..
zip -r CPUSchedulingSimulator.zip CPUSchedulingSimulator/* -x CPUSchedulingSimulator/.git/* CPUSchedulingSimulator/.gitignore/*
scp CPUSchedulingSimulator.zip coffeebot:/mnt/ics143/www/project

