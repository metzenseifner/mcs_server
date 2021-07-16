# Media Control System

The Media Control System is designed to provide access control, 
automatic tagging of media content with existing data from the 
course database (implemented by TVR) and the personnel database
(implemented by VIS).  This data is also used to ensure the
integrity of data stored in the archive (implemented by Opencast).

 For example, it provides a
level of security to prevent unauthorized access in the lecture halls. 
Content created by hardware encoders in the lecture halls get tagged
with data gathered from the TVR and VIS, thereby maintaining a strict 
relationship between archived content and official course bookings in the TVR.
This strict relationship makes it possible to manage
the lifecycle of archived content based on more than just the creation date.

The system is based on the client-server paradigm. A server application
serves resources to a client application. The client application is a web
interface that utilizes services offered by the server.

This integration project aims to connect the following entities
in the infrastructure at the University of Innsbruck:

  1. The course booking database (implemented by TVR).
  1. The personnel database (implemented by VIS).
  1. The recorders (hardware encoders, usually SMP 351s) in lecture halls.
  1. The terminals (stationary computers) in lecture halls.
  1. The media archive (implemented by Opencast).

# Build

This project is a multi-project Gradle build.
To build mcs-server, run `./gradlew build`. The RPMs will land under `mcs-server/build/distributions`.
The Apache Karaf KAR files are created per assembly under `mcs-server/build/assemblies/<name>/kar`.

A couple of notes: The main build requires a convention plugin, also defined in
this project. It suffices to build it and store it in a local Maven repository.
There is a task for that: `./gradlew publishToMavenLocal`.
The convention plugin helps to manage versions of dependencies, and to
maintain consistency accross components and their dependencies. Also, because
of a quirk in the Karaf Plugin, it is required to first run `gradle build` on
the assembly projects first. The problem has to do with the fact that the
author of this plugin adds files to the subproject's (assembly's) dependency
configuration in the "after project" phase. These build dependencies are not
caught by the top-level project. This should be fixed at some point.

# Other Documentation

This is part of the project, "AV Portal", whose plan can be found under [Arbeitspaket:
AV-Portal](https://sp.uibk.ac.at/sites/zid/nml/avportal/_layouts/15/start.aspx#/Lists/Arbeitspakete%202021/AllItems.aspx). The project's wiki documentation can be found at [AV-Portal Wiki](https://wiki.uibk.ac.at/display/zidecamptec/AV-Portal).

# Infrastructure Checklist

1. Open ticket for new host with linux server group Zentrale Systeme Linux Server LXSERVER. Florian Faltermeier or Alex Bihlmaier.
1. Open ticket to register new ips and domain names for hosts with IKT INNET Admin NETADM. Walter Müller.
1. Open ticket for domain nam entry on IdP (entity id with certificate) with WEB Identity Management und Datenbank Entwicklung DBDEVEL. Martin Krenn.
