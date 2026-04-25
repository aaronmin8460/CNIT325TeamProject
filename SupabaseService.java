import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

/**
 * This class connects to Supabase with simple HTTP requests.
 * It expects public tables named users, classes, class_members,
 * questions, and attempts.
 */
public class SupabaseService implements DataService {

    private static final String USERS_TABLE = "users";

    private static final String CLASSES_TABLE = "classes";

    private static final String CLASS_MEMBERS_TABLE = "class_members";

    private static final String QUESTIONS_TABLE = "questions";

    private static final String ATTEMPTS_TABLE = "attempts";

    private String projectUrl;

    private String apiKey;

    public SupabaseService() {
        String url = System.getenv("SUPABASE_URL");
        String key = System.getenv("SUPABASE_SERVICE_KEY");

        if (url == null || url.length() == 0 || key == null || key.length() == 0) {
            try {
                java.util.Properties props = new java.util.Properties();
                java.io.FileInputStream file = new java.io.FileInputStream("supabase.properties");
                props.load(file);
                file.close();

                url = props.getProperty("SUPABASE_URL");
                key = props.getProperty("SUPABASE_SERVICE_KEY");
            } catch (Exception e) {
                System.out.println("supabase.properties not found. Using environment variables only.");
            }
        }

        this.projectUrl = normalizeProjectUrl(url);
        this.apiKey = key;
    }

    public SupabaseService(String projectUrl, String apiKey) {

        this.projectUrl = normalizeProjectUrl(projectUrl);

        this.apiKey = apiKey;

    }

    @Override
    public User login(String email, String password) {

        String path;
        String requestUrl;
        SupabaseResponse response;
        ArrayList<HashMap<String, String>> rows;

        if (!isConfigured()) {
            return null;
        }

        if (email == null || password == null) {
            return null;
        }

        path = USERS_TABLE
                + "?select=user_id,name,email,password,role"
                + "&email=eq." + encodeQueryValue(email)
                + "&password=eq." + encodeQueryValue(password)
                + "&limit=1";

        requestUrl = buildApiUrl(path);

        System.out.println("Supabase login request URL: " + requestUrl);

        response = sendRequest("GET", path, null, null, "application/json");

        System.out.println("Supabase login response code: " + response.getStatusCode());
        System.out.println("Supabase login response body: " + response.getBody());

        if (!response.isSuccess()) {
            return null;
        }

        rows = parseJsonArray(response.getBody());

        if (rows.size() == 0) {
            return null;
        }

        return buildUserFromRow(rows.get(0));

    }

    @Override
    public CourseClass createClass(String className, Instructor instructor) {

        String classCode;
        StringBuilder body;
        SupabaseResponse response;
        ArrayList<HashMap<String, String>> rows;
        CourseClass courseClass;

        if (!isConfigured()) {
            return null;
        }

        if (className == null || instructor == null) {
            return null;
        }

        classCode = generateClassCode();

        body = new StringBuilder();
        body.append("{");
        body.append("\"class_code\":\"");
        body.append(escapeJson(classCode));
        body.append("\",");
        body.append("\"class_name\":\"");
        body.append(escapeJson(className));
        body.append("\",");
        body.append("\"instructor_id\":");
        body.append("\"");
        body.append(escapeJson(instructor.getUserId()));
        body.append("\"");
        body.append("}");

        response = sendRequest(
                "POST",
                CLASSES_TABLE,
                body.toString(),
                "return=representation",
                "application/json");

        if (!response.isSuccess()) {
            return null;
        }

        rows = parseJsonArray(response.getBody());

        if (rows.size() == 0) {
            return null;
        }

        courseClass = buildCourseClassFromRow(rows.get(0));

        if (courseClass != null) {
            courseClass.setInstructor(instructor);
        }

        return courseClass;

    }

