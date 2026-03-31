# CIT 325 Team Project — QuizTrack (Client/Server Learning System)

QuizTrack is a **Java client/server** application that asks users questions (MCQ / True-False / Short Answer), records responses, and synchronizes student progress via **AWS**. The system helps students identify weak topics and prepare for exams by tracking performance over time.

---

## Key Features

- **Question delivery**: multiple-choice, true/false, and short-answer
- **Instant feedback**: correctness shown for MCQ / T-F (short-answer can be “manual/auto-graded” later)
- **Tracking & analytics**: stores each submission with timestamps; aggregates answer-choice statistics
- **Assignments**: group questions into assignments
- **Classlists**: group users into classes/sections for targeted assignments
- **Proximity-gated servers**: optional location-based access (only allow clients within a defined radius)
- **AWS sync**: student/account + attempt history stored remotely and synchronized

---

## CIT 325 Requirements Mapping

This project is designed to satisfy the CIT 325 team project requirements, including:

- **10+ new classes**
- **Inheritance relationships across 5+ classes**
- **Distributed element using sockets (TCP/UDP)**
- **At least 1 Java interface**
- **2 external interfaces** (AWS + Maps/Geolocation API)
- **Time classes used**
- **GUI (Swing/AWT)**
- **Internationalization (2+ locales)** :contentReference[oaicite:1]{index=1}

---

## Architecture (High-Level)

**Client/Server model**

- Client connects to server over sockets
- Server sends a question payload
- Client returns an answer payload
- Server validates (for MCQ/T-F), stores results, and returns feedback
- Data is synchronized to AWS storage for persistence and multi-device access

---

## Planned Class Design (Draft)

> Names may change as we implement.

### Core Domain

- `User` — account/profile info (name, ID, class membership)
- `Question` — prompt + type + choices + correct answer (if applicable)
- `Answer` — user response + correctness + timestamps
- `Assignment` — collection of questions (and optional due dates)
- `ClassList` — group of users assigned to a course/section

### Distributed / Networking

- `Server` — listens for connections; dispatches questions; records answers
- `Client` — connects to server; displays question; sends back answer
- `Message` (or `Packet`) — common request/response payload structure (often serialized)

### Location / Time / GUI

- `LocationPolicy` — proximity rules (lat/long + radius)
- `TimeStamp` (or `SessionTime`) — start/stop times, attempt timestamps (uses `java.time`)
- `GUI` (Swing/AWT) — screens for login, join server, answer question, view results, view assignments

### External Services

- `AwsStorageService` — save/load users, attempts, assignments (e.g., DynamoDB/S3)
- `GeoService` — geolocation / distance calculations (and/or Maps API integration)

### Interface (Example)

- `Storable` or `SerializablePayload` — ensures objects can be saved/transmitted consistently

---

## Inheritance Plan (Example)

We’ll implement inheritance to reduce duplication, for example:

- `Question` (base) → `MCQQuestion`, `TrueFalseQuestion`, `ShortAnswerQuestion`
- `Message` (base) → `QuestionMessage`, `AnswerMessage`, `AuthMessage`, `ResultMessage`
- `User` (base) → `Student`, `Instructor` (optional, if we add instructor tools)

---

## Repository Structure (Suggested)

/docs # diagrams, spec, screenshots  
/src  
/client # client-side code  
/server # server-side code  
/common # shared models (Question, Answer, Message, etc.)  
/tests  
README.md

---

## How to Run (Placeholder)

> We’ll update once the first runnable version exists.

### 1) Start Server

- Run `ServerMain` (binds to configured port)

### 2) Start Client

- Run `ClientMain`
- Connect to server IP + port
- Answer questions through the GUI

---

## Team Workflow

- **Main branch**: protected; PR required
- **Feature branches**: `feature/<name>-<topic>`
- **PR expectations**:
  - concise description
  - screenshots for GUI changes
  - notes on any protocol changes (client/server message formats)

---

## Project Status

- [ ] Protocol design (message types + serialization)
- [ ] Basic server loop (accept → send question → receive answer)
- [ ] GUI v1 (connect + answer screen)
- [ ] AWS persistence
- [ ] Assignment + classlist support
- [ ] Location gating
- [ ] Internationalization (en + another locale)

---

## UML Class Box Contents (Draft)

> These are the planned class-box contents for the UML diagram.  
> Names may be adjusted slightly during implementation.

### Interface

#### `Storable`

**Methods**

- `+ save() : void`
- `+ load() : void`

---

### Core Domain

#### `User`

**Attributes**

- `- userId : int`
- `- name : String`
- `- email : String`
- `- classMembership : String`

