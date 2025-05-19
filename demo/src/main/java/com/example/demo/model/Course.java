package com.example.demo.model;

public class Course {
    private String CourseTitle;
    private String CourseCode;

    public Course(){}

    public Course(String CourseCode, String CourseTitle) {
        this.CourseCode = CourseCode;
        this.CourseTitle = CourseTitle;
    }

    public String getCourseTitle() {
        return CourseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        CourseTitle = courseTitle;
    }

    public String getCourseCode() {
        return CourseCode;
    }

    public void setCourseCode(String courseCode) {
        CourseCode = courseCode;
    }

    
}