    @Override
    public boolean joinClass(String classCode, Student student) {

        CourseClass courseClass;
        String path;
        String requestUrl;
        StringBuilder body;
        SupabaseResponse response;
        ArrayList<HashMap<String, String>> rows;

        if (!isConfigured()) {
            return false;
        }

        if (classCode == null || student == null) {
            return false;
        }

        courseClass = findClassByCode(classCode);

        if (courseClass == null) {
            return false;
        }

        path = CLASS_MEMBERS_TABLE
                + "?select=class_id,student_id"
                + "&class_id=eq." + encodeQueryValue(courseClass.getClassId())
                + "&student_id=eq." + encodeQueryValue(student.getUserId())
                + "&limit=1";

        requestUrl = buildApiUrl(path);

        System.out.println("Supabase joinClass check request URL: " + requestUrl);

        response = sendRequest("GET", path, null, null, "application/json");

        System.out.println("Supabase joinClass check response code: " + response.getStatusCode());
        System.out.println("Supabase joinClass check response body: " + response.getBody());

        if (!response.isSuccess()) {
            return false;
        }

        rows = parseJsonArray(response.getBody());

        if (rows.size() > 0) {
            return false;
        }

        body = new StringBuilder();
        body.append("{");
        body.append("\"class_id\":\"");
        body.append(escapeJson(courseClass.getClassId()));
        body.append("\",");
        body.append("\"student_id\":");
        body.append("\"");
        body.append(escapeJson(student.getUserId()));
        body.append("\"");
        body.append("}");

        requestUrl = buildApiUrl(CLASS_MEMBERS_TABLE);

        System.out.println("Supabase joinClass insert request URL: " + requestUrl);

        response = sendRequest(
                "POST",
                CLASS_MEMBERS_TABLE,
                body.toString(),
                "return=representation",
                "application/json");

        System.out.println("Supabase joinClass insert response code: " + response.getStatusCode());
        System.out.println("Supabase joinClass insert response body: " + response.getBody());

        if (!response.isSuccess()) {
            return false;
        }

        student.setClassCode(classCode);

        return true;

    }

    @Override
    public CourseClass findClassByCode(String classCode) {

        String path;
        String requestUrl;
        SupabaseResponse response;
        ArrayList<HashMap<String, String>> rows;

        if (!isConfigured()) {
            return null;
        }

        if (classCode == null) {
            return null;
        }

        path = CLASSES_TABLE
                + "?select=class_id,class_code,class_name,instructor_id"
                + "&class_code=eq." + encodeQueryValue(classCode)
                + "&limit=1";

        requestUrl = buildApiUrl(path);

        System.out.println("Supabase findClassByCode request URL: " + requestUrl);

        response = sendRequest("GET", path, null, null, "application/json");

        System.out.println("Supabase findClassByCode response code: " + response.getStatusCode());
        System.out.println("Supabase findClassByCode response body: " + response.getBody());

        if (!response.isSuccess()) {
            return null;
        }

        rows = parseJsonArray(response.getBody());

        if (rows.size() == 0) {
            return null;
        }

        return buildCourseClassFromRow(rows.get(0));

    }

    @Override
    public Question saveQuestion(String classCode, Question question) {

        CourseClass courseClass;
        StringBuilder body;
        SupabaseResponse response;
        ArrayList<HashMap<String, String>> rows;
        Question savedQuestion;

        if (!isConfigured()) {
            return null;
        }

        if (classCode == null || question == null) {
            return null;
        }

        courseClass = findClassByCode(classCode);

        if (courseClass == null) {
            System.out.println("Supabase saveQuestion class lookup response: class not found");
            return null;
        }

        if (courseClass.getClassId() == null || courseClass.getClassId().length() == 0) {
            System.out.println("Supabase saveQuestion class lookup response: class_id is missing");
            return null;
        }

        System.out.println(
                "Supabase saveQuestion class lookup response: classId="
                        + courseClass.getClassId()
                        + ", classCode="
                        + courseClass.getClassCode()
                        + ", className="
                        + courseClass.getClassName());

        body = new StringBuilder();
        body.append("{");
        body.append("\"class_id\":\"");
        body.append(escapeJson(courseClass.getClassId()));
        body.append("\",");
        body.append("\"question_type\":\"");
        body.append(escapeJson(getQuestionType(question)));
        body.append("\",");
        body.append("\"prompt\":\"");
        body.append(escapeJson(question.getPrompt()));
        body.append("\",");
        body.append("\"choice_a\":\"");
        body.append(escapeJson(getChoice(question, 0)));
        body.append("\",");
        body.append("\"choice_b\":\"");
        body.append(escapeJson(getChoice(question, 1)));
        body.append("\",");
        body.append("\"choice_c\":\"");
        body.append(escapeJson(getChoice(question, 2)));
        body.append("\",");
        body.append("\"choice_d\":\"");
        body.append(escapeJson(getChoice(question, 3)));
        body.append("\",");
        body.append("\"correct_answer\":\"");
        body.append(escapeJson(getCorrectAnswer(question)));
        body.append("\"");
        body.append("}");

        System.out.println("Supabase saveQuestion insert request body: " + body.toString());

        response = sendRequest(
                "POST",
                QUESTIONS_TABLE,
                body.toString(),
                "return=representation",
                "application/json");

        System.out.println("Supabase saveQuestion response code: " + response.getStatusCode());
        System.out.println("Supabase saveQuestion response body: " + response.getBody());

        if (!response.isSuccess()) {
            return null;
        }

        rows = parseJsonArray(response.getBody());

        if (rows.size() == 0) {
            return null;
        }

        savedQuestion = buildQuestionFromRow(rows.get(0));

        if (savedQuestion != null) {
            savedQuestion.setClassCode(classCode);
            System.out.println("Supabase saveQuestion parsed questionId: " + savedQuestion.getQuestionId());
        }

        return savedQuestion;

    }

