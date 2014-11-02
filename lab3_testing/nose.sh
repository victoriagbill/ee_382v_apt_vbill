nosetests -v --with-coverage ExperimentTest.py &> testFile.txt 
pylint -r n Experiment.py >> testFile.txt
