#version 410 core

out vec4 FragColor;

in vec3 color;
in vec2 uvs;

uniform sampler2D diffuse;

void main()
{
    FragColor = texture(diffuse, uvs) * vec4(color, 1.0f);
}