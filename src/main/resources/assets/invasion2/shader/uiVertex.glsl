#version 410 core

layout(location = 0) in vec3 positionIn;
layout(location = 1) in vec3 colorIn;
layout(location = 2) in vec3 _;
layout(location = 3) in vec2 uvsIn;

uniform mat4 projection;
uniform mat4 model;

out vec3 color;
out vec2 uvs;

void main()
{
    gl_Position = projection * model * vec4(positionIn, 1.0);

    color = colorIn;
    uvs = uvsIn;
}