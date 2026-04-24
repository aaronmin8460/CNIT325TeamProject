# QuizTrack

QuizTrack is a simple client/server quiz application built for a CNIT 325 style project. It uses plain Java, Swing, sockets, `Scanner`, and `PrintWriter` so the code stays close to lab examples and is easy for a student to follow.

## Main Features

- Instructor and student login with test accounts
- Swing GUI for login, instructor controls, student controls, and question dialog
- Text-based socket protocol using commands separated by `|`
- Real-time question push from instructor to connected students
- Multiple-choice, true/false, and short-answer question types
- In-memory demo data with `MockDataService`
- Light internationalization with `ResourceBundle`

## Simple Directory Structure

```text
src/
  app/
    ClientMain.java
    ServerMain.java
  client/
    ClientConnection.java
    InstructorFrame.java
    LoginFrame.java
    QuestionDialog.java
    ServerMessageHandler.java
    StudentFrame.java
  server/
    QuizServer.java
    ClientHandler.java
    AuthController.java
    ClassController.java
    QuestionController.java
    CodeGenerator.java
  model/
    User.java
    Student.java
    Instructor.java
    CourseClass.java
    Question.java
    MultipleChoiceQuestion.java
    TrueFalseQuestion.java
    ShortAnswerQuestion.java
    Attempt.java
  service/
    DataService.java
    MockDataService.java
    SupabaseService.java
  i18n/
    messages_en.properties
    messages_es.properties
README.md
```

## How To Compile From Command Line

On macOS or Linux:

```bash
mkdir -p out
javac -d out $(find src -name "*.java" | sort)
```

The GUI text files are in `src/i18n`, so keep `src` on the runtime classpath.

On Windows Command Prompt, use `;` instead of `:` in the classpath examples below.

## How To Run The Server

```bash
java -cp out:src app.ServerMain
```

The server listens on port `8189`.

## How To Run The Client

Open another terminal and run:

```bash
java -cp out:src app.ClientMain
```

## Test Accounts

- Instructor
  `instructor@test.com` / `pass123`
- Student
  `student@test.com` / `pass123`

## Instructor Workflow

1. Log in with the instructor account.
2. Enter a class name and click `Create Class`.
3. Copy or share the generated class code.
4. Enter question information and click `Create Question`.
5. Click `Results` to see simple class attempt results.

## Student Workflow

1. Log in with the student account.
2. Enter the class code and click `Join Class`.
3. Wait for the instructor to create a question.
4. When the server pushes a question, a dialog opens automatically.
5. Submit the answer and read the result message in the student window.

## How Real-Time Question Push Works

- The instructor sends `CREATE_QUESTION|...` to the server.
- The server saves the question in `MockDataService`.
- `QuizServer` keeps a `HashMap<String, ArrayList<ClientHandler>>` of connected students by class code.
- The server immediately sends `QUESTION_PUSH|...` to every connected student handler in that class.
- The student client listener thread receives the message and opens `QuestionDialog`.

## Internationalization

- The GUI uses `ResourceBundle`.
- `messages_en.properties` is the default language file.
- `messages_es.properties` provides simple Spanish labels.
- The login window has a small language `JComboBox` so the user can switch between English and Spanish before logging in.
- The program still falls back to English text if the resource files are not found.

## AWS EC2 Later

AWS EC2 is not required for the current demo build. Later, EC2 can be used to host the server so multiple clients can connect from different machines on a shared public IP address instead of only running everything locally.

## Supabase Later

`SupabaseService` is only a placeholder right now. Later, it can replace or work alongside `MockDataService` to store:

- users
- classes
- questions
- attempts

That would allow data to stay available after the program stops.

## Why Geo Location Was Removed

Geo location was intentionally removed from this rebuild. The earlier project idea included location checks, but that added complexity that does not fit the simpler CNIT 325 lab-style version. The current version focuses on Swing, sockets, inheritance, interfaces, and readable Java code.

## Why MockDataService Is Used Now

`MockDataService` is the active data layer for demo and testing. It uses simple Java collections like `ArrayList` and `HashMap`, includes built-in test users, and makes it easy to run the project without setting up a real database or cloud service.