    @Override
    public ArrayList<Question> getQuestionsForClass(String classCode) {

        CourseClass courseClass;
        String path;
        SupabaseResponse response;
        ArrayList<HashMap<String, String>> rows;
        ArrayList<Question> questions;
        Question question;
        int i;

        questions = new ArrayList<Question>();

        if (!isConfigured()) {
            return questions;
        }

        if (classCode == null) {
            return questions;
        }

        courseClass = findClassByCode(classCode);

        if (courseClass == null) {
            return questions;
        }

        if (courseClass.getClassId() == null || courseClass.getClassId().length() == 0) {
            return questions;
        }

        path = QUESTIONS_TABLE
                + "?select=question_id,class_id,question_type,prompt,choice_a,choice_b,choice_c,choice_d,correct_answer"
                + "&class_id=eq." + encodeQueryValue(courseClass.getClassId())
                + "&order=question_id.asc";

        response = sendRequest("GET", path, null, null, "application/json");

        if (!response.isSuccess()) {
            return questions;
        }

        rows = parseJsonArray(response.getBody());

        for (i = 0; i < rows.size(); i++) {
            question = buildQuestionFromRow(rows.get(i));

            if (question != null) {
                question.setClassCode(classCode);
                questions.add(question);
            }
        }

        return questions;

    }

    @Override
    public Attempt saveAttempt(Attempt attempt) {

        StringBuilder body;
        SupabaseResponse response;
        ArrayList<HashMap<String, String>> rows;
        Attempt savedAttempt;
        String studentId;
        String questionId;

        if (!isConfigured()) {
            return null;
        }

        if (attempt == null) {
            return null;
        }

        if (attempt.getQuestion() == null || attempt.getStudent() == null) {
            return null;
        }

        studentId = attempt.getStudent().getUserId();
        questionId = attempt.getQuestion().getQuestionId();

        if (studentId == null || studentId.length() == 0) {
            return null;
        }

        if (questionId == null || questionId.length() == 0) {
            return null;
        }

        body = new StringBuilder();
        body.append("{");
        body.append("\"student_id\":\"");
        body.append(escapeJson(studentId));
        body.append("\",");
        body.append("\"question_id\":\"");
        body.append(escapeJson(questionId));
        body.append("\",");
        body.append("\"answer\":\"");
        body.append(escapeJson(attempt.getSubmittedAnswer()));
        body.append("\",");
        body.append("\"correct\":");
        body.append(attempt.isCorrect());
        body.append("}");

        response = sendRequest(
                "POST",
                ATTEMPTS_TABLE,
                body.toString(),
                "return=representation",
                "application/json");

        System.out.println("Supabase saveAttempt response code: " + response.getStatusCode());
        System.out.println("Supabase saveAttempt response body: " + response.getBody());

        if (!response.isSuccess()) {
            return null;
        }

        rows = parseJsonArray(response.getBody());

        if (rows.size() == 0) {
            return null;
        }

        savedAttempt = buildAttemptFromRow(rows.get(0));

        if (savedAttempt != null) {
            savedAttempt.setStudent(attempt.getStudent());
            savedAttempt.setQuestion(attempt.getQuestion());
        }

        return savedAttempt;

    }

