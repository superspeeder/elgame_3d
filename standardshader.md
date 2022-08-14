# Standard Shaders

A standard shader is a shader which uses standardized uniform names and a standardized vertex layout

## Vertex Layout

The standard vertex layout provides for:
- Position (x,y,z)
- Color (r,g,b,a)
- Texture Coords (u, v)
- Normals (x,y,z)

### Attributes:
| Attribute | Name           | Size |
|-----------|----------------|------|
| 0         | Position       | 3    |
| 1         | Color          | 4    |
| 2         | Texture Coords | 2    |
| 3         | Normals        | 3    |
| 4         | Light Data     | 1    |




## Uniforms

There are a number of standard uniforms:

| Uniform Name      | Type      |
|-------------------|-----------|
| uColor            | vec4      |
| uViewProjection   | mat4      |
| uModel            | mat4      |
| uTexture0         | sampler2D |
| uTexture1         | sampler2D |
| uTime             | float     |
| uMousePosition    | vec2      |
| uScreenResolution | ivec2     |

A standard shader may contain extra uniforms, which may be used by a supplemental rendering code.
A standard shader will always be able to use these uniforms
