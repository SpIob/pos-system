# CLAUDE.md
This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# Introduction
This codebase is for a POS System, a point of sale system for managing sales and inventory.

# Architecture
The system consists of the following components:
- UI: The user interface, built with Java Swing.
- DAO: The data access object, responsible for interacting with the database.
- Model: The business logic, responsible for managing the data and behavior of the system.

# Commands
To build the project, run `ant build`.
To run the tests, run `ant test`.
To run the application, run `ant run`.

# Development
To develop in this codebase, you will need to have Java and Apache Ant installed.
You can run a single test by running `ant test -Dtestcase=<test_name>`.