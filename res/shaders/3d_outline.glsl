//--- VERT
#version 450 core

layout(location = 0) in vec3 pos;
layout(location = 1) in vec2 texPos;

layout (std140) uniform ViewBlock {
    mat4 projection;
    mat4 view;
};

uniform mat4 model;

void main() {
    gl_Position = projection * view * model * vec4(pos, 1);
}

//--- FRAG
#version 450 core

out vec4 colour;

void main() {
    colour = vec4(1, gl_FragCoord.x / 800, gl_FragCoord.y / 800, 1);
}