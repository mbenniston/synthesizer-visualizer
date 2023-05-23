#version 330 core

out vec4 FragColor;

in vec2 v_position;

uniform float u_time;
uniform vec3 u_color;

uniform sampler2D u_texture;

void main()
{
    vec2 texCoord = vec2(v_position.x, 1.0f - v_position.y);
    FragColor = texture(u_texture, texCoord) * vec4(u_color, 1.0f);
}