**Methods**

- `+ login() : boolean`
- `+ logout() : void`

#### `Student extends User`

**Attributes**

- `- attempts : List<Answer>`

**Methods**

- `+ submitAnswer() : void`
- `+ viewProgress() : void`

#### `Instructor extends User`

**Attributes**

- `- courses : List<String>`

**Methods**

- `+ createQuestion() : void`
- `+ createAssignment() : void`
- `+ reviewResults() : void`

#### `Question` _(abstract)_

**Attributes**

- `- questionId : int`
- `- prompt : String`
- `- topic : String`
- `- points : int`

**Methods**

- `+ displayQuestion() : String`

#### `MCQQuestion extends Question`

**Attributes**

- `- choices : List<String>`
- `- correctChoice : String`

**Methods**

- `+ isCorrect(answer : String) : boolean`

#### `TrueFalseQuestion extends Question`

**Attributes**

- `- correctValue : boolean`

**Methods**

- `+ isCorrect(answer : String) : boolean`

#### `ShortAnswerQuestion extends Question`

**Attributes**

- `- expectedAnswer : String`

**Methods**

- `+ compareAnswer(answer : String) : boolean`

#### `Answer`

**Attributes**

- `- answerId : int`
- `- response : String`
- `- correct : boolean`
- `- timestamp : LocalDateTime`

**Methods**

- `+ checkCorrectness() : boolean`

#### `Assignment`

**Attributes**

- `- assignmentId : int`
- `- title : String`
- `- dueDate : LocalDateTime`
- `- questions : List<Question>`

**Methods**

- `+ addQuestion() : void`
- `+ removeQuestion() : void`

#### `ClassList`

**Attributes**

- `- classId : int`
- `- className : String`
- `- students : List<Student>`

**Methods**

- `+ addStudent() : void`
- `+ removeStudent() : void`

---

### Distributed / Networking

#### `Server`

**Attributes**

- `- port : int`
- `- activeClients : List<Client>`

**Methods**

- `+ startServer() : void`
- `+ sendQuestion() : void`
- `+ receiveAnswer() : void`

#### `Client`

**Attributes**

- `- socket : Socket`
- `- currentUser : User`

**Methods**

- `+ connect() : void`
- `+ submitAnswer() : void`
- `+ receiveQuestion() : void`

#### `Message` _(abstract)_

**Attributes**

- `- messageId : int`
- `- timestamp : LocalDateTime`
- `- senderId : int`

**Methods**

- `+ serialize() : String`
- `+ deserialize() : Message`

#### `QuestionMessage extends Message`

**Attributes**

- `- question : Question`

**Methods**

- `+ getQuestion() : Question`

#### `AnswerMessage extends Message`

**Attributes**

- `- answer : Answer`

**Methods**

- `+ getAnswer() : Answer`

#### `AuthMessage extends Message`

**Attributes**

- `- username : String`
- `- password : String`

**Methods**

- `+ authenticate() : boolean`

#### `ResultMessage extends Message`

**Attributes**

- `- correct : boolean`
- `- feedback : String`

**Methods**

- `+ getFeedback() : String`

---

### Location / Time / GUI

#### `LocationPolicy`

**Attributes**

- `- latitude : double`
- `- longitude : double`
- `- allowedRadius : double`

**Methods**

- `+ isWithinRange() : boolean`

#### `SessionTime`

**Attributes**

- `- startTime : LocalDateTime`
- `- endTime : LocalDateTime`

**Methods**

- `+ getDuration() : Duration`

#### `GUI`

**Attributes**

- `- title : String`

**Methods**

- `+ launch() : void`
- `+ showLoginScreen() : void`
- `+ showQuestionScreen() : void`
- `+ showResultsScreen() : void`

---

### External Services

#### `AwsStorageService`

**Attributes**

- `- awsEndpoint : String`

**Methods**

- `+ uploadData() : void`
- `+ downloadData() : void`

#### `GeoService`

**Attributes**

- `- apiKey : String`

**Methods**

- `+ getCoordinates() : double[]`
- `+ calculateDistance() : double`

---

## Inheritance Summary

- `Student extends User`
- `Instructor extends User`
- `MCQQuestion extends Question`
- `TrueFalseQuestion extends Question`
- `ShortAnswerQuestion extends Question`
- `QuestionMessage extends Message`
- `AnswerMessage extends Message`
- `AuthMessage extends Message`
- `ResultMessage extends Message`

---

## Interface Implementation (Draft)

- `AwsStorageService implements Storable`
- `Assignment implements Storable`
- `Answer implements Storable`
