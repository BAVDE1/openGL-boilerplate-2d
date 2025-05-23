//--- VERT
#version 450 core

layout(location = 0) in vec3 pos;
layout(location = 1) in vec2 texPos;

layout (std140) uniform ViewBlock {
    mat4 projection;
    mat4 view;
};

uniform mat4 model;

out vec2 v_texPos;

void main() {
    gl_Position = projection * view * model * vec4(pos, 1);
    v_texPos = texPos;
}

//--- FRAG
#version 450 core

uniform sampler2D theTexture;

in vec2 v_texPos;

out vec4 colour;

void main() {
    colour = texture(theTexture, v_texPos);
}