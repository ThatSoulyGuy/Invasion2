#version 410 core

out vec4 FragColor;

in vec2 TexCoords;

uniform sampler2D gPosition;
uniform sampler2D gNormal;
uniform sampler2D gAlbedo;
uniform sampler2D ssao;

void main()
{
    vec3 albedo = texture(gAlbedo, TexCoords).rgb;
    float ambientOcclusion = texture(ssao, TexCoords).r;

    vec3 ambient = albedo * ambientOcclusion;

    FragColor = vec4(ambient, 1.0f);
}
