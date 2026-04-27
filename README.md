# QuizTrack

QuizTrack is a simple client/server quiz application. The program provides instructor and students' live quiz interaction.

## How To Compile From Command Line

```bash
javac *.java
```

## How To Run The Server

Mock mode:

```bash
java ServerMain mock 8189
```

Supabase mode:

```bash
java ServerMain supabase
```

If you do not pass an argument, the server uses mock mode. The server listens on port `8189`.

## How To Run The Client

Open another terminal and run:

Mock mode:

```bash
java ClientMain 127.0.0.1 8189
```

Actual server running mode:

```bash
java ClinetMain YOUR_EC2_IP 8189
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
4. When the server pushes a question, a dialog opens.
5. Submit the answer and read the result message in the student JTextArea.

## Internationalization

- `messages_en.properties` is the default language file.
- `messages_es.properties` provides Spanish labels.
- The login window has a small language `JComboBox` so the user can switch between English and Spanish before logging in.

## AWS EC2 Setup

AWS EC2 is not required for the demo build which use local ip address.
EC2 can be used to host the server so multiple clients can connect from different machines on a shared public IP address instead of only running everything locally.

1. Create a new AWS EC2 instance with Amazon Linux
2. Add security gruop as following configuration

```bash
Custom TCP
Port: 8189
Source: 0.0.0.0/0
```

3. Connect to the EC2 via following command with the .pem key you created when you launch the instance

```bash
chmod 400 YOUR_KEY.pem
ssh -i YOUR_KEY.pem ec2-user@YOUR_EC2_IP
```

4. Install Java and Git on EC2

```bash
sudo yum update -y
sudo yum install git -y
sudo yum install java-17-amazon-corretto-devel -y
```

5. Pull Github Repository

```bash
git clone https://github.com/aaronmin8460/CNIT325TeamProject.git
cd CNIT325TeamProject
```

6. Start your Server

Mock mode:

```bash
java ServerMain mock
```

Supabase mode:

```bash
java ServerMain supabase
```

## Supabase Setup

Before starting the `supabase` mode, create `supabase.properties` file and put the following lines.

```bash
export SUPABASE_URL=https://your-project-ref.supabase.co
export SUPABASE_SERVICE_KEY=your_service_role_key
```