    @Override
    public ArrayList<Attempt> getAttemptsForClass(String classCode) {

        ArrayList<Question> questions;
        HashMap<String, Student> studentsById;
        ArrayList<Attempt> attempts;
        Question question;
        Attempt attempt;
        Student student;
        ArrayList<HashMap<String, String>> rows;
        SupabaseResponse response;
        String path;
        String questionId;
        String studentId;
        int i;
        int j;

        attempts = new ArrayList<Attempt>();

        if (!isConfigured()) {
            return attempts;
        }

        if (classCode == null) {
            return attempts;
        }

        questions = getQuestionsForClass(classCode);

        if (questions.size() == 0) {
            return attempts;
        }

        studentsById = new HashMap<String, Student>();

        for (i = 0; i < questions.size(); i++) {
            question = questions.get(i);

            if (question == null) {
                continue;
            }

            questionId = question.getQuestionId();

            if (questionId == null || questionId.length() == 0) {
                continue;
            }

            path = ATTEMPTS_TABLE
                    + "?select=attempt_id,student_id,question_id,answer,correct"
                    + "&question_id=eq." + encodeQueryValue(questionId)
                    + "&order=submitted_at.asc";

            response = sendRequest("GET", path, null, null, "application/json");

            if (!response.isSuccess()) {
                continue;
            }

            rows = parseJsonArray(response.getBody());

            for (j = 0; j < rows.size(); j++) {
                attempt = buildAttemptFromRow(rows.get(j));

                if (attempt == null) {
                    continue;
                }

                attempt.setQuestion(question);
                student = attempt.getStudent();

                if (student != null) {
                    studentId = student.getUserId();

                    if (studentId != null && studentId.length() > 0) {
                        student = studentsById.get(studentId);

                        if (student == null) {
                            student = loadStudentById(studentId);

                            if (student != null) {
                                studentsById.put(studentId, student);
                            }
                        }

                        if (student != null) {
                            attempt.setStudent(student);
                        }
                    }
                }

                attempts.add(attempt);
            }
        }

        return attempts;

    }

    public boolean isConfigured() {

        if (projectUrl == null || projectUrl.length() == 0) {
            return false;
        }

        if (apiKey == null || apiKey.length() == 0) {
            return false;
        }

        return true;

    }

    private String normalizeProjectUrl(String url) {

        String fixedUrl;

        if (url == null) {
            return "";
        }

        fixedUrl = url.trim();

        while (fixedUrl.endsWith("/")) {
            fixedUrl = fixedUrl.substring(0, fixedUrl.length() - 1);
        }

        if (fixedUrl.endsWith("/rest/v1")) {
            fixedUrl = fixedUrl.substring(0, fixedUrl.length() - 8);
        }

        return fixedUrl;

    }

    private String generateClassCode() {

        long number;

        number = System.currentTimeMillis() % 100000000L;

        return "CLS" + number;

    }

    private String getQuestionType(Question question) {

        if (question instanceof MultipleChoiceQuestion) {
            return "MULTIPLE_CHOICE";
        }

        if (question instanceof TrueFalseQuestion) {
            return "TRUE_FALSE";
        }

        if (question instanceof ShortAnswerQuestion) {
            return "SHORT_ANSWER";
        }

        return "QUESTION";

    }

    private String getChoice(Question question, int index) {

        ArrayList<String> choices;

        if (question instanceof MultipleChoiceQuestion) {
            choices = ((MultipleChoiceQuestion) question).getChoices();

            if (choices != null) {
                if (index >= 0 && index < choices.size()) {
                    return choices.get(index);
                }
            }
        }

        if (question instanceof TrueFalseQuestion) {
            if (index == 0) {
                return "True";
            }

            if (index == 1) {
                return "False";
            }
        }

        return "";

    }

    private String getCorrectAnswer(Question question) {

        if (question instanceof MultipleChoiceQuestion) {
            return ((MultipleChoiceQuestion) question).getCorrectAnswer();
        }

        if (question instanceof TrueFalseQuestion) {
            return String.valueOf(((TrueFalseQuestion) question).isCorrectAnswer());
        }

        if (question instanceof ShortAnswerQuestion) {
            return ((ShortAnswerQuestion) question).getSampleAnswer();
        }

        return "";

    }

