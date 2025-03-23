package mfy.server.global.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import mfy.server.domain.user.entity.User;

public class UserSerializer extends StdSerializer<User> {

    public UserSerializer() {
        this(null);
    }

    public UserSerializer(Class<User> t) {
        super(t);
    }

    @Override
    public void serialize(
            User value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        jgen.writeNumberField("id", value.getId());
        jgen.writeStringField("nickname", value.getNickname());
        jgen.writeStringField("avatar", value.getAvatar());
        jgen.writeStringField("fullName", value.getFullName());
        jgen.writeNumberField("score", value.getScore());
        jgen.writeStringField("joinedAt", value.getJoinedAt().toString());
        jgen.writeEndObject();
    }
}