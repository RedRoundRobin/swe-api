package com.redroundrobin.thirema.apirest.models;

import com.google.gson.JsonObject;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class TopicTest {

    @Test
    public void setMessage() {
        Topic topic = new Topic("id");
        topic.setMessage(new ArrayList<JsonObject>());
        assertEquals("[]", topic.getMessage());
    }
}