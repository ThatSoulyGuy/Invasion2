#version 410 core

layout(location = 0) in vec2 positionIn;
layout(location = 1) in vec2 uvsIn;

out vec2 uvs;

void main()
{
    gl_Position = vec4(positionIn, 0.0, 1.0);

    uvs = uvsIn;
}