    private User buildUserFromRow(HashMap<String, String> row) {

        String userId;
        String email;
        String password;
        String name;
        String role;
        Instructor instructor;
        Student student;

        userId = getString(row, "user_id", "id");
        email = getString(row, "email");
        password = getString(row, "password");
        name = getString(row, "name");
        role = getString(row, "role");

        if ("instructor".equalsIgnoreCase(role)) {
            instructor = new Instructor(userId, name, email, password);
            return instructor;
        }

        if ("student".equalsIgnoreCase(role)) {
            student = new Student(userId, name, email, password);
            return student;
        }

        return new User(userId, email, password, name, role);

    }

    private CourseClass buildCourseClassFromRow(HashMap<String, String> row) {

        CourseClass courseClass;
        String instructorId;
        Instructor instructor;

        courseClass = new CourseClass(
                getString(row, "class_code", "code"),
                getString(row, "class_name", "name"));

        courseClass.setClassId(getString(row, "class_id"));

        instructorId = getString(row, "instructor_id");

        if (instructorId.length() > 0) {
            instructor = new Instructor();
            instructor.setUserId(instructorId);
            courseClass.setInstructor(instructor);
        }

        return courseClass;

    }

    private Question buildQuestionFromRow(HashMap<String, String> row) {

        String type;
        String questionId;
        int points;
        String classCode;
        String prompt;
        MultipleChoiceQuestion multipleChoiceQuestion;
        TrueFalseQuestion trueFalseQuestion;
        ShortAnswerQuestion shortAnswerQuestion;
        ArrayList<String> choices;

        type = getString(row, "question_type", "type");
        questionId = getString(row, "question_id", "id");
        points = getInt(row, "points");
        classCode = getString(row, "class_code");
        prompt = getString(row, "prompt");

        if ("TRUE_FALSE".equalsIgnoreCase(type)) {
            trueFalseQuestion = new TrueFalseQuestion(questionId, prompt, points, classCode);
            trueFalseQuestion.setCorrectAnswer(Boolean.parseBoolean(getString(row, "correct_answer")));
            return trueFalseQuestion;
        }

        if ("SHORT_ANSWER".equalsIgnoreCase(type)) {
            shortAnswerQuestion = new ShortAnswerQuestion(questionId, prompt, points, classCode);
            shortAnswerQuestion.setSampleAnswer(getString(row, "correct_answer"));
            return shortAnswerQuestion;
        }

        multipleChoiceQuestion = new MultipleChoiceQuestion(questionId, prompt, points, classCode);
        choices = new ArrayList<String>();

        addChoiceIfPresent(choices, getString(row, "choice_a"));
        addChoiceIfPresent(choices, getString(row, "choice_b"));
        addChoiceIfPresent(choices, getString(row, "choice_c"));
        addChoiceIfPresent(choices, getString(row, "choice_d"));

        multipleChoiceQuestion.setChoices(choices);
        multipleChoiceQuestion.setCorrectAnswer(getString(row, "correct_answer"));

        return multipleChoiceQuestion;

    }

    private void addChoiceIfPresent(ArrayList<String> choices, String value) {

        if (value == null) {
            return;
        }

        if (value.length() == 0) {
            return;
        }

        choices.add(value);

    }

    private Attempt buildAttemptFromRow(HashMap<String, String> row) {

        Attempt attempt;
        Student student;
        Question question;

        attempt = new Attempt();
        attempt.setAttemptId(getInt(row, "attempt_id", "id"));
        attempt.setSubmittedAnswer(getString(row, "answer", "submitted_answer"));
        attempt.setCorrect(getBoolean(row, "correct"));
        attempt.setPointsEarned(getInt(row, "points_earned"));

        student = new Student();
        student.setUserId(getString(row, "student_id"));
        attempt.setStudent(student);

        question = new ShortAnswerQuestion();
        question.setQuestionId(getString(row, "question_id"));
        question.setClassCode(getString(row, "class_code"));
        attempt.setQuestion(question);

        return attempt;

    }

