# Software Testing Lab 4 - The Library Alert System

## Overview

This project tests a Java-based **Library Alert System** using **JUnit 5** and **Mockito**.

The `LibraryService` class handles the checkout of a library resource. It works with two dependencies:

- **`ResourceRepository`** — checks whether a resource is available and updates its checkout status.
- **`EmailProvider`** — sends an email to the member after a successful status update.

Mockito is used to mock these dependencies so the tests can focus on `LibraryService` without needing a real database or email service.

## Technologies Used

- Java 17
- Maven
- JUnit Jupiter 5.11.4
- Mockito 5.15.2
- Maven Surefire 3.5.2
- IntelliJ IDEA

## Project Structure

```text
SoftwareTestingLab4/
├── pom.xml
├── .gitignore
└── src/
    ├── main/
    │   └── java/
    │       └── Library/
    │           ├── LibraryService.java
    │           ├── ResourceRepository.java
    │           ├── EmailProvider.java
    │           ├── DatabaseFailureException.java
    │           └── EmailFailureException.java
    └── test/
        └── java/
            └── LibraryServiceTest.java
```

## How the Library Checkout System Works

1. If the resource ID is `null`, the service returns `false`.
2. The service checks whether the resource is available.
3. If the resource is unavailable, the service returns `false`.
4. If the resource is available, the service updates its status to unavailable.
5. If the status update fails, the service throws `DatabaseFailureException`.
6. After a successful status update, the service sends an email notification to the member.
7. If sending the email fails, the service throws `EmailFailureException`.
8. If all operations succeed, the service returns `true`.

## Test Cases

The tests use mocked `ResourceRepository` and `EmailProvider` dependencies. This allows the service to be tested without connecting to a real database or sending real emails.

The following table describes the core test scenarios discussed for this project. The final list should match the test methods included in `LibraryServiceTest.java`.

| # | Test Scenario | Expected Result |
|---|---|---|
| 1 | Resource ID is null | Returns `false`; no repository or email interactions occur. |
| 2 | Resource is unavailable | Returns `false`; status is not updated and no email is sent. |
| 3 | Checkout succeeds | Returns `true`; resource status is updated and a notification email is sent. |
| 4 | Status update returns false | Throws `DatabaseFailureException`; no email is sent. |
| 5 | Email sending returns false | Throws `EmailFailureException`. |
| 6 | Availability check throws a runtime exception | The exception is propagated; no email is sent. |
| 7 | Email provider throws `EmailFailureException` | The exception is propagated after the status update succeeds. |
| 8 | Status update throws `DatabaseFailureException` | The exception is propagated and no email is sent. |

### What the Tests Verify

- **Return values:** Verify that the service returns the expected `true` or `false`.
- **Exception handling:** Verify that database and email failures are handled as expected.
- **Interaction verification:** Use Mockito to verify whether dependencies are called or not called.
- **Correct arguments:** Check that the correct resource ID, availability value, recipient email, and message are used.
- **Test isolation:** Mock dependencies so tests do not require a live database or email server.

Some additional tests may check the same behavior in more detail. Avoid redundant tests that verify exactly the same thing without adding meaningful coverage.
## Mocking with Mockito

### What Did We Mock?

In this project, we mock two dependencies of `LibraryService` using Mockito:

**1. `ResourceRepository` — Database Dependency**

The `ResourceRepository` interface represents the database operations used by the library service.

Its methods are:

- `isResourceAvailable(resourceId)` — checks whether a resource is available.
- `updateStatus(resourceId, false)` — updates the resource status to unavailable after checkout.

We mock this dependency so we can control the database responses without connecting to a real database. For example, the mock can return `true`, return `false`, or throw an exception.

**2. `EmailProvider` — Email Dependency**

The `EmailProvider` interface represents the email service.

Its method is:

- `sendEmail(memberEmail, message)` — sends a checkout notification to the member.

We mock this dependency so we do not need a real email service or send actual emails during testing. We can simulate successful email delivery, failed delivery, or an exception.

### How Do We Use Mockito?

Mockito allows us to define how mocked objects should behave.

For example:

```java
when(resourceRepository.isResourceAvailable(resourceId))
    .thenReturn(true);
```

This tells the mock repository to return `true` when the service checks whether the specified resource is available.

Another example:

```java
when(emailProvider.sendEmail(memberEmail, message))
    .thenReturn(false);
```

This simulates an email delivery failure. The test can then verify that `LibraryService` throws the expected `EmailFailureException`.

We also use Mockito verification methods, such as `verify()` and `verifyNoInteractions()`, to check whether dependencies are called as expected.

### Real Objects vs. Mock Objects

| Component | Type | Purpose |
|---|---|---|
| `LibraryService` | Real object | Contains the business logic being tested. |
| `ResourceRepository` | Mock object | Simulates database responses. |
| `EmailProvider` | Mock object | Simulates email service responses. |
| Database | Not connected | Database behavior is simulated through the mock repository. |
| Email service | Not connected | Email behavior is simulated through the mock email provider. |

### Why Did We Use Mocking?

Mocking helps us:

- Test `LibraryService` independently of external systems.
- Simulate both successful and unsuccessful operations.
- Test exception-handling behavior.
- Verify that methods are called with the correct arguments.
- Check that certain methods are not called when an operation fails.
- Run tests without requiring a database or a real email service.

**Important:** We are not mocking `LibraryService` itself. We test the real `LibraryService` while mocking its dependencies. This allows us to evaluate the actual checkout logic in a controlled testing environment.

## Prerequisites

Before running the project, make sure you have:

- JDK 17 or newer.
- IntelliJ IDEA or another Java IDE.
- Apache Maven, or IntelliJ's integrated Maven support.
- Git, if you want to clone the repository.

## How to Run the Tests in IntelliJ IDEA

1. Open the project in IntelliJ IDEA.
2. Wait for Maven to download and import the dependencies.
3. Open `src/test/java/LibraryServiceTest.java`.
4. Click the green run icon beside the test class or an individual test method.
5. Review the results in the Run window. Successful tests should be marked as passed.

If Maven dependencies do not load, open the Maven tool window and select **Reload All Maven Projects**.

## How to Run the Tests Using Maven

Open the terminal in the project root directory, where `pom.xml` is located.

Run all tests:

```bash
mvn test
```

Run only `LibraryServiceTest`:

```bash
mvn -Dtest=LibraryServiceTest test
```

Clean the project and run all tests:

```bash
mvn clean test
```

A successful run should end with `BUILD SUCCESS` and show the number of tests run, failures, and errors.

If a test fails, review the error details in the terminal or IntelliJ's Run window.

## Dependencies

The Maven configuration in `pom.xml` includes:

- JUnit Jupiter for writing and running unit tests.
- Mockito Core for creating mock objects.
- Mockito JUnit Jupiter integration for using Mockito with JUnit 5.
- Maven Surefire for executing tests during the Maven test phase.

Maven downloads the required dependencies when the project is imported or built.

## GitHub Repository

Repository: https://github.com/MuhammadBasit29/SoftwareTestingLab4

## Author

Muhammad Basit

## Purpose of This Lab

This lab demonstrates unit testing with JUnit 5 and Mockito, including normal behavior, failure scenarios, exception handling, and verification of interactions between a service and its dependencies.
