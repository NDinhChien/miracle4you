package mfy.server.global.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import mfy.server.domain.project.entity.Project;

public class ProjectSerializer extends StdSerializer<Project> {

    public ProjectSerializer() {
        this(null);
    }

    public ProjectSerializer(Class<Project> t) {
        super(t);
    }

    @Override
    public void serialize(
            Project value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        jgen.writeNumberField("id", value.getId());
        jgen.writeStringField("title", value.getTitle());
        jgen.writeStringField("srcUrl", value.getSrcUrl());
        jgen.writeStringField("srcLang", value.getSrcLang().name());
        jgen.writeStringField("desLang", value.getDesLang().name());
        jgen.writeStringField("category", value.getCategory().name());
        jgen.writeEndObject();
    }
}