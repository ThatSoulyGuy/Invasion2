#version 410 core

out vec4 FragColor;

in vec2 uvs;

uniform sampler2D sceneTexture;

void main()
{
    FragColor = texture(sceneTexture, uvs);
}