    private Student loadStudentById(String studentId) {

        String path;
        SupabaseResponse response;
        ArrayList<HashMap<String, String>> rows;
        HashMap<String, String> row;
        Student student;

        if (studentId == null || studentId.length() == 0) {
            return null;
        }

        path = USERS_TABLE
                + "?select=user_id,name,email,role"
                + "&user_id=eq." + encodeQueryValue(studentId)
                + "&limit=1";

        response = sendRequest("GET", path, null, null, "application/json");

        if (!response.isSuccess()) {
            return null;
        }

        rows = parseJsonArray(response.getBody());

        if (rows.size() == 0) {
            return null;
        }

        row = rows.get(0);
        student = new Student();
        student.setUserId(getString(row, "user_id", "id"));
        student.setName(getString(row, "name"));
        student.setEmail(getString(row, "email"));
        student.setRole(getString(row, "role"));

        if (student.getRole() == null || student.getRole().length() == 0) {
            student.setRole("student");
        }

        return student;

    }

    private int getInt(HashMap<String, String> row, String... keys) {

        String value;
        int i;

        for (i = 0; i < keys.length; i++) {
            value = row.get(keys[i]);

            if (value != null && value.length() > 0) {
                try {

                    return Integer.parseInt(value);

                } catch (NumberFormatException e) {

                    return 0;

                }
            }
        }

        return 0;

    }

    private boolean getBoolean(HashMap<String, String> row, String... keys) {

        String value;
        int i;

        for (i = 0; i < keys.length; i++) {
            value = row.get(keys[i]);

            if (value != null) {
                return Boolean.parseBoolean(value);
            }
        }

        return false;

    }

    private String getString(HashMap<String, String> row, String... keys) {

        String value;
        int i;

        for (i = 0; i < keys.length; i++) {
            value = row.get(keys[i]);

            if (value != null) {
                return value;
            }
        }

        return "";

    }

    private SupabaseResponse sendRequest(String method, String path, String requestBody, String preferHeader,
            String acceptHeader) {

        HttpURLConnection connection;
        InputStream inputStream;
        OutputStream outputStream;
        int statusCode;
        String responseBody;

        connection = null;

        try {

            connection = (HttpURLConnection) new URL(buildApiUrl(path)).openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("apikey", apiKey);
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Content-Type", "application/json");

            if (acceptHeader != null && acceptHeader.length() > 0) {
                connection.setRequestProperty("Accept", acceptHeader);
            }

            if (preferHeader != null && preferHeader.length() > 0) {
                connection.setRequestProperty("Prefer", preferHeader);
            }

            if (requestBody != null) {
                connection.setDoOutput(true);
                outputStream = connection.getOutputStream();
                outputStream.write(requestBody.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                outputStream.close();
            }

            statusCode = connection.getResponseCode();

            if (statusCode >= 200 && statusCode < 300) {
                inputStream = connection.getInputStream();
            } else {
                inputStream = connection.getErrorStream();
            }

            responseBody = readStream(inputStream);

            return new SupabaseResponse(statusCode, responseBody);

        } catch (IOException e) {

            return new SupabaseResponse(500, e.getMessage());

        } finally {

            if (connection != null) {
                connection.disconnect();
            }

        }

    }

    private String buildApiUrl(String path) {

        return projectUrl + "/rest/v1/" + path;

    }

    private String readStream(InputStream inputStream) {

        BufferedReader reader;
        StringBuilder builder;
        String line;

        if (inputStream == null) {
            return "";
        }

        builder = new StringBuilder();

        try {

            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            line = reader.readLine();

            while (line != null) {
                builder.append(line);
                line = reader.readLine();
            }

            reader.close();

        } catch (IOException e) {

            return "";

        }

        return builder.toString();

    }

    private String encodeQueryValue(String value) {

        String encodedValue;

        if (value == null) {
            return "";
        }

        encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8);
        encodedValue = encodedValue.replace("+", "%20");

        return encodedValue;

    }

    private String escapeJson(String text) {

        String value;

        if (text == null) {
            return "";
        }

        value = text;
        value = value.replace("\\", "\\\\");
        value = value.replace("\"", "\\\"");
        value = value.replace("\n", "\\n");
        value = value.replace("\r", "\\r");
        value = value.replace("\t", "\\t");

        return value;

    }

