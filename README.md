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
To build mcs-server, run `gradle build`. The RPMs will land under `mcs-server/build/distributions`.
The Apache Karaf KAR files are created per assembly under `mcs-server/build/assemblies/<name>/kar`.
