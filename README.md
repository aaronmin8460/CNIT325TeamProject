# QuizTrack

QuizTrack is a simple client/server quiz application built for a CNIT 325 style project. It uses plain Java, Swing, sockets, `Scanner`, and `PrintWriter` so the code stays close to lab examples and is easy for a student to follow.

## Main Features

- Instructor and student login with test accounts
- Swing GUI for login, instructor controls, student controls, and question dialog
- Text-based socket protocol using commands separated by `|`
- Real-time question push from instructor to connected students
- Multiple-choice, true/false, and short-answer question types
- In-memory demo data with `MockDataService`
- Optional Supabase-backed storage with `HttpURLConnection`
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

The GUI text files are in `src/i18n`, so keep `src` on the runtime classpath for the client.

On Windows Command Prompt, use `;` instead of `:` in the classpath examples below.

## How To Run The Server

Mock mode:

```bash
java -cp out app.ServerMain mock
```

Supabase mode:

```bash
java -cp out app.ServerMain supabase
```

If you do not pass an argument, the server uses mock mode. The server listens on port `8189`.

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
- The server saves the question in the active data service.
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

## Supabase Setup

`SupabaseService` uses plain `HttpURLConnection` with no external libraries.

### Environment Variables

Set these before starting `supabase` mode:

```bash
export SUPABASE_URL=https://your-project-ref.supabase.co
export SUPABASE_SERVICE_KEY=your_service_role_key
```

Use the service key only on the server side. Do not put it in the client.

### Headers Used

Every request sends:

- `apikey: SUPABASE_SERVICE_KEY`
- `Authorization: Bearer SUPABASE_SERVICE_KEY`
- `Content-Type: application/json`

### Expected Tables

The current `SupabaseService` expects these `public` tables:

- `users`
- `classes`
- `class_members`
- `questions`
- `attempts`

Expected columns:

- `users`: `user_id`, `name`, `email`, `password`, `role`
- `classes`: `class_id`, `instructor_id`, `class_name`, `class_code`, `created_at`
- `class_members`: `class_id`, `student_id`, `joined_at`
- `questions`: `question_id`, `class_code`, `type`, `prompt`, `choice_a`, `choice_b`, `choice_c`, `choice_d`, `correct_answer`, `points`
- `attempts`: `attempt_id`, `class_code`, `question_id`, `student_id`, `submitted_answer`, `correct`, `points_earned`

For Supabase mode, `user_id`, `instructor_id`, and `student_id` can be UUID strings.

If your Supabase table names or column names are different, update `SupabaseService.java` to match your schema.

### Supabase Methods Connected

These methods now call Supabase REST:

- `login`
- `createClass`
- `joinClass`
- `findClassByCode`
- `saveQuestion`
- `saveAttempt`
- `getAttemptsForClass`

`getQuestionsForClass` is also implemented so the service matches the current interface.
