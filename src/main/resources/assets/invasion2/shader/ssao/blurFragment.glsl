#version 410 core

out float FragColor;

in vec2 TexCoords;

uniform sampler2D ssaoInput;

void main()
{
    float result = 0.0;

    for(int x = -2; x <= 2; x++)
    {
        for(int y = -2; y <= 2; y++)
        {
            vec2 offset = vec2(float(x), float(y)) * 0.001;
            result += texture(ssaoInput, TexCoords + offset).r;
        }
    }

    result /= 25.0;
    FragColor = result;
}