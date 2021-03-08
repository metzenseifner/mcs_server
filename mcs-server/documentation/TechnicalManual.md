---
Title: Technical Documentation for MCS Connect Server
Author: Jonathan L. Komar
---

# Workflow

# Classes

## PreparableCache

This is a generic class that is designed to solve the problem of slow resource acquisition. The resource in question is an SSH session to an SMP device.

The following cases are possible where $e$ is a element in the cache and $C$ is the cache.

1. $e \notin C$, prepare succeeds $ \implies $ success
2. $e \notin C$, prepare fails $ \implies $ failure
3. $e \in C$, test condition $t$ succeeds $ \implies $ success
4. $e \in C$, test condition $t$ fails, prepare succeeds $ \implies $ success
5. $e \in C$, test condition $t$ fails, prepare fails $ \implies $ failure

The caller code (code using the cache) supplies the prepare and condition functions. The success or failure can then also be handled by the caller code, for example, implementing retries.

## RoomRepo

This class is a registry of all rooms at the university. 

## Room

This class represents a physical room with recording capability. It contains references to the resources available at a given room. This object is registered in the LocationRegistry. This class owns a thread, TaskDetermineLocationReady,  responsible for setting a flag that specifies whether this room is ready to go.

## RecordingInstance

This class represents a abstraction layer between the client, 
the data source (TVR over ESB), and the recorders. 
Only one of these exists at a given time in a room.
It's running state (whether a recording is recording (which is called "running" for disambiguity)
is also abstracted away from the SMPs. The running state is assigned by the recording instance. 

## SmpRecorder

This class represents an SMP 351 recorder.

Wipe out the greeter on new SSH connections:

```
Pattern pattern = Pattern.compile("[0-9]{2}:[0-9]{2}:[0-9]{2}\\n")
private Effect<Pattern> afterOpenChannel = (p) -> write("", pattern);
```

### Error Codes

| Code     | Meaning             |
| ---- | ----------------------- |
| E13  | Invalid parameter (number is out of range)/ Unintrepreted character ? |
|      |                         |

# Metadata Mapping

```
FIELDS      = SMP DCES       = Opencast DCMI       = MCS
CONTRIBUTOR = dc:contributor = dcterms:contributor =
COVERAGE    = dc:coverage    = dcterms:coverage    =
PRESENTER   = dc:creator     = dcterms:creator     = userId
DATE        = dc:date        = dcterms:date        = (read-only, usable for merge decisions)
DESCRIPTION = dc:description = dcterms:description = reserved
FORMAT      = dc:format      = dcterms:format      =
IDENTIFIER  = dc:identifier  = dcterms:identifier  = (read-only)
LANGUAGE    = dc:language    = dcterms:language    =
PUBLISHER   = dc:publisher   = dcterms:publisher   =
RELATION    = dc:relation    = dcterms:relation    = bookingId
RIGHTS      = dc:rights      = dcterms:rights      =
SOURCE      = dc:source      = dcterms:source      = groupId
SUBJECT     = dc:subject     = dcterms:subject     = courseNumber-groupNumber termId courseName
TITLE       = dc:title       = dcterms:title       = recordingName
TYPE        = dc:type        = dcterms:type        =
COURSE      = dc:course      = dcterms:course      = ? (not in Dublin)
```

# State Handling

A recorder $\mathcal{R}$ is a DFA state machine. 

$$\mathcal{R}:= \{S, D\}$$ | $S$ are the supported running states. $D$ is the metadata dataset.

$$ \mathcal{S} := \{ \text{STOPPED}, \text{RECORDING}, \text{PAUSED}\} $$

$$ \mathcal{D}:= \{ \text{see SMP DCES} \} $$

MCS uses a subset of $\mathcal{S}$, $ S:= \{S\setminus\text{PAUSED}\} \equiv \{STOPPED, RECORDING\}$ to eliminate redundancies and simplify state handling, but adds one state to handle communication errors: $ M := S \cup E$ | $E$ is $\{\text{UNKNOWN}\}$.

## State Update Strategy

When to notify recorders of change in **running state**:

```
if (oldR exists AND oldR.state == newR.state)
  do nothing

if (oldR exists AND oldR.state != newR.state)
  notify running state

if (oldR empty)
  notify running state 
 
 if (newR empty), fail (this state should be prevented by the caller)
```

When to notify recorders of change in **metadata**:

```
if (oldR exists AND oldR.state == newR.state)
  do nothing

if (oldR exists AND (oldR.state == STOPPED AND newR.state == RECORDING))
  notify metadata
  
if (oldR exists AND (oldR.state == RECORDING AND newR.state == STOPPED))
	do nothing

if (oldR empty AND newR.state == RECORDING)
  notify metadata
 
 if (newR empty), fail (this state should be prevented by the caller)
```

**Finalized Overlapped Logic**:

```
if (oldR exists AND oldR.state == newR.state):
  do nothing

if (oldR exists AND (oldR.state == STOPPED AND newR.state == RECORDING))
  notify metadata
  notify running state

if (oldR exists AND (oldR.state == RECORDING AND newR.state == STOPPED))
	notify running state
	
if (oldR empty AND newR.state == RECORDING)
  notify metadata
  notify running state
  
 if (newR empty), fail (this state should be prevented by the caller)
```

