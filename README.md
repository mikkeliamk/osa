OSA - Open Source Archive
===
OSA is an open source platform for digital archives and repositories.
  
Features
---
* Content agnostic digital archive and repository platform
* Premade models for common objects (document, audio, moving image, picture etc.)
* Ingest, management and distribution of digital contents (files and/or metadata)
* Full-text and natural search
* Access management
* Linked data and ontology support
* SaaS and multitenancy support
* Completely customizable user interface, content models and preservation policies
  
Background
---
**OSA (Open Source Archive) means two things:**  
 1.  A development and research project
 2.  This open source software (OSA frontend)

The OSA project was administrated by Mikkeli University of Applied Sciences (Mamk) and carried out by Mamk and partners during May 2012 - December 2014. 

The project was funded by European Union Regional Development Fund granted by South Savo Regional Council. Other funders included City of Mikkeli, Mikkeli Region, Mamk and the project partners.

**Partners**  
Central Archives of Finnish Business Records (Elka)  
Brages Pressarkiv  
Monikko Oy  
Mikkelin Puhelin Oyj  
MariaDB Services AB (now MariaDB Corporation)  
Disec Oy  

**Collaborators**  
National Archives of Finland, AHAA project  
Otavan Opisto  
DigitalMikkeli  

The original OSA project is now completed but Mamk is commited to further developing the software. Active development is done by projects, archiving services and the community. 

Currently, there are no active projects but they will be announced here. Stay tuned, we are not dead and gone.

Technology
---
OSA is built with a few core components:
* Handcrafted frontend application (this repository)
* SOSWE (a micro-service based workflow engine) ... link here
* Fedora Commons 3.x
* Apache Solr 4.x
* MongoDB and MariaDB
* LDAP user directory

In addition, there are a few middleware and other software tools included.

Install
---
###Install environment and dependencies  
 1. Java 1.7 and a web container
* Tested with Oracle Java but OpenJDK probably works
* We used Apache Tomcat 7  

 2. LDAP compatible user directory
* We used OpenLDAP with phpLdapAdmin web management

 3. MariaDB 5.5
* A SQL database is used for both Fedora Commons and OSA frontend

 4. Fedora Commons 3.6
* OSA platform has been tested with the latest Fedora Commons 3.6.x

 5. Apache Solr 4.6
* Install any additional language components for Solr
* Install FedoraGenericSearch (GSearch) middleware
* OSA platform has been tested with GSearch 2.7 and Solr 4.6

 6. MongoDB
* OSA frontend has been tested with MongoDB 2.4.6

 7. SOSWE
* Refer to SOSWE documentation for more information

###Install OSA frontend
Compile the project and deploy the WAR file to Tomcat. You can change the configurations before creating the WAR or after Tomcat has deployed it.

###Deploy and configure
OpenLDAP  
* Create ‘instance admin’ user for OSA-application

MariaDB  
* Create the database for OSA frontend (run createOsaDatabase.sql)

Fedora Commons  
* Ingest content models into Fedora Commons (osa-basecollection.xml, etc.)

Solr with GSearch  
* Deploy the mappings (XSLT transformation) for Fedora datastreams
* Refer to the GSearch documentation for more information

OSA frontend  
* Modify osa.conf to meet your environment
* Create directories in /var/osa as configured in osa.conf and set access rights
* Modify organization configuration file (default.xml) to meet your use case

Tests
--
Coming later. You can help us by adding yours and committing them to the community.

Examples
---
Here is a brief description of our development environment during the OSA project (2012-2014). It is provided solely as an example of a working setup.

We provide sample configurations, data models, Solr schemas etc. with the OSA frontend application. You can find them in /examples directory.

This setup is made for easy and isolated development. You can scale and plan your environment based on your workload and amount of content. One server setup is possible.

Each server runs a minimal install of up to date Centos 6. Has not been tested with newer major releases of Centos.
  
Server 1: frontend
* public server for OSA frontend
* hosts SOSWE and the microservices

Server 2: database
* hosts both MariaDB and MongoDB
* hosts LDAP user directory

Server 3: repository
* hosts Fedora Commons

Server 4: search and indexing
* hosts Apache Solr
* hosts GSearch
* file system linked with Fedora Commons data directory for faster reindexing

Contact
---
Email     osa@mamk.fi  
Twitter   @OSArchive  
Web       http://osa.mamk.fi  

Mikkeli University of Applied Sciences

Contributors
---
**Developers**  
Liisa Uosukainen  
Mikko Lampi  
Teemu Lantiainen  
Heikki Kurhinen  

**Student contributions**   
Tytti Vuorikari  
Outi Hilola  
Saara Komulainen  
Olga Kushanova  
Ekaterina Danilova  
Pekka Hapuli  

License
---
The software is licensed under AGPL-3.0.  
All attached documentation is licensed under CC-BY-SA 4.0.