    private ArrayList<HashMap<String, String>> parseJsonArray(String json) {

        JsonParser parser;

        if (json == null || json.trim().length() == 0) {
            return new ArrayList<HashMap<String, String>>();
        }

        parser = new JsonParser(json);

        return parser.parseArrayOfObjects();

    }

    private static class SupabaseResponse {

        private int statusCode;

        private String body;

        public SupabaseResponse(int statusCode, String body) {

            this.statusCode = statusCode;

            this.body = body;

        }

        public boolean isSuccess() {

            return statusCode >= 200 && statusCode < 300;

        }

        public int getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

    }

    private static class JsonParser {

        private String text;

        private int index;

        public JsonParser(String text) {

            this.text = text;

            this.index = 0;

        }

        public ArrayList<HashMap<String, String>> parseArrayOfObjects() {

            ArrayList<HashMap<String, String>> rows;
            HashMap<String, String> row;

            rows = new ArrayList<HashMap<String, String>>();

            skipWhitespace();

            if (!consume('[')) {
                return rows;
            }

            skipWhitespace();

            if (peek() == ']') {
                index++;
                return rows;
            }

            while (index < text.length()) {

                row = parseObject();

                if (row != null) {
                    rows.add(row);
                }

                skipWhitespace();

                if (consume(',')) {
                    continue;
                }

                consume(']');
                break;

            }

            return rows;

        }

        private HashMap<String, String> parseObject() {

            HashMap<String, String> row;
            String key;
            String value;

            row = new HashMap<String, String>();

            skipWhitespace();

            if (!consume('{')) {
                return row;
            }

            skipWhitespace();

            if (peek() == '}') {
                index++;
                return row;
            }

            while (index < text.length()) {

                key = parseString();

                skipWhitespace();
                consume(':');
                skipWhitespace();

                value = parseValue();

                row.put(key, value);

                skipWhitespace();

                if (consume(',')) {
                    skipWhitespace();
                    continue;
                }

                consume('}');
                break;

            }

            return row;

        }

        private String parseValue() {

            char current;
            int start;
            String value;

            skipWhitespace();

            current = peek();

            if (current == '"') {
                return parseString();
            }

            start = index;

            while (index < text.length()) {
                current = text.charAt(index);

                if (current == ',' || current == '}' || current == ']') {
                    break;
                }

                index++;
            }

            value = text.substring(start, index).trim();

            if ("null".equals(value)) {
                return null;
            }

            return value;

        }

        private String parseString() {

            StringBuilder builder;
            char current;
            String unicodeText;
            int unicodeValue;

            builder = new StringBuilder();

            consume('"');

            while (index < text.length()) {
                current = text.charAt(index);
                index++;

                if (current == '"') {
                    break;
                }

                if (current == '\\') {
                    if (index >= text.length()) {
                        break;
                    }

                    current = text.charAt(index);
                    index++;

                    if (current == '"' || current == '\\' || current == '/') {
                        builder.append(current);
                    } else if (current == 'b') {
                        builder.append('\b');
                    } else if (current == 'f') {
                        builder.append('\f');
                    } else if (current == 'n') {
                        builder.append('\n');
                    } else if (current == 'r') {
                        builder.append('\r');
                    } else if (current == 't') {
                        builder.append('\t');
                    } else if (current == 'u') {
                        if (index + 4 <= text.length()) {
                            unicodeText = text.substring(index, index + 4);

                            try {

                                unicodeValue = Integer.parseInt(unicodeText, 16);
                                builder.append((char) unicodeValue);

                            } catch (NumberFormatException e) {

                                builder.append(unicodeText);

                            }

                            index = index + 4;
                        }
                    }
                } else {
                    builder.append(current);
                }
            }

            return builder.toString();

        }

        private void skipWhitespace() {

            while (index < text.length()) {
                if (!Character.isWhitespace(text.charAt(index))) {
                    return;
                }

                index++;
            }

        }

        private boolean consume(char expected) {

            skipWhitespace();

            if (peek() == expected) {
                index++;
                return true;
            }

            return false;

        }

        private char peek() {

            if (index >= text.length()) {
                return '\0';
            }

            return text.charAt(index);

        }

    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public void setProjectUrl(String projectUrl) {
        this.projectUrl = normalizeProjectUrl(projectUrl);
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

}
