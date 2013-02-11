# add your scheduling algorithms here!
SCHEDULING_ALGORITHMS =  RandomSchedulingAlgorithm.java RoundRobinSchedulingAlgorithm.java
############################################

#Build up SOURCES to include all java files in the package directory with full relative pathnames
PACKAGE_DIR = com/jimweller/cpuscheduler
RAW_SOURCES = ClockPanel.java BetterFileFilter.java CPUScheduler.java CPUSchedulerFrame.java CPUSchedulerFrameForApplet.java JunkApplet.java JunkGenerator.java MainApp.java Process.java ProcessPanel.java StatsPanel.java SchedulingAlgorithm.java
RAW_SOURCES += $(SCHEDULING_ALGORITHMS)
SOURCES = $(foreach s, $(RAW_SOURCES), $(PACKAGE_DIR)/$(s))
PICS_DIR = ~/repos/CPUSchedulingSimulator/pics/

all: classes javadocs tarfile jarfile

classes: $(SOURCES) Makefile
#We need to tell java compiler where to put the package hierarchy (current directory)
	javac -d . -cp $(PICS_DIR):.:$(PACKAGE_DIR) -sourcepath $(PACKAGE_DIR) $(SOURCES)
	@echo 'To run do "java com.jimweller.cpuscheduler.MainApp" or "make run"'

javadocs: classes
	javadoc -private -version -author -d ../javadocs/ *.java > ../javadocs/javadoc.log 2>&1

jarfile:
	mkdir -p $(PACKAGE_DIR)
	cp -a *.class pics $(PACKAGE_DIR)
	jar	cfm ../cpu.jar manifest.txt com 

tarfile:
	cd ..; tar -czf	../cpu.tar.gz *;mv ../cpu.tar.gz .

clean:
	mv ../javadocs/CVS ../javadocs/.CVS
	rm -rf *.class ../cpu.jar ../cpu.tar.gz ../javadocs/*
	mv ../javadocs/.CVS ../javadocs/CVS
	rm -rf *.class

run:
	java -cp $(PICS_DIR):. com.jimweller.cpuscheduler.MainApp
