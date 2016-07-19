# uc-tdm-socialsciences

##eu.openminted.uc-tdm-socialsciences.tools-experiments
###GROBID
For running the GROBID example, you have to build GROBID first on your local machine (Linux or Mac OS only!). Follow the instructions here: http://grobid.readthedocs.io/en/latest/Install-Grobid/

After building, you may have to replace the grobid-core-0.4.1-SNAPSHOT.jar in the lib folder with the one in your own build. Then point the properties in the application.properties file to the base dir of your GROBID installation.

##eu.openminted.uc-tdm-socialsciences.kb-preparation

There are some files you'll need for running the tests that are not pushed to the repo (ignored by git).

In src/main/resources, create a file ```application.properties``` with the following content: 
```
# the connection type
type.connection=db.sql

#db.sql.type=sqlite
db.sql.type=mysql

db.sql.mysql.driver=com.mysql.jdbc.Driver
#example url: localhost/studydata
db.sql.mysql.url=jdbc:mysql://<URL_TO_YOUR_DB>
db.sql.mysql.username=<USER>
db.sql.mysql.password=<PASS>
db.sql.mysql.autoincrement=AUTO_INCREMENT

db.sql.sqlite.driver=org.sqlite.JDBC
#example: test.sqlite (will be created in project root)
db.sql.sqlite.url=jdbc:sqlite:<PATH_TO_DB>
db.sql.sqlite.username=
db.sql.sqlite.password=
db.sql.sqlite.autoincrement=AUTOINCREMENT
```

For running the tests, you can put some pdf files under src/test/resources/pdf. Their names are currently hard-coded in the DocReadingTest, so you also have to adjust this.

The Excel files to read in the labeled data have to reside in src/test/resources/xlsx. They follow a specific format: There are 5 columns. The first row contains the column labels "Variable", "Label", "Question", "Reference" and "Paper". Each sheet contains labeled data for one dataset and the sheet name is equal to the external ID of that dataset, e.g. "ZA2150".

Example labeled data (xlsx files) can be obtained from: https://drive.google.com/open?id=0B9gxCbOD2BYRM0pNN3V0OWNJUnc
