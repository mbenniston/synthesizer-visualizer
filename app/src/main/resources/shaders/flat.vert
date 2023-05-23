#version 330 core

layout(location = 0) in vec2 position;

uniform mat4 u_proj, u_model;

out vec2 v_position;

void main()
{
    v_position = position;
    gl_Position = u_proj * u_model* vec4(position, 0.0f, 1.0f);
}