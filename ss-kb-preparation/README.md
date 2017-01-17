##ss-kb-preparation

There are some files you'll need for running the tests that are not pushed to the repo (ignored by git).

In [src/main/resources](ss-kb-preparation/src/main/resources), create a file ```application.properties``` according to the provided [template](ss-kb-preparation/src/main/resources/application.properties.TEMPLATE). For example, the contents for storing the data in a MySQL database could look like this:

```
# the connection type
type.connection=db.sql

db.sql.type=mysql

db.sql.mysql.driver=com.mysql.jdbc.Driver
db.sql.mysql.url=jdbc:mysql://<URL_TO_YOUR_DB>
db.sql.mysql.username=<USER>
db.sql.mysql.password=<PASS>
db.sql.mysql.autoincrement=AUTO_INCREMENT
```

For working with a SQLite database, your settings should look similar to this:

```
# the connection type
type.connection=db.sql

db.sql.type=sqlite

db.sql.sqlite.driver=org.sqlite.JDBC
db.sql.sqlite.url=jdbc:sqlite:<PATH_TO_DB>
db.sql.sqlite.username=<USER>
db.sql.sqlite.password=<PASS>
db.sql.sqlite.autoincrement=AUTOINCREMENT
```

For running the tests, some test files are provided in [src/test/resources/pdf](ss-kb-preparation/src/test/resources/pdf).

The Excel files to read in the labeled data have to reside in [src/test/resources/xlsx](ss-kb-preparation/src/test/resources/xlsx). An example file is provided. They follow a specific format: There are 5 columns. The first row contains the column labels "Variable", "Label", "Question", "Reference" and "Paper". Each sheet contains labeled data for one dataset and the sheet name is equal to the external ID of that dataset, e.g. "ZA2150".

Example labeled data (xlsx files) can be obtained from: https://drive.google.com/open?id=0B9gxCbOD2BYRM0pNN3V0OWNJUnc